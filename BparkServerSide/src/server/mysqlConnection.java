package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * mysqlConnection provides methods to connect to a MySQL database and perform
 * operations related to order management, such as retrieving and updating orders.
 */
public class mysqlConnection {

    public static Connection conn;

    /**
     * Establishes a connection to the MySQL database.
     * @return The database connection object.
     */
    public static Connection connectToDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver definition succeed");
        } catch (Exception ex) {
            System.out.println("Driver definition failed");
        }

        try {
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/bpark?serverTimezone=IST&useSSL=false",
                "root", "Carmel2025!");
            System.out.println("SQL connection succeed");
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        return conn;
    }

    /**
     * Retrieves all orders from the database and formats them as a string.
     * @return A formatted string of all orders.
     */
    public static String getAllOrders() {
        List<String> orders = new ArrayList<>();
        StringBuilder formattedOrders = new StringBuilder();
        formattedOrders.append("=====ORDER LIST=====\n");
        String query = "SELECT * FROM orders";

        try (Connection conn = connectToDB();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String order = "Order# " + rs.getInt("order_number") +
                        " | Parking: " + rs.getInt("parking_space") +
                        " | Date: " + rs.getString("order_date") +
                        " | Confirmation code: " + rs.getInt("confirmation_code") +
                        " | Subscriber: " + rs.getInt("subscriber_id") +
                        " | Placed: " + rs.getString("date_of_placing_an_order");
                orders.add(order);
            }
        } catch (SQLException e) {
            orders.add("Error retrieving orders: " + e.getMessage());
        }

        for (String order : orders) {
            formattedOrders.append("[").append(order).append("]\n");
        }
        return formattedOrders.toString();
    }

    /**
     * Updates a specific field in an order.
     * @param orderId The ID of the order to update.
     * @param field The field to update ("parking_space" or "order_date").
     * @param newValue The new value for the field.
     * @return true if the update was successful, false otherwise.
     */
    public static boolean updateOrderField(String orderId, String field, String newValue) {
        if (!field.equals("parking_space") && !field.equals("order_date")) {
            System.err.println("Invalid field name: " + field);
            return false;
        }

        String sql = "UPDATE orders SET " + field + " = ? WHERE order_number = ?";
        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newValue);
            stmt.setString(2, orderId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Update failed: " + e.getMessage());
            return false;
        }
    }
    /**
     * Retrieves subscriber information from the database based on the given subscriber ID.
     * Builds a formatted string containing all relevant subscriber fields if found.
     * If the subscriber does not exist, returns a message indicating so.
     * In case of a database access error, returns an error message and logs the stack trace.
     *
     * @param subscriberId The ID of the subscriber to look up.
     * @return A formatted string with subscriber details or an error message.
     */

    public static String getSubscriberInfo(String subscriberId) {
        StringBuilder sb = new StringBuilder();
        String query = "SELECT * FROM subscribers WHERE subscriber_id = ?";

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, subscriberId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                sb.append("Subscriber ID: ").append(rs.getString("subscriber_id")).append("\n");
                sb.append("Full Name: ").append(rs.getString("full_name")).append("\n");
                sb.append("Email: ").append(rs.getString("email")).append("\n");
                sb.append("Phone: ").append(rs.getString("phone")).append("\n");
                sb.append("Vehicle #1: ").append(rs.getString("vehicle_number1")).append("\n");
                sb.append("Vehicle #2: ").append(rs.getString("vehicle_number2")).append("\n");
                sb.append("Subscription Code: ").append(rs.getString("subscription_code")).append("\n");
                sb.append("Notes: ").append(rs.getString("notes")).append("\n");
                sb.append("Credit Card: ").append(rs.getString("credit_card")).append("\n");
            } else {
                sb.append("Subscriber not found.");
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            sb.append("Error accessing the database.");
        }

        return sb.toString();
    }

    /*
     * Checks if a subscriber with the provided full name and subscription code exists in the database.
     *
     * @param fullName The full name of the subscriber.
     * @param code The subscription code associated with the subscriber.
     * @return true if a matching subscriber is found, false otherwise.
     */
    public static boolean checkLogin(String ID, String code) 
    {
        String query = "SELECT * FROM subscribers WHERE subscriber_id = ? AND subscription_code = ?";

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query)) 
        {

            stmt.setString(1, ID);
            stmt.setString(2, code);

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
            return false;
        }
    }
    
    /*

    * Validates login credentials for a management user by checking the 'employees' table.
    *
    * @param username The username entered by the manager.
    * @param password The password entered by the manager.
    * @return true if credentials are valid, false otherwise.
      */

    public static boolean checkLoginManagement(String username, String password) 
    {
        String query = "SELECT * FROM employees WHERE username = ? AND password = ?";

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query)) 
        {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
            return false;
        }
    }
    /*
     * Retrieves all parking spots marked as 'available' from the database.
     *
     * @return A list of strings describing each available parking spot.
     */

    public static List<String> getAvailableSpots() {
        List<String> availableSpots = new ArrayList<>();
        String query = "SELECT spot_number FROM parking_spots WHERE status = 'available'";

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String spot = "Parking Spot #" + rs.getInt("spot_number") + " is available.";
                availableSpots.add(spot);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return availableSpots;
    }


}
