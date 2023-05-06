package com.example.appointmentapp.Controllers;

import com.example.appointmentapp.Main.HelloApplication;
import com.example.appointmentapp.Main.JDBC;
import com.example.appointmentapp.Model.Appointment;
import com.example.appointmentapp.Model.AppointmentSummary;
import com.example.appointmentapp.Model.Customer;
import com.example.appointmentapp.Model.StateSummary;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** The reportsController class is responsible for displaying various reports related to appointments. */
public class reportsController implements Initializable {

    /** UI Componenets */
    public TableView appointmentsTable;
    public TableColumn apptIdColumn;
    public TableColumn titleColumn;
    public TableColumn descriptionColumn;
    public TableColumn locationColumn;
    public TableColumn contactIdColumn;
    public TableColumn typeColumn;
    public TableColumn startDateColumn;
    public TableColumn endDateColumn;
    public TableColumn customerIdColumn;
    public TableColumn userIdColumn;
    public ChoiceBox customerChoice;
    public TableView appointmentFilterTable;
    public TableColumn totalColumn;
    public TableColumn apptTypeColumn;
    public TableColumn apptMonthColumn;
    public TableView stateFilterTable;
    public TableColumn dName;
    public TableColumn tCustomers;


    private Stage stage;

    private Scene scene;

    private Parent root;

    /**
     * Returns an observable list of all appointments in the database.
     * @return an observable list of appointments.
     */
    private ObservableList<Appointment> getAppointments() {
        ObservableList<Appointment> appointments = FXCollections.observableArrayList();

        try {
            JDBC.openConnection();
            Connection conn = JDBC.connection;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM appointments");

            while (rs.next()) {
                int appointmentId = rs.getInt("Appointment_ID");
                String title = rs.getString("Title");
                String description = rs.getString("Description");
                int contactId = rs.getInt("Contact_ID");
                String type = rs.getString("Type");
                Timestamp startTimestamp = rs.getTimestamp("Start");
                Timestamp endTimestamp = rs.getTimestamp("End");

                ZonedDateTime startUTC = startTimestamp.toInstant().atZone(ZoneOffset.UTC);
                ZonedDateTime endUTC = endTimestamp.toInstant().atZone(ZoneOffset.UTC);

                ZonedDateTime startLocal = startUTC.withZoneSameInstant(ZoneId.systemDefault());
                ZonedDateTime endLocal = endUTC.withZoneSameInstant(ZoneId.systemDefault());
                String start = startLocal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                String end = endLocal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                int customerId = rs.getInt("Customer_ID");
                int userId = rs.getInt("User_ID");
                String location = rs.getString("Location");

                appointments.add(new Appointment(appointmentId, title, description, contactId, type, start, end, customerId, userId, location));
            }
            JDBC.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return appointments;
    }

    /**
     * Navigates back to the appointments view.
     * @param event the action event that triggered this method.
     * @throws IOException if there is an error loading the view.
     */
    public void goBack (ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/appointments.fxml"));
        Parent root = fxmlLoader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();


    }

    /** Populates the customer choice box with customer data from the database. */
    private void populateCustomers() {
        String url = "jdbc:mysql://localhost:3306/client_schedule";
        String user = "sqlUser";
        String password = "Passw0rd!";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String selectQuery = "SELECT c.Customer_ID, c.Customer_Name, c.Address, d.Division, ct.Country, c.Postal_Code, c.Phone " +
                    "FROM customers c " +
                    "JOIN first_level_divisions d ON c.Division_ID = d.Division_ID " +
                    "JOIN countries ct ON d.Country_ID = ct.Country_ID";
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int customerId = resultSet.getInt("Customer_ID");
                String name = resultSet.getString("Customer_Name");
                String address = resultSet.getString("Address");
                String division = resultSet.getString("Division");
                String country = resultSet.getString("Country");
                String postalCode = resultSet.getString("Postal_Code");
                String phoneNumber = resultSet.getString("Phone");

                customerChoice.getItems().add(new Customer(customerId, name, address, division, country, postalCode, phoneNumber));
            }
        } catch (Exception e) {
            System.out.println("Error fetching customers: " + e.getMessage());
        }
    }

    /**
     * Filters the appointments table by the selected customer.
     * @param customerId the ID of the selected customer.
     */

    private void filterAppointmentsByCustomer(int customerId) {
        ObservableList<Appointment> allAppointments = getAppointments();
        ObservableList<Appointment> filteredAppointments = FXCollections.observableArrayList();

        allAppointments.stream()
                .filter(appointment -> appointment.getCustomerId() == customerId)
                .forEach(filteredAppointments::add);

        appointmentsTable.setItems(filteredAppointments);
    }

    /**
     * Returns an observable list of appointment summaries containing the appointment count per month and type.
     * @return an observable list of appointment summaries.
     */
    private ObservableList<AppointmentSummary> getAppointmentSummary() {
        ObservableList<AppointmentSummary> appointmentSummaryList = FXCollections.observableArrayList();
        Map<String, Integer> summaryMap = new HashMap<>();


        for (Appointment appointment : getAppointments()) {
            String month = appointment.getStartDate().substring(0, 7);
            String key = month + "|" + appointment.getType();
            summaryMap.put(key, summaryMap.getOrDefault(key, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : summaryMap.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            String month = parts[0];
            String type = parts[1];
            int count = entry.getValue();

            appointmentSummaryList.add(new AppointmentSummary(month, type, count));
        }

        return appointmentSummaryList;
    }

    /**
     * Returns an observable list of state summaries containing the count of customers per state.
     * @return an observable list of state summaries.
     */
    private ObservableList<StateSummary> getStateSummary() {
        ObservableList<StateSummary> stateSummaryList = FXCollections.observableArrayList();
        Map<String, Integer> summaryMap = new HashMap<>();

        try {
            JDBC.openConnection();
            Connection conn = JDBC.connection;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Division, COUNT(*) AS count FROM customers " +
                    "JOIN first_level_divisions ON customers.Division_ID = first_level_divisions.Division_ID " +
                    "GROUP BY Division");

            while (rs.next()) {
                String state = rs.getString("Division");
                int count = rs.getInt("count");

                summaryMap.put(state, count);
            }
            JDBC.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, Integer> entry : summaryMap.entrySet()) {
            String state = entry.getKey();
            int count = entry.getValue();

            stateSummaryList.add(new StateSummary(state, count));
        }

        return stateSummaryList;
    }


    /**
     * Logs the user out and navigates back to the login screen.
     * @param event the action event that triggered this method.
     * @throws IOException if there is an error loading the view.
     */
    public void logOut(ActionEvent event) throws IOException {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("com.example.appointmentapp.translations", Locale.getDefault());

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(resourceBundle.getString("logoutDialogTitle"));
        alert.setHeaderText(resourceBundle.getString("logoutDialogHeader"));
        alert.setContentText(resourceBundle.getString("logoutDialogContent"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/log-in.fxml"));
            fxmlLoader.setResources(resourceBundle);

            Parent root = fxmlLoader.load();
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        }
    }

    /**
     * Initializes the controller and sets up the required data for the view.
     * @param url the location used to resolve relative paths for the root object.
     * @param resourceBundle the resources used to localize the root object.
     */

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        apptIdColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        contactIdColumn.setCellValueFactory(new PropertyValueFactory<>("contactId"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        customerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));

        appointmentsTable.setItems(getAppointments());

        populateCustomers();

        /**
         * This is a lambda expression that creates an implementation of the ChangeListener interface.
         * It is used to add a listener to the selectedItemProperty of the customerChoice selection model.
         * Using a lambda expression makes the code easier to read, as it eliminates the need for
         * creating a separate anonymous inner class or implementing the ChangeListener interface in a named class.
         */

        customerChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                Customer selectedCustomer = (Customer) newValue;
                filterAppointmentsByCustomer(selectedCustomer.getCustomerId());
            } else {
                appointmentsTable.setItems(getAppointments());
            }
        });



        apptMonthColumn.setCellValueFactory(new PropertyValueFactory<>("month"));
        apptTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
        appointmentFilterTable.setItems(getAppointmentSummary());



        dName.setCellValueFactory(new PropertyValueFactory<>("state"));
        tCustomers.setCellValueFactory(new PropertyValueFactory<>("stateCount"));
        stateFilterTable.setItems(getStateSummary());



    }
}
