from flask import Flask, request, jsonify
import joblib
import numpy as np
from flask_cors import CORS

# Initialize the Flask app
app = Flask(__name__)
CORS(app)  # Enable CORS for cross-origin requests

# Load the trained model in app context
with app.app_context():
    model = joblib.load('titanic_model.pkl')

# Helper function to make predictions
def make_prediction(data):
    try:
        # Extract features from the request
        pclass = int(data.get('Pclass'))  # Convert Pclass to integer
        sex = 1 if data.get('Sex', '').lower() == 'male' else 0
        age = float(data.get('Age'))  # Convert Age to float
        fare = float(data.get('Fare'))  # Convert Fare to float

        # Since SibSp and Parch are not included in your input,
        # we will default them to 0
        sibsp = 0
        parch = 0

        # Convert Embarked to numerical value (C=0, Q=1, S=2)
        embarked = data.get('Embarked', '')
        embarked_map = {'C': 0, 'Q': 1, 'S': 2}
        embarked_num = embarked_map.get(embarked, -1)

        # Check if embarked is valid
        if embarked_num == -1:
            return {'error': 'Invalid Embarked value'}, 400

        # Prepare the feature vector for prediction
        features = np.array([[pclass, sex, age, sibsp, parch, fare, embarked_num]])

        # Make a prediction
        prediction = model.predict(features)

        # Return the result
        result = {'Survived': int(prediction[0])}
        return result, 200

    except KeyError as ke:
        return {'error': f'Missing key: {str(ke)}'}, 400
    except ValueError as ve:
        return {'error': f'Invalid value: {str(ve)}'}, 400
    except Exception as e:
        return {'error': str(e)}, 500

# Define the prediction route
@app.route('/predict', methods=['POST'])
def predict():
    data = request.get_json()
    print("Received data:", data)  # Log received data for debugging
    
    if not data:
        return jsonify({'error': 'No data provided'}), 400

    result, status_code = make_prediction(data)
    return jsonify(result), status_code

# Run the Flask app
if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
