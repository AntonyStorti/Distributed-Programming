from flask import Blueprint, jsonify, request, current_app, flash, redirect, url_for
from app.utils.session_handler import get_session_data, verify_restaurant_access
from app.utils.get_coordinates import get_coordinates_nominatim
from app.utils.producer_kafka import send_order_to_kafka
from app.utils.consumer_rabbitmq import consume_orders
from app.utils.haversine_distance import haversine
from app.extensions import db, socketio, app
from app.models.restaurant import Restaurant
from app.models.menu import MenuItem
from app.models.order import Order
from flask import render_template
from app.models.user import User



bp = Blueprint('restaurant', __name__)

# Route per il rendering del template restaurant_manage_menu.html
@bp.route('/menu/<int:restaurant_id>', methods=['GET'])
def view_menu(restaurant_id):

    """Visualizza il menu di un ristorante con l'interfaccia HTML."""
    # Verifica l'accesso del ristorante
    access_denied = verify_restaurant_access(restaurant_id)
    if access_denied:
        return access_denied

    # Recupera gli articoli del menu per il ristorante
    menu_items = MenuItem.query.filter_by(restaurant_id=restaurant_id).all()

    # Renderizza la pagina del menu
    return render_template('restaurant_manage_menu.html', menu_items=menu_items, session=get_session_data())


@bp.route('/menu/json/<int:restaurant_id>', methods=['GET'])
def menu_json(restaurant_id):

    """Restituisce il menu di un ristorante in formato JSON."""
    menu_items = MenuItem.query.filter_by(restaurant_id=restaurant_id).all()

    # Creiamo un dizionario per ogni piatto
    menu_data = [
        {
            'id': item.id,
            'name': item.name,
            'description': item.description,
            'price': item.price,
            'available': item.available
        }
        for item in menu_items
    ]

    return jsonify(menu_data)


@bp.route('/menu/<int:restaurant_id>', methods=['POST'])
def add_menu_item(restaurant_id):

    """Aggiunge un elemento al menu di un ristorante."""
    access_denied = verify_restaurant_access(restaurant_id)
    if access_denied:
        return access_denied

    data = request.json

    # Log dei dati ricevuti
    current_app.logger.info(f"Ricevuti dati: {data}")

    # Verifica che i dati siano corretti
    if not data.get('name') or not data.get('price'):
        return jsonify({'error': 'Nome e prezzo sono obbligatori'}), 400

    # Crea il nuovo piatto
    new_item = MenuItem(
        restaurant_id=restaurant_id,
        name=data['name'],
        description=data.get('description'),
        price=data['price'],
        available=data.get('available', True)
    )

    # Aggiungi il nuovo item e commetti al database
    try:
        db.session.add(new_item)
        db.session.commit()  # Questo salva effettivamente nel database
        current_app.logger.info(f"Nuovo piatto aggiunto: {new_item.to_dict()}")
    except Exception as e:
        db.session.rollback()  # Rollback in caso di errore
        current_app.logger.error(f"Errore nell'aggiunta dell'item: {e}")
        return jsonify({'error': 'Errore durante l\'aggiunta del piatto'}), 500

    # Dopo aver aggiunto l'elemento, recupera l'intero menu
    menu_items = MenuItem.query.filter_by(restaurant_id=restaurant_id).all()
    return jsonify([item.to_dict() for item in menu_items]), 201


@bp.route('/menu/<int:restaurant_id>/<int:item_id>', methods=['DELETE'])
def delete_menu_item(restaurant_id, item_id):

    access_denied = verify_restaurant_access(restaurant_id)
    if access_denied:
        return access_denied

    item = MenuItem.query.filter_by(restaurant_id=restaurant_id, id=item_id).first()
    if not item:
        return jsonify({'error': 'Item non trovato'}), 404  # Risposta JSON

    try:
        db.session.delete(item)
        db.session.commit()
        return jsonify({'message': 'Item rimosso con successo'})  # Risposta JSON
    except Exception as e:
        return jsonify({'error': f'Errore durante l\'eliminazione: {str(e)}'}), 500  # Risposta JSON


@bp.route('/menu/<int:restaurant_id>/<int:item_id>', methods=['GET'])
def get_menu_item(restaurant_id, item_id):

    """Restituisce i dettagli di un piatto specifico."""
    access_denied = verify_restaurant_access(restaurant_id)
    if access_denied:
        return access_denied

    item = MenuItem.query.filter_by(restaurant_id=restaurant_id, id=item_id).first()
    if not item:
        return jsonify({'error': 'Item non trovato'}), 404
    return jsonify(item.to_dict())


@bp.route('/menu/<int:restaurant_id>/<int:item_id>', methods=['PUT'])
def update_menu_item(restaurant_id, item_id):

    """Aggiorna un piatto nel menu."""
    access_denied = verify_restaurant_access(restaurant_id)
    if access_denied:
        return access_denied

    data = request.json
    item = MenuItem.query.filter_by(restaurant_id=restaurant_id, id=item_id).first()
    if not item:
        return jsonify({'error': 'Item non trovato'}), 404

    item.name = data.get('name', item.name)
    item.description = data.get('description', item.description)
    item.price = data.get('price', item.price)
    item.available = data.get('available', item.available)

    db.session.commit()
    return jsonify(item.to_dict())  # Restituisce il piatto aggiornato


@bp.route('/restaurant/menu/<int:restaurant_id>')
def menu(restaurant_id):

    access_denied = verify_restaurant_access(restaurant_id)
    if access_denied:
        return access_denied

    # Recupera il ristorante dal database
    restaurant = Restaurant.query.get_or_404(restaurant_id)
    return render_template('restaurant_manage_menu.html', restaurant=restaurant)


@bp.route('/restaurant/<int:restaurant_id>/orders', methods=['GET'])
def view_orders(restaurant_id):

    """Visualizza gli ordini di un ristorante."""
    access_denied = verify_restaurant_access(restaurant_id)
    if access_denied:
        return access_denied

    # Avvia il consumatore solo una volta per ristorante
    if not hasattr(socketio, 'consumer_started') or not socketio.consumer_started:
        socketio.start_background_task(consume_orders, restaurant_id, app, socketio)
        socketio.consumer_started = True  # Imposta il flag per evitare che venga avviato più volte

    orders = Order.query.filter_by(restaurant_id=restaurant_id).all()
    return render_template('restaurant_view_orders.html', orders=orders)


@bp.route('/restaurant/order/<int:order_id>', methods=['GET', 'POST'])
def view_order_details(order_id):

    """Visualizza i dettagli di un ordine."""
    # Recupera l'ordine
    order = Order.query.get_or_404(order_id)
    # Verifica l'accesso del ristorante
    access_denied = verify_restaurant_access(order.restaurant_id)
    if access_denied:
        return access_denied

    # Gestisci la modifica dello stato
    if request.method == 'POST':
        # Ottieni il nuovo stato dal modulo
        new_status = request.form['status']

        # Aggiorna lo stato dell'ordine
        order.status = new_status
        db.session.commit()

        # Se lo stato diventa "ready_for_pickup", invia l'ordine a Kafka
        if new_status == "ready_for_pickup":
            restaurant = Restaurant.query.get(order.restaurant_id)  # Recupera l'oggetto ristorante completo
            if restaurant:
                order_lat, order_lon = get_coordinates_nominatim(restaurant.address)
            else:
                # Gestisci l'errore, se il ristorante non viene trovato
                print("Ristorante non trovato!")

            # Invia i dettagli dell'ordine a Kafka
            send_order_to_kafka(order.id, order.restaurant_id, order_lat, order_lon)

        # Redirect alla pagina dell'ordine per visualizzare i dati aggiornati
        return redirect(url_for('restaurant.view_order_details', order_id=order.id))

    # Mostra i dettagli dell'ordine
    return render_template('restaurant_order_details.html', order=order)


@bp.route('/<int:restaurant_id>/profile', methods=['GET', 'POST'])
def view_profile(restaurant_id):

    """Visualizza e aggiorna il profilo del ristorante."""
    # Verifica accesso al ristorante
    access_denied = verify_restaurant_access(restaurant_id)
    if access_denied:
        return access_denied

    # Recupera il ristorante dal database
    restaurant = Restaurant.query.get_or_404(restaurant_id)

    if request.method == 'POST':
        # Recupera i dati dal form e gestisci i campi vuoti come NULL
        restaurant.name = request.form.get('name') if request.form.get('name') else None
        restaurant.address = request.form.get('address') if request.form.get('address') else None
        restaurant.phone_number = request.form.get('phone') if request.form.get('phone') else None

        # Se l'email è presente nel form e non è vuota, aggiorna il campo, altrimenti lascialo come NULL
        restaurant.email = request.form.get('email') if request.form.get('email') else None

        restaurant.is_open = request.form.get('is_open') == '1'
        restaurant.opening_hours = request.form.get('hours') if request.form.get('hours') else None  # Usa NULL se vuoto
        restaurant.description = request.form.get('description') if request.form.get('description') else None
        restaurant.logo_url = request.form.get('logo_url') if request.form.get('logo_url') else None

        # Salva le modifiche nel database
        try:
            db.session.commit()
            flash("Profilo aggiornato con successo!", "success")
        except Exception as e:
            db.session.rollback()
            flash(f"Errore nell'aggiornamento del profilo: {str(e)}", "error")

        # Redirect per evitare il reinvio del form
        return redirect(url_for('restaurant.view_profile', restaurant_id=restaurant_id))

    # Passa i dati del ristorante al template
    return render_template('restaurant_view_profile.html', restaurant=restaurant)


@bp.route('/search/menu/items', methods=['GET'])
def search_menu_items():

    # Ottieni l'ID del cliente e la query dai parametri della richiesta
    customer_id = request.args.get('customer_id')
    query = request.args.get('query', '')

    # Recupera il cliente dal database
    customer = User.query.get(customer_id)
    if not customer:
        return jsonify({"message": "Cliente non trovato"}), 404

    # Ottieni l'indirizzo del cliente
    customer_address = customer.address

    # Recupera le coordinate del cliente
    customer_lat, customer_lon = get_coordinates_nominatim(customer_address)
    if customer_lat is None or customer_lon is None:
        return jsonify({"message": "Impossibile ottenere le coordinate del cliente"}), 400

    # Recupera tutti i ristoranti dal database
    restaurants = Restaurant.query.all()
    nearby_restaurants = []

    # Filtra i ristoranti che sono entro 10 km dal cliente
    for restaurant in restaurants:
        # Ottieni l'indirizzo del ristorante
        restaurant_address = restaurant.address

        # Recupera le coordinate del ristorante
        restaurant_lat, restaurant_lon = get_coordinates_nominatim(restaurant_address)
        if restaurant_lat is None or restaurant_lon is None:
            continue  # Se non possiamo ottenere le coordinate, salta questo ristorante

        # Calcola la distanza tra il cliente e il ristorante
        distance = haversine(customer_lat, customer_lon, restaurant_lat, restaurant_lon)

        # Se la distanza è minore o uguale a 10 km, aggiungi il ristorante alla lista
        if distance <= 10:
            nearby_restaurants.append(restaurant)

    # Se non ci sono ristoranti nel raggio di 10 km, restituisci un messaggio
    if not nearby_restaurants:
        return jsonify({"message": "Nessun ristorante nelle vicinanze (entro 10 km)"}), 404

    # Ora cerca gli articoli del menu nei ristoranti vicini
    menu_items = []
    for restaurant in nearby_restaurants:
        if not query:
            # Se la query è vuota, restituisci tutti gli articoli del menu disponibili
            menu_items += MenuItem.query.filter(MenuItem.restaurant_id == restaurant.id,
                                                MenuItem.available == True).all()
        else:
            # Filtra gli articoli del menu in base al nome e alla disponibilità
            menu_items += MenuItem.query.filter(MenuItem.restaurant_id == restaurant.id,
                                                MenuItem.name.ilike(f"%{query}%"),
                                                MenuItem.available == True).all()

    # Se non trovi articoli, restituisci una risposta vuota
    if not menu_items:
        return jsonify({"message": "Nessun elemento trovato"}), 404

    # Restituisci gli articoli del menu trovati come JSON
    return jsonify([item.to_dict() for item in menu_items]), 200


@bp.route('/orders/customer/<int:customer_id>', methods=['GET'])
def get_orders_by_customer(customer_id):

    """Recupera gli ordini per un determinato cliente e avvia i consumatori per tutti i ristoranti."""

    # Recupera tutti i ristoranti dal database
    all_restaurants = Restaurant.query.all()

    # Avvia il consumatore RabbitMQ per ciascun restaurant_id, se non già avviato
    for restaurant in all_restaurants:
        # Inizializza socketio.consumer_started come un dizionario se non esiste o è stato sovrascritto
        if not hasattr(socketio, 'consumer_started') or not isinstance(socketio.consumer_started, dict):
            socketio.consumer_started = {}

        # Avvia il consumatore solo se non è già stato avviato
        if not socketio.consumer_started.get(restaurant.id, False):
            socketio.start_background_task(consume_orders, restaurant.id, app, socketio)
            socketio.consumer_started[restaurant.id] = True  # Segna il consumatore come avviato

    # Recupera gli ordini per il cliente
    orders = Order.query.filter_by(customer_id=customer_id).all()

    if not orders:
        return jsonify({"message": "Nessun ordine trovato per questo cliente"}), 404

    # Restituisci gli ordini del cliente come JSON
    return jsonify([order.to_dict() for order in orders]), 200


@bp.route('/orderstraceable/customer/<int:customer_id>', methods=['GET'])
def get_orders_traceable_by_customer(customer_id):

    """Recupera gli ordini tracciabili per un determinato cliente e avvia i consumatori per tutti i ristoranti."""

    # Recupera tutti i ristoranti dal database
    all_restaurants = Restaurant.query.all()

    # Avvia il consumatore RabbitMQ per ciascun restaurant_id, se non già avviato
    for restaurant in all_restaurants:
        if not hasattr(socketio, 'consumer_started'):
            socketio.consumer_started = {}

        if not socketio.consumer_started.get(restaurant.id, False):
            socketio.start_background_task(consume_orders, restaurant.id, app, socketio)
            socketio.consumer_started[restaurant.id] = True  # Segna il consumatore come avviato

    # Recupera gli ordini per il cliente
    orders = Order.query.filter_by(customer_id=customer_id).filter(Order.status.in_(['shipped'])).all()

    if not orders:
        return jsonify({"message": "Non ci sono ordini tracciabili per questo cliente.", "orders": []}), 200

    # Restituisci gli ordini del cliente come JSON
    return jsonify([order.to_dict() for order in orders]), 200


@bp.route('/order/<int:order_id>', methods=['GET'])
def get_order_details(order_id):

    # Recupera i dettagli di un ordine specifico
    order = Order.query.get_or_404(order_id)
    return jsonify(order.to_dict()), 200