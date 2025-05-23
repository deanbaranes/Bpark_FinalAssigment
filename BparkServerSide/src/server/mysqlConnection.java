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
                "root", "Daniel2204");
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
     * Checks whether an order with the given ID exists in the database.
     * @param orderId The order ID to check.
     * @return true if the order exists, false otherwise.
     */
    public static boolean doesOrderExist(int orderId) {
        boolean exists = false;

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM orders WHERE order_number = ?")) {

            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            exists = rs.next();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return exists;
    }
}
