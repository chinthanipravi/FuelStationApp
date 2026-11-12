package com.example.fuelstationapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StockActivity extends AppCompatActivity {

    private EditText etStockFuelName, etCapacity;
    private Button btnSaveStock;
    private RecyclerView rvStock;

    private DatabaseHelper dbHelper;
    private StockAdapter adapter;
    private List<FuelStock> stockList;
    private int selectedStockId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        etStockFuelName = findViewById(R.id.etStockFuelName);
        etCapacity = findViewById(R.id.etCapacity);
        btnSaveStock = findViewById(R.id.btnSaveStock);
        rvStock = findViewById(R.id.rvStock);

        dbHelper = new DatabaseHelper(this);
        rvStock.setLayoutManager(new LinearLayoutManager(this));

        btnSaveStock.setOnClickListener(v -> saveStockRecord());

        loadStockData();
    }

    private void loadStockData() {
        stockList = dbHelper.getAllStock();
        adapter = new StockAdapter(this, stockList, new StockAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(FuelStock stock) {
                if (dbHelper.deleteStock(stock.getId())) {
                    Toast.makeText(StockActivity.this, "Stock Deleted Successfully", Toast.LENGTH_SHORT).show();
                    loadStockData();
                }
            }

            @Override
            public void onItemClick(FuelStock stock) {
                selectedStockId = stock.getId();
                etStockFuelName.setText(stock.getFuelName());
                etCapacity.setText(String.valueOf(stock.getCapacityLiters()));
                btnSaveStock.setText("Update Tank Stock");
            }
        });
        rvStock.setAdapter(adapter);
    }

    private void saveStockRecord() {
        String fuelName = etStockFuelName.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();

        if (TextUtils.isEmpty(fuelName) || TextUtils.isEmpty(capacityStr)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double capacity = Double.parseDouble(capacityStr);

        if (capacity <= 0) {
            Toast.makeText(this, "Capacity must be greater than zero", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedStockId == -1) {
            if (dbHelper.addStock(fuelName, capacity)) {
                Toast.makeText(this, "Stock Record Added", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (dbHelper.updateStock(selectedStockId, fuelName, capacity)) {
                Toast.makeText(this, "Stock Record Updated", Toast.LENGTH_SHORT).show();
                selectedStockId = -1;
                btnSaveStock.setText("Add Tank Stock");
            }
        }

        clearInputs();
        loadStockData();
    }

    private void clearInputs() {
        etStockFuelName.setText("");
        etCapacity.setText("");
    }
}