package com.zybooks.eventtracking_tylerbryant;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Main login screen for Event Tracking app
 * Handles user authentication using SharedPreferences
 * Automatically redirects logged-in users to DataGridActivity
 */
public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in - skip login screen
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(KEY_LOGGED_IN, false)) {
            startActivity(new Intent(this, DataGridActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Initialize UI components
        EditText editUsername = findViewById(R.id.editUsername);
        EditText editPassword = findViewById(R.id.editPassword);
        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonCreateAccount = findViewById(R.id.buttonCreateAccount);
        TextView textStatus = findViewById(R.id.textStatus);

        // Set click listeners for login and account creation
        buttonLogin.setOnClickListener(v ->
                loginUser(editUsername, editPassword, textStatus)
        );
        buttonCreateAccount.setOnClickListener(v ->
                createAccount(editUsername, editPassword, textStatus)
        );
    }

    /**
     * Validates username/password against stored credentials
     * Shows error message if credentials don't match
     */
    private void loginUser(EditText editUsername, EditText editPassword, TextView textStatus) {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            textStatus.setText(getString(R.string.error_empty_fields));
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUsername = prefs.getString(KEY_USERNAME, "");
        String savedPassword = prefs.getString(KEY_PASSWORD, "");

        // Simple string comparison for authentication
        if (username.equals(savedUsername) && password.equals(savedPassword)) {
            prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply();
            startActivity(new Intent(this, DataGridActivity.class));
            finish();
        } else {
            textStatus.setText(getString(R.string.login_failed));
        }
    }

    /**
     * Creates new user account by storing username/password in SharedPreferences
     * Overwrites any existing account (single-user app)
     */
    private void createAccount(EditText editUsername, EditText editPassword, TextView textStatus) {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            textStatus.setText(getString(R.string.error_fill_fields));
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_USERNAME, username)
                .putString(KEY_PASSWORD, password)
                .apply();

        textStatus.setText(getString(R.string.account_created));
    }
}