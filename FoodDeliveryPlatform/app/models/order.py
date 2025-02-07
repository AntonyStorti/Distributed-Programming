from datetime import datetime
from app.extensions import db



class Order(db.Model):

    __tablename__ = 'orders'

    id = db.Column(db.Integer, primary_key=True)
    customer_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    restaurant_id = db.Column(db.Integer, db.ForeignKey('restaurants.id'), nullable=False)
    order_date = db.Column(db.DateTime, default=datetime.utcnow)
    status = db.Column(db.String(50), default='pending')  # Stato dell'ordine:
                                                          # pending, accepted, preparing, ready for pickup, completed
    total = db.Column(db.Float, nullable=False)


    # Relazioni:
    customer = db.relationship('User', backref='orders', lazy=True)
    restaurant = db.relationship('Restaurant', backref='orders', lazy=True)
    delivery = db.relationship('Delivery', back_populates='order', uselist=False)

    def __repr__(self):
        return f'<Order {self.id} - {self.status}>'

    def calculate_total(self):
        """Metodo per calcolare il totale dell'ordine in base agli articoli."""
        self.total = sum(
            item.item.price * item.quantity for item in self.order_items)  # Usa 'order_items' al posto di 'items'
        db.session.commit()

    def add_item(self, item):
        """Metodo per aggiungere un articolo all'ordine."""
        self.items.append(item)
        self.calculate_total()

    def remove_item(self, item):
        """Metodo per rimuovere un articolo dall'ordine."""
        self.items.remove(item)
        self.calculate_total()

    def to_dict(self):
        return {
            'id': self.id,
            'customer_id': self.customer_id,
            'order_date': self.order_date,
            'restaurant': self.restaurant.to_dict() if self.restaurant else None,
            'total': self.total,
            'status': self.status,
            'items': [item.to_dict() for item in self.order_items]
        }



class OrderItem(db.Model):

    __tablename__ = 'order_items'

    order_id = db.Column(db.Integer, db.ForeignKey('orders.id'), primary_key=True)
    item_id = db.Column(db.Integer, db.ForeignKey('menu_items.id'), primary_key=True)
    quantity = db.Column(db.Integer, nullable=False)

    # Relazioni:
    order = db.relationship('Order', backref=db.backref('order_items', lazy=True))
    item = db.relationship('MenuItem', backref=db.backref('order_items', lazy=True))

    def to_dict(self):
        return {
            'order_id': self.order_id,
            'item_id': self.item_id,
            'quantity': self.quantity,
            'item': self.item.to_dict()
        }
