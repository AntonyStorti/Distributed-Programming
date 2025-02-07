from app.utils.session_handler import verify_restaurant_access, verify_admin_access, verify_rider_access, verify_customer_access
from flask import Blueprint, render_template, request, redirect, url_for, session, flash
from app.utils.consumer_rabbitmq import consume_orders
from app.models.restaurant import Restaurant
from app.extensions import app, socketio
from app.models.user import db, User
import threading



auth_bp = Blueprint('auth', __name__)


@auth_bp.route('/login', methods=['GET', 'POST'])
def login():

    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        restaurant_id_input = request.form.get('restaurant_id')  # Ottieni l'ID del ristorante dal form

        # Trova l'utente
        user = User.query.filter_by(username=username).first()

        if user and user.check_password(password):
            # Se il ruolo è "restaurant", verifica il restaurant_id
            if user.role == 'restaurant':
                if not restaurant_id_input:
                    flash('L\'ID del ristorante è obbligatorio per i ristoranti.')
                    return redirect(url_for('auth.login'))
                try:
                    restaurant_id_input = int(restaurant_id_input)
                except ValueError:
                    flash('ID del ristorante non valido.')
                    return redirect(url_for('auth.login'))

                if user.restaurant_id != restaurant_id_input:
                    flash('L\'ID del ristorante non corrisponde al tuo ristorante.')
                    return redirect(url_for('auth.login'))

            # Salva i dati nella sessione
            session['user_id'] = user.id
            session['role'] = user.role
            session['restaurant_id'] = restaurant_id_input if user.role == 'restaurant' else None

            if user.role == 'restaurant':
                # Avvia il consumatore subito dopo il login
                consumer_thread = threading.Thread(target=consume_orders, args=(restaurant_id_input, app, socketio))
                consumer_thread.start()

            # Redirigi alla dashboard del ristorante
            if user.role == 'restaurant':
                return redirect(url_for('auth.restaurant_dashboard'))
            elif user.role == 'customer':
                return redirect(url_for('auth.customer_dashboard'))
            elif user.role == 'rider':
                return redirect(url_for('auth.rider_dashboard'))
            elif user.role == 'admin':
                return redirect(url_for('auth.admin_dashboard'))

        flash('Credenziali non valide.')
    return render_template('login.html')



@auth_bp.route('/register', methods=['GET', 'POST'])
def register():

    restaurant_id_input = request.args.get('restaurant_id')  # Ottieni restaurant_id dalla query string (se presente)

    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']
        name = request.form['name']
        address = request.form['address']
        role = request.form['role']
        restaurant_id_input = request.form.get('restaurant_id')  # Ottieni anche dal form, se è stato inserito manualmente

        # Verifica che l'username non esista già
        if User.query.filter_by(username=username).first():
            flash('Il nome utente esiste già.')
            return redirect(url_for('auth.register', restaurant_id=restaurant_id_input if restaurant_id_input else None))

        # Se il ruolo è "restaurant", verifichiamo l'ID del ristorante
        if role == 'restaurant':
            if not restaurant_id_input:
                flash('L\'ID del ristorante è obbligatorio per i ristoranti.')
                return redirect(url_for('home.home'))
            # Verifica se il ristorante esiste già nel database
            existing_restaurant = Restaurant.query.filter_by(id=restaurant_id_input).first()
            if existing_restaurant:
                flash('Questo ristorante è già registrato. Scegli un altro ristorante.')
                return redirect(url_for('auth.register', restaurant_id=restaurant_id_input))

            # Se non esiste, creiamo un nuovo ristorante
            restaurant = Restaurant(id=restaurant_id_input, name=name, address=address)
            db.session.add(restaurant)
            db.session.commit()  # Commit subito per ottenere l'ID del ristorante

        # Creazione dell'utente
        user = User(username=username, name=name, address=address, role=role)

        # Se il ruolo è "restaurant", aggiungiamo l'ID del ristorante all'utente
        if role == 'restaurant' and restaurant_id_input:
            user.restaurant_id = restaurant.id

        user.set_password(password)
        db.session.add(user)
        db.session.commit()

        flash('Registrazione completata. Puoi ora effettuare il login.')

        # Passiamo restaurant_id all'URL del login
        return redirect(url_for('auth.login', restaurant_id=restaurant_id_input if restaurant_id_input else None))

    # Mostriamo il form di registrazione con l'ID del ristorante (se presente)
    return render_template('register.html', restaurant_id=restaurant_id_input)



@auth_bp.route('/logout', methods=['POST'])
def logout():

    session.clear()  # Rimuove tutti i dati dalla sessione
    return redirect(url_for('home.home'))  # Reindirizza alla home page



@auth_bp.route('/restaurant_dashboard', methods=['GET'])
def restaurant_dashboard():

    # Verifica l'accesso dell'utente
    access_denied = verify_restaurant_access(session.get('restaurant_id'))
    if access_denied:
        return access_denied

    user = User.query.get(session['user_id'])

    # Rendi visibile il dashboard del ristorante
    return render_template('restaurant_dashboard.html', user=user)



@auth_bp.route('/customer_dashboard', methods=['GET'])
def customer_dashboard():

    access_denied = verify_customer_access()
    if access_denied:
        return access_denied

    user = User.query.get(session['user_id'])
    return render_template('customer_dashboard.html', user=user)


@auth_bp.route('/rider_dashboard', methods=['GET'])
def rider_dashboard():

    access_denied = verify_rider_access()
    if access_denied:
        return access_denied

    user = User.query.get(session['user_id'])
    return render_template('rider_dashboard.html', user=user)


@auth_bp.route('/administrator_dashboard', methods=['GET'])
def admin_dashboard():

    # Verifica che l'utente sia autenticato e abbia accesso di amministratore
    access_denied = verify_admin_access()
    if access_denied:
        return access_denied

    user = User.query.get(session.get('user_id'))
    if not user:
        return redirect(url_for('auth.login'))

    return render_template('admin_dashboard.html', user=user)

