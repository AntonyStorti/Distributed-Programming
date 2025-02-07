from confluent_kafka import Producer
import json



def send_order_to_kafka(order_id, restaurant_id, lat, lon):

    producer = Producer({'bootstrap.servers': 'localhost:9092'})  # Connessione a Kafka

    # Callback per la gestione degli errori
    def delivery_report(err, msg):

        if err is not None:
            print('Errore nel messaggio: {}'.format(err))


    # Crea il messaggio con i dati dell'ordine
    message = {
        'order_id': order_id,
        'restaurant_id': restaurant_id,
        'status': 'ready_for_pickup',
        'coordinates': {'lat': lat, 'lon': lon}
    }


    # Serializza il messaggio in JSON e lo invia a Kafka
    producer.produce('delivery_requests', json.dumps(message).encode('utf-8'), callback=delivery_report)
    producer.flush()  # Assicura che il messaggio venga inviato prima di continuare

