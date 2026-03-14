package com.resways.app.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.resways.app.R;
import com.resways.app.models.SurpriseBag;

import java.util.List;

public class PartnerBagsAdapter extends RecyclerView.Adapter<PartnerBagsAdapter.ViewHolder> {

    private List<SurpriseBag> bags;

    public PartnerBagsAdapter(List<SurpriseBag> bags) {
        this.bags = bags;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_partner_bag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SurpriseBag bag = bags.get(position);
        holder.bagName.setText(bag.getName());
        holder.bagPrice.setText(bag.getNewPrice() + " MAD");
        
        String status = bag.getStatus();
        holder.bagStatus.setText(status);
        
        if ("Available".equals(status)) {
            holder.bagStatus.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
            holder.bagPin.setText("Awaiting Order");
        } else if ("Reserved".equals(status)) {
            holder.bagStatus.setBackgroundColor(Color.parseColor("#FF9800")); // Orange
            holder.bagPin.setText("PIN: " + (bag.getReservationCode() != null ? bag.getReservationCode() : "****"));
        } else if ("Completed".equals(status)) {
            holder.bagStatus.setBackgroundColor(Color.parseColor("#9E9E9E")); // Grey
            holder.bagPin.setText("Picked Up");
        }
    }

    @Override
    public int getItemCount() {
        return bags == null ? 0 : bags.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView bagName, bagPrice, bagStatus, bagPin;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bagName = itemView.findViewById(R.id.bagName);
            bagPrice = itemView.findViewById(R.id.bagPrice);
            bagStatus = itemView.findViewById(R.id.bagStatus);
            bagPin = itemView.findViewById(R.id.bagPin);
        }
    }
}
