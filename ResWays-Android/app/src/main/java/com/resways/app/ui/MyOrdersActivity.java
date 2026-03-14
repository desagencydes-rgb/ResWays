package com.resways.app.ui;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.resways.app.R;
import com.resways.app.models.SurpriseBag;
import com.resways.app.models.UserSession;
import com.resways.app.network.NetworkClient;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MyOrdersActivity extends AppCompatActivity {

    private RecyclerView ordersRecyclerView;
    private BagsAdapter adapter;
    private List<SurpriseBag> ordersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ordersList = new ArrayList<>();
        adapter = new BagsAdapter(ordersList);
        ordersRecyclerView.setAdapter(adapter);

        fetchMyOrders();
    }

    private void fetchMyOrders() {
        Long userId = UserSession.getInstance().getUserId();
        if (userId == null) userId = 1L; // Fallback

        String url = NetworkClient.BASE_URL + "/bags/reserved/" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<SurpriseBag>>(){}.getType();
                        List<SurpriseBag> fetchedBags = gson.fromJson(response, listType);

                        ordersList.clear();
                        ordersList.addAll(fetchedBags);
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing orders", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Network Error. Could not load orders.", Toast.LENGTH_SHORT).show();
                });

        NetworkClient.getInstance(this).addToRequestQueue(request);
    }
}
