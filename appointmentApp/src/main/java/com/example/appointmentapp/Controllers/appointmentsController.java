package com.example.appointmentapp.Controllers;

import com.example.appointmentapp.Main.HelloApplication;
import com.example.appointmentapp.Main.JDBC;
import com.example.appointmentapp.Model.Appointment;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

/** Controller for managing appointments */
public class appointmentsController implements Initializable {

    /** UI Components */
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

    @FXML
    public RadioButton viewAll;
    @FXML
    public RadioButton viewByWeek;
    @FXML
    public RadioButton viewByMonth;
    public Button deleteButton;

    private Stage stage;

    private Scene scene;

    private Parent root;

    private static boolean alertDisplayed = false;


    private appointmentsController appointmentsController;

    /**
     * gets all appointment records from the database and returns them as an ObservableList of Appointment objects.
     * @return an ObservableList containing Appointment objects.
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

                ZoneId localZone = ZoneId.systemDefault();
                ZonedDateTime startLocal = startTimestamp.toInstant().atZone(localZone);
                ZonedDateTime endLocal = endTimestamp.toInstant().atZone(localZone);

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
     * Filters appointments to show only those that fall within the current week.
     * @return an ObservableList containing appointments for the current week.
     */
    private ObservableList<Appointment> FilterByWeek() {
        ObservableList<Appointment> allAppointments = getAppointments();
        ObservableList<Appointment> filteredAppointments = FXCollections.observableArrayList();

        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        for (Appointment appointment : allAppointments) {
            LocalDate appointmentDate = LocalDate.parse(appointment.getStartDate().substring(0, 10));
            if (appointmentDate.isAfter(startOfWeek.minusDays(1)) && appointmentDate.isBefore(endOfWeek.plusDays(1))) {
                filteredAppointments.add(appointment);
            }
        }

        return filteredAppointments;
    }

    /**
     * Filters appointments to show only those that fall within the current month.
     * @return an ObservableList containing appointments for the current month.
     */
    private ObservableList<Appointment> FilterByMonth() {
        ObservableList<Appointment> allAppointments = getAppointments();
        ObservableList<Appointment> filteredAppointments = FXCollections.observableArrayList();

        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        for (Appointment appointment : allAppointments) {
            LocalDate appointmentDate = LocalDate.parse(appointment.getStartDate().substring(0, 10));
            if (appointmentDate.getMonthValue() == currentMonth && appointmentDate.getYear() == currentYear) {
                filteredAppointments.add(appointment);
            }
        }

        return filteredAppointments;
    }


    /** Sets the TableView items to display all appointments. */
    public void ViewALL() {
        appointmentsTable.setItems(getAppointments());
    }

    /** Sets the TableView items to display appointments filtered by the current week. */
    public void ViewByWeek() {
        appointmentsTable.setItems(FilterByWeek());
    }

    /** Sets the TableView items to display appointments filtered by the current month. */
    public void ViewByMonth() {
        appointmentsTable.setItems(FilterByMonth());
    }


    /**
     * Deletes the selected appointment from the TableView and database.
     * @param event the ActionEvent that triggered the deletion.
     * @throws IOException if an I/O error occurs.
     */
    @FXML
    private void deleteRow(ActionEvent event) throws IOException {
        Appointment selectedAppointment = (Appointment) appointmentsTable.getSelectionModel().getSelectedItem();

        if (selectedAppointment == null) {
            ResourceBundle resourceBundle = ResourceBundle.getBundle("com.example.appointmentapp.translations", Locale.getDefault());
            showAlert(resourceBundle.getString("NoAppointmentSelected"));
            return;
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("com.example.appointmentapp.translations", Locale.getDefault());
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(resourceBundle.getString("ConfirmationAPP"));
        alert.setHeaderText(resourceBundle.getString("DeleteAppointment"));
        alert.setContentText(resourceBundle.getString("AreYouSure"));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            appointmentsTable.getItems().remove(selectedAppointment);
            deleteAppointmentFromDatabase(selectedAppointment.getAppointmentId());


            String customMessage = String.format(resourceBundle.getString("DeletedAppointmentMessage"), selectedAppointment.getAppointmentId(), selectedAppointment.getType());
            showAlert(customMessage);
        }
    }

    /**
     * Deletes the appointment with the specified ID from the database.
     * @param appointmentId the ID of the appointment to delete.
     */
    private void deleteAppointmentFromDatabase(int appointmentId) {
        String url = "jdbc:mysql://localhost:3306/client_schedule";
        String user = "sqlUser";
        String password = "Passw0rd!";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            String deleteQuery = "DELETE FROM appointments WHERE Appointment_ID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery);
            preparedStatement.setInt(1, appointmentId);
            preparedStatement.executeUpdate();

        } catch (Exception e) {
            System.out.println("Problem deleting appointment from database: " + e.getMessage());
        }
    }


    /**
     * Logs the user out and returns to the log-in screen.
     * @param event the ActionEvent that triggered the logout.
     * @throws IOException if an I/O error occurs.
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

    public void addAPP (ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/addAppointment.fxml"));
        Parent root = fxmlLoader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void modifyAPP (ActionEvent event) throws IOException {

        Appointment selectedAppointment = (Appointment) appointmentsTable.getSelectionModel().getSelectedItem();


        if (selectedAppointment == null) {

            return;
        }



        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/modifyAppointment.fxml"));
        Parent root = fxmlLoader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        modifyAppointmentController controller = fxmlLoader.getController();
        controller.setSelectedAppointment(selectedAppointment);
        controller.setAppointmentsController(this);


        }

   public void viewCustomers (ActionEvent event) throws IOException {

       FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/customers.fxml"));
       Parent root = fxmlLoader.load();
       stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
       scene = new Scene(root);
       stage.setScene(scene);
       stage.show();

   }

    public void viewReports (ActionEvent event) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/Reports.fxml"));
        Parent root = fxmlLoader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }

    public void setAppointmentsController(appointmentsController appointmentsController) {
        this.appointmentsController = appointmentsController;
    }

    /**
     * Updates the given appointment in the database with new information.
     * @param updatedAppointment the Appointment object containing updated information.
     */
    public void updateAppointmentInDatabase(Appointment updatedAppointment) {
        try {
            JDBC.openConnection();
            Connection conn = JDBC.connection;
            String updateQuery = "UPDATE appointments SET Title = ?, Description = ?, Contact_ID = ?, Type = ?, Start = ?, End = ?, Customer_ID = ?, User_ID = ?, Location = ? WHERE Appointment_ID = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(updateQuery);

            preparedStatement.setString(1, updatedAppointment.getTitle());
            preparedStatement.setString(2, updatedAppointment.getDescription());
            preparedStatement.setInt(3, updatedAppointment.getContactId());
            preparedStatement.setString(4, updatedAppointment.getType());
            preparedStatement.setString(5, updatedAppointment.getStartDate());
            preparedStatement.setString(6, updatedAppointment.getEndDate());
            preparedStatement.setInt(7, updatedAppointment.getCustomerId());
            preparedStatement.setInt(8, updatedAppointment.getUserId());
            preparedStatement.setString(9, updatedAppointment.getLocation());
            preparedStatement.setInt(10, updatedAppointment.getAppointmentId());

            preparedStatement.executeUpdate();
            JDBC.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks for any upcoming appointments within the next 15 minutes and displays
     * an alert to inform the user.
     */
    private void checkUpcomingAppointments() {
        ObservableList<Appointment> appointments = getAppointments();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusMinutes(15);

        boolean appointmentSoon = false;
        int appointmentId = -1;

        for (Appointment appointment : appointments) {
            LocalDateTime appointmentStartTime = LocalDateTime.parse(appointment.getStartDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            if (now.isBefore(appointmentStartTime) && threshold.isAfter(appointmentStartTime)) {
                appointmentSoon = true;
                appointmentId = appointment.getAppointmentId();
                break;
            }
        }

        ResourceBundle resourceBundle = ResourceBundle.getBundle("com.example.appointmentapp.translations", Locale.getDefault());

        if (appointmentSoon) {
            String message = String.format(resourceBundle.getString("AppointmentWithin15Minutes") + " Appointment ID: %d", appointmentId);
            showAlert(message);
        } else {
            showAlert(resourceBundle.getString("NoAppointmentWithin15Minutes"));
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        ResourceBundle resourceBundle = ResourceBundle.getBundle("com.example.appointmentapp.translations", Locale.getDefault());
        alert.setTitle(resourceBundle.getString("UpcomingAppointments"));

        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    /**
     * Initializes the appointmentsController and sets up the TableView columns.
     * @param url the location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resourceBundle the resources used to localize the root object, or null if the root object was not localized.
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
            if (!alertDisplayed) {
                checkUpcomingAppointments();
                alertDisplayed = true;
            }

            System.out.println("Local time zone: " + ZoneId.systemDefault());


    }
}
