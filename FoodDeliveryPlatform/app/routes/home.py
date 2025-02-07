from flask import Blueprint, render_template, request, redirect, url_for, session


bp = Blueprint('home', __name__)

# Route per la home page
@bp.route('/', methods=['GET'])
def home():

    # Cancella la sessione precedente quando si accede alla home
    session.clear()
    return render_template('home.html')



# Route per gestire la selezione del ruolo ed il reindirizzamento
@bp.route('/redirect', methods=['POST'])
def redirect_user():

    restaurant_id = request.form.get('restaurant_id', '').strip()  # Ottieni il restaurant_id dal form
    # Redirigi al login passando il restaurant_id
    return redirect(url_for('auth.login', restaurant_id=restaurant_id if restaurant_id else None))
