package com.example.appointmentapp.Controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Predicate;

/** Controller class for the login scene*/
public class loginController implements Initializable {

    /** UI Component */
    public TextField userTextField;
    public TextField passTextField;
    public Label timeLabel;

    private ResourceBundle resourceBundle;


    /** Updates the time label every second. */
    private void UpdateTime() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            ZonedDateTime currentTime = ZonedDateTime.now();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
            DateTimeFormatter zoneFormatter = DateTimeFormatter.ofPattern("VV");

            String formattedTime = currentTime.format((timeFormatter));
            String formattedZone = currentTime.getZone().getId();

            String localTimeKey = resourceBundle.getString("LocalTime");
            timeLabel.setText(localTimeKey + ": " + formattedTime + " (" + formattedZone + ")");
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Authenticates a user based on the provided username and password.
     * @param username the username to be authenticated.
     * @param password the password to be authenticated.
     * @return true if the user is authenticated, false otherwise.
     */
    private boolean authenticate(String username, String password) {
        return "test".equals(username) && "test".equals(password);
    }

    /**
     * Handles the login process when the user clicks the login button.
     * It uses two lambda expressions to initialize instances of the Predicate interface
     * for username and password validation.
     * The lambda expressions make the code more concise and easier to read
     * @param event the ActionEvent that triggers the method.
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        String username = userTextField.getText();
        String password = passTextField.getText();

        Predicate<String> isUsernameValid = user -> "test".equals(user);
        Predicate<String> isPasswordValid = pass -> "test".equals(pass);

        boolean isAuthenticated = isUsernameValid.test(username) && isPasswordValid.test(password);

        logLoginAttempt(username, isAuthenticated);

        if (isAuthenticated) {
            try {
                Parent mainSceneParent = FXMLLoader.load(getClass().getResource("/com/example/appointmentapp/appointments.fxml"));
                Scene mainScene = new Scene(mainSceneParent);
                Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();

                window.setScene(mainScene);
                window.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(resourceBundle.getString("LoginError"));
            alert.setHeaderText(resourceBundle.getString("WrongUsernameAndPassword"));
            alert.setContentText(resourceBundle.getString("Invalid"));
            alert.showAndWait();
        }
    }

    /**
     * Logs the login attempt with the provided username and the success status.
     * @param username the username of the user who attempted to log in.
     * @param success true if the login attempt was successful, false otherwise.
     */
    private void logLoginAttempt(String username, boolean success) {
        try {
            String fileName = "login_activity.txt";
            Path filePath = Paths.get(fileName);
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            String timeStamp = now.format(formatter);

            String loginStatus = success ? "Success" : "Failure";
            String logEntry = String.format("%s - %s - %s - %s%n", timeStamp, username, loginStatus, success);

            Files.write(filePath, logEntry.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println(" Unable to Log " + e.getMessage());
        }
    }

    /**
     * Initializes the login view and updates the time label.
     * @param url the URL used to resolve relative paths for the root object, or null if the location is not known.
     * @param rb the resource bundle containing localized for the current locale, or null if none is provided.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        resourceBundle = ResourceBundle.getBundle("com.example.appointmentapp.translations", Locale.getDefault());
        UpdateTime();


    }
}