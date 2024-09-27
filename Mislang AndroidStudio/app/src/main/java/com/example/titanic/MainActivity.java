package com.example.titanic;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText editTextAge, editTextTicketPrice;
    private RadioGroup radioGroupGender, radioGroupClass, radioGroupEmbarked;
    private Button buttonSubmit;
    private TextView textViewResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        editTextAge = findViewById(R.id.editTextAge);
        editTextTicketPrice = findViewById(R.id.editTextTicketPrice);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        radioGroupClass = findViewById(R.id.radioGroupClass);
        radioGroupEmbarked = findViewById(R.id.radioGroupEmbarked);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        textViewResult = findViewById(R.id.textViewResult);

        // Submit button onClick listener
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPassengerData();
            }
        });
    }

    // Function to get user inputs and send to Flask API
    private void submitPassengerData() {
        // Get the input values
        String age = editTextAge.getText().toString().trim();
        String ticketPrice = editTextTicketPrice.getText().toString().trim();

        // Validate inputs
        if (age.isEmpty() || ticketPrice.isEmpty()) {
            Toast.makeText(this, "Please enter age and ticket price.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get gender
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        RadioButton selectedGenderButton = findViewById(selectedGenderId);
        String gender = selectedGenderButton.getText().toString().equals("Male") ? "male" : "female";

        // Get passenger class and convert to numeric (1, 2, or 3)
        int selectedClassId = radioGroupClass.getCheckedRadioButtonId();
        RadioButton selectedClassButton = findViewById(selectedClassId);
        String passengerClass = selectedClassButton.getText().toString(); // Assuming options like "1st", "2nd", "3rd"
        int passengerClassInt = passengerClass.contains("1") ? 1 : passengerClass.contains("2") ? 2 : 3;

        // Get port of embarkation
        int selectedEmbarkedId = radioGroupEmbarked.getCheckedRadioButtonId();
        RadioButton selectedEmbarkedButton = findViewById(selectedEmbarkedId);
        String embarked = selectedEmbarkedButton.getText().toString().substring(0, 1); // Assuming "Cherbourg" gives "C"

        // Prepare JSON object for the API request
        JSONObject postData = new JSONObject();
        try {
            postData.put("Age", Integer.parseInt(age)); // Key is case-sensitive
            postData.put("Fare", Double.parseDouble(ticketPrice)); // Key is case-sensitive
            postData.put("Sex", gender); // Key is case-sensitive
            postData.put("Pclass", passengerClassInt); // Key is case-sensitive
            postData.put("Embarked", embarked); // Key is case-sensitive
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating JSON object.", Toast.LENGTH_SHORT).show();
            return;
        }

        // API URL (replace with your Flask server IP)
        String url = "http://192.168.0.108:5000/predict"; // Change this IP accordingly

        // Send the POST request to the API
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> {
                    try {
                        // Parse JSON response
                        String prediction = response.getString("Survived");
                        textViewResult.setText("Prediction: " + (prediction.equals("1") ? "Survived" : "Did not survive"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        textViewResult.setText("Error in parsing prediction.");
                    }
                }, error -> {
            // Log the error message
            error.printStackTrace();
            // Log full error response for debugging
            if (error.networkResponse != null) {
                String errorMessage = new String(error.networkResponse.data);
                textViewResult.setText("Error occurred: " + error.networkResponse.statusCode + " - " + errorMessage);
            } else {
                textViewResult.setText("Error occurred: " + error.toString());
            }
        });

        // Add the request to the RequestQueue
        queue.add(jsonObjectRequest);
    }
}
