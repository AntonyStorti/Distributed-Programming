from apscheduler.schedulers.background import BackgroundScheduler
from app.utils.trigger import update_restaurant_open_status
from app.routes.restaurant import bp as restaurant_bp
from app.extensions import db, socketio, orders, app
from app.routes.customer import customer_bp
from app.routes.administrator import admin_bp
from app.routes.delivery import delivery_bp
from app.routes.home import bp as home_bp
from app.routes.auth import auth_bp
from flask_migrate import Migrate
from app.models.user import User



def start_scheduler():

    """Avvia lo scheduler per eseguire il task periodicamente."""
    scheduler = BackgroundScheduler()
    scheduler.add_job(update_restaurant_open_status, 'interval', minutes=5)  # Ogni 5 min
    scheduler.start()


migrate = Migrate()


def create_app():

    app.config.from_object('config.Config')

    db.init_app(app)
    migrate.init_app(app, db)

    socketio.init_app(app)


    # Registra i blueprint:
    # Evita la gestione di tutte le route in un unico file
    app.register_blueprint(home_bp)
    app.register_blueprint(restaurant_bp, url_prefix='/restaurant')
    app.register_blueprint(auth_bp, url_prefix='/auth')
    app.register_blueprint(customer_bp, url_prefix='/customer')
    app.register_blueprint(delivery_bp, url_prefix='/delivery')
    app.register_blueprint(admin_bp, url_prefix='/admin')

    start_scheduler()

    return app, socketio, orders
