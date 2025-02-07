from werkzeug.security import generate_password_hash, check_password_hash
from app.extensions import db



class User(db.Model):

    __tablename__ = 'users'

    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    name = db.Column(db.String(100))
    address = db.Column(db.String(200))
    role = db.Column(db.String(20))
    password_hash = db.Column(db.String(128))
    restaurant_id = db.Column(db.Integer, db.ForeignKey('restaurants.id'), nullable=True)
    email = db.Column(db.String(120), unique=True, nullable=True)
    phone_number = db.Column(db.String(15), unique=True, nullable=True)

    # Relazione tra User e Restaurant:
    restaurant = db.relationship('Restaurant', backref='users', lazy=True)
    deliveries = db.relationship('Delivery', back_populates='delivery_person')
    availability = db.relationship('DeliveryPersonAvailability', back_populates='delivery_person', uselist=False)


    def set_password(self, password):
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password_hash, password)

    def __repr__(self):
        return f'<User {self.username}>'

