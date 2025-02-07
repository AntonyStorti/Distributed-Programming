document.querySelectorAll('.status-select').forEach(select => {
    select.addEventListener('change', function () {
        const deliveryId = this.getAttribute('data-id');
        const newStatus = this.value;

        fetch(`/delivery/${deliveryId}/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ status: newStatus }),
        })
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                alert('Errore: ' + data.error);
            } else {
                alert('Stato aggiornato con successo!');
            }
        })
        .catch(error => console.error('Errore:', error));
    });
});
