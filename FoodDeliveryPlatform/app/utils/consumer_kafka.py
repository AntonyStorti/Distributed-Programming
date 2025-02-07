from confluent_kafka import KafkaError, Consumer, TopicPartition
from app.utils.get_coordinates import get_coordinates_nominatim
from app.utils.haversine_distance import haversine
from app.models.user import User
from app.extensions import app
import json



def consume_kafka_messages(user_id, queue):
    with app.app_context():

        # Configura il consumer Kafka
        consumer = Consumer({
            'bootstrap.servers': 'localhost:9092',
            'group.id': f'rider_{user_id}',  # Gruppo di consumatori
            'auto.offset.reset': 'earliest',  # Avvio dal primo messaggio disponibile
            'enable.auto.commit': False       # Disabilita il commit automatico
        })

        # Ottieni informazioni sui topic e le partizioni disponibili
        topics = consumer.list_topics()
        partitions = topics.topics['delivery_requests'].partitions

        # Crea una lista di TopicPartition
        topic_partitions = [TopicPartition('delivery_requests', partition) for partition in partitions]

        # Assegna manualmente le partizioni al consumer
        consumer.assign(topic_partitions)

        # Ottieni l'indirizzo e le coordinate del rider
        rider_address = User.query.get(user_id).address
        rider_lat, rider_lon = get_coordinates_nominatim(rider_address)

        relevant_requests = []

        try:
            while True:
                # Consuma il messaggio
                msg = consumer.poll(timeout=1.0)  # Tempo di attesa in secondi

                if msg is None:
                    # Nessun messaggio disponibile
                    continue

                if msg.error():
                    if msg.error().code() != KafkaError._PARTITION_EOF:
                        print(msg.error())
                        continue

                # Elabora il messaggio
                request_data = json.loads(msg.value().decode('utf-8'))
                restaurant_coords = request_data['coordinates']

                # Calcola la distanza tra il rider e il ristorante
                distance = haversine(rider_lat, rider_lon, restaurant_coords['lat'], restaurant_coords['lon'])

                # Aggiungi la distanza ai dati
                request_data['distance'] = distance

                # Aggiungi la richiesta se è pertinente
                if distance <= 10:
                    relevant_requests.append(request_data)

                    # Commit dell'offset del messaggio processato
                    consumer.commit(asynchronous=False)

                # Inserisci i dati nella coda
                queue.put(relevant_requests)

        except Exception as e:
            print(f"Errore durante la lettura delle richieste: {e}")

        finally:
            # Chiudi il consumer una volta che il loop termina
            consumer.close()

    return relevant_requests
