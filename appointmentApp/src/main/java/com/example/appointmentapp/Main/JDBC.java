package com.example.appointmentapp.Main;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * The JDBC abstract class provides methods to establish and close connections to the database.
 * It contains the configurations for connecting to the database.
 */
public abstract class JDBC {
    private static final String protocol = "jdbc";
    private static final String vendor = ":mysql:";
    private static final String location = "//localhost/";
    private static final String databaseName = "client_schedule";
    private static final String jdbcUrl = protocol + vendor + location + databaseName + "?connectionTimeZone = SERVER";
    private static final String driver = "com.mysql.cj.jdbc.Driver";
    private static final String userName = "sqlUser";
    private static String password = "Passw0rd!";
    public static Connection connection;

    /**
     * Opens a connection to the database.
     * Prints a message to the console upon successful connection.
     */
    public static void openConnection()
    {
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(jdbcUrl, userName, password); // Reference Connection object
            System.out.println("Connected to Database");
        }
        catch(Exception e)
        {
            System.out.println("Error:" + e.getMessage());
        }
    }

    /**
     * Closes the database connection.
     * Prints a message to the console upon successful closure of the connection.
     */
    public static void closeConnection() {
        try {
            connection.close();
            System.out.println("Disconnected");
        }
        catch(Exception e)
        {
            System.out.println("Error:" + e.getMessage());
        }
    }
}
