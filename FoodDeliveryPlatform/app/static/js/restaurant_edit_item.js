document.addEventListener("DOMContentLoaded", () => {
    const editItemForm = document.getElementById("edit-item-form");
    const urlParams = new URLSearchParams(window.location.search);
    const itemId = urlParams.get("id"); // Prendi l'id del piatto dalla query string

    const loadItemData = async () => {
        try {
            const response = await fetch(`/restaurant/menu/${restaurantId}/${itemId}`);
            const data = await response.json();

            if (response.ok) {
                // Popola il modulo con i dati esistenti
                editItemForm.name.value = data.name;
                editItemForm.description.value = data.description;
                editItemForm.price.value = data.price;
                editItemForm.available.value = data.available.toString();
            } else {
                console.error("Errore nel recupero del piatto:", data.error);
            }
        } catch (error) {
            console.error("Errore nella richiesta al server:", error);
        }
    };

    editItemForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const updatedItem = {
            name: editItemForm.name.value,
            description: editItemForm.description.value,
            price: parseFloat(editItemForm.price.value),
            available: editItemForm.available.value === "true"
        };

        try {
            const response = await fetch(`/restaurant/menu/${restaurantId}/${itemId}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(updatedItem)
            });

            if (response.ok) {
                alert("Piatto aggiornato con successo!");
                window.location.href = "/menu"; // Torna alla pagina del menu dopo l'aggiornamento
            } else {
                const data = await response.json();
                console.error("Errore:", data.error);
                alert("Errore nell'aggiornamento del piatto.");
            }
        } catch (error) {
            console.error("Errore nella richiesta al server:", error);
            alert("Si è verificato un errore nel contattare il server.");
        }
    });

    loadItemData();
});
