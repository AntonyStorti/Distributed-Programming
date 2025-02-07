import pika
import json



def publish_order(order_data, restaurant_id):

    """Invia un ordine al ristorante tramite RabbitMQ."""
    connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
    channel = connection.channel()

    # Creazione di una coda (una per ristorante)
    queue_name = f"restaurant_{restaurant_id}_orders"
    channel.queue_declare(queue=queue_name, durable=True)

    # Pubblica il messaggio
    channel.basic_publish(
        exchange='',
        routing_key=queue_name,
        body=json.dumps(order_data),
        properties=pika.BasicProperties(delivery_mode=2)  # Persistenza del messaggio
    )
    connection.close()

