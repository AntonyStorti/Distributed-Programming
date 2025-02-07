from flask import session, flash, redirect, url_for



def get_session_data():
    """
    Recupera i dati essenziali dalla sessione.
    """
    return {
        'user_id': session.get('user_id'),
        'role': session.get('role'),
        'restaurant_id': session.get('restaurant_id') if session.get('role') == 'restaurant' else None,
    }


def verify_admin_access():
    """
    Verifica se l'utente è un amministratore.
    """
    session_data = get_session_data()

    if session_data['role'] != 'admin':
        flash('Accesso riservato agli amministratori.', 'error')
        return redirect(url_for('auth.login'))

    return None  # Accesso consentito


def verify_rider_access():
    """
    Verifica se l'utente è un rider.
    """
    session_data = get_session_data()

    if session_data['role'] != 'rider':
        flash('Accesso riservato ai rider.', 'error')
        return redirect(url_for('auth.login'))

    return None  # Accesso consentito


def verify_customer_access():
    """
    Verifica se l'utente è un customer.
    """
    session_data = get_session_data()

    if session_data['role'] != 'customer':
        flash('Accesso riservato ai clienti.', 'error')
        return redirect(url_for('auth.login'))

    return None  # Accesso consentito


def verify_restaurant_access(restaurant_id):
    """
    Verifica se l'utente corrente ha accesso al ristorante specificato.
    """
    session_data = get_session_data()

    # Verifica l'accesso solo per i ristoranti
    if session_data['role'] == 'restaurant':
        if (
            not session_data['user_id']
            or session_data['restaurant_id'] != restaurant_id
        ):
            flash('Accesso non autorizzato. Effettua il login per continuare.', 'error')
            return redirect(url_for('auth.login'))

    # Per gli altri ruoli, non richiedere il controllo del restaurant_id
    return None  # Indica che l'accesso è consentito
