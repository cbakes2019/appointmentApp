package com.example.appointmentapp.Controllers;

import com.example.appointmentapp.Main.HelloApplication;
import com.example.appointmentapp.Main.JDBC;
import com.example.appointmentapp.Model.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/** Controller class for the customers view. */
public class customersController implements Initializable {

    /** UI Components. */

    public TableView customerTableView;
    public TableColumn customerIdColumn;
    public TableColumn nameColumn;
    public TableColumn addyColumn;
    public TableColumn stateProvinceColumn;
    public TableColumn phoneNumberColumn;
    public TableColumn postalCodeColumn;
    public TableColumn countryColumn;

    private Stage stage;

    private Scene scene;

    private Parent root;


    /**
     * Loads customers from the database and returns them as an ObservableList.
     * @return an ObservableList of Customer objects.
     */
    public ObservableList<Customer> loadCustomers() {
        ObservableList<Customer> customers = FXCollections.observableArrayList();

        try {
            JDBC.openConnection();
            Connection conn = JDBC.connection;

            String query = "SELECT c.Customer_ID, c.Customer_Name, c.Address, c.Postal_Code, c.Phone, " +
                    "fld.Division, co.Country " +
                    "FROM customers c " +
                    "JOIN first_level_divisions fld ON c.Division_ID = fld.Division_ID " +
                    "JOIN countries co ON fld.Country_ID = co.Country_ID";

            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int customerId = rs.getInt("Customer_ID");
                String name = rs.getString("Customer_Name");
                String address = rs.getString("Address");
                String stateProvince = rs.getString("Division");
                String country = rs.getString("Country");
                String postalCode = rs.getString("Postal_Code");
                String phoneNumber = rs.getString("Phone");

                customers.add(new Customer(customerId, name, address, stateProvince, country, postalCode, phoneNumber));
            }
            rs.close();
            JDBC.closeConnection();

        } catch (SQLException e) {
            System.err.println("Problem loading customers from database: " + e.getMessage());
        }

        return customers;
    }

    /**
     * Modifies the selected customer by launching the modify customer view.
     * @param event the ActionEvent that triggers the method.
     * @throws IOException if an error occurs while loading the modify customer view.
     */
    public void modCustomer(ActionEvent event) throws IOException {
        Customer selectedCustomer = (Customer) customerTableView.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Warning_A_customer_must_be_selected_in_order_to_modify_it", false);
        } else {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/modifyCustomer.fxml"));
            Parent root = fxmlLoader.load();
            modifyCustomerController modifyController = fxmlLoader.getController();
            modifyController.setCustomer(selectedCustomer);
            modifyController.setCustomersController(this);
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
    }

    /**
     * Adds a new customer by launching the add customer view.
     * @param event the ActionEvent that triggers the method.
     * @throws IOException if an error occurs while loading the add customer view.
     */
    public void addCustomer (ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/addCustomer.fxml"));
        Parent root = fxmlLoader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    /**
     * Deletes the selected customer from the database.
     * @param event the ActionEvent that triggers the method.
     */
    public void deleteCustomer(ActionEvent event) {
        Customer selectedCustomer = (Customer) customerTableView.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Warning_A_customer_must_be_selected_in_order_to_delete_it", false);
        } else if (customerAppointment(selectedCustomer.getCustomerId())) {
            showAlert(Alert.AlertType.ERROR, "Error_Unable_to_Delete_Customer", "Error_A_customer_with_existing_appointments_cannot_be_deleted", false);
        } else {
            Alert alert = showAlert(Alert.AlertType.CONFIRMATION, "Confirmation", "Confirmation_Are_you_sure_you_want_to_delete_the_selected_customer", true);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    JDBC.openConnection();
                    Connection conn = JDBC.connection;

                    String query = "DELETE FROM customers WHERE Customer_ID = ?";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setInt(1, selectedCustomer.getCustomerId());

                    int rowsAffected = stmt.executeUpdate();

                    if (rowsAffected > 0) {
                        customerTableView.getItems().remove(selectedCustomer);

                        showAlert(Alert.AlertType.INFORMATION, "Success", "Success_Customer_successfully_deleted", false);
                    } else {
                        System.err.println("No customer was deleted.");
                    }

                    stmt.close();
                    JDBC.closeConnection();

                } catch (SQLException e) {
                    System.err.println("Problem deleting customer from database: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Checks if the given customer has any appointments.
     * @param customerId the ID of the customer to check.
     * @return true if the customer has appointments, false otherwise.
     */
    public boolean customerAppointment(int customerId) {
        boolean hasAppointment = false;

        try {
            JDBC.openConnection();
            Connection conn = JDBC.connection;

            String query = "SELECT COUNT(*) as count FROM appointments WHERE Customer_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, customerId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                hasAppointment = count > 0;
            }

            rs.close();
            stmt.close();
            JDBC.closeConnection();

        } catch (SQLException e) {
            System.err.println("Problem checking customer appointments: " + e.getMessage());
        }

        return hasAppointment;
    }

    /**
     * Navigates back to the appointments view.
     * @param event the ActionEvent that triggers the method.
     * @throws IOException if an error occurs while loading the appointments view.
     */
    public void goBack(ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/appointments.fxml"));
        Parent root = fxmlLoader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    /**
     * Displays an alert with the given properties.
     * @param alertType    the type of the alert.
     * @param titleKey     the title of the alert, retrieved from the resource bundle.
     * @param contentKey   the content of the alert, retrieved from the resource bundle.
     * @param returnResult true if the alert should return a result, false otherwise.
     * @return the Alert object if returnResult is true, null otherwise.
     */
    private Alert showAlert(Alert.AlertType alertType, String titleKey, String contentKey, boolean returnResult) {
        ResourceBundle bundle = ResourceBundle.getBundle("com.example.appointmentapp.translations", Locale.getDefault());
        String title = bundle.getString(titleKey);
        String content = bundle.getString(contentKey);

        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        if (returnResult) {
            return alert;
        } else {
            alert.showAndWait();
            return null;
        }
    }

    /**
     * Initializes the customer table view and its columns.
     * @param url the URL used to resolve relative paths for the root object, or null if the location is not known.
     * @param resourceBundle the resource bundle containing localized data for the current locale, or null if none is provided.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addyColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        stateProvinceColumn.setCellValueFactory(new PropertyValueFactory<>("stateProvince"));
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
        postalCodeColumn.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
        phoneNumberColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        customerTableView.setItems(loadCustomers());


    }
}
