window.onload = function () {
    const ordersData = JSON.parse(document.getElementById('orders-data').textContent);
    const mapInstances = {}; // Memorizza le mappe create

    function initializeMap(orderId, lat, lon, destLat, destLon) {
        const mapContainer = document.getElementById(`map-${orderId}`);

        // Log per verificare se il div esiste e le coordinate sono corrette
        console.log(`Inizializzazione mappa per ordine #${orderId}`, { lat, lon, destLat, destLon });

        // Controlla se il div esiste
        if (!mapContainer) {
            console.error(`Contenitore mappa non trovato per ordine #${orderId}`);
            return;
        }

        // Pulisci il contenitore della mappa
        mapContainer.innerHTML = '';
        mapContainer._leaflet_id = null;

        // Crea la mappa
        const map = L.map(mapContainer).setView([lat, lon], 13);
        mapInstances[orderId] = map;

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap contributors',
        }).addTo(map);

        // Aggiungi i marker
        const restaurantMarker = L.marker([lat, lon]).addTo(map).bindPopup("<b>Ristorante</b>");
        let destinationMarker = null;

        if (destLat && destLon) {
            destinationMarker = L.marker([destLat, destLon]).addTo(map).bindPopup("<b>Destinazione</b>");
        }

        // Aggiungi la linea che collega i due punti
        if (lat && lon && destLat && destLon) {
            const line = L.polyline([[lat, lon], [destLat, destLon]], { color: 'blue', weight: 3 }).addTo(map);

            // Centra la mappa sulla linea
            map.fitBounds(line.getBounds());
        } else {
            // Centra solo sul marker del ristorante
            map.setView([lat, lon], 13);
        }

        return map;
    }

    console.log("Ordini ricevuti:", ordersData);

    ordersData.forEach(order => {
        const restaurantCoords = order.restaurant.coords || {};
        const lat = parseFloat(restaurantCoords.lat || 0);
        const lon = parseFloat(restaurantCoords.lon || 0);
        const destCoords = order.dest_coords || {};
        const destLat = parseFloat(destCoords.lat || 0);
        const destLon = parseFloat(destCoords.lon || 0);

        if (lat && lon) {
            initializeMap(order.id, lat, lon, destLat, destLon);
        } else {
            console.error(`Coordinate non valide per l'ordine #${order.id}`);
        }
    });
};
