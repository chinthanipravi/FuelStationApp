package com.example.fuelstationapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private Context context;
    private List<FuelStock> stockList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDeleteClick(FuelStock stock);
        void onItemClick(FuelStock stock);
    }

    public StockAdapter(Context context, List<FuelStock> stockList, OnItemClickListener listener) {
        this.context = context;
        this.stockList = stockList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_stock, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        FuelStock stock = stockList.get(position);

        holder.tvStockFuelName.setText(stock.getFuelName());
        holder.tvStockCapacity.setText("Tank Capacity: " + stock.getCapacityLiters() + " Liters");

        if (stock.isLowStock()) {
            holder.tvLowStockBadge.setText("⚠️ LOW STOCK ALERT");
            holder.tvLowStockBadge.setTextColor(Color.parseColor("#D32F2F"));
        } else {
            holder.tvLowStockBadge.setText("✓ NORMAL STOCK");
            holder.tvLowStockBadge.setTextColor(Color.parseColor("#2E7D32"));
        }
        holder.tvLowStockBadge.setVisibility(View.VISIBLE);

        holder.btnDeleteStock.setOnClickListener(v -> listener.onDeleteClick(stock));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(stock));
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public static class StockViewHolder extends RecyclerView.ViewHolder {
        TextView tvStockFuelName, tvStockCapacity, tvLowStockBadge;
        ImageButton btnDeleteStock;

        public StockViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStockFuelName = itemView.findViewById(R.id.tvStockFuelName);
            tvStockCapacity = itemView.findViewById(R.id.tvStockCapacity);
            tvLowStockBadge = itemView.findViewById(R.id.tvLowStockBadge);
            btnDeleteStock = itemView.findViewById(R.id.btnDeleteStock);
        }
    }
}