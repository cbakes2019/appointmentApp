package com.example.appointmentapp.Model;

/**
 * The AppointmentSummary class represents a summary of appointments based on their type and month.
 * It provides a constructor to create AppointmentSummary instances and getter and setter methods for each property.
 */
public class AppointmentSummary {
    private String month;
    private String type;
    private int count;

    /**
     * Constructs an AppointmentSummary object with the specified properties.
     * @param month The month of the appointment summary.
     * @param type  The type of the appointments included in the summary.
     * @param count The number of appointments of the specified type for the given month.
     */
    public AppointmentSummary(String month, String type, int count) {
        this.month = month;
        this.type = type;
        this.count = count;
    }

    /** Getters and Setters */

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
