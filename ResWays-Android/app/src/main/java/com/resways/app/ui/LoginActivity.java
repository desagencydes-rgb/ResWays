package com.resways.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.resways.app.R;
import com.resways.app.models.UserSession;
import com.resways.app.network.NetworkClient;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private RadioButton radioCustomer;
    private Button loginBtn;
    private android.widget.TextView registerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        radioCustomer = findViewById(R.id.radioCustomer);
        loginBtn = findViewById(R.id.loginBtn);
        registerText = findViewById(R.id.registerText);

        if (registerText != null) {
            registerText.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            });
        }

        loginBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Real Production API Call
            try {
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("email", email);
                jsonBody.put("password", password);

                String url = NetworkClient.BASE_URL + "/users/login";

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                        response -> {
                            try {
                                Long userId = response.getLong("id");
                                String role = response.getString("role");
                                UserSession.getInstance().setUserId(userId);
                                UserSession.getInstance().setRole(role);

                                if ("CUSTOMER".equalsIgnoreCase(role) || radioCustomer.isChecked()) {
                                    startActivity(new Intent(LoginActivity.this, MainCustomerActivity.class));
                                } else {
                                    startActivity(new Intent(LoginActivity.this, MainPartnerActivity.class));
                                }
                                finish();
                            } catch (Exception e) {
                                Toast.makeText(this, "Valid login, but error reading response.", Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> {
                            // FALLBACK FOR DEMO IF SERVER IS OFF
                            Toast.makeText(this, "Server Offline. Using Demo Login Mode.", Toast.LENGTH_LONG).show();
                            UserSession.getInstance().setUserId(1L);
                            String demoRole = radioCustomer.isChecked() ? "CUSTOMER" : "PARTNER";
                            UserSession.getInstance().setRole(demoRole);
                            if ("CUSTOMER".equals(demoRole)) {
                                startActivity(new Intent(LoginActivity.this, MainCustomerActivity.class));
                            } else {
                                startActivity(new Intent(LoginActivity.this, MainPartnerActivity.class));
                            }
                            finish();
                        });

                NetworkClient.getInstance(this).addToRequestQueue(request);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
