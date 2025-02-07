document.getElementById('availability-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    const isAvailable = document.getElementById('isAvailable').checked;
    const startHour = document.getElementById('startHour').value;
    const startMinute = document.getElementById('startMinute').value;
    const endHour = document.getElementById('endHour').value;
    const endMinute = document.getElementById('endMinute').value;

    const startTime = `${startHour}:${startMinute}`;
    const endTime = `${endHour}:${endMinute}`;

    const response = await fetch('/delivery/availability', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            is_available: isAvailable,
            start_time: startTime,
            end_time: endTime,
        }),
    });

    const result = await response.json();
    if (response.ok) {
        alert(result.message);
        window.location.reload();
    } else {
        alert(result.error || 'Errore durante l’aggiornamento della disponibilità');
    }
});
