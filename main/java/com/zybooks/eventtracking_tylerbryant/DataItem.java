package com.zybooks.eventtracking_tylerbryant;

import androidx.annotation.NonNull;


/**
 * Data model for individual events
 * Serializable to/from JSON using Gson for SharedPreferences storage
 * Supports add/edit/delete operations through getters/setters
 */
public class DataItem {
    private String name; // Event name (e.g., "Dentist Appointment")
    private String date; // Event date in YYYY-MM-DD format

    // Constructor for creating new events
    public DataItem(String name, String date) {
        this.name = name;
        this.date = date;
    }

    // Getters and setters for data access
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    // toString for debugging and Gson serialization support
    @NonNull
    @Override
    public String toString() {
        return "DataItem{name='" + name + "', date='" + date + "'}";
    }
}