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

/**
 * This class is the controller for the modify customer screen.
 */
public class modifyCustomerController implements Initializable {

    /** UI Components */
    public TextField customerIdField;
    public TextField nameField;
    public TextField addyField;
    public TextField postalField;
    public TextField numberField;
    public ChoiceBox countryChoice;
    public ChoiceBox stateChoice;

    private Stage stage;

    private Scene scene;

    private Parent root;

    private customersController customersControllerInstance;

    /**
     * Sets the instance of the customersController.
     * @param customersControllerInstance the customersController instance
     */
    public void setCustomersController(customersController customersControllerInstance) {
        this.customersControllerInstance = customersControllerInstance;
    }

    /**
     * This method shows an error alert with a specified message.
     * @param messageKey the key for the error message to be displayed, fetched from the translations ResourceBundle
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
     * This method saves the modified customer to the database.
     * @return true if the customer is successfully saved, false if an error occurs or if the country and state fields are empty
     */
    public boolean saveCustomer() {

        int customerId = Integer.parseInt(customerIdField.getText());
        String name = nameField.getText();
        String address = addyField.getText();
        String stateProvince = (String) stateChoice.getSelectionModel().getSelectedItem();
        String country = (String) countryChoice.getSelectionModel().getSelectedItem();
        String postalCode = postalField.getText();
        String phoneNumber = numberField.getText();

        if (stateProvince == null || country == null) {
            showAlert("ErrorCountryAndStateRequired");
            return false;
        } else {

            Customer updatedCustomer = new Customer(customerId, name, address, stateProvince, country, postalCode, phoneNumber);
            updateCustomerInDatabase(updatedCustomer);


            if (customersControllerInstance != null) {
                customersControllerInstance.loadCustomers();

                if (customersControllerInstance != null) {
                    customersControllerInstance.customerTableView.setItems(customersControllerInstance.loadCustomers());
                }
            }
        }
        return true;
    }


    /**
     * Handles the modify customer button click event, saving the customer and navigating back to the customer list.
     * @param event the ActionEvent representing the modify customer button click
     * @throws IOException if an error occurs when loading the FXML file
     */
    public void modifyCustomer(ActionEvent event) throws IOException {

        if (saveCustomer()) {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/customers.fxml"));
            Parent root = fxmlLoader.load();
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
    }

    /**
     * Sets the customer information in the input fields for editing.
     * @param selectedCustomer the Customer object to be edited
     */

    public void setCustomer(Customer selectedCustomer) {
        if (selectedCustomer != null) {
            customerIdField.setText(Integer.toString(selectedCustomer.getCustomerId()));
            nameField.setText(selectedCustomer.getName());
            addyField.setText(selectedCustomer.getAddress());
            postalField.setText(selectedCustomer.getPostalCode());
            numberField.setText(selectedCustomer.getPhoneNumber());


            String country = selectedCustomer.getCountry();
            String stateProvince = selectedCustomer.getStateProvince();
            countryChoice.getSelectionModel().select(country);
            stateChoice.getSelectionModel().select(stateProvince);

            Platform.runLater(() -> nameField.requestFocus());
        }
    }

    /**
     * Handles the go back button click event, navigating back to the customer list without saving any changes.
     * @param event the ActionEvent representing the go back button click
     * @throws IOException if an error occurs when loading the FXML file
     */
   public void goBack (ActionEvent event) throws IOException {

       FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/customers.fxml"));
       Parent root = fxmlLoader.load();
       stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
       scene = new Scene(root);
       stage.setScene(scene);
       stage.show();


   }

    /** Loads the allowed countries from the database and populates the countryChoice ComboBox. */
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
     * Loads the states for a given country and populates the stateChoice ComboBox.
     * @param countryId the ID of the country for which to load the states
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
     * Retrieves the country ID for a given country name from the database.
     * @param countryName the name of the country for which to retrieve the ID
     * @return the ID of the specified country or -1 if the country is not found
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
            System.err.println("Problem getting country ID from database: " + e.getMessage());
        }

        return countryId;
    }

    /**
     * Updates the customer information in the database.
     * @param customer the Customer object containing the updated information
     */
    private void updateCustomerInDatabase(Customer customer) {
        try {
            JDBC.openConnection();
            Connection conn = JDBC.connection;

            int divisionId = getDivisionId(customer.getStateProvince());

            String query = "UPDATE customers SET Customer_Name = ?, Address = ?, Postal_Code = ?, Phone = ?, Division_ID = ? WHERE Customer_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);

            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getAddress());
            stmt.setString(3, customer.getPostalCode());
            stmt.setString(4, customer.getPhoneNumber());
            stmt.setInt(5, divisionId);
            stmt.setInt(6, customer.getCustomerId());

            stmt.executeUpdate();
            JDBC.closeConnection();

        } catch (SQLException e) {
            System.err.println("Error updating customer in database: " + e.getMessage());
        }
    }

    /**
     * Retrieves the division ID for a given state or province name from the database.
     * @param stateProvince the name of the state or province for which to retrieve the ID
     * @return the ID of the specified state or province or -1 if not found
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
     * Initializes the controller class. This method is called after the FXML file has been loaded.
     * It sets up the country and state ComboBoxes, as well as their listeners.
     * @param url the location used to resolve relative paths for the root object, or null if the location is not known
     * @param resourceBundle the ResourceBundle used to localize the object, or null if the object was not localized
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

    }
}
