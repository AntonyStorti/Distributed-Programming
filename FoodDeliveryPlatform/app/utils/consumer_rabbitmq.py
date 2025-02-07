from app.models.order import Order, OrderItem
from app.models.menu import MenuItem
from app.extensions import db
from datetime import datetime
import pika
import json



def consume_orders(restaurant_id, app, socketio):

    """Funzione di consumazione asincrona per RabbitMQ."""
    connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
    channel = connection.channel()
    queue_name = f"restaurant_{restaurant_id}_orders"
    channel.queue_declare(queue=queue_name, durable=True)

    def on_message(channel, method, properties, body):
        order_data = json.loads(body)

        with app.app_context():
            order_date = datetime.strptime(order_data['order_date'], '%Y-%m-%d %H:%M:%S')

            # Crea il nuovo ordine nel database
            new_order = Order(
                customer_id=order_data['customer_id'],
                restaurant_id=order_data['restaurant_id'],
                order_date=order_date,
                status='pending',  # Lo stato iniziale dell'ordine
                total=order_data['total']
            )

            db.session.add(new_order)
            db.session.flush()  # Genera l'ID per il nuovo ordine prima di usarlo

            # Aggiungi gli articoli dell'ordine al database:
            for item_data in order_data['items']:
                item = MenuItem.query.filter_by(name=item_data['name']).first()
                if item:
                    order_item = OrderItem(
                        order_id=new_order.id,
                        item_id=item.id,
                        quantity=item_data['quantity']
                    )
                    db.session.add(order_item)
                else:
                    print(f"Articolo non trovato: {item_data['name']}")

            db.session.commit()

            # Ricarica l'ordine con gli articoli dal database (usando le relazioni di SQLAlchemy):
            complete_order = Order.query.filter_by(id=new_order.id).first()

            # Usa direttamente l'oggetto `complete_order` per raccogliere i dati:
            order_to_send = {
                'id': complete_order.id,
                'customer_id': complete_order.customer_id,
                'order_date': complete_order.order_date.strftime('%Y-%m-%d %H:%M:%S'),  # Converti datetime in stringa
                'status': complete_order.status,
                'total': complete_order.total,
            }

        # Usa socketio.emit() per inviare l'ordine ai client connessi
        with app.app_context():
            socketio.emit('new_order', order_to_send)

        # Conferma il messaggio nella coda
        channel.basic_ack(delivery_tag=method.delivery_tag)

    channel.basic_consume(queue=queue_name, on_message_callback=on_message)
    channel.start_consuming()
