package com.example.fuelstationapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText etFuelType, etLiters, etPricePerLiter, etSearch;
    private Button btnSave, btnGoToStock;
    private ImageButton btnLogout;
    private TextView tvTotalRevenue, tvUserInfo;
    private RecyclerView recyclerView;

    private DatabaseHelper dbHelper;
    private FuelAdapter adapter;
    private List<FuelRecord> recordList;
    private SessionManager sessionManager;
    private int selectedId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        etFuelType = findViewById(R.id.etFuelType);
        etLiters = findViewById(R.id.etLiters);
        etPricePerLiter = findViewById(R.id.etPricePerLiter);
        etSearch = findViewById(R.id.etSearch);
        btnSave = findViewById(R.id.btnSave);
        btnGoToStock = findViewById(R.id.btnGoToStock);
        btnLogout = findViewById(R.id.btnLogout);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvUserInfo = findViewById(R.id.tvUserInfo);
        recyclerView = findViewById(R.id.recyclerView);

        dbHelper = new DatabaseHelper(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvUserInfo.setText("User: " + sessionManager.getUsername() + " (" + sessionManager.getRole() + ")");

        btnSave.setOnClickListener(v -> saveFuelRecord());

        btnGoToStock.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StockActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Toast.makeText(MainActivity.this, "Logged Out Successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadData("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(etSearch.getText().toString());
    }

    private void loadData(String searchQuery) {
        recordList = dbHelper.searchRecords(searchQuery);
        adapter = new FuelAdapter(this, recordList, new FuelAdapter.OnItemClickListener() {
            @Override
            public void onDeleteClick(FuelRecord record) {
                if (dbHelper.deleteRecord(record.getId())) {
                    Toast.makeText(MainActivity.this, "Record Deleted Successfully", Toast.LENGTH_SHORT).show();
                    loadData(etSearch.getText().toString());
                }
            }

            @Override
            public void onItemClick(FuelRecord record) {
                selectedId = record.getId();
                etFuelType.setText(record.getFuelType());
                etLiters.setText(String.valueOf(record.getLiters()));
                etPricePerLiter.setText(String.valueOf(record.getPricePerLiter()));
                btnSave.setText("Update Sale Record");
            }
        });
        recyclerView.setAdapter(adapter);

        double totalRevenue = 0;
        for (FuelRecord r : recordList) {
            totalRevenue += r.getTotalAmount();
        }

        tvTotalRevenue.setText("Total Revenue: Rs. " + String.format("%.2f", totalRevenue));
    }

    private void saveFuelRecord() {
        String fuelType = etFuelType.getText().toString().trim();
        String litersStr = etLiters.getText().toString().trim();
        String priceStr = etPricePerLiter.getText().toString().trim();

        if (TextUtils.isEmpty(fuelType) || TextUtils.isEmpty(litersStr) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double liters = Double.parseDouble(litersStr);
        double price = Double.parseDouble(priceStr);

        if (liters <= 0 || price <= 0) {
            Toast.makeText(this, "Liters and Price must be greater than zero", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalBill = liters * price;

        if (selectedId == -1) {
            if (dbHelper.addRecord(fuelType, liters, price, totalBill)) {
                Toast.makeText(this, "Sale Added & Tank Stock Auto-Deducted!", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (dbHelper.updateRecord(selectedId, fuelType, liters, price, totalBill)) {
                Toast.makeText(this, "Record Updated Successfully", Toast.LENGTH_SHORT).show();
                selectedId = -1;
                btnSave.setText("Add Fuel Sale Record");
            }
        }

        clearInputs();
        loadData(etSearch.getText().toString());
    }

    private void clearInputs() {
        etFuelType.setText("");
        etLiters.setText("");
        etPricePerLiter.setText("");
    }
}