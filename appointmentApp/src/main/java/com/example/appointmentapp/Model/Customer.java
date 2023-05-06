package com.example.appointmentapp.Model;

/**
 * The Customer class represents a customer with various properties such as name, address, state/province, country, postal code, and phone number.
 * It provides constructors for creating Customer instances and getter and setter methods for each property.
 */
public class Customer {

        private int customerId;
        private String name;
        private String address;
        private String stateProvince;
        private String country;
        private String postalCode;
        private String phoneNumber;


    /**
     * Constructs a Customer object with the specified properties, including a customer ID.
     * @param customerId    The unique ID of the customer.
     * @param name          The name of the customer.
     * @param address       The address of the customer.
     * @param stateProvince The state or province of the customer.
     * @param country       The country of the customer.
     * @param postalCode    The postal code of the customer.
     * @param phoneNumber   The phone number of the customer.
     */

        public Customer(int customerId, String name, String address, String stateProvince, String country, String postalCode, String phoneNumber) {
            this.customerId = customerId;
            this.name = name;
            this.address = address;
            this.stateProvince = stateProvince;
            this.country = country;
            this.postalCode = postalCode;
            this.phoneNumber = phoneNumber;

        }

    /**
     * Constructs a Customer object with the specified properties, but this time without a customer ID.
     * @param name          The name of the customer.
     * @param address       The address of the customer.
     * @param stateProvince The state or province of the customer.
     * @param country       The country of the customer.
     * @param postalCode    The postal code of the customer.
     * @param phoneNumber   The phone number of the customer.
     */
    public Customer(String name, String address, String stateProvince, String country, String postalCode, String phoneNumber) {
        this.name = name;
        this.address = address;
        this.stateProvince = stateProvince;
        this.country = country;
        this.postalCode = postalCode;
        this.phoneNumber = phoneNumber;
    }

    /** Getters and Setters */
    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        }


    @Override
    public String toString() {
        return this.name;
    }


}
