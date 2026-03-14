package com.resways.app.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.resways.app.R;
import com.resways.app.models.SurpriseBag;
import com.resways.app.network.NetworkClient;
import com.resways.app.utils.MoodAiEngine;
import android.widget.Toast;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainCustomerActivity extends AppCompatActivity {

    private RecyclerView bagsRecyclerView;
    private BagsAdapter adapter;
    private List<SurpriseBag> bagsList;
    
    private TextView tvMoneySaved;
    private TextView tvMealsRescued;
    private TextView tvCo2Saved;

    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    // DEMO TOGGLE: Set to true if server fails during pitch, seamlessly falling back to dummy data
    private static final boolean USE_DUMMY_DATA = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_customer);

        bagsRecyclerView = findViewById(R.id.bagsRecyclerView);
        bagsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvMoneySaved = findViewById(R.id.tvMoneySaved);
        tvMealsRescued = findViewById(R.id.tvMealsRescued);
        tvCo2Saved = findViewById(R.id.tvCo2Saved);

        FloatingActionButton fabMyOrders = findViewById(R.id.fabMyOrders);
        fabMyOrders.setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, MyOrdersActivity.class));
        });

        bagsList = new ArrayList<>();
        
        if (USE_DUMMY_DATA) {
            loadDummyData();
        } else {
            fetchBagsFromApi();
            fetchImpactDataFromApi();
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();

        // Apply Local AI context sorting before displaying list
        MoodAiEngine.sortBagsByContext(bagsList);

        adapter = new BagsAdapter(bagsList);
        bagsRecyclerView.setAdapter(adapter);
    }
    
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchLocationAndUpdateDistances();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndUpdateDistances();
            } else {
                Toast.makeText(this, "Location permission denied. Distances may be inaccurate.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchLocationAndUpdateDistances() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLocation = location;
                    updateBagDistances();
                }
            });
        }
    }

    private void updateBagDistances() {
        if (currentLocation == null) return;
        
        for (SurpriseBag bag : bagsList) {
            // For dummy data, we might not have lat/lng set correctly, but we calculate anyway
            // Real API bags will have lat/lng
            if (bag.getLat() != 0.0 || bag.getLng() != 0.0) {
                double dist = com.resways.app.utils.LocationHelper.calculateDistance(
                        currentLocation.getLatitude(), currentLocation.getLongitude(),
                        bag.getLat(), bag.getLng()
                );
                bag.setDistanceKm(dist);
            }
        }
        
        MoodAiEngine.sortBagsByContext(bagsList);
        adapter.notifyDataSetChanged();
    }

    private void loadDummyData() {
        bagsList.clear();
        SurpriseBag.User user1 = new SurpriseBag.User();
        user1.setId(1L);
        user1.setName("Patisserie Amine");

        SurpriseBag bag1 = new SurpriseBag();
        bag1.setId(1L);
        bag1.setRestaurant(user1);
        bag1.setName("Surprise Pastry Box");
        bag1.setOldPrice(60.0);
        bag1.setNewPrice(20.0);
        bag1.setLat(33.5731); // Casa coords for dummy data
        bag1.setLng(-7.5898);
        bag1.setDistanceKm(1.2);
        bag1.setStatus("Available");
        
        bagsList.add(bag1);
        
        if (tvMoneySaved != null) {
            tvMoneySaved.setText("80.0 MAD");
            tvMealsRescued.setText("2");
            tvCo2Saved.setText("5.0 KG");
        }
    }
    
    private void fetchImpactDataFromApi() {
        Long userId = com.resways.app.models.UserSession.getInstance().getUserId();
        if (userId == null) userId = 1L;

        String url = NetworkClient.BASE_URL + "/users/" + userId + "/impact";
        
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response);
                        double moneySaved = obj.getDouble("moneySaved");
                        int mealsRescued = obj.getInt("mealsRescued");
                        double co2Saved = obj.getDouble("co2Saved");
                        
                        tvMoneySaved.setText(String.format("%.1f MAD", moneySaved));
                        tvMealsRescued.setText(String.valueOf(mealsRescued));
                        tvCo2Saved.setText(String.format("%.1f KG", co2Saved));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Silent fail
                });
        NetworkClient.getInstance(this).addToRequestQueue(request);
    }
    
    private void fetchBagsFromApi() {
        String url = NetworkClient.BASE_URL + "/bags/available";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<SurpriseBag>>(){}.getType();
                        List<SurpriseBag> fetchedBags = gson.fromJson(response, listType);
                        
                        bagsList.clear();
                        bagsList.addAll(fetchedBags);
                        
                        if (currentLocation != null) {
                            updateBagDistances();
                        } else {
                            MoodAiEngine.sortBagsByContext(bagsList);
                            adapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainCustomerActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(MainCustomerActivity.this, "Network Error. Falling back to dummy data.", Toast.LENGTH_LONG).show();
                    loadDummyData();
                    MoodAiEngine.sortBagsByContext(bagsList);
                    adapter.notifyDataSetChanged();
                });

        NetworkClient.getInstance(this).addToRequestQueue(request);
    }
}
