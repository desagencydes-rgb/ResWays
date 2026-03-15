package com.resways.app.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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

        // Back button
        ImageButton backBtn = findViewById(R.id.backBtn);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

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
                        loadDummyOrders();
                    }
                },
                error -> loadDummyOrders());

        NetworkClient.getInstance(this).addToRequestQueue(request);
    }

    private void loadDummyOrders() {
        ordersList.clear();

        SurpriseBag.User r1 = new SurpriseBag.User();
        r1.setId(2L); r1.setName("Patisserie Amine");
        SurpriseBag b1 = new SurpriseBag();
        b1.setId(1L); b1.setRestaurant(r1); b1.setName("Surprise Pastry Box");
        b1.setOldPrice(60.0); b1.setNewPrice(20.0);
        b1.setStatus("Reserved"); b1.setReservationCode("7392");

        SurpriseBag.User r2 = new SurpriseBag.User();
        r2.setId(3L); r2.setName("Boulangerie Atlas");
        SurpriseBag b2 = new SurpriseBag();
        b2.setId(2L); b2.setRestaurant(r2); b2.setName("Morning Bread Bag");
        b2.setOldPrice(30.0); b2.setNewPrice(10.0);
        b2.setStatus("Reserved"); b2.setReservationCode("4821");

        ordersList.add(b1);
        ordersList.add(b2);
        adapter.notifyDataSetChanged();
    }
}
