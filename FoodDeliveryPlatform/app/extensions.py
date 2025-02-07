from flask_sqlalchemy import SQLAlchemy
from flask_socketio import SocketIO
from flask import Flask


app = Flask(__name__)
db = SQLAlchemy()
socketio = SocketIO(app)
orders = []