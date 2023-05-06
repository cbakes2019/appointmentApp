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
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

/** Controller class for the modify appointment scene. */
public class modifyAppointmentController implements Initializable {

    /** UI Component.  */
    public TextField appointmentField;
    public TextField titleField;
    public TextArea descriptionField;
    public TextField typeField;
    public TextField locationField;
    public DatePicker startDateChoice;
    public DatePicker endDateChoice;
    public ComboBox <LocalTime> startTimeChoice;
    public ComboBox <LocalTime> endTimeChoice;

    public ComboBox customerIdChoice;
    public ComboBox userIdChoice;
    public ComboBox contactIdChoice;
    private Stage stage;

    private Scene scene;

    private Parent root;

    private appointmentsController appointmentsController;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Sets the selected appointment's details in the view fields.
     * @param selectedAppointment the Appointment object containing the details of the selected appointment.
     */
    public void setSelectedAppointment(Appointment selectedAppointment) {

        appointmentField.setText(Integer.toString(selectedAppointment.getAppointmentId()));
        titleField.setText(selectedAppointment.getTitle());
        descriptionField.setText(selectedAppointment.getDescription());
        typeField.setText(selectedAppointment.getType());
        locationField.setText(selectedAppointment.getLocation());


        LocalDateTime startDateTime = LocalDateTime.parse(selectedAppointment.getStartDate(), FORMATTER);
        LocalDateTime endDateTime = LocalDateTime.parse(selectedAppointment.getEndDate(), FORMATTER);
        startDateChoice.setValue(startDateTime.toLocalDate());
       endDateChoice.setValue(endDateTime.toLocalDate());
        startTimeChoice.setValue(startDateTime.toLocalTime());
        endTimeChoice.setValue(endDateTime.toLocalTime());


        customerIdChoice.setValue(selectedAppointment.getCustomerId());
        userIdChoice.setValue(selectedAppointment.getUserId());
        contactIdChoice.setValue(selectedAppointment.getContactId());

    }


    /**
     * Handles the cancel modify action, which navigates back to the appointments view.
     * @param event the ActionEvent that triggers the method.
     * @throws IOException if an error occurs during FXML loading.
     */
    public void cancelModify (ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/appointments.fxml"));
        Parent root = fxmlLoader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Handles the modify appointment action, which updates the appointment details in the database and returns to the appointments view.
     * @param event the ActionEvent that triggers the method.
     * @throws IOException if an error occurs during FXML loading.
     * @throws SQLException if an error occurs during database access.
     */
    public void modifyAppointment(ActionEvent event) throws IOException, SQLException {

        if (!validateFields()) {
            return;
        }

        int appointmentId = Integer.parseInt(appointmentField.getText());
        String title = titleField.getText();
        String description = descriptionField.getText();
        String type = typeField.getText();
        String location = locationField.getText();
        LocalDate startDate = startDateChoice.getValue();
        LocalTime startTime = startTimeChoice.getValue();

        LocalDate endDate = endDateChoice.getValue();
        LocalTime endTime = endTimeChoice.getValue();

        ZoneId localZone = ZoneId.systemDefault();
        ZoneId utcZone = ZoneId.of("UTC");

        ZonedDateTime startDateTimeLocal = LocalDateTime.of(startDate, startTime).atZone(localZone);
        ZonedDateTime endDateTimeLocal = LocalDateTime.of(endDate, endTime).atZone(localZone);

        ZonedDateTime startDateTimeUTC = startDateTimeLocal.withZoneSameInstant(utcZone);
        ZonedDateTime endDateTimeUTC = endDateTimeLocal.withZoneSameInstant(utcZone);

        String start = startDateTimeUTC.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
        String end = endDateTimeUTC.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));

        int customerId = (int) customerIdChoice.getValue();
        int userId = (int)
                userIdChoice.getValue();
        int contactId = (int) contactIdChoice.getValue();

        Appointment updatedAppointment = new Appointment(appointmentId, title, description, contactId, type, start, end, customerId, userId, location);

        if (!isWithinBusinessHours(startDateTimeLocal, endDateTimeLocal)) {
            showAlert("ErrorBusinessHours");
            return;
        }


        if (!isWeekday(startDate, endDate)) {
            showAlert("Error_Appointments_cannot_be_scheduled_on_weekends");
            return;
        }

        if (isOverlap(startDateTimeLocal.toLocalDateTime(), endDateTimeLocal.toLocalDateTime(), appointmentId)) {
            showAlert("Error_Appointment_overlaps_with_another_appointment");
            return;
        }

        appointmentsController.updateAppointmentInDatabase(updatedAppointment);

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/appointments.fxml"));
        Parent root = fxmlLoader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Sets the appointments controller for this view.
     * @param appointmentsController the appointments controller.
     */
    public void setAppointmentsController(appointmentsController appointmentsController) {
        this.appointmentsController = appointmentsController;
    }


    /**
     * This method populates the ComboBox with customer IDs from the database.
     * It connects to the database, retrieves customer IDs from the customers table,
     * and adds them to the customerIdChoice ComboBox.
     */
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
            System.out.println("Error fetching customers: " + e.getMessage());
        }
    }

    /**
     * This method populates the ComboBox with user IDs from the database.
     * It connects to the database, retrieves user IDs from the users table,
     * and adds them to the userIdChoice ComboBox.
     */
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
            System.out.println("Error fetching users: " + e.getMessage());
        }
    }

    /**
     * This method populates the ComboBox with contact IDs from the database.
     * It connects to the database, retrieves contact IDs from the contacts table,
     * and adds them to the contactIdChoice ComboBox.
     */
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
            System.out.println("Error fetching contacts: " + e.getMessage());
        }
    }

    /**
     * This method checks if the appointment start and end times are within the business hours.
     * @param start the start time of the appointment as a ZonedDateTime
     * @param end the end time of the appointment as a ZonedDateTime
     * @return true if the appointment times are within business hours, false otherwise
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
     * This method checks if the appointment start and end dates are on weekdays.
     * @param startDate the start date of the appointment as a LocalDate
     * @param endDate the end date of the appointment as a LocalDate
     * @return true if both the start and end dates are weekdays, false otherwise
     */
    private boolean isWeekday(LocalDate startDate, LocalDate endDate) {
        DayOfWeek startDay = startDate.getDayOfWeek();
        DayOfWeek endDay = endDate.getDayOfWeek();
        return !(startDay == DayOfWeek.SATURDAY || startDay == DayOfWeek.SUNDAY || endDay == DayOfWeek.SATURDAY || endDay == DayOfWeek.SUNDAY);
    }

    /**
     * This method checks if the appointment start and end times overlap with other appointments.
     * @param startDateTime the start time of the appointment as a LocalDateTime
     * @param endDateTime the end time of the appointment as a LocalDateTime
     * @param appointmentId the ID of the appointment being checked for overlaps
     * @return true if the appointment times overlap with another appointment, false otherwise
     * @throws SQLException if a database error occurs
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
            System.out.println("Error checking overlapping appointments: " + e.getMessage());
        }
        return false;
    }

    /**
     * This method shows an error alert with a specified message.
     * @param messageKey the key for the error message to be displayed, fetched from the translations ResourceBundle
     */

    private void showAlert(String messageKey) {
        ResourceBundle bundle = ResourceBundle.getBundle("com.example.appointmentapp.translations", Locale.getDefault());
        String message = bundle.getString(messageKey);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("error_title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * This method validates the user input in the appointment modification form.
     * @return true if all fields are filled out correctly and appointment times are valid, false otherwise
     */
    private boolean validateFields() {
        if (titleField.getText().isEmpty() || descriptionField.getText().isEmpty() || typeField.getText().isEmpty() || locationField.getText().isEmpty() || startDateChoice.getValue() == null || endDateChoice.getValue() == null || userIdChoice.getValue() == null || customerIdChoice.getValue() == null || contactIdChoice.getValue() == null) {
            showAlert("Error_All_Fields_Must_Be_Filled_Out");
            return false;
        }

        if (startDateChoice.getValue().isAfter(endDateChoice.getValue()) || (startDateChoice.getValue().isEqual(endDateChoice.getValue()) && startTimeChoice.getValue().isAfter(endTimeChoice.getValue()))) {
            showAlert("Error_Start_date_and_time_cannot_be_after_end_date_and_time");
            return false;
        }

        return true;
    }


    /**
     * Initializes the modify appointment view by populating UI components.
     * @param url the URL used to resolve relative paths for the root object, or null if the location is not known.
     * @param resourceBundle the resource bundle containing localized data for the current locale, or null if none is provided.
     */

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

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
