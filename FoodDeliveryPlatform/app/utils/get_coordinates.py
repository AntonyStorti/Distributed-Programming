import requests


def get_coordinates_nominatim(address):

    try:
        # Codifica l'indirizzo per essere usato nella URL
        url = f"https://nominatim.openstreetmap.org/search?q={address}&format=json&addressdetails=1"

        headers = {
            "User-Agent": "FoodDeliveryPlatform/1.0 (contact@example.com)"
        }


        # Fai la richiesta GET
        response = requests.get(url, headers=headers, timeout=10)  # Imposta un timeout per evitare blocchi
        response.raise_for_status()  # Solleva un'eccezione se lo stato HTTP non è 200

        # Verifica se il contenuto è JSON
        data = response.json()
        if data:
            # Prendi il primo risultato e estrai latitudine e longitudine
            lat = float(data[0]['lat'])
            lon = float(data[0]['lon'])
            return lat, lon
        else:
            return None, None

    except requests.exceptions.RequestException as e:

        print(f"Errore nella richiesta a Nominatim: {e}")
        return None, None

    except (KeyError, ValueError) as e:

        print(f"Errore nella decodifica della risposta Nominatim: {e}")
        return None, None
