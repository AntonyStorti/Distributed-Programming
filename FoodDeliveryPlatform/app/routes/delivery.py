from flask import Blueprint, request, jsonify, session, flash, redirect, url_for, render_template
from app.models.delivery import Delivery, DeliveryPersonAvailability, OrderReject
from app.utils.get_coordinates import get_coordinates_nominatim
from app.utils.consumer_kafka import consume_kafka_messages
from app.utils.session_handler import verify_rider_access
from app.utils.haversine_distance import haversine
from app.models.restaurant import Restaurant
from confluent_kafka import Producer
from app.models.order import Order
from app.models.user import User
from queue import Queue, Empty
from datetime import datetime
from app.extensions import db
import threading
import json



delivery_bp = Blueprint('delivery', __name__, url_prefix='/delivery')


requests_queue = Queue()

def update_request_status(delivery_id, status):
    order = Order.query.get_or_404(delivery_id)
    order.status = status  # Imposta lo stato come 'accepted' o 'rejected'
    db.session.commit()


# Endpoint per ottenere le richieste
@delivery_bp.route('/requests_data', methods=['GET'])
def get_requests_data():

    access_denied = verify_rider_access()
    if access_denied:
        return access_denied

    user_id = session.get('user_id')
    user = User.query.get_or_404(user_id)
    rider_lat, rider_lon = get_coordinates_nominatim(user.address)

    relevant_requests = []

    # Gestisci l'eccezione se la coda è vuota
    try:
        relevant_requests = requests_queue.get_nowait()  # Non bloccante
    except Empty:
        relevant_requests = []  # Se la coda è vuota, restituisci una lista vuota


    # Escludi gli ordini che sono stati rifiutati dal rider o da altri:
    rejected_order_ids = [reject.order_id for reject in OrderReject.query.filter_by(rider_id=user_id).all()]

    relevant_requests = [request for request in relevant_requests if request['order_id'] not in rejected_order_ids]

    deliveries = Delivery.query.filter_by(delivery_person_id=user_id).all()

    for delivery in deliveries:
        order = delivery.order
        restaurant = Restaurant.query.get_or_404(order.restaurant_id)
        restaurant_lat, restaurant_lon = get_coordinates_nominatim(restaurant.address)
        distance = haversine(rider_lat, rider_lon, restaurant_lat, restaurant_lon)
        request_data =  {
            'order_id': delivery.order_id,
            'restaurant_id': restaurant.id,
            'status': delivery.status,
            'distance': distance
        }
        relevant_requests.append(request_data)

    # Assicurati di restituire lo stato aggiornato delle richieste:
    for request in relevant_requests:
        order = Order.query.get(request['order_id'])
        if order is not None:
            request['status'] = order.status  # Stato aggiornato

    return jsonify(relevant_requests)


@delivery_bp.route('/requests', methods=['GET'])
def view_requests():

    access_denied = verify_rider_access()
    if access_denied:
        return access_denied

    user_id = session.get('user_id')

    # Avvia il task Kafka in un thread separato
    task_thread = threading.Thread(target=consume_kafka_messages, args=(user_id, requests_queue))
    task_thread.start()

    # Ritorna una risposta immediata alla richiesta HTTP
    return render_template('rider_delivery_requests.html')


@delivery_bp.route('/requests/<int:delivery_id>/action', methods=['POST'])
def handle_delivery_request(delivery_id):

    access_denied = verify_rider_access()
    if access_denied:
        return access_denied

    data = request.get_json(silent=True)
    if not data or 'action' not in data:
        return jsonify({'error': 'Dati non validi o mancanti'}), 400

    action = data.get('action')  # "accept" o "reject"

    if action not in ['accept', 'reject']:
        return jsonify({'error': 'Azione non valida'}), 400

    # Configura Kafka producer
    producer = Producer({'bootstrap.servers': 'localhost:9092'})

    try:
        # Crea un messaggio per Kafka
        message = {
            'delivery_id': delivery_id,
            'action': action,
            'rider_id': session.get('user_id'),
            'timestamp': datetime.utcnow().isoformat()
        }

        # Invia il messaggio al topic Kafka
        producer.produce('delivery_responses', json.dumps(message).encode('utf-8'))
        producer.flush()

        # Esegui la logica per cambiare lo stato e aggiornare il database
        if action == 'accept':
            # Aggiorna lo stato come accettato
            update_request_status(delivery_id, 'accepted_by_rider')

            # Ottieni l'ordine corrispondente e l'utente attuale
            order = Order.query.get_or_404(delivery_id)  # Assumi che delivery_id corrisponda all'ordine
            rider_id = session.get('user_id')

            # Controlla che l'utente sia un rider
            rider = User.query.get_or_404(rider_id)

            # Ottieni le coordinate del ristorante
            restaurant_lat, restaurant_lon = get_coordinates_nominatim(Restaurant.query.get_or_404(order.restaurant_id))

            # Inserisci l'ordine nella tabella deliveries
            new_delivery = Delivery(
                order_id=order.id,
                delivery_person_id=rider.id,
                status='accepted_by_rider',
                current_latitude=restaurant_lat,
                current_longitude=restaurant_lon,
                created_at=datetime.utcnow()
            )
            db.session.add(new_delivery)
            db.session.commit()

            # Invia un aggiornamento al frontend
            return jsonify({'message': 'Ordine accettato con successo', 'status': 'accepted_by_rider', 'order': order.id})

        elif action == 'reject':
            # Aggiorna lo stato come rifiutato
            update_request_status(delivery_id, 'ready_for_pickup')

            # Recupera l'ordine e il ristorante
            order = Order.query.get_or_404(delivery_id)
            restaurant = Restaurant.query.get_or_404(order.restaurant_id)

            # Ottieni le coordinate del ristorante
            restaurant_lat, restaurant_lon = get_coordinates_nominatim(restaurant)

            # Registra il rifiuto da parte del rider
            new_rejection = OrderReject(order_id=order.id, rider_id=session.get('user_id'))
            db.session.add(new_rejection)
            db.session.commit()

            return jsonify({'message': 'Ordine rifiutato', 'status': 'rejected'})

    except Exception as e:

        print(f"Errore durante l'invio della risposta: {e}")
        return jsonify({'error': 'Errore durante l\'invio della risposta'}), 500


@delivery_bp.route('/optimal-route', methods=['GET'])
def optimal_routes():

    access_denied = verify_rider_access()
    if access_denied:
        return access_denied

    # Recupera tutti gli ordini con stato "accepted_by_rider" o "shipped"
    deliveries = Delivery.query.filter(Delivery.delivery_person_id == session.get('user_id'), Delivery.status.in_(['accepted_by_rider', 'shipped'])).all()

    # Se non ci sono consegne, restituisci un messaggio di errore
    if not deliveries:
        return jsonify({'error': 'Nessun ordine accettato o in transito'}), 404

    # Lista per memorizzare i percorsi ottimali
    routes = []

    # Cicla su ogni ordine per calcolare la route
    for delivery in deliveries:
        # Ottieni le coordinate del cliente (destinazione)
        customer_address_lat, customer_address_lon = get_coordinates_nominatim(delivery.order.customer.address)

        # Calcola il percorso (qui possiamo fare un'implementazione fittizia o usare un'API di routing come Google Maps o OpenStreetMap)
        route = {
            'delivery_id': delivery.id,
            'start': {'lat': delivery.current_latitude, 'lon': delivery.current_longitude},
            'destination': {'lat': customer_address_lat, 'lon': customer_address_lon},
            'path': [
                {'lat': delivery.current_latitude, 'lon': delivery.current_longitude},
                {'lat': customer_address_lat, 'lon': customer_address_lon}
            ]
        }

        routes.append(route)

    # Restituisci la lista dei percorsi
    return jsonify(routes)



@delivery_bp.route('/tracking')
def tracking_page():

    access_denied = verify_rider_access()
    if access_denied:
        return access_denied

    return render_template('rider_track_optimal_routes.html')


# Aggiorna stato della consegna
@delivery_bp.route('/update-status', methods=['GET'])
def update_status():

    access_denied = verify_rider_access()
    if access_denied:
        return access_denied

    # Recupera tutte le consegne con stato "accepted_by_rider"
    deliveries = Delivery.query.filter_by(delivery_person_id=session.get('user_id')).all()
    # Ritorna una pagina HTML con l'elenco
    return render_template('rider_update_status.html', deliveries=deliveries)


@delivery_bp.route('/<int:delivery_id>/status', methods=['PUT'])
def update_delivery_status(delivery_id):

    access_denied = verify_rider_access()
    if access_denied:
        return access_denied

    data = request.json
    new_status = data.get('status')  # E.g., "accepted_by_rider", "completed"

    if not new_status:
        return jsonify({'error': 'Stato mancante'}), 400

    if new_status not in ['accepted_by_rider', 'shipped', 'completed']:
        return jsonify({'error': 'Stato non valido'}), 400

    delivery = Delivery.query.get_or_404(delivery_id)
    delivery.status = new_status
    delivery.updated_at = datetime.utcnow()
    order = Order.query.get_or_404(delivery.order_id)
    order.status = new_status
    db.session.commit()

    return jsonify({'message': 'Stato aggiornato', 'status': delivery.status})



# Gestisci disponibilità del fattorino
@delivery_bp.route('/availability', methods=['GET', 'POST'])
def update_availability():

    access_denied = verify_rider_access()
    if access_denied:
        return access_denied

    user = User.query.get_or_404(session.get('user_id'))

    if request.method == 'GET':
        # Recupera la disponibilità del fattorino
        availability = DeliveryPersonAvailability.query.filter_by(delivery_person_id=user.id).first()
        return render_template('rider_update_availability.html', availability=availability)

    if request.method == 'POST':
        data = request.json
        is_available = data.get('is_available')
        start_time = data.get('start_time')  # Formato "HH:MM"
        end_time = data.get('end_time')  # Formato "HH:MM"

        availability = DeliveryPersonAvailability.query.filter_by(delivery_person_id=user.id).first()
        if not availability:
            availability = DeliveryPersonAvailability(delivery_person_id=user.id)

        availability.is_available = is_available
        if start_time:
            availability.start_time = datetime.strptime(start_time, '%H:%M').time()
        if end_time:
            availability.end_time = datetime.strptime(end_time, '%H:%M').time()

        db.session.add(availability)
        db.session.commit()
        return jsonify({'message': 'Disponibilità aggiornata', 'availability': availability.is_available})



# Gestisci profilo e le impostazioni del fattorino
@delivery_bp.route('/profile', methods=['GET', 'POST'])
def manage_profile():

    access_denied = verify_rider_access()
    if access_denied:
        return access_denied

    user = User.query.get_or_404(session.get('user_id'))

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
        return redirect(url_for('rider.manage_profile', user_id=user.id))

        # Passa i dati del ristorante al template
    return render_template('rider_view_profile.html', user=user)

