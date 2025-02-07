document.addEventListener("DOMContentLoaded", () => {
    const menuList = document.getElementById("menu-list");
    const addItemForm = document.getElementById("add-item-form");

    const restaurantId = window.location.pathname.split("/").pop();

    let isEditing = false;
    let editingItemId = null;

    console.log("Pagina caricata e script eseguito");

    // Funzione per caricare il menu
    const loadMenu = async () => {
    try {
        const response = await fetch(`/restaurant/menu/json/${restaurantId}`); // Nuovo endpoint JSON
        const data = await response.json();

        if (response.ok) {
            renderMenu(data);
        } else {
            console.error("Errore nel recupero del menu:", data.error);
        }
    } catch (error) {
        console.error("Errore nella richiesta al server:", error);
    }
};


    // Funzione per renderizzare il menu
    const renderMenu = (menuItems) => {
        menuList.innerHTML = "";
        menuItems.forEach(item => {
            const li = document.createElement("li");
            li.innerHTML = `
                <div>
                    <strong>${item.name}</strong> - €${item.price.toFixed(2)}<br>
                    <small>${item.description || "Nessuna descrizione"}</small>
                </div>
                <div class="actions">
                    <button class="edit-item" data-id="${item.id}">Modifica</button>
                    <button class="delete-item" data-id="${item.id}">Elimina</button>
                </div>
            `;
            menuList.appendChild(li);
        });

        // Aggiungi il listener per il pulsante di modifica
        document.querySelectorAll(".edit-item").forEach(button => {
            button.addEventListener("click", (e) => {
                console.log("Pulsante Modifica cliccato per l'elemento:", e.target.dataset.id);
                const itemId = e.target.dataset.id;
                startEdit(itemId);
            });
        });

        // Aggiungi il listener per il pulsante di eliminazione
        document.querySelectorAll(".delete-item").forEach(button => {
            button.addEventListener("click", (e) => {
                console.log("Pulsante Elimina cliccato per l'elemento:", e.target.dataset.id);
                const itemId = e.target.dataset.id;
                deleteItem(itemId);  // Qui chiamiamo la funzione per eliminare
            });
        });
    };

    // Funzione per eliminare un piatto
    const deleteItem = async (itemId) => {
    const confirmDelete = confirm("Sei sicuro di voler eliminare questo piatto?");
    if (!confirmDelete) return;

    try {
        const response = await fetch(`/restaurant/menu/${restaurantId}/${itemId}`, {
            method: "DELETE"
        });

        if (response.ok) {
            alert("Piatto eliminato con successo!");
            loadMenu(); // Ricarica il menu dopo l'eliminazione
        } else {
            const data = await response.json();
            alert(`Errore nell'eliminazione: ${data.error}`);
        }
    } catch (error) {
        console.error("Errore nella richiesta al server:", error);
        alert("Si è verificato un errore nel contattare il server.");
    }
};


    // Funzione per iniziare la modifica
    const startEdit = async (itemId) => {
    try {
        const response = await fetch(`/restaurant/menu/${restaurantId}/${itemId}`);
        const data = await response.json();

        if (response.ok) {
            isEditing = true;
            editingItemId = itemId;

            // Popola il modulo con i dati esistenti
            addItemForm.name.value = data.name;
            addItemForm.description.value = data.description;
            addItemForm.price.value = data.price;
            addItemForm.available.value = data.available.toString();

            // Cambia il titolo del form per indicare che si sta modificando
            document.getElementById("form-title").textContent = "Modifica il Piatto";
        } else {
            console.error("Errore nel recupero del piatto:", data.error);
        }
    } catch (error) {
        console.error("Errore nella richiesta al server:", error);
    }
};

    // Aggiungi o modifica un piatto
    addItemForm.addEventListener("submit", async (e) => {
        e.preventDefault();

        const newItem = {
            name: addItemForm.name.value,
            description: addItemForm.description.value,
            price: parseFloat(addItemForm.price.value),
            available: addItemForm.available.value === "true"
        };

        const url = isEditing
            ? `/restaurant/menu/${restaurantId}/${editingItemId}`
            : `/restaurant/menu/${restaurantId}`;

        const method = isEditing ? "PUT" : "POST";

        try {
            const response = await fetch(url, {
                method: method,
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(newItem)
            });

            if (response.ok) {
                loadMenu();
                addItemForm.reset();
                isEditing = false;
                editingItemId = null;
            } else {
                const data = await response.json();
                console.error("Errore:", data.error);
            }
        } catch (error) {
            console.error("Errore nella richiesta al server:", error);
        }
    });

    loadMenu();
});


// Aggiungi o modifica un piatto
addItemForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const newItem = {
        name: addItemForm.name.value,
        description: addItemForm.description.value,
        price: parseFloat(addItemForm.price.value),
        available: addItemForm.available.value === "true"
    };

    const url = isEditing
        ? `/restaurant/menu/${restaurantId}/${editingItemId}` // Quando stai modificando, l'URL include l'ID dell'elemento
        : `/restaurant/menu/${restaurantId}`; // Altrimenti, per aggiungere un nuovo piatto

    const method = isEditing ? "PUT" : "POST";

    try {
        const response = await fetch(url, {
            method: method,
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(newItem)
        });

        if (response.ok) {
            loadMenu(); // Ricarica il menu dopo l'operazione
            addItemForm.reset();
            isEditing = false;
            editingItemId = null;
        } else {
            const data = await response.json();
            console.error("Errore:", data.error);
        }
    } catch (error) {
        console.error("Errore nella richiesta al server:", error);
    }
});

