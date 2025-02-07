// Connessione al server SocketIO
var socket = io.connect('http://' + document.domain + ':' + location.port);

// Funzione per aggiornare la lista degli ordini
socket.on('new_order', function(order) {
    console.log('Nuovo ordine ricevuto:', order);
    var ordersList = document.getElementById('orders-list');
    var newRow = document.createElement('tr');
    newRow.innerHTML = `
        <td>${order.id}</td>
        <td>${order.customer_id}</td>
        <td>${order.order_date}</td>
        <td>${order.status}</td>
        <td>${order.total.toFixed(1)}</td>
        <td><a href="/restaurant/order/${order.id}" class="btn btn-info btn-sm">Visualizza Dettagli</a></td>
    `;
    ordersList.appendChild(newRow);
});
