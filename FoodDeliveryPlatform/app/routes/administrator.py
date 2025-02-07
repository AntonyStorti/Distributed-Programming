from flask import Blueprint, session, jsonify, request, render_template
from app.utils.session_handler import verify_admin_access
from app.models.restaurant import Restaurant
from app.models.delivery import Delivery
from app.models.order import Order
from app.models.user import User
from app.extensions import db



admin_bp = Blueprint('admin', __name__)

@admin_bp.route('/manage_accounts', methods=['GET', 'POST'])
def manage_accounts():

    access_denied = verify_admin_access()
    if access_denied:
        return access_denied

    if request.method == 'GET':
        # Recupera tutti gli utenti, eccetto l'admin stesso
        users = User.query.filter(User.id != session.get('user_id')).all()
        return render_template('admin_manage_accounts.html', users=users)

    if request.method == 'POST':
        # Elimina un utente specifico
        data = request.json
        user_to_delete_id = data.get('user_id')

        if not user_to_delete_id:
            return jsonify({'error': 'ID utente mancante'}), 400

        user_to_delete = User.query.get_or_404(user_to_delete_id)

        if user_to_delete.role == 'admin':
            return jsonify({'error': 'Non puoi eliminare un amministratore'}), 403

        db.session.delete(user_to_delete)
        db.session.commit()

        return jsonify({'message': 'Utente eliminato con successo', 'user_id': user_to_delete_id})


@admin_bp.route('/monitor_performance', methods=['GET'])
def monitor_performance():

    access_denied = verify_admin_access()
    if access_denied:
        return access_denied

    # Recupero dati sulle performance
    performance_data = db.session.query(
        User.id,
        User.name,
        db.func.count(Delivery.id).label('completed_deliveries')
    ).join(Delivery, User.id == Delivery.delivery_person_id) \
    .filter(User.role == 'rider') \
    .group_by(User.id).all()

    total_completed_deliveries = db.session.query(
        db.func.count(Delivery.id)
    ).filter(Delivery.status == 'completed').scalar()

    total_orders = db.session.query(db.func.count(Order.id)).scalar()


    # Dati per il grafico
    restaurant_orders = db.session.query(
        Restaurant.name,
        db.func.count(Order.id).label('total_orders')
    ).join(Order, Restaurant.id == Order.restaurant_id) \
    .group_by(Restaurant.name).all()

    return render_template(
        'admin_monitor_performance.html',
        performance_data=performance_data,
        total_completed_deliveries=total_completed_deliveries,
        total_orders=total_orders,
        restaurant_orders=restaurant_orders
    )

