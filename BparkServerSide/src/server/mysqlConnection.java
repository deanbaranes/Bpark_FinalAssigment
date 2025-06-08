package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import common.ParkingHistory;
import common.Reservation;
import common.Subscriber;
import common.ActiveParking;

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
     * Checks if a subscriber with the given ID number already exists in the database.
     *
     * @param idNumber The subscriber's ID number.
     * @return true if the subscriber exists, false otherwise.
     */
    public static boolean doesSubscriberExist(String idNumber) {
    	String query = "SELECT 1 FROM subscribers WHERE subscriber_id = ?";

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, idNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
    }
    /**
     * Generates a random subscription code for new subscribers.
     * Format: SUB123456
     *
     * @return A unique subscription code string.
     */
    private static String generateSubscriptionCode() {
        int randomNum = (int)(Math.random() * 1_000_000);
        return "SUB" + String.format("%06d", randomNum);
    }

    /**
     * Registers a new subscriber in the database with the given details.
     *
     * @return true if the insertion was successful, false otherwise.
     */
    public static boolean registerSubscriber(String firstName, String lastName, String idNumber,
            String email, String phone, String vehicleNumber1, String vehicleNumber2,
            String creditCard) {

        String query = "INSERT INTO subscribers (subscriber_id, full_name, email, phone, vehicle_number1, vehicle_number2, subscription_code, credit_card) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = connectToDB();
            stmt = conn.prepareStatement(query);

            String fullName = firstName + " " + lastName;
            String subscriptionCode = generateSubscriptionCode();
            System.out.println(" Registering subscriber: " + idNumber);
            stmt.setString(1, idNumber);
            stmt.setString(2, fullName);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setString(5, vehicleNumber1);

            if (vehicleNumber2 == null || vehicleNumber2.trim().isEmpty()) {
                stmt.setNull(6, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(6, vehicleNumber2);
            }
            stmt.setString(7, subscriptionCode);
            stmt.setString(8, creditCard);

            int rows = stmt.executeUpdate();
            System.out.println(" Rows inserted: " + rows);
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("❌ SQL ERROR: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.err.println("Failed to close statement: " + e.getMessage());
            }

            try {
                if (conn != null) conn.close(); 
            } catch (SQLException e) {
                System.err.println("Failed to close connection: " + e.getMessage());
            }
        }
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

    
    
    /**
     * Retrieves subscriber information from the database using a subscriber ID.
     * This method queries the subscribers table and constructs a
     * Subscriber object with all relevant fields if a matching record is found.
     * If no record is found or an error occurs, the method returns null.
     * @param id The unique ID of the subscriber to retrieve.
     * @return A Subscriber object containing the subscriber’s details,
     * or null if no such subscriber exists or an exception occurred.
     */
    public static Subscriber getSubscriberById(String id) {
        try {
            Connection conn = connectToDB();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM subscribers WHERE subscriber_id = ?");
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Subscriber(
                    rs.getString("subscriber_id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("vehicle_number1"),
                    rs.getString("vehicle_number2"),
                    rs.getString("subscription_code"),
                    rs.getString("notes"),
                    rs.getString("credit_card")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Updates email and phone number of a subscriber in the database.
     *
     * @param id The subscriber ID to update.
     * @param email The new email address.
     * @param phone The new phone number.
     * @return true if the update succeeded, false otherwise.
     */
    public static boolean updateSubscriberContactInfo(String id, String email, String phone) {
        try {
            Connection conn = connectToDB();
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE subscribers SET email = ?, phone = ? WHERE subscriber_id = ?"
            );
            stmt.setString(1, email);
            stmt.setString(2, phone);
            stmt.setString(3, id);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Retrieves the parking history records for a specific subscriber.
     * This method queries the parking_history table in the database
     * using the provided subscriber ID, and constructs a list of ParkingHistory
     * objects representing the subscriber's past parking sessions.
     * @param subscriberId The ID of the subscriber whose history is to be retrieved.
     * @return A list of ParkingHistory objects for the given subscriber.
     * If no records are found or an error occurs, an empty list is returned.
     */
    public static List<ParkingHistory> getHistoryForSubscriber(String subscriberId) {
        List<ParkingHistory> historyList = new ArrayList<>();
        String query = "SELECT * FROM parking_history WHERE subscriber_id = ?";

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, subscriberId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // שימוש ב־getString ואז המרה ידנית (כדי לא "לסבול" מהמרת אזור זמן)
                LocalDate entryDate = LocalDate.parse(rs.getString("entry_date"));
                LocalTime entryTime = LocalTime.parse(rs.getString("entry_time"));
                LocalDate exitDate = LocalDate.parse(rs.getString("exit_date"));
                LocalTime exitTime = LocalTime.parse(rs.getString("exit_time"));

                ParkingHistory record = new ParkingHistory(
                    rs.getInt("history_id"),
                    rs.getString("subscriber_id"),
                    rs.getString("vehicle_number"),
                    entryDate,
                    entryTime,
                    exitDate,
                    exitTime
                );

                historyList.add(record);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historyList;
    }


    /**
     * Retrieves all existing reservations for a specific subscriber.
     * This method queries the reservations table in the database
     * using the given subscriber ID and constructs a list of Reservation
     * objects that represent each scheduled parking reservation.
     * Dates and times are parsed explicitly to avoid time zone conversion issues.
     * @param subscriberId The ID of the subscriber whose reservations are requested.
     * @return A list of Reservation objects for the specified subscriber.
     * If the subscriber has no reservations or an error occurs, an empty list is returned.
     */
    public static List<Reservation> getReservationsForSubscriber(String subscriberId) {
        List<Reservation> reservations = new ArrayList<>();
        String query = "SELECT * FROM reservations WHERE subscriber_id = ?";

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, subscriberId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // קריאה כ־String ולאחר מכן המרה — כדי להימנע מהשפעת TimeZone של JVM
                LocalDate entryDate = LocalDate.parse(rs.getString("entry_date"));
                LocalTime entryTime = LocalTime.parse(rs.getString("entry_time"));
                LocalDate exitDate = LocalDate.parse(rs.getString("exit_date"));
                LocalTime exitTime = LocalTime.parse(rs.getString("exit_time"));

                Reservation r = new Reservation(
                    rs.getInt("reservation_id"),
                    rs.getString("subscriber_id"),
                    rs.getString("parking_code"),
                    entryDate,
                    entryTime,
                    exitDate,
                    exitTime,
                    rs.getInt("parking_spot")
                );

                reservations.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reservations;
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

    public static String checkLoginManagement(String username, String password) {
        String query = "SELECT role FROM employees WHERE username = ? AND password = ?";

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role").toLowerCase(); // מחזיר "manager" או "attendant"
            } else {
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
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
    /**
     * Returns the total number of parking spots in the system.
     * @return total count of all parking spots
     */
    public static int getTotalParkingSpots() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM parking_spots";

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Returns the number of available (free) parking spots.
     * @return count of available spots
     */
    public static int getAvailableSpotsCount() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM parking_spots WHERE status = 'available'";

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Finds the number of the first available parking spot.
     * @return spot_number or throws an error if none found
     */
    public static int findAvailableSpot() {
        String query = "SELECT spot_number FROM parking_spots WHERE status = 'available' LIMIT 1";

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("spot_number");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("No available parking spots found.");
    }

    /**
     * Reserves a spot by updating its status and inserts a new reservation.
     * @param subscriberId the subscriber making the reservation
     * @param code the generated reservation code
     * @param entryDate the reservation start date
     * @param entryTime the reservation start time
     * @param exitDate the reservation end date
     * @param exitTime the reservation end time
     * @param spotNumber the parking spot to reserve
     */
    public static void insertReservationAndUpdateSpot(String subscriberId, String code,
                                                      LocalDate entryDate, LocalTime entryTime,
                                                      LocalDate exitDate, LocalTime exitTime,
                                                      int spotNumber) {
        try (Connection conn = connectToDB()) {
            conn.setAutoCommit(false);

            // Update parking spot to 'reserved'
            try (PreparedStatement update = conn.prepareStatement(
                    "UPDATE parking_spots SET status = 'reserved' WHERE spot_number = ?")) {
                update.setInt(1, spotNumber);
                update.executeUpdate();
            }

            // Insert reservation record
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO reservations (subscriber_id, parking_code, entry_date, entry_time, exit_date, exit_time, parking_spot) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                insert.setString(1, subscriberId);
                insert.setString(2, code);
                insert.setDate(3, java.sql.Date.valueOf(entryDate));
                insert.setTime(4, java.sql.Time.valueOf(entryTime));
                insert.setDate(5, java.sql.Date.valueOf(exitDate));
                insert.setTime(6, java.sql.Time.valueOf(exitTime));
                insert.setInt(7, spotNumber);
                insert.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * Searches for active parking records by subscriber ID.
     *
     * Retrieves all active parking records from the database where the given subscriber ID
     * matches and the expected exit date is today or later.
     *
     * @param subscriberId The subscriber ID to search for.
     * @return A list of ActiveParking records matching the search criteria.
     */
    public static List<ActiveParking> searchActiveParkingByMemberId(int subscriberId) {
        List<ActiveParking> result = new ArrayList<>();
        String query = "SELECT * FROM active_parkings WHERE subscriber_id = ? AND expected_exit_date >= CURDATE()";

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, subscriberId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                result.add(new ActiveParking(
                    rs.getInt("parking_code"),
                    rs.getInt("subscriber_id"),
                    rs.getString("entry_date"),
                    rs.getString("entry_time"),
                    rs.getString("expected_exit_date"),
                    rs.getString("expected_exit_time"),
                    rs.getString("parking_spot"),
                    rs.getBoolean("extended")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
    /**
     * Searches for active parking records by parking spot.
     *
     * Retrieves all active parking records from the database where the parking spot matches
     * the provided value and the expected exit date is today or later.
     *
     * @param spot The parking spot identifier to search for.
     * @return A list of ActiveParking records matching the search criteria.
     */
    public static List<ActiveParking> searchActiveParkingBySpot(String spot) {
        List<ActiveParking> result = new ArrayList<>();
        String query = "SELECT * FROM active_parkings WHERE parking_spot = ? AND expected_exit_date >= CURDATE()";

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, spot);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                result.add(new ActiveParking(
                    rs.getInt("parking_code"),
                    rs.getInt("subscriber_id"),
                    rs.getString("entry_date"),
                    rs.getString("entry_time"),
                    rs.getString("expected_exit_date"),
                    rs.getString("expected_exit_time"),
                    rs.getString("parking_spot"),
                    rs.getBoolean("extended")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }


}
