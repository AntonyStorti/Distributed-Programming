document.querySelectorAll('.delete-btn').forEach(button => {
    button.addEventListener('click', async () => {
        const userId = button.getAttribute('data-user-id');
        if (confirm('Sei sicuro di voler eliminare questo utente?')) {
            const response = await fetch('/admin/manage_accounts', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ user_id: userId }),
            });

            const result = await response.json();
            if (response.ok) {
                alert(result.message);
                window.location.reload();
            } else {
                alert(result.error || 'Errore durante l’eliminazione dell’utente');
            }
        }
    });
});
