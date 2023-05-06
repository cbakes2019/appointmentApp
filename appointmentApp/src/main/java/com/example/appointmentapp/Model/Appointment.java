package com.example.appointmentapp.Model;

/**
 * The Appointment class represents an appointment object and its properties.
 * It provides constructors to create appointment instances and getter and setter methods for each property.
 */
public class Appointment {

    private int appointmentId;
    private String title;
    private String description;
    private int contactId;
    private String type;
    private String startDate;
    private String endDate;
    private int customerId;
    private int userId;
    private String location;

    /**
     * Constructs an Appointment object without an appointmentId.
     * @param title       The title of the appointment.
     * @param description The description of the appointment.
     * @param contactId   The ID of the contact associated with the appointment.
     * @param type        The type of the appointment.
     * @param startDate   The start date and time of the appointment.
     * @param endDate     The end date and time of the appointment.
     * @param customerId  The ID of the customer associated with the appointment.
     * @param userId      The ID of the user who created the appointment.
     * @param location    The location of the appointment.
     */


    public Appointment(String title, String description, int contactId, String type, String startDate, String endDate, int customerId, int userId, String location) {
        this.title = title;
        this.description = description;
        this.contactId = contactId;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.customerId = customerId;
        this.userId = userId;
        this.location = location;
    }

/**
 * Constructs an Appointment object with all properties but this time with an appointmentId.
 * @param appointmentId The ID of the appointment.
 * @param title         The title of the appointment.
 * @param description   The description of the appointment.
 * @param contactId     The ID of the contact associated with the appointment.
 * @param type          The type of the appointment.
 * @param startDate     The start date and time of the appointment.
 * @param endDate       The end date and time of the appointment.
 * @param customerId    The ID of the customer associated with the appointment.
 * @param userId        The ID of the user who created the appointment.
 * @param location      The location of the appointment.
 */

    public Appointment(int appointmentId, String title, String description, int contactId, String type, String startDate, String endDate, int customerId, int userId, String location) {
        this.appointmentId = appointmentId;
        this.title = title;
        this.description = description;
        this.contactId = contactId;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.customerId = customerId;
        this.userId = userId;
        this.location = location;
    }

/** Getters and Setters */


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }



    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }


}
