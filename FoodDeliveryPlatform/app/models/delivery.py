from app.extensions import db
from datetime import datetime



class Delivery(db.Model):

    __tablename__ = 'deliveries'

    id = db.Column(db.Integer, primary_key=True)
    order_id = db.Column(db.Integer, db.ForeignKey('orders.id'), nullable=False)
    delivery_person_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    status = db.Column(db.String(50), nullable=False, default='pending')  # pending, accepted, rejected, completed
    current_latitude = db.Column(db.Float, nullable=True)
    current_longitude = db.Column(db.Float, nullable=True)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)
    updated_at = db.Column(db.DateTime, onupdate=datetime.utcnow)

    order = db.relationship('Order', back_populates='delivery')
    delivery_person = db.relationship('User', back_populates='deliveries')

    def __repr__(self):
        return f'<Delivery {self.id} for Order {self.order_id}>'



class DeliveryPersonAvailability(db.Model):

    __tablename__ = 'delivery_person_availability'

    id = db.Column(db.Integer, primary_key=True)
    delivery_person_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    is_available = db.Column(db.Boolean, default=True)
    start_time = db.Column(db.Time, nullable=True)
    end_time = db.Column(db.Time, nullable=True)

    delivery_person = db.relationship('User', back_populates='availability')

    def __repr__(self):
        return f'<Availability for Delivery Person {self.delivery_person_id}: {self.is_available}>'



class OrderReject(db.Model):

    id = db.Column(db.Integer, primary_key=True)
    order_id = db.Column(db.Integer, db.ForeignKey('orders.id'), nullable=False)
    rider_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)

    order = db.relationship('Order', backref=db.backref('rejects', lazy=True))
    rider = db.relationship('User', backref=db.backref('rejected_orders', lazy=True))

