module com.example.appointmentapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.appointmentapp to javafx.fxml;
    opens com.example.appointmentapp.Model to javafx.base;
    exports com.example.appointmentapp.Controllers;
    opens com.example.appointmentapp.Controllers to javafx.fxml;
    exports com.example.appointmentapp.Main;
    opens com.example.appointmentapp.Main to javafx.fxml;
}