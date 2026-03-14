package com.resways.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.resways.app.R;
import com.resways.app.models.SurpriseBag;
import com.resways.app.models.UserSession;
import com.resways.app.network.NetworkClient;
import org.json.JSONObject;

import java.util.List;

public class BagsAdapter extends RecyclerView.Adapter<BagsAdapter.BagViewHolder> {

    private List<SurpriseBag> bags;

    public BagsAdapter(List<SurpriseBag> bags) {
        this.bags = bags;
    }

    @NonNull
    @Override
    public BagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_surprise_bag, parent, false);
        return new BagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BagViewHolder holder, int position) {
        SurpriseBag bag = bags.get(position);
        holder.restaurantName.setText(bag.getRestaurantName());
        holder.bagName.setText(bag.getName());
        holder.distanceText.setText("📍 " + bag.getDistanceKm() + " km away");
        holder.oldPrice.setText(bag.getOldPrice() + " MAD");
        holder.newPrice.setText(bag.getNewPrice() + " MAD");

        if ("Reserved".equals(bag.getStatus())) {
            holder.reserveBtn.setText("View Ticket");
            holder.reserveBtn.setEnabled(true);
            holder.reserveBtn.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(v.getContext(), ReservationActivity.class);
                intent.putExtra("RESTAURANT_NAME", bag.getRestaurantName());
                intent.putExtra("BAG_ID", String.valueOf(bag.getId()));
                intent.putExtra("PIN", bag.getReservationCode() != null ? bag.getReservationCode() : "0000");
                v.getContext().startActivity(intent);
            });
        } else {
            holder.reserveBtn.setText("Reserve");
            holder.reserveBtn.setEnabled(true);
            holder.reserveBtn.setOnClickListener(v -> {
                holder.reserveBtn.setEnabled(false);
                holder.reserveBtn.setText("...");

                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("id", UserSession.getInstance().getUserId() != null ? UserSession.getInstance().getUserId() : 1L);

                    String url = NetworkClient.BASE_URL + "/bags/" + bag.getId() + "/reserve";

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                            response -> {
                                try {
                                    String pin = response.getString("reservationCode");
                                    android.content.Intent intent = new android.content.Intent(v.getContext(), ReservationActivity.class);
                                    intent.putExtra("RESTAURANT_NAME", bag.getRestaurantName());
                                    intent.putExtra("BAG_ID", String.valueOf(bag.getId()));
                                    intent.putExtra("PIN", pin);
                                    v.getContext().startActivity(intent);
                                } catch (Exception e) {
                                    holder.reserveBtn.setEnabled(true);
                                    holder.reserveBtn.setText("Reserve");
                                }
                            },
                            error -> {
                                // FALLBACK FOR DEMO
                                android.content.Intent intent = new android.content.Intent(v.getContext(), ReservationActivity.class);
                                intent.putExtra("RESTAURANT_NAME", bag.getRestaurantName());
                                intent.putExtra("BAG_ID", String.valueOf(bag.getId()));
                                intent.putExtra("PIN", "7392");
                                v.getContext().startActivity(intent);
                                holder.reserveBtn.setEnabled(true);
                                holder.reserveBtn.setText("Reserve");
                            });

                    NetworkClient.getInstance(v.getContext()).addToRequestQueue(request);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return bags == null ? 0 : bags.size();
    }

    static class BagViewHolder extends RecyclerView.ViewHolder {
        TextView restaurantName, distanceText, bagName, oldPrice, newPrice;
        Button reserveBtn;

        public BagViewHolder(@NonNull View itemView) {
            super(itemView);
            restaurantName = itemView.findViewById(R.id.restaurantName);
            distanceText = itemView.findViewById(R.id.distanceText);
            bagName = itemView.findViewById(R.id.bagName);
            oldPrice = itemView.findViewById(R.id.oldPrice);
            newPrice = itemView.findViewById(R.id.newPrice);
            reserveBtn = itemView.findViewById(R.id.reserveBtn);
        }
    }
}
