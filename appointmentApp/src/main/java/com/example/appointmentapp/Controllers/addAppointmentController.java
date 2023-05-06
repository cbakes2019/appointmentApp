package com.example.appointmentapp.Controllers;

import com.example.appointmentapp.Main.HelloApplication;
import com.example.appointmentapp.Model.Appointment;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;

/** Controller for adding appointments */
public class addAppointmentController implements Initializable {

    /** UI components */
    public TextField appointmentIdField;
    public TextField titleField;
    public TextArea descriptionField;

    public TextField typeField;
    public TextField locationField;
    public DatePicker startDateChoice;
    public DatePicker endDateChoice;

    public ChoiceBox userIdChoice;
    public ComboBox<LocalTime> startTimeChoice;
    public ComboBox<LocalTime> endTimeChoice;

    public ChoiceBox customerIdChoice;
    public ChoiceBox contactIdChoice;

    private Stage stage;

    private Scene scene;

    private Parent root;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");



    /**
     * Handles the save appointment button click event.
     * @param event the action event
     * @throws IOException  if there is an error while loading the appointments.fxml file
     * @throws SQLException if there is an error while try to access the database
     */



    public void saveAppointment(ActionEvent event) throws IOException, SQLException {

        if (!validateFields()) {
            return;
        }

        String title = titleField.getText();
        String description = descriptionField.getText();
        String type = typeField.getText();
        String location = locationField.getText();
        LocalDate startDate = startDateChoice.getValue();
        LocalDate endDate = endDateChoice.getValue();
        LocalTime startTime = startTimeChoice.getValue();
        LocalTime endTime = endTimeChoice.getValue();

        if (startTime == null || endTime == null) {
            showAlert("Error: Start time and/or end time not selected.");
            return;
        }

        ZoneId localZone = ZoneId.systemDefault();
        ZoneId utcZone = ZoneId.of("UTC");

        ZonedDateTime startDateTimeLocal = LocalDateTime.of(startDate, startTime).atZone(localZone);
        ZonedDateTime endDateTimeLocal = LocalDateTime.of(endDate, endTime).atZone(localZone);

        ZonedDateTime startDateTimeUTC = startDateTimeLocal.withZoneSameInstant(utcZone);
        ZonedDateTime endDateTimeUTC = endDateTimeLocal.withZoneSameInstant(utcZone);

        String start = startDateTimeUTC.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
        String end = endDateTimeUTC.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));

        int customerId = (int) customerIdChoice.getValue();
        int userId = (int) userIdChoice.getValue();
        int contactId = (int) contactIdChoice.getValue();

        Appointment newAppointment = new Appointment(title, description, contactId, type, start, end, customerId, userId, location);

        if (!isWithinBusinessHours(startDateTimeLocal, endDateTimeLocal)) {
            showAlert("ErrorBusinessHours");
            return;
        }

        if (!isWeekday(startDate, endDate)) {
            showAlert("ErrorWeekends");
            return;
        }

        if (isOverlap(startDateTimeLocal.toLocalDateTime(), endDateTimeLocal.toLocalDateTime(), -1)) {
            showAlert("ErrorOverlappingAppointments");
            return;
        }

        addAppointmentToDatabase(newAppointment);

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/appointments.fxml"));
        Parent root = fxmlLoader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Adds a new appointment to the database.
     * @param appointment the appointment object that will be added to the database
     */
    private void addAppointmentToDatabase(Appointment appointment) {
        String url = "jdbc:mysql://localhost:3306/client_schedule";
        String user = "sqlUser";
        String password = "Passw0rd!";

        Timestamp start = Timestamp.valueOf(LocalDateTime.parse(appointment.getStartDate(), FORMATTER));
        Timestamp end = Timestamp.valueOf(LocalDateTime.parse(appointment.getEndDate(), FORMATTER));

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String insertQuery = "INSERT INTO appointments (Title, Description, Contact_ID, Type, Start, End, Customer_ID, User_ID, Location) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

            preparedStatement.setString(1, appointment.getTitle());
            preparedStatement.setString(2, appointment.getDescription());
            preparedStatement.setInt(3, appointment.getContactId());
            preparedStatement.setString(4, appointment.getType());
            preparedStatement.setTimestamp(5, start);
            preparedStatement.setTimestamp(6, end);
            preparedStatement.setInt(7, appointment.getCustomerId());
            preparedStatement.setInt(8, appointment.getUserId());
            preparedStatement.setString(9, appointment.getLocation());

            preparedStatement.executeUpdate();

        } catch (Exception e) {
            System.out.println("Couldn't add appointment to database: " + e.getMessage());
        }
    }

    public void cancelButton (ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/appointments.fxml"));
        Parent root = fxmlLoader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    /** Populates the contact choice field with data from the database. */
    private void populateContacts() {
        String url = "jdbc:mysql://localhost:3306/client_schedule";
        String user = "sqlUser";
        String password = "Passw0rd!";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String selectQuery = "SELECT Contact_ID FROM contacts";
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int contactId = resultSet.getInt("Contact_ID");
                contactIdChoice.getItems().add(contactId);
            }
        } catch (Exception e) {
            System.out.println("Problem fetching contacts: " + e.getMessage());
        }
    }

    /** Populates the customer choice field with data from the database. */
    private void populateCustomers() {
        String url = "jdbc:mysql://localhost:3306/client_schedule";
        String user = "sqlUser";
        String password = "Passw0rd!";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String selectQuery = "SELECT Customer_ID FROM customers";
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int customerId = resultSet.getInt("Customer_ID");
                customerIdChoice.getItems().add(customerId);
            }
        } catch (Exception e) {
            System.out.println("Problem fetching customers: " + e.getMessage());
        }
    }

    /** Populates the user choice field with data from the database. */
    private void populateUsers() {
        String url = "jdbc:mysql://localhost:3306/client_schedule";
        String user = "sqlUser";
        String password = "Passw0rd!";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String selectQuery = "SELECT User_ID FROM users";
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int userId = resultSet.getInt("User_ID");
                userIdChoice.getItems().add(userId);
            }
        } catch (Exception e) {
            System.out.println("Problem fetching users: " + e.getMessage());
        }
    }


    /**
     * Determines if the given start and end times are within business hours.
     * @param start the start time
     * @param end   the end time
     * @return true if both times are within business hours, false otherwise
     */
    private boolean isWithinBusinessHours(ZonedDateTime start, ZonedDateTime end) {

        ZoneId estZone = ZoneId.of("America/New_York");
        ZonedDateTime startEst = start.withZoneSameInstant(estZone);
        ZonedDateTime endEst = end.withZoneSameInstant(estZone);

        LocalTime businessStart = LocalTime.of(8, 0);
        LocalTime businessEnd = LocalTime.of(22, 0);

        LocalTime startTime = startEst.toLocalTime();
        LocalTime endTime = endEst.toLocalTime();

        return !startTime.isBefore(businessStart) && !endTime.isAfter(businessEnd);
    }
    /**
     * Determines if the given start and end dates are weekdays.
     * @param startDate the start date
     * @param endDate   the end date
     * @return true if both dates are weekdays, false otherwise
     */

    private boolean isWeekday(LocalDate startDate, LocalDate endDate) {
        DayOfWeek startDay = startDate.getDayOfWeek();
        DayOfWeek endDay = endDate.getDayOfWeek();
        return !(startDay == DayOfWeek.SATURDAY || startDay == DayOfWeek.SUNDAY || endDay == DayOfWeek.SATURDAY || endDay == DayOfWeek.SUNDAY);
    }

    /**
     * Checks if the given start and end times overlap with any existing appointments, excluding the appointment with the specified ID.
     * @param startDateTime the start date and time
     * @param endDateTime   the end date and time
     * @param appointmentId the appointment ID to exclude from the overlap check
     * @return true if there is an overlapping appointment, false otherwise
     * @throws SQLException if there is an error while accessing the database
     */
    private boolean isOverlap(LocalDateTime startDateTime, LocalDateTime endDateTime, int appointmentId) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/client_schedule";
        String user = "sqlUser";
        String password = "Passw0rd!";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String selectQuery = "SELECT * FROM appointments WHERE (? < End AND ? > Start) AND Appointment_ID != ?";
            PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setString(1, startDateTime.format(FORMATTER));
            preparedStatement.setString(2, endDateTime.format(FORMATTER));
            preparedStatement.setInt(3, appointmentId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return true;
            }
        } catch (Exception e) {
            System.out.println("Problem checking overlapping appointments: " + e.getMessage());
        }
        return false;
    }

    /**
     * Shows an alert with the specified message from the resource bundle.
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
     * Validates the input fields for creating a new appointment.
     * @return true if all input fields are valid, false otherwise
     */

    private boolean validateFields() {
        if (titleField.getText().isEmpty() || descriptionField.getText().isEmpty() || typeField.getText().isEmpty() || locationField.getText().isEmpty() || startDateChoice.getValue() == null || endDateChoice.getValue() == null || userIdChoice.getValue() == null || customerIdChoice.getValue() == null || contactIdChoice.getValue() == null) {
            showAlert("ErrorAllFieldsRequired");
            return false;
        }

        if (startDateChoice.getValue().isAfter(endDateChoice.getValue()) || (startDateChoice.getValue().isEqual(endDateChoice.getValue()) && startTimeChoice.getValue().isAfter(endTimeChoice.getValue()))) {
            showAlert("ErrorStartAfterEnd");
            return false;
        }

        return true;
    }

    /**
     * Initializes the add appointment controller.
     *
     * @param url the location used to resolve relative paths for the root object, or null if the location is not known
     * @param resourceBundle the resources used to localize the root object
     */

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        /** Populates the time choice fields with 15 minute intervals. */

        for (int i = 0; i < 24; i++) {
            for (int j = 0; j < 60; j += 15) {
                LocalTime timeOption = LocalTime.of(i, j);
                startTimeChoice.getItems().add(timeOption);
                endTimeChoice.getItems().add(timeOption);
            }
        }

        populateUsers();


        populateCustomers();


        populateContacts();


        Platform.runLater(() -> titleField.requestFocus());

    }



}
