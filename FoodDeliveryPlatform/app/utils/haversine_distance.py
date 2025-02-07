from math import radians, sin, cos, atan2, sqrt


# Funzione Haversine per calcolare la distanza tra due coordinate su un sistema sferico (globo terrestre)
def haversine(lat1, lon1, lat2, lon2):

        # Converte le coordinate da gradi a radianti
        lat1, lon1, lat2, lon2 = map(radians, [lat1, lon1, lat2, lon2])

        # Differenze
        dlat = lat2 - lat1
        dlon = lon2 - lon1

        # Formula Haversine
        a = sin(dlat / 2) ** 2 + cos(lat1) * cos(lat2) * sin(dlon / 2) ** 2
        c = 2 * atan2(sqrt(a), sqrt(1 - a))

        # Raggio della Terra in km
        radius = 6371

        # Distanza in km
        return radius * c
