const ctx = document.getElementById('ordersChart').getContext('2d');

new Chart(ctx, {
    type: 'bar',
    data: {
        labels: restaurantNames,
        datasets: [{
            label: 'Numero di Ordini',
            data: orderCounts,
            backgroundColor: 'rgba(54, 162, 235, 0.6)',
            borderColor: 'rgba(54, 162, 235, 1)',
            borderWidth: 1
        }]
    },
    options: {
        responsive: true,
        scales: {
            y: {
                beginAtZero: true
            }
        }
    }
});
