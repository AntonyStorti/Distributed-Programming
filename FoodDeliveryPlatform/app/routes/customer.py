from flask import Blueprint, render_template, request, session, redirect, url_for, flash, Response
from app.utils.get_coordinates import get_coordinates_nominatim
from app.utils.session_handler import verify_customer_access
from app.utils.producer_rabbitmq import publish_order
from app.utils.haversine_distance import haversine
from app.models.restaurant import Restaurant
from app.models.menu import MenuItem
from app.extensions import db, app
from app.models.order import Order
from app.models.user import User
from datetime import datetime
import requests
import time



customer_bp = Blueprint('customer', __name__)

@customer_bp.route('/<int:user_id>/browse', methods=['GET', 'POST'])
def browse(user_id):

    access_denied = verify_customer_access()
    if access_denied:
        return access_denied

    user = User.query.get_or_404(user_id)

    # Recupera l'indirizzo dell'utente dalla sessione o dal database
    user_address = user.address
    if not user_address:
        return "Errore: Indirizzo utente non disponibile.", 400

    # Recupera le coordinate dell'utente
    user_lat, user_lon = get_coordinates_nominatim(user_address)
    if user_lat is None or user_lon is None:
        return "Errore: Impossibile determinare le coordinate dell'utente.", 400

    # Recupera tutti i ristoranti
    all_restaurants = Restaurant.query.all()
    nearby_restaurants = []

    # Filtra i ristoranti che sono entro 10 km
    for restaurant in all_restaurants:
        # Recupera le coordinate del ristorante
        restaurant_lat, restaurant_lon = get_coordinates_nominatim(restaurant.address)
        if restaurant_lat is None or restaurant_lon is None:
            continue  # Salta i ristoranti senza coordinate valide

        # Calcola la distanza di Haversine
        distance = haversine(user_lat, user_lon, restaurant_lat, restaurant_lon)
        if distance <= 10:
            nearby_restaurants.append(restaurant)

    # Applica il filtro di ricerca, se presente
    if request.method == 'POST':
        search_query = request.form.get('search_query', '')
        if search_query:
            nearby_restaurants = [
                r for r in nearby_restaurants if search_query.lower() in r.name.lower()
            ]

    return render_template('customer_browse_restaurants.html', user=user, restaurants=nearby_restaurants)



@customer_bp.route('/<int:user_id>/place-order', methods=['POST', 'GET'])
def place_order(user_id):

    access_denied = verify_customer_access()
    if access_denied:
        return access_denied

    user = User.query.get_or_404(user_id)

    # Carica i piatti dal carrello dalla sessione
    foods_in_cart = session.get('cart', [])
    food_items = {food.id: food for food in MenuItem.query.filter(MenuItem.id.in_(foods_in_cart)).all()}

    # Recupera il ristorante
    restaurant = None
    if foods_in_cart:
        first_food_id = foods_in_cart[0]
        first_food = MenuItem.query.get(first_food_id)
        if first_food:
            restaurant = Restaurant.query.get(first_food.restaurant_id)

    if request.method == 'POST':
        action = request.form.get('action')
        if action == 'update_quantities':
            updated_cart = {}
            for food_id in foods_in_cart:
                try:
                    quantity = int(request.form.get(f'quantity_{food_id}', 1))
                    if quantity > 0:
                        updated_cart[food_id] = quantity
                except ValueError:
                    # In caso di valore non valido, salta l'aggiornamento per quell'elemento
                    continue
            session['cart'] = list(updated_cart.keys())
            session['quantities'] = {int(k): v for k, v in updated_cart.items()}  # Converti le chiavi in interi
            flash("Quantità aggiornate con successo!", "success")
            return redirect(url_for('customer.place_order', user_id=user.id))

        elif action == 'complete_order':
            if not foods_in_cart or restaurant is None:
                flash("Non ci sono articoli nel carrello per effettuare un ordine.", "warning")
                return redirect(url_for('customer.browse', user_id=user.id))

            # Carica le quantità dal carrello, inizializzandole a 1 se non esistono
            quantities = session.get('quantities', {food_id: 1 for food_id in foods_in_cart})

            quantities = {int(food_id): quantity for food_id, quantity in quantities.items()}

            order_data = {
                'customer_id': user.id,
                'restaurant_id': restaurant.id,
                'order_date': datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S'),
                'status': 'pending',
                'items': [{
                    'name': item.name,
                    'quantity': quantities[int(item.id)],
                    'price': item.price * quantities[int(item.id)]
                } for item in food_items.values()],
                'total': sum(item.price * quantities[int(item.id)] for item in food_items.values()),
            }

            publish_order(order_data, restaurant.id)

            # Svuota il carrello
            session.pop('cart', None)
            session.pop('quantities', None)

            flash("Ordine completato con successo!", "success")
            return redirect(url_for('customer.browse', user_id=user.id))

    # Inizializza le quantità se non esistono
    quantities = session.get('quantities', {})

    # Assicurati che le chiavi siano interi
    quantities = {int(food_id): quantity for food_id, quantity in quantities.items()}

    # Aggiungi gli articoli al carrello se non ci sono quantità
    for food_id in foods_in_cart:
        if food_id not in quantities:
            quantities[food_id] = 1
    session['quantities'] = quantities

    return render_template(
        'customer_place_order.html',
        user=user,
        foods_in_cart=foods_in_cart,
        food_items=food_items,
        restaurant=restaurant,
        quantities=quantities
    )



@customer_bp.route('/<int:user_id>/add-to-cart/<int:item_id>', methods=['GET', 'POST'])
def add_to_cart(user_id, item_id):

    access_denied = verify_customer_access()
    if access_denied:
        return access_denied

    user = User.query.get_or_404(user_id)
    item = MenuItem.query.get_or_404(item_id)

    # Ottieni il carrello dalla sessione, o crea uno nuovo se non esiste
    cart = session.get('cart', [])

    # Aggiungi l'item_id al carrello, se non è già presente
    if item_id not in cart:
        cart.append(item_id)
        session['cart'] = cart
        flash(f'{item.name} è stato aggiunto al carrello!', 'success')
    else:
        flash(f'{item.name} è già nel carrello.', 'info')

    # Redirect alla pagina del carrello (place_order) per visualizzare gli articoli nel carrello
    return redirect(url_for('customer.place_order', user_id=user.id))



@customer_bp.route('/<int:user_id>/track-orders')
def track_orders(user_id):

    # Verifica se l'utente ha il permesso di accedere a questa sezione
    access_denied = verify_customer_access()
    if access_denied:
        return access_denied

    # Recupera l'utente dal database
    user = User.query.get_or_404(user_id)
    # Recupera gli ordini effettuati dall'utente
    response = requests.get(f'http://127.0.0.1:5000/restaurant/orderstraceable/customer/{user.id}')
    if response.status_code == 200:
        orders = response.json()
    else:
        flash("Errore nel recupero degli ordini", "error")
        orders = []
    if orders != {'message': 'Non ci sono ordini tracciabili per questo cliente.', 'orders': []}:
        # Recupera le coordinate per ciascun ordine
        for order in orders:
            restaurant_address=order['restaurant']['address']
            lat, lon = get_coordinates_nominatim(restaurant_address)
            order['restaurant']['coords'] = {'lat': lat, 'lon': lon}

        # Aggiungi anche le coordinate della destinazione
        dest_lat, dest_lon = get_coordinates_nominatim(user.address)
        order['dest_coords'] = {'lat': dest_lat, 'lon': dest_lon}
    else:
        orders = []
    # Passa gli ordini al template
    return render_template('customer_track_orders.html', user=user, orders=orders)


@customer_bp.route('/orders/<int:order_id>/status')
def order_status_stream(order_id):

    access_denied = verify_customer_access()
    if access_denied:
        return access_denied

    def generate_status_updates():
        with app.app_context():
            # Simuliamo il monitoraggio in tempo reale dello stato dell'ordine
            order = Order.query.get(order_id)
            if not order:
                return "Ordine non trovato"

            while True:
                # Simula l'aggiornamento dello stato dell'ordine
                order_status = order.status
                yield f"data: {order_status}\n\n"
                time.sleep(5)  # Simula l'aggiornamento ogni 5 secondi

    return Response(generate_status_updates(), content_type='text/event-stream')



@customer_bp.route('/<int:user_id>/profile', methods=['GET', 'POST'])
def profile(user_id):

    access_denied = verify_customer_access()
    if access_denied:
        return access_denied

    user = User.query.get_or_404(user_id)

    if request.method == 'POST':
        user.username = request.form.get('username') if request.form.get('username') else None
        user.name = request.form.get('name') if request.form.get('name') else None
        user.address = request.form.get('address') if request.form.get('address') else None
        user.email = request.form.get('email') if request.form.get('email') else None
        user.phone_number = request.form.get('phone_number') if request.form.get('phone_number') else None

        # Salva le modifiche nel database
        try:
            db.session.commit()
            flash("Profilo aggiornato con successo!", "success")
        except Exception as e:
            db.session.rollback()
            flash(f"Errore nell'aggiornamento del profilo: {str(e)}", "error")

        # Redirect per evitare il reinvio del form
        return redirect(url_for('customer.profile', user_id=user.id))

        # Passa i dati del ristorante al template
    return render_template('customer_view_profile.html', user=user)



@customer_bp.route('/<int:user_id>/search-foods', methods=['GET', 'POST'])
def search_foods(user_id):

    access_denied = verify_customer_access()
    if access_denied:
        return access_denied

    user = User.query.get_or_404(user_id)
    foods = []

    if request.method == 'POST':
        search_query = request.form.get('search_query')
        if search_query:
            # Invia la richiesta a un server esterno per ottenere i cibi
            response = requests.get(f'http://127.0.0.1:5000/restaurant/search/menu/items', params={'customer_id': user_id, 'query': search_query})

            if response.status_code == 200:
                foods = response.json()
            else:
                flash("Errore nella ricerca dei cibi", "error")

    return render_template('customer_search_foods.html', user=user, foods=foods)



@customer_bp.route('/<int:user_id>/restaurant/<int:restaurant_id>/menu', methods=['GET', 'POST'])
def view_menu(user_id, restaurant_id):

    access_denied = verify_customer_access()
    if access_denied:
        return access_denied
    user = User.query.get_or_404(user_id)
    restaurant = Restaurant.query.get_or_404(restaurant_id)

    # Invia la richiesta al server del ristorante per ottenere il menu
    response = requests.get(f'http://127.0.0.1:5000/restaurant/menu/json/{restaurant_id}')

    if response.status_code == 200:
        menu_items = response.json()

        # Filtra gli articoli non disponibili
        menu_items = [item for item in menu_items if item.get('available')]

    else:
        flash("Errore nel recupero del menu", "error")
        menu_items = []

    return render_template('customer_view_menu.html', user=user, restaurant=restaurant, menu_items=menu_items)



@customer_bp.route('/<int:user_id>/view-orders')
def view_orders(user_id):

    access_denied = verify_customer_access()
    if access_denied:
        return access_denied
    # Recupera l'utente
    user = User.query.get_or_404(user_id)

    # Recupera gli ordini effettuati dall'utente
    response = requests.get(f'http://127.0.0.1:5000/restaurant/orders/customer/{user.id}')

    if response.status_code == 200:
        orders = response.json()
    else:
        flash("Errore nel recupero degli ordini", "error")
        orders = []

    return render_template('customer_view_orders.html', user=user, orders=orders)



@customer_bp.route('/order/<int:order_id>/details')
def view_order_details(order_id):

    access_denied = verify_customer_access()
    if access_denied:
        return access_denied
    response = requests.get(f'http://127.0.0.1:5000/restaurant/order/{order_id}')
    if response.status_code == 200:
        order = response.json()
    else:
        flash("Errore nel recupero dei dettagli dell'ordine", "error")
        order = {}
    return render_template('customer_view_order_details.html', order=order)

