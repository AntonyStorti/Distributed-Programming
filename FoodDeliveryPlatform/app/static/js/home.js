// Mostra o nasconde il campo 'restaurant_id' a seconda del ruolo selezionato
document.getElementById("role").addEventListener("change", function() {
    if (this.value === "restaurant") {
        document.getElementById("restaurant-id").style.display = "block";
    } else {
        document.getElementById("restaurant-id").style.display = "none";
    }
});

// Verifica che un ruolo valido sia selezionato prima di inviare il form
document.getElementById("roleForm").addEventListener("submit", function(event) {
    var role = document.getElementById("role").value;
    if (!role) {
        alert("Per favore, seleziona un ruolo.");
        event.preventDefault();  // Impedisce l'invio del form se il ruolo non è selezionato
    }
});
