package com.example.fuelstationapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FuelShedDB.db";
    private static final int DATABASE_VERSION = 3;

    // Table 1: Users
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_ROLE = "role";

    // Table 2: Fuel Sales
    private static final String TABLE_SALES = "fuel_sales";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FUEL_TYPE = "fuel_type";
    private static final String COLUMN_LITERS = "liters";
    private static final String COLUMN_PRICE_PER_LITER = "price_per_liter";
    private static final String COLUMN_TOTAL_AMOUNT = "total_amount";
    private static final String COLUMN_CREATED_AT = "created_at";

    // Table 3: Fuel Tank Stock
    private static final String TABLE_STOCK = "fuel_stock";
    private static final String COLUMN_STOCK_ID = "stock_id";
    private static final String COLUMN_STOCK_NAME = "fuel_name";
    private static final String COLUMN_CAPACITY = "capacity_liters";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_ROLE + " TEXT)";

        String CREATE_SALES_TABLE = "CREATE TABLE " + TABLE_SALES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FUEL_TYPE + " TEXT, " +
                COLUMN_LITERS + " REAL, " +
                COLUMN_PRICE_PER_LITER + " REAL, " +
                COLUMN_TOTAL_AMOUNT + " REAL, " +
                COLUMN_CREATED_AT + " TEXT)";

        String CREATE_STOCK_TABLE = "CREATE TABLE " + TABLE_STOCK + " (" +
                COLUMN_STOCK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_STOCK_NAME + " TEXT UNIQUE, " +
                COLUMN_CAPACITY + " REAL)";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_SALES_TABLE);
        db.execSQL(CREATE_STOCK_TABLE);

        // Insert default admin user
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, "admin");
        cv.put(COLUMN_PASSWORD, "admin123");
        cv.put(COLUMN_ROLE, "Manager");
        db.insert(TABLE_USERS, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SALES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCK);
        onCreate(db);
    }

    // ========================================================
    // 1. USER AUTHENTICATION
    // ========================================================

    public boolean registerUser(String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_USERNAME, username);
        cv.put(COLUMN_PASSWORD, password);
        cv.put(COLUMN_ROLE, role);
        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    public User checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " +
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?", new String[]{username, password});

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            String user = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
            String role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE));
            cursor.close();
            return new User(id, user, role);
        }
        if (cursor != null) cursor.close();
        return null;
    }

    // ========================================================
    // 2. FUEL SALES WITH AUTO DEDUCTION & TIMESTAMP
    // ========================================================

    public boolean addRecord(String fuelType, double liters, double pricePerLiter, double totalAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            ContentValues cv = new ContentValues();
            cv.put(COLUMN_FUEL_TYPE, fuelType);
            cv.put(COLUMN_LITERS, liters);
            cv.put(COLUMN_PRICE_PER_LITER, pricePerLiter);
            cv.put(COLUMN_TOTAL_AMOUNT, totalAmount);
            cv.put(COLUMN_CREATED_AT, currentTime);

            long saleResult = db.insert(TABLE_SALES, null, cv);
            if (saleResult == -1) return false;

            // Auto-deduct matching fuel stock capacity
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_STOCK + " WHERE LOWER(" + COLUMN_STOCK_NAME + ") = LOWER(?)", new String[]{fuelType.trim()});
            if (cursor.moveToFirst()) {
                int stockId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STOCK_ID));
                double currentCap = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY));
                double newCap = Math.max(0, currentCap - liters);

                ContentValues stockCv = new ContentValues();
                stockCv.put(COLUMN_CAPACITY, newCap);
                db.update(TABLE_STOCK, stockCv, COLUMN_STOCK_ID + "=?", new String[]{String.valueOf(stockId)});
            }
            cursor.close();

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public List<FuelRecord> getAllRecords() {
        return searchRecords("");
    }

    public List<FuelRecord> searchRecords(String query) {
        List<FuelRecord> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_SALES;
        String[] args = null;

        if (query != null && !query.trim().isEmpty()) {
            sql += " WHERE " + COLUMN_FUEL_TYPE + " LIKE ?";
            args = new String[]{"%" + query.trim() + "%"};
        }
        sql += " ORDER BY " + COLUMN_ID + " DESC";

        Cursor cursor = db.rawQuery(sql, args);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String fuelType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FUEL_TYPE));
                double liters = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LITERS));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE_PER_LITER));
                double total = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_AMOUNT));
                String createdAt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT));

                list.add(new FuelRecord(id, fuelType, liters, price, total, createdAt));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean updateRecord(int id, String fuelType, double liters, double pricePerLiter, double totalAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_FUEL_TYPE, fuelType);
        cv.put(COLUMN_LITERS, liters);
        cv.put(COLUMN_PRICE_PER_LITER, pricePerLiter);
        cv.put(COLUMN_TOTAL_AMOUNT, totalAmount);

        return db.update(TABLE_SALES, cv, COLUMN_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteRecord(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_SALES, COLUMN_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    // ========================================================
    // 3. FUEL TANK STOCK OPERATIONS
    // ========================================================

    public boolean addStock(String fuelName, double capacity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STOCK_NAME, fuelName);
        cv.put(COLUMN_CAPACITY, capacity);
        return db.insertWithOnConflict(TABLE_STOCK, null, cv, SQLiteDatabase.CONFLICT_REPLACE) != -1;
    }

    public List<FuelStock> getAllStock() {
        List<FuelStock> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_STOCK, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STOCK_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STOCK_NAME));
                double cap = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY));

                list.add(new FuelStock(id, name, cap));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public boolean updateStock(int id, String fuelName, double capacity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STOCK_NAME, fuelName);
        cv.put(COLUMN_CAPACITY, capacity);

        return db.update(TABLE_STOCK, cv, COLUMN_STOCK_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteStock(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_STOCK, COLUMN_STOCK_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }
}