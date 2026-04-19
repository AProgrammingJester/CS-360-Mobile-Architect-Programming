package com.zybooks.eventtracking_tylerbryant;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Settings screen for SMS notification preferences
 * Toggles SMS feature on/off and stores preference
 * Provides test SMS preview functionality
 */
public class SMSPermissionActivity extends AppCompatActivity {

    private boolean smsEnabled;
    private TextView textStatus;
    private Button buttonToggle;
    private Button buttonSendTest;

    private static final String PREFS_NAME = "UserPrefs";
    private static final String KEY_SMS_ENABLED = "smsEnabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        // Enable back arrow in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // show back arrow
            getSupportActionBar().setTitle(getString(R.string.settings)); // display "Settings"
        }

        // Initialize UI components
        textStatus = findViewById(R.id.textStatus);
        buttonToggle = findViewById(R.id.buttonEnableSMS);
        buttonSendTest = findViewById(R.id.buttonSendTest);

        // Load current SMS preference
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        smsEnabled = prefs.getBoolean(KEY_SMS_ENABLED, false);
        updateUI();

        // Toggle SMS enabled/disabled
        buttonToggle.setOnClickListener(v -> {
            smsEnabled = !smsEnabled;
            prefs.edit().putBoolean(KEY_SMS_ENABLED, smsEnabled).apply();
            updateUI();
        });

        // Test SMS shows preview toast
        buttonSendTest.setOnClickListener(v -> {
            if (smsEnabled) {
                Toast.makeText(this, getString(R.string.test_sms_message), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.sms_permission_denied, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates button text and status based on smsEnabled state
     */
    private void updateUI() {
        if (smsEnabled) {
            textStatus.setText(R.string.sms_permission_granted);
            buttonToggle.setText(R.string.disable_sms);
        } else {
            textStatus.setText(R.string.sms_permission_denied);
            buttonToggle.setText(R.string.enable_sms_button);
        }
    }

    /**
     * Handles back arrow click to return to previous screen
     */
    @Override
    public boolean onSupportNavigateUp() {
        finish(); // close this activity and go back
        return true;
    }
}