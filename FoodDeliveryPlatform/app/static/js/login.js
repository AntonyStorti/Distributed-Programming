// Verifica che gli elementi esistano prima di procedere
const restaurantIdField = document.getElementById("restaurant_id");
const registerLink = document.getElementById("register-link");

if (restaurantIdField && registerLink) {
    const restaurantId = restaurantIdField.value; // Prendi il valore dal campo nascosto
    const baseUrl = new URL(registerLink.href); // Usa URL API per gestire il link in modo sicuro

    if (restaurantId) {
        baseUrl.searchParams.set("restaurant_id", restaurantId); // Aggiungi/aggiorna il parametro
    } else {
        baseUrl.searchParams.delete("restaurant_id"); // Rimuovi il parametro se non è presente
    }

    registerLink.href = baseUrl.toString(); // Aggiorna l'attributo href
}
