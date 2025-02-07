// Funzione per caricare le richieste via AJAX
function loadRequests() {
    console.log('Caricamento richieste...');
    document.getElementById('loading-spinner').style.display = 'block'; // Mostra il loading spinner

    fetch('/delivery/requests_data')
        .then(response => response.json())
        .then(data => {
            console.log('Dati ricevuti:', data);
            const requestsList = document.getElementById('requests-list');
            requestsList.innerHTML = '';  // Pulisce la lista esistente

            if (data.length === 0) {
                requestsList.innerHTML = '<p class="text-center">Nessuna richiesta disponibile.</p>';
            } else {
                data.forEach(request => {
                    const listItem = document.createElement('div');
                    listItem.className = 'list-group-item d-flex justify-content-between align-items-center mb-3';

                    // Condizione per mostrare o meno i pulsanti in base allo stato della richiesta
                    let buttons = '';
                    if (request['status'] === 'ready_for_pickup') {
                        buttons = `
                            <button class="btn btn-success btn-sm" onclick="handleRequest(${request['order_id']}, 'accept', this)">Accetta</button>
                            <button class="btn btn-danger btn-sm ms-2" onclick="handleRequest(${request['order_id']}, 'reject', this)">Rifiuta</button>
                        `;
                    }

                    let statusBadge = `<span class="badge"></span>`;
                    if (request['status'] === 'accepted_by_rider') {
                        statusBadge = `<span class="badge bg-success">Accettato</span>`;
                    } else if (request['status'] === 'ready_for_pickup') {
                        statusBadge = `<span class="badge bg-info">Pronto per il Ritiro</span>`;
                    } else if (request['status'] === 'shipped') {
                        statusBadge = `<span class="badge bg-primary">In Consegna</span>`;
                    } else if (request['status'] === 'completed') {
                        statusBadge = `<span class="badge bg-warning">Completato</span>`;
                    }

                    listItem.innerHTML = `
                        <div>
                            <h5 class="mb-1">ID Ristorante: ${request['restaurant_id']}</h5>
                            <p class="mb-1">Distanza: ${request['distance'].toFixed(2)} km</p>
                            ${statusBadge}
                        </div>
                        <div>
                            ${buttons}
                        </div>
                    `;
                    requestsList.appendChild(listItem);
                });
            }
            document.getElementById('loading-spinner').style.display = 'none'; // Nascondi il loading spinner quando i dati sono pronti
        })
        .catch(error => {
            console.error('Errore durante il caricamento delle richieste:', error);
            document.getElementById('loading-spinner').style.display = 'none'; // Nascondi comunque il loading spinner in caso di errore
        });
}

// Funzione per gestire l'azione dell'ordine
function handleRequest(orderId, action, buttonElement) {
    fetch(`/delivery/requests/${orderId}/action`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ action: action })
    })
    .then(response => response.json())
    .then(data => {
        alert(data.message);  // Mostra un messaggio di successo

        // Dopo aver eseguito l'azione, disabilitiamo i pulsanti
        const listItem = buttonElement.closest('.list-group-item');
        const buttons = listItem.querySelectorAll('button');
        buttons.forEach(button => button.disabled = true);

        // Aggiungi il badge di completamento
        const statusBadge = listItem.querySelector('.badge');
        if (statusBadge) {
            if (action === 'accept') {
                statusBadge.className = 'badge bg-success';
                statusBadge.textContent = 'Accettato';
            } else if (action === 'reject') {
                statusBadge.className = 'badge bg-danger';
                statusBadge.textContent = 'Rifiutato';
            }
        } else {
            console.warn('Badge di stato non trovato per l\'ordine', orderId);
        }
        // Ricarica le richieste
        loadRequests();
    })
    .catch(error => {
        console.error('Errore durante l\'invio della risposta:', error);
    });
}

// Carica le richieste quando la pagina è pronta
window.onload = function() {
    loadRequests();
};
