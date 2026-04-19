package com.zybooks.eventtracking_tylerbryant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Main event management screen
 * Displays RecyclerView of events, handles CRUD operations, SMS settings
 * Persists data using SharedPreferences + Gson serialization
 */
public class DataGridActivity extends AppCompatActivity
        implements DataAdapter.OnItemClickListener {

    private ArrayList<DataItem> dataList; // Event data storage
    private DataAdapter dataAdapter; // RecyclerView adapter
    private boolean smsEnabled; // SMS feature toggle

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_DATA_LIST = "dataList";

    /**
     * Suppress lint warning for notifyDataSetChanged() usage
     * Initializes UI, loads data, sets up button listeners
     */
    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_grid);

        // Load persisted events from SharedPreferences using Gson
        loadDataList();

        // Setup RecyclerView with click-to-edit functionality
        RecyclerView recyclerViewData = findViewById(R.id.recyclerViewData);
        dataAdapter = new DataAdapter(this, dataList, this); // 'this' implements OnItemClickListener
        recyclerViewData.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewData.setAdapter(dataAdapter);

        // Load SMS toggle setting
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        smsEnabled = prefs.getBoolean("smsEnabled", false);

        // Add new event button - opens add dialog
        Button buttonAddData = findViewById(R.id.buttonAddData);
        buttonAddData.setOnClickListener(v -> showAddDataDialog());

        // Delete ALL events button
        Button buttonDeleteAll = findViewById(R.id.buttonDeleteAll);
        buttonDeleteAll.setOnClickListener(v -> {
            if (!dataList.isEmpty()) {
                dataList.clear();
                dataAdapter.notifyDataSetChanged(); // Updates entire list
                saveDataList();
                Toast.makeText(this, "All events deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No events to delete", Toast.LENGTH_SHORT).show();
            }
        });

        // Navigate to SMS settings screen
        Button buttonSettings = findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SMSPermissionActivity.class))
        );

        // Test SMS preview button - shows dialogs (not real SMS)
        Button buttonSendSMS = findViewById(R.id.buttonSendUpcomingSMS);
        buttonSendSMS.setOnClickListener(v -> {
            SharedPreferences prefs2 = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            smsEnabled = prefs2.getBoolean("smsEnabled", false);
            if (!smsEnabled) {
                Toast.makeText(this, "SMS is disabled", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dataList.isEmpty()) {
                Toast.makeText(this, "No events to send SMS for", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show preview dialog for each event
            for (DataItem item : dataList) {
                String smsMessage = getString(R.string.upcoming_event_sms,
                        item.getName(), item.getDate());
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Test SMS")
                        .setMessage(smsMessage)
                        .setPositiveButton("OK", null)
                        .show();
            }
        });

        // Logout button - clears login state and returns to MainActivity
        Button buttonLogout = findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(v -> {
            SharedPreferences prefs3 = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs3.edit().putBoolean("isLoggedIn", false).apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        // First-time SMS permission prompt (non-cancelable)
        SharedPreferences prefs4 = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean smsChecked = prefs4.getBoolean("smsPermissionChecked", false);
        if (!smsChecked) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("SMS Notifications")
                    .setMessage("Enable SMS notifications to receive alerts for events?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        startActivity(new Intent(this, SMSPermissionActivity.class));
                        prefs4.edit().putBoolean("smsPermissionChecked", true).apply();
                    })
                    .setNegativeButton("No", (dialog, which) ->
                            prefs4.edit().putBoolean("smsPermissionChecked", true).apply())
                    .setCancelable(false)
                    .show();
        }
    }

    /**
     * Handles RecyclerView row clicks - opens edit dialog for selected position
     */
    @Override
    public void onItemClick(int position) {
        showEditDataDialog(position);
    }

    /**
     * Loads event list from SharedPreferences using Gson JSON deserialization
     * Creates empty list if no saved data exists
     */
    private void loadDataList() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(KEY_DATA_LIST, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<DataItem>>(){}.getType();
            dataList = gson.fromJson(json, type);
        } else {
            dataList = new ArrayList<>();
        }
    }

    /**
     * Saves current dataList to SharedPreferences as JSON string
     * Public method called by DataAdapter after deletions
     */
    public void saveDataList() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        prefs.edit().putString(KEY_DATA_LIST, gson.toJson(dataList)).apply();
    }

    /**
     * Shows dialog for adding new events with date picker integration
     * Validates input before adding to dataList
     */
    private void showAddDataDialog() {
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_add_data, null);

        android.widget.EditText editName = dialogView.findViewById(R.id.editDataName);
        android.widget.EditText editDate = dialogView.findViewById(R.id.editDataDate);

        // DatePickerDialog with YYYY-MM-DD formatting (leading zeros)
        editDate.setOnClickListener(v -> {
            java.util.Calendar c = java.util.Calendar.getInstance();
            new android.app.DatePickerDialog(this,
                    (view, year, month, dayOfMonth) ->
                            editDate.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth)),
                    c.get(java.util.Calendar.YEAR),
                    c.get(java.util.Calendar.MONTH),
                    c.get(java.util.Calendar.DAY_OF_MONTH)
            ).show();
        });

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle("Add Event")
                .setView(dialogView)
                .setPositiveButton("Add", (d, which) -> {
                    String name = editName.getText().toString().trim();
                    String date = editDate.getText().toString().trim();
                    if (!name.isEmpty() && !date.isEmpty()) {
                        DataItem newItem = new DataItem(name, date);
                        dataList.add(newItem);
                        dataAdapter.notifyItemInserted(dataList.size() - 1);
                        saveDataList();
                    } else {
                        Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }

    /**
     * Shows edit dialog for existing event at specified position
     * Pre-fills form with current values, updates item in place
     */
    private void showEditDataDialog(int position) {
        DataItem item = dataList.get(position);
        android.view.LayoutInflater inflater = getLayoutInflater();
        android.view.View dialogView = inflater.inflate(R.layout.dialog_add_data, null);

        android.widget.EditText editName = dialogView.findViewById(R.id.editDataName);
        android.widget.EditText editDate = dialogView.findViewById(R.id.editDataDate);

        // Pre-fill existing values for editing
        editName.setText(item.getName());
        editDate.setText(item.getDate());

        // Same date picker as add dialog
        editDate.setOnClickListener(v -> {
            java.util.Calendar c = java.util.Calendar.getInstance();
            new android.app.DatePickerDialog(this,
                    (view, year, month, dayOfMonth) ->
                            editDate.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth)),
                    c.get(java.util.Calendar.YEAR),
                    c.get(java.util.Calendar.MONTH),
                    c.get(java.util.Calendar.DAY_OF_MONTH)
            ).show();
        });

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle("Edit Event")
                .setView(dialogView)
                .setPositiveButton("Update", (d, which) -> {
                    String name = editName.getText().toString().trim();
                    String date = editDate.getText().toString().trim();
                    if (!name.isEmpty() && !date.isEmpty()) {
                        item.setName(name);
                        item.setDate(date);
                        dataAdapter.notifyItemChanged(position); // Update single row
                        saveDataList();
                        Toast.makeText(this, "Event updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }
}