package com.example.fuelstationapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegUsername, etRegPassword;
    private Spinner spinnerRole;
    private Button btnRegister;
    private TextView tvLoginLink;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegUsername = findViewById(R.id.etRegUsername);
        etRegPassword = findViewById(R.id.etRegPassword);
        spinnerRole = findViewById(R.id.spinnerRole);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        dbHelper = new DatabaseHelper(this);

        String[] roles = {"Cashier", "Manager"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, roles);
        spinnerRole.setAdapter(adapter);

        btnRegister.setOnClickListener(v -> registerAccount());

        // Navigate back to LoginActivity when text link is clicked
        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registerAccount() {
        String username = etRegUsername.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();
        String role = spinnerRole.getSelectedItem().toString();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.registerUser(username, password, role)) {
            Toast.makeText(this, "Registration Successful! Please Login.", Toast.LENGTH_SHORT).show();

            // Immediately navigate to LoginActivity upon successful registration
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Username already exists!", Toast.LENGTH_SHORT).show();
        }
    }
}