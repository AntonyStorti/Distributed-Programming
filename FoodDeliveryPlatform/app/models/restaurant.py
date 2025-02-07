from app.extensions import db



class Restaurant(db.Model):

    __tablename__ = 'restaurants'

    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(100), unique=True, nullable=False)
    address = db.Column(db.String(200), unique=True, nullable=False)
    menu_items = db.relationship('MenuItem', backref='restaurants', lazy=True)
    phone_number = db.Column(db.String(20), unique=True, nullable=True)
    email = db.Column(db.String(100), unique=True, nullable=True)

    is_open = db.Column(db.Boolean, default=True, nullable=False)
    opening_hours = db.Column(db.String(500), nullable=True)  # Orari di apertura come JSON
    description = db.Column(db.String(500), nullable=True)
    logo_url = db.Column(db.String(200), nullable=True)


    def __repr__(self):
        return f'<Restaurant {self.name}>'

    def to_dict(self):
        return {
            'id': self.id,
            'name': self.name,
            'address': self.address,
            'phone_number': self.phone_number,
            'email': self.email,
            'is_open': self.is_open,
            'opening_hours': self.opening_hours,
            'description': self.description,
            'logo_url': self.logo_url,
        }
