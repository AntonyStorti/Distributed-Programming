// Funzione per caricare gli ordini tracciabili via AJAX
function loadOrders() {
    fetch('/delivery/optimal-route')
        .then(response => response.json())
        .then(data => {
            const ordersList = document.getElementById('orders-list');
            ordersList.innerHTML = '';  // Pulisce la lista esistente

            if (data.length === 0) {
                ordersList.innerHTML = '<p class="text-center">Nessun ordine disponibile per la tracciatura.</p>';
            } else {
                data.forEach(order => {
                    const listItem = document.createElement('div');
                    listItem.className = 'order-item';  // Usato per disporre gli elementi in colonna

                    const mapId = `map-${order.delivery_id}`; // ID unico per ogni mappa

                    listItem.innerHTML = `
                        <div>
                            <h5 class="mb-1">ID Consegna: ${order.delivery_id}</h5>
                            <p class="mb-1">Partenza: ${order.start.lat}, ${order.start.lon}</p>
                            <p class="mb-1">Destinazione: ${order.destination.lat}, ${order.destination.lon}</p>
                        </div>
                        <button class="btn btn-primary btn-sm" data-order='${JSON.stringify(order)}' onclick="trackRoute(this, '${mapId}')">Traccia percorso</button>
                        <div id="${mapId}" class="map-container"></div>  <!-- Div per la mappa che appare sotto il pulsante -->
                    `;
                    ordersList.appendChild(listItem);
                });
            }
        })
        .catch(error => {
            console.error('Errore durante il caricamento degli ordini:', error);
        });
}

// Funzione per tracciare il percorso
function trackRoute(button, mapId) {
    const order = JSON.parse(button.getAttribute('data-order'));
    const path = order.path;

    // Nasconde tutte le mappe
    const maps = document.querySelectorAll('.map-container');
    maps.forEach(map => map.style.display = 'none');

    // Mostra la mappa per l'ordine selezionato
    const mapDiv = document.getElementById(mapId);
    mapDiv.style.display = 'block';
    mapDiv.innerHTML = '';  // Resetta la mappa prima di crearne una nuova

    // Crea la mappa per l'ordine selezionato
    const map = L.map(mapId).setView([path[0].lat, path[0].lon], 13);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    // Aggiungi marker per il punto di partenza e di arrivo
    L.marker([path[0].lat, path[0].lon]).addTo(map).bindPopup('Punto di partenza');
    L.marker([path[path.length - 1].lat, path[path.length - 1].lon]).addTo(map).bindPopup('Destinazione');

    // Aggiungi una polilinea per il percorso
    const route = path.map(p => [p.lat, p.lon]);
    L.polyline(route, { color: 'blue', weight: 4 }).addTo(map);
}

// Carica gli ordini quando la pagina è pronta
window.onload = function() {
    loadOrders();
};
