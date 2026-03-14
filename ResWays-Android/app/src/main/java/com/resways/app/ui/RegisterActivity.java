package com.resways.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.resways.app.R;
import com.resways.app.models.UserSession;
import com.resways.app.network.NetworkClient;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private RadioButton radioCustomer;
    private Button registerBtn;
    private TextView loginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        radioCustomer = findViewById(R.id.radioCustomer);
        registerBtn = findViewById(R.id.registerBtn);
        loginText = findViewById(R.id.loginText);

        loginText.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        registerBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String role = radioCustomer.isChecked() ? "CUSTOMER" : "PARTNER";

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(name, email, password, role);
        });
    }

    private void registerUser(String name, String email, String password, String role) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", name);
            jsonBody.put("email", email);
            jsonBody.put("password", password);
            jsonBody.put("role", role);

            String url = NetworkClient.BASE_URL + "/users/register";

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        try {
                            Long userId = response.getLong("id");
                            UserSession.getInstance().setUserId(userId);
                            UserSession.getInstance().setRole(role);

                            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                            if ("CUSTOMER".equalsIgnoreCase(role)) {
                                startActivity(new Intent(RegisterActivity.this, MainCustomerActivity.class));
                            } else {
                                startActivity(new Intent(RegisterActivity.this, MainPartnerActivity.class));
                            }
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(this, "Registration successful, but error parsing response.", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Toast.makeText(this, "Registration Failed. Email might be in use or server offline.", Toast.LENGTH_LONG).show();
                    });

            NetworkClient.getInstance(this).addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
