package com.example.appointmentapp.Controllers;

import com.example.appointmentapp.Main.HelloApplication;
import com.example.appointmentapp.Main.JDBC;
import com.example.appointmentapp.Model.Customer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/** Controller for adding a customer */
public class addCustomerController implements Initializable {

    /** UI Components */
    public TextField custIdField;
    public TextField custNameField;
    public TextField addyField;
    public TextField postalCodeField;
    public TextField pNumberField;
    public ChoiceBox countryChoice;
    public ChoiceBox stateChoice;

    private Stage stage;

    private Scene scene;

    private Parent root;


    private customersController customersControllerInstance;

    /**
     * Sets the customersController for communication between controllers.
     * @param customersControllerInstance the instance of the customersController class
     */

    public void setCustomersController(customersController customersControllerInstance) {
        this.customersControllerInstance = customersControllerInstance;
    }



    /**
     * Adds a new customer and navigates back to the customers view.
     * @param event the action event
     * @throws IOException if there is an error while loading the customers.fxml file
     */

    public void addCustomer(ActionEvent event) throws IOException {
        String name = custNameField.getText();
        String address = addyField.getText();
        String stateProvince = (String) stateChoice.getSelectionModel().getSelectedItem();
        String country = (String) countryChoice.getSelectionModel().getSelectedItem();
        String postalCode = postalCodeField.getText();
        String phoneNumber = pNumberField.getText();

        if (stateProvince == null || country == null) {
            showAlert("ErrorCountryAndStateRequired");
            return;
        }

        Customer newCustomer = new Customer(name, address, stateProvince, country, postalCode, phoneNumber);
        addCustomerToDatabase(newCustomer);

        if (customersControllerInstance != null) {
            customersControllerInstance.loadCustomers();

            if (customersControllerInstance != null) {
                customersControllerInstance.customerTableView.setItems(customersControllerInstance.loadCustomers());
            }
        }
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/customers.fxml"));
        Parent root = fxmlLoader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Navigates back to the customers view without adding a new customer.
     * @param event the action event
     * @throws IOException if there is an error while loading the customers.fxml file
     */
    public void goBack(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/customers.fxml"));
        Parent root = fxmlLoader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Loads the list of countries from the database.
     */
    private void loadCountries() {
        Set<String> uniqueCountries = new HashSet<>();
        List<String> allowedCountries = Arrays.asList("Canada", "U.S", "UK");

        try {
            JDBC.openConnection();
            Connection conn = JDBC.connection;

            String query = "SELECT * FROM countries";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String country = rs.getString("Country");
                if (allowedCountries.contains(country)) {
                    uniqueCountries.add(country);
                }
            }
            rs.close();
            JDBC.closeConnection();

        } catch (SQLException e) {
            System.err.println("Problem loading countries from database: " + e.getMessage());
        }


        countryChoice.getItems().addAll(uniqueCountries);
    }

    /**
     * Loads the list of states or provinces for the specified country ID.
     * @param countryId the country ID
     */
    private void loadStates(int countryId) {
        Set<String> uniqueStates = new HashSet<>();
        stateChoice.getItems().clear();

        try {
            JDBC.openConnection();
            Connection conn = JDBC.connection;

            String query = "SELECT Division FROM first_level_divisions WHERE Country_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, countryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String state = rs.getString("Division");
                uniqueStates.add(state);
            }
            rs.close();
            JDBC.closeConnection();

        } catch (SQLException e) {
            System.err.println("Problem loading states from database: " + e.getMessage());
        }


        stateChoice.getItems().addAll(uniqueStates);
    }

    /**
     * Adds a new customer to the database.
     * @param customer the customer object to be added to the database
     */
    private void addCustomerToDatabase(Customer customer) {
        try {
            JDBC.openConnection();
            Connection conn = JDBC.connection;

            int divisionId = getDivisionId(customer.getStateProvince());


            String query = "INSERT INTO customers (Customer_Name, Address, Postal_Code, Phone, Division_ID) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getAddress());
            stmt.setString(3, customer.getPostalCode());
            stmt.setString(4, customer.getPhoneNumber());
            stmt.setInt(5, divisionId);

            stmt.executeUpdate();
            JDBC.closeConnection();

        } catch (SQLException e) {
            System.err.println("Problem adding customer to database: " + e.getMessage());
        }
    }

    /**
     * Retrieves the country ID for the specified country name from the database.
     * @param countryName the country name
     * @return the country ID
     */
    private int getCountryId(String countryName) {
        int countryId = -1;

        try {
            JDBC.openConnection();
            Connection conn = JDBC.connection;

            String query = "SELECT Country_ID FROM countries WHERE Country = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, countryName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                countryId = rs.getInt("Country_ID");
            }
            rs.close();
            JDBC.closeConnection();

        } catch (SQLException e) {
            System.err.println("Error getting country ID from database: " + e.getMessage());
        }

        return countryId;
    }

    /**
     * Retrieves the division ID for the specified state or province from the database.
     * @param stateProvince the state or province
     * @return the division ID
     */
    private int getDivisionId(String stateProvince) {
        int divisionId = -1;

        try {
            JDBC.openConnection();
            Connection conn = JDBC.connection;

            String query = "SELECT Division_ID FROM first_level_divisions WHERE Division = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, stateProvince);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                divisionId = rs.getInt("Division_ID");
            }
            rs.close();
            JDBC.closeConnection();

        } catch (SQLException e) {
            System.err.println("Error getting division ID from database: " + e.getMessage());
        }

        return divisionId;
    }

    /**
     * Shows an alert with the specified message key from the resource bundle.
     * @param messageKey the message key for the resource bundle
     */
    private void showAlert(String messageKey) {
        ResourceBundle bundle = ResourceBundle.getBundle("com.example.appointmentapp.translations", Locale.getDefault());
        String message = bundle.getString(messageKey);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Initializes the add customer controller.
     * @param url the location used to resolve relative paths for the root object, or null if the location is not known
     * @param resourceBundle the resources used to localize the root object
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCountries();
        countryChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            String selectedCountry = (String) newValue;
            if (selectedCountry != null) {
                int countryId = getCountryId(selectedCountry);
                loadStates(countryId);
            }
        });

        Platform.runLater(() -> custNameField.requestFocus());

}

    }


