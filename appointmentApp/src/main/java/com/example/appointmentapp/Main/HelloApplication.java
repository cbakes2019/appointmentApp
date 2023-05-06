package com.example.appointmentapp.Main;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.ZoneId;
import java.util.*;

/**
 * The HelloApplication class is the main entry point for the Java Appointment App.
 * Provides the start and stop methods for the application lifecycle.
 */
public class HelloApplication extends Application {


    /**
     * Starts the application and displays the login scene.
     * @param stage the primary stage for the application.
     * @throws IOException if there is an error loading the view.
     */
    @Override
    public void start(Stage stage) throws IOException {


        Locale locale = Locale.getDefault();
        ResourceBundle bundle = ResourceBundle.getBundle("com.example.appointmentapp.translations", locale);

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/example/appointmentapp/log-in.fxml"), bundle);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Java Appointment App");
        stage.setScene(scene);
        stage.show();

    }




    /**
     * Stops the application and closes the database connection if it exists.
     * @throws Exception if there is an error closing the database connection.
     */
    @Override
    public void stop() throws Exception {
        if (JDBC.connection != null) {
            JDBC.closeConnection();
        }
        super.stop();
    }
    /**
     * The main method that launches the application.
     */
    public static void main(String[] args) {

        launch(args);
    }

}