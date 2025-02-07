from app import create_app
import threading


# Inizializza la app Flask usando create_app
app, socketio, orders = create_app()

@app.route("/test")
def home():

    thread_id = threading.get_ident()  # Ottiene l'ID del thread corrente
    total_threads = threading.active_count()  # Conta il numero totale di thread attivi

    return f"Hello, World! from thread {thread_id}, Total threads: {total_threads}"


if __name__ == '__main__':

    # Avvia l'app Flask con SocketIO
    socketio.run(app, debug=True, threaded=True)
