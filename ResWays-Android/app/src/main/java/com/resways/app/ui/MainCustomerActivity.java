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

        FloatingActionButton fabLogout = findViewById(R.id.fabLogout);
        if (fabLogout != null) {
            fabLogout.setOnClickListener(v -> {
                com.resways.app.models.UserSession.getInstance().setUserId(null);
                com.resways.app.models.UserSession.getInstance().setRole(null);
                startActivity(new android.content.Intent(this, LoginActivity.class));
                finish();
            });
        }

        if (USE_DUMMY_DATA) {
            loadDummyData();
        } else {
            fetchBagsFromApi();
            fetchImpactDataFromApi();
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();

        // Sort then attach adapter
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

        SurpriseBag.User u1 = new SurpriseBag.User(); u1.setId(2L); u1.setName("Patisserie Amine");
        SurpriseBag bag1 = new SurpriseBag();
        bag1.setId(1L); bag1.setRestaurant(u1); bag1.setName("Surprise Pastry Box");
        bag1.setOldPrice(60.0); bag1.setNewPrice(20.0);
        bag1.setLat(33.5731); bag1.setLng(-7.5898); bag1.setDistanceKm(1.2);
        bag1.setStatus("Available");
        bagsList.add(bag1);

        SurpriseBag.User u2 = new SurpriseBag.User(); u2.setId(3L); u2.setName("Boulangerie Atlas");
        SurpriseBag bag2 = new SurpriseBag();
        bag2.setId(2L); bag2.setRestaurant(u2); bag2.setName("Morning Bread Bag");
        bag2.setOldPrice(30.0); bag2.setNewPrice(10.0);
        bag2.setLat(33.5742); bag2.setLng(-7.5901); bag2.setDistanceKm(2.0);
        bag2.setStatus("Available");
        bagsList.add(bag2);

        SurpriseBag.User u3 = new SurpriseBag.User(); u3.setId(4L); u3.setName("Restaurant Zitoun");
        SurpriseBag bag3 = new SurpriseBag();
        bag3.setId(3L); bag3.setRestaurant(u3); bag3.setName("Tagine Surprise Box");
        bag3.setOldPrice(80.0); bag3.setNewPrice(25.0);
        bag3.setLat(33.5700); bag3.setLng(-7.5870); bag3.setDistanceKm(0.8);
        bag3.setStatus("Available");
        bagsList.add(bag3);

        SurpriseBag.User u4 = new SurpriseBag.User(); u4.setId(5L); u4.setName("Maison du Couscous");
        SurpriseBag bag4 = new SurpriseBag();
        bag4.setId(4L); bag4.setRestaurant(u4); bag4.setName("Friday Couscous Deal");
        bag4.setOldPrice(90.0); bag4.setNewPrice(30.0);
        bag4.setLat(33.5760); bag4.setLng(-7.5920); bag4.setDistanceKm(3.1);
        bag4.setStatus("Available");
        bagsList.add(bag4);

        SurpriseBag.User u5 = new SurpriseBag.User(); u5.setId(6L); u5.setName("Grill House Marrakchi");
        SurpriseBag bag5 = new SurpriseBag();
        bag5.setId(5L); bag5.setRestaurant(u5); bag5.setName("Mixed Grill Dinner Box");
        bag5.setOldPrice(120.0); bag5.setNewPrice(40.0);
        bag5.setLat(33.5680); bag5.setLng(-7.5855); bag5.setDistanceKm(1.7);
        bag5.setStatus("Available");
        bagsList.add(bag5);

        if (tvMoneySaved != null) {
            tvMoneySaved.setText("220.0 MAD");
            tvMealsRescued.setText("5");
            tvCo2Saved.setText("12.5 KG");
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
