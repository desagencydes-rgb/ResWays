package com.resways.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.resways.app.R;
import com.resways.app.models.SurpriseBag;
import com.resways.app.models.UserSession;
import com.resways.app.network.NetworkClient;
import org.json.JSONObject;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainPartnerActivity extends AppCompatActivity {

    private RecyclerView partnerBagsRecyclerView;
    private PartnerBagsAdapter adapter;
    private List<SurpriseBag> bagsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_partner);

        CardView scanCard = findViewById(R.id.scanCard);
        Button addBagBtn = findViewById(R.id.addBagBtn);
        Button logoutBtn = findViewById(R.id.logoutBtn);

        scanCard.setOnClickListener(v -> {
            showScanDialog();
        });

        addBagBtn.setOnClickListener(v -> {
            showCreateBagDialog();
        });

        logoutBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        partnerBagsRecyclerView = findViewById(R.id.partnerBagsRecyclerView);
        partnerBagsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bagsList = new ArrayList<>();
        adapter = new PartnerBagsAdapter(bagsList);
        partnerBagsRecyclerView.setAdapter(adapter);

        fetchStoreBagsFromApi();
    }

    private void fetchStoreBagsFromApi() {
        Long storeId = UserSession.getInstance().getUserId();
        if (storeId == null) storeId = 1L; // Fallback for testing

        String url = NetworkClient.BASE_URL + "/bags/store/" + storeId;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<SurpriseBag>>() {}.getType();
                    List<SurpriseBag> fetchedBags = gson.fromJson(response.toString(), listType);

                    bagsList.clear();
                    bagsList.addAll(fetchedBags);
                    adapter.notifyDataSetChanged();
                },
                error -> {
                    Toast.makeText(this, "Failed to load bags. Using Demo Mode.", Toast.LENGTH_SHORT).show();
                    loadDummyData();
                });

        NetworkClient.getInstance(this).addToRequestQueue(request);
    }
    
    private void loadDummyData() {
        bagsList.clear();
        SurpriseBag bag = new SurpriseBag();
        bag.setName("Demo Pastry Box");
        bag.setNewPrice(30.0);
        bag.setStatus("Available");
        bagsList.add(bag);
        
        SurpriseBag bag2 = new SurpriseBag();
        bag2.setName("Demo Bread Bag");
        bag2.setNewPrice(15.0);
        bag2.setStatus("Reserved");
        bag2.setReservationCode("7392");
        bagsList.add(bag2);
        
        adapter.notifyDataSetChanged();
    }

    private void showScanDialog() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan Customer's QR Code. Volume up to turn on flash.");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                String code = result.getContents();
                if (code.contains("-")) {
                    String[] parts = code.split("-");
                    if (parts.length >= 2) {
                        String bagId = parts[0];
                        String pin = parts[1];
                        verifyPickup(bagId, pin);
                        return;
                    }
                }
                // Fallback if the format is fundamentally wrong but they scanned something
                Toast.makeText(this, "Scanned unrecognized QR format.", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void verifyPickup(String bagId, String pin) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("reservationCode", pin);
            
            String url = NetworkClient.BASE_URL + "/bags/" + bagId + "/pickup";
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> Toast.makeText(this, "Pickup Verified & Completed!", Toast.LENGTH_LONG).show(),
                    error -> Toast.makeText(this, "Verification Failed. Wrong PIN?", Toast.LENGTH_LONG).show()
            );
            NetworkClient.getInstance(this).addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showCreateBagDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Surprise Bag");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        final EditText nameInput = new EditText(this);
        nameInput.setHint("Bag Name (e.g. Pastry Box)");
        layout.addView(nameInput);

        final EditText priceInput = new EditText(this);
        priceInput.setHint("Price in MAD (e.g. 20)");
        layout.addView(priceInput);

        builder.setView(layout);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String name = nameInput.getText().toString();
            String price = priceInput.getText().toString();
            createBagApiCall(name, price);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void createBagApiCall(String name, String price) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", name);
            jsonBody.put("newPrice", Double.parseDouble(price.isEmpty() ? "0" : price));
            jsonBody.put("oldPrice", Double.parseDouble(price.isEmpty() ? "0" : price) * 3); // mock old price
            jsonBody.put("description", "Surplus Rescue");
            jsonBody.put("distanceKm", 0.0);
            jsonBody.put("lat", 33.5731);
            jsonBody.put("lng", -7.5898);
            
            JSONObject restaurant = new JSONObject();
            restaurant.put("id", UserSession.getInstance().getUserId() != null ? UserSession.getInstance().getUserId() : 1L);
            jsonBody.put("restaurant", restaurant);

            String url = NetworkClient.BASE_URL + "/bags";
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        Toast.makeText(this, "Bag Listed Successfully!", Toast.LENGTH_LONG).show();
                        fetchStoreBagsFromApi(); // Refresh the list
                    },
                    error -> Toast.makeText(this, "Failed to create bag (Demo mode active?)", Toast.LENGTH_LONG).show()
            );
            NetworkClient.getInstance(this).addToRequestQueue(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
