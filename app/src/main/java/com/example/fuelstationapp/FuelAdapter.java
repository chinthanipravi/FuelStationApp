package com.example.fuelstationapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FuelAdapter extends RecyclerView.Adapter<FuelAdapter.FuelViewHolder> {

    private Context context;
    private List<FuelRecord> recordList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(FuelRecord record);
        void onItemClick(FuelRecord record);
    }

    public FuelAdapter(Context context, List<FuelRecord> recordList, OnItemClickListener listener) {
        this.context = context;
        this.recordList = recordList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FuelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_fuel, parent, false);
        return new FuelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FuelViewHolder holder, int position) {
        FuelRecord record = recordList.get(position);

        holder.tvFuelType.setText(record.getFuelType());
        holder.tvDetails.setText("Qty: " + record.getLiters() + " L  |  Rs." + record.getPricePerLiter() + "/L");
        holder.tvTotalBill.setText("Rs. " + String.format("%.2f", record.getTotalAmount()));

        if (record.getCreatedAt() != null && !record.getCreatedAt().isEmpty()) {
            holder.tvTimestamp.setText("Date: " + record.getCreatedAt());
            holder.tvTimestamp.setVisibility(View.VISIBLE);
        } else {
            holder.tvTimestamp.setVisibility(View.GONE);
        }

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(record));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(record));
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public static class FuelViewHolder extends RecyclerView.ViewHolder {
        TextView tvFuelType, tvDetails, tvTotalBill, tvTimestamp;
        ImageButton btnDelete;

        public FuelViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFuelType = itemView.findViewById(R.id.tvFuelType);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvTotalBill = itemView.findViewById(R.id.tvTotalBill);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}