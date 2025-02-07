from app.models.restaurant import Restaurant
from app.extensions import db, app
from datetime import datetime
import json



# Mappa i giorni in italiano a quelli in inglese
DAYS_MAP = {
    'lunedi': 'monday',
    'martedi': 'tuesday',
    'mercoledi': 'wednesday',
    'giovedi': 'thursday',
    'venerdi': 'friday',
    'sabato': 'saturday',
    'domenica': 'sunday'
}


def is_restaurant_open(opening_hours: str) -> bool:

    # Carica gli orari di apertura dal database in formato JSON
    try:
        hours = json.loads(opening_hours)
    except json.JSONDecodeError as e:
        return False

    # Ottieni il giorno corrente in italiano e poi mappalo in inglese
    current_day_english = datetime.now().strftime('%A').lower()  # Giorno corrente in inglese

    # Mappiamo il giorno in inglese al corrispondente giorno in italiano nel JSON
    current_day = next((day for day, english_day in DAYS_MAP.items() if english_day == current_day_english), None)

    if not current_day:
        return False  # Se non troviamo il giorno, ritorniamo False

    current_time = datetime.now().strftime('%H:%M')  # Orario corrente in formato HH:MM

    if current_day in hours:
        opening = hours[current_day]['apertura']
        closing = hours[current_day]['chiusura']

        # Gestione della chiusura oltre la mezzanotte (es. 00:30)
        if closing == "00:30":
            # Se l'orario di chiusura è 00:30, consideriamo l'orario di apertura del giorno successivo
            if current_time >= opening or current_time <= closing:
                return True
        else:
            if opening <= current_time <= closing:
                return True

    return False



def update_restaurant_open_status():

    """Aggiorna automaticamente lo stato di apertura dei ristoranti."""
    with app.app_context():

        # Recupera tutti i ristoranti
        restaurants = Restaurant.query.all()

        for restaurant in restaurants:
            # Verifica se il ristorante è aperto o chiuso
            is_open = 1 if is_restaurant_open(restaurant.opening_hours) else 0
            restaurant.is_open = is_open

            # Salva lo stato aggiornato nel database
            try:
                db.session.commit()
            except Exception as e:
                db.session.rollback()

