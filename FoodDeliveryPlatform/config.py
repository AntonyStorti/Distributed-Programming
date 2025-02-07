import os


class Config:

    SQLALCHEMY_DATABASE_URI = 'sqlite:///food_delivery.db'  # Percorso del database SQLite usato da SQLAlchemy
    SECRET_KEY = os.urandom(24)  # Chiave segreta usata per proteggere le sessioni e i dati delle richieste
    SQLALCHEMY_TRACK_MODIFICATIONS = False # Disabilita il tracking delle modifiche agli oggetti SQLAlchemy per migliorare le prestazioni
                                           # Una volta eseguito il commit gli stati precedenti degli oggetti non sono recuperabili!
    DEBUG = True
    FLASK_DEBUG = 1
    LOGGING_LEVEL = 'DEBUG'  # Imposta il livello di logging dell'applicazione su DEBUG
