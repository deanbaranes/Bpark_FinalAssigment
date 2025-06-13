package server;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        		    "jdbc:mysql://localhost:3306/bpark?serverTimezone=Asia/Jerusalem&useSSL=false",
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
     * @return A unique subscription code string..
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
            String email, String phone, String vehicleNumber1,
            String creditCard) {

        String query = "INSERT INTO subscribers (subscriber_id, full_name, email, phone, vehicle_number1,subscription_code, credit_card) VALUES (?, ?, ?, ?, ?, ?, ?)";

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
            stmt.setString(6, subscriptionCode);
            stmt.setString(7, creditCard);
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
                sb.append("Subscription Code: ").append(rs.getString("subscription_code")).append("\n");
                sb.append("late_count: ").append(rs.getString("late_count")).append("\n");
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
                    rs.getString("subscription_code"),
                    rs.getInt("late_count"),
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

    /**
     * Checks whether a reservation exists in the database based on the provided parking code.
     *
     * @param parkingCode The parking code to check.
     * @return true if a reservation with the given code exists, false otherwise.
     */
    public static boolean doesReservationCodeExist(String parkingCode) {
        String query = "SELECT 1 FROM reservations WHERE parking_code = ?";

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, parkingCode);
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next();
            if (!exists)
            {
            	return rs.next();
            }
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    /*
     * Checks if a subscriber with the provided full name and subscription code exists in the database.
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
        System.out.println(count); 
        return count;
    }

    /**
     * Finds the number of the first available parking spot.
     * @return spot_number or throws an error if none found
     */
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
                int spot = rs.getInt("spot_number");
                return spot;
            } else {
                System.out.println(" No rows returned from query.");
            }

        } catch (SQLException e) {
            System.out.println("SQL Exception while searching for available spot:");
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
        String query = "SELECT * FROM active_parkings WHERE subscriber_id = ? ";

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
        String query = "SELECT * FROM active_parkings WHERE parking_spot = ? ";

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
    
    
    /**
     * Extends the expected exit time of an active parking session by 4 hours.
     * Updates the 'extended' flag to 1 in the database.
     * @param ap The ActiveParking object representing the active session to be extended.
     * @return true if the update succeeded, false otherwise.
     */
    public static boolean extendParkingTime(ActiveParking ap) {
        String query = "UPDATE active_parkings SET expected_exit_time = ?, extended = 1 WHERE parking_code = ?";
        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            LocalTime currentExit = LocalTime.parse(ap.getExpectedExitTime());
            LocalTime newExit = currentExit.plusHours(4);

            stmt.setString(1, newExit.toString());
            stmt.setInt(2, ap.getParkingCode());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ap.setExtended(true);
                ap.setExpectedExitTime(newExit.toString());
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    
    
    /**
     * Checks whether a reservation already exists in the database for a given subscriber ID,
     * entry date, and entry time.
     *
     * This method is used to prevent duplicate reservations at the exact same date and time
     * for the same subscriber. It queries the "reservations" table and returns true if such
     * a reservation exists.
     * @param subscriberId The ID of the subscriber attempting to create a reservation.
     * @param entryDate    The date of the requested reservation entry.
     * @param entryTime    The time of the requested reservation entry.
     * @return true if a reservation already exists for the given subscriber at the specified date and time, false otherwise.
     */
    public static boolean reservationExists(String subscriberId, LocalDate entryDate, LocalTime entryTime) {
        try (Connection conn = connectToDB()) {
            String query = "SELECT COUNT(*) FROM reservations WHERE subscriber_id = ? AND entry_date = ? AND entry_time = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, subscriberId);
                stmt.setDate(2, java.sql.Date.valueOf(entryDate));
                stmt.setTime(3, java.sql.Time.valueOf(entryTime));
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Creates a new active parking entry for a subscriber who arrives without a prior reservation.
     * 
     * This method performs the following steps:
     * - Checks if the subscriber already has an active parking record.
     * - Searches for the first available parking spot.
     * - Generates a unique parking code based on subscriber ID and parking spot.
     * - Ensures the generated parking code does not collide with existing codes.
     * - Calculates entry and expected exit times (current time + 4 hours).
     * - Inserts a new record into the active_parkings table.
     * - Updates the parking_spots table to mark the selected spot as occupied.
     *
     * @param subscriber The subscriber attempting to create a new parking entry.
     * @return 
     *    - "CAR_ALREADY_PARKED" if the subscriber already has an active parking session.
     *    - "NO_SPOTS_AVAILABLE" if there are no available parking spots.
     *    - "SUCSESSFUL_PARKING" followed by the generated parking code if insertion succeeded.
     *    - "ERROR" if an exception occurred during processing.
     */
    public static String createNewActiveParking(Subscriber subscriber) {
        List<String> existingCodes = new ArrayList<>();
        String query1 = "SELECT spot_number FROM parking_spots WHERE status = 'available' ORDER BY spot_number ASC LIMIT 1";
        String query2 = "SELECT parking_code FROM active_parkings";
        String query3 = "SELECT 1 FROM active_parkings WHERE subscriber_id = ?";

        try (Connection conn = connectToDB()) 
        {
        	try (PreparedStatement stmt = conn.prepareStatement(query3)) 
        	{
        	    stmt.setString(1, subscriber.getSubscriber_id());
        	    ResultSet rs = stmt.executeQuery();
        	    if (rs.next()) {
        	    	return "CAR_ALREADY_PARKED";
        	    } 
        	    else 
        	    {
        	    	try (PreparedStatement stmt1 = conn.prepareStatement(query1)) 
                    {
                        ResultSet parkingSpots = stmt1.executeQuery();
                        if (!parkingSpots.next()) {
                            return "NO_SPOTS_AVAILABLE";
                        } else {
                            int parkingSpot = parkingSpots.getInt("spot_number");
                            String newParkingCode = generateUniqueParkingCode(subscriber.getSubscriber_id(), parkingSpot);
                            try (PreparedStatement stmt2 = conn.prepareStatement(query2)) {
                                ResultSet activeCodes = stmt2.executeQuery();
                                while (activeCodes.next()) {
                                    String parkingCode = activeCodes.getString("parking_code");
                                    existingCodes.add(parkingCode);
                                }
                               

                                LocalDate entryDate = LocalDate.now();
                                LocalTime entryTime = LocalTime.now();

                                LocalDateTime entryDateTime = LocalDateTime.of(entryDate, entryTime);
                                LocalDateTime expectedExitDateTime = entryDateTime.plusHours(4);

                                LocalDate expectedExitDate = expectedExitDateTime.toLocalDate();
                                LocalTime expectedExitTime = expectedExitDateTime.toLocalTime();

                                String updateSpotStatusQuery = "UPDATE parking_spots SET status = 'occupied' WHERE spot_number = ?";
                                String insertQuery = "INSERT INTO active_parkings " +
                                        "(parking_code, subscriber_id, entry_date, entry_time, expected_exit_date, expected_exit_time, parking_spot, extended) " +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

                                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                                    insertStmt.setString(1, newParkingCode);
                                    insertStmt.setString(2, subscriber.getSubscriber_id());
                                    insertStmt.setDate(3, Date.valueOf(LocalDate.now()));
                                    insertStmt.setTime(4, Time.valueOf(LocalTime.now()));
                                    insertStmt.setDate(5, Date.valueOf(expectedExitDate));
                                    insertStmt.setTime(6, Time.valueOf(expectedExitTime));
                                    insertStmt.setInt(7, parkingSpot);
                                    insertStmt.setInt(8, 0);
                                    insertStmt.executeUpdate();
                                    
                                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSpotStatusQuery))
                                    {
                                    updateStmt.setInt(1, parkingSpot);
                                    updateStmt.executeUpdate();
                                    }

                                    return "SUCSESSFUL_PARKING" + newParkingCode;
                                } catch (SQLException e) {
                                    System.out.println("error in inserting new data to active_parkings");
                                    e.printStackTrace();
                                } 
                            }
                        }
                    }
        	    }
        	}

            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

    
    /**
     * Generates a unique parking code for a new active parking entry.
     * 
     * The generated code is built from:
     * - The subscriber ID.
     * - The selected parking spot number.
     * - A random component to help avoid collisions.
     * - A hash function is used to mix the input values into a consistent 4-digit code.
     * 
     * Final format: "BPARKxxxx" where xxxx is a 4-digit number.
     *
     * @param subscriberId The ID of the subscriber requesting the parking.
     * @param parkingSpot The allocated parking spot number.
     * @return A formatted parking code string in the form "BPARKxxxx".
     */
    public static String generateParkingCode(String subscriberId, int parkingSpot) {
        String baseString = subscriberId + parkingSpot;
        int randomPart = new Random().nextInt(10000); 
        String mixedString = baseString + randomPart;
        int rawHash = mixedString.hashCode();
        rawHash = Math.abs(rawHash);
        int fourDigits = rawHash % 10000;
        return String.format("BPARK%04d", fourDigits);
    }
    /**
     * Generates a unique parking code by combining subscriber ID and parking spot number,
     * ensuring the generated code does not already exist in either the reservations
     * or active_parkings tables.
     *
     * The method repeatedly calls generateParkingCode and verifies uniqueness by checking
     * against both reservations and active parkings until a valid, unused code is created.
     *
     * @param subscriberId The ID of the subscriber requesting the reservation or parking.
     * @param parkingSpot The number of the allocated parking spot.
     * @return A unique parking code in the format "BPARKxxxx".
     */
    public static String generateUniqueParkingCode(String subscriberId, int parkingSpot) {
        String code;
        do {
            code = generateParkingCode(subscriberId, parkingSpot);
        } while (reservationCodeExists(code) || activeParkingCodeExists(code));
        return code;
    }
    
    
    /**
     * Checks whether a given parking code already exists in the reservations table.
     *
     * @param code The parking code to check for existence.
     * @return true if the code exists in the reservations table, false otherwise.
     */
    public static boolean reservationCodeExists(String code) {
        String query = "SELECT 1 FROM reservations WHERE parking_code = ?";
        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks whether a given parking code already exists in the active_parkings table.
     *
     * @param code The parking code to check for existence.
     * @return true if the code exists in the active_parkings table, false otherwise.
     */
    public static boolean activeParkingCodeExists(String code) {
        String query = "SELECT 1 FROM active_parkings WHERE parking_code = ?";
        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }





    public static boolean updateReservationDateTime(int reservationId, LocalDate newDate, LocalTime newTime) {
        String query = "UPDATE reservations SET entry_date = ?, entry_time = ?, exit_date = ?, exit_time = ? WHERE reservation_id = ?";

        //calculate exit time
        LocalDateTime entryDateTime = LocalDateTime.of(newDate, newTime);
        LocalDateTime exitDateTime = entryDateTime.plusHours(4);
        LocalDate newExitDate = exitDateTime.toLocalDate();
        LocalTime newExitTime = exitDateTime.toLocalTime();

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, Date.valueOf(newDate));
            stmt.setTime(2, Time.valueOf(newTime));
            stmt.setDate(3, Date.valueOf(newExitDate));
            stmt.setTime(4, Time.valueOf(newExitTime));
            stmt.setInt(5, reservationId);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean cancelReservationById(int reservationId) {
        String deleteQuery = "DELETE FROM reservations WHERE reservation_id = ?";

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {

            stmt.setInt(1, reservationId);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Retrieves all future reservations from the database.
     * A future reservation is one where the entry date and time
     * are after the current system timestamp.
     *
     * @return A list of Reservation objects representing upcoming reservations.
     */
    public static List<Reservation> getFutureReservations() {
        List<Reservation> futureReservations = new ArrayList<>();

        String sql = """
            SELECT reservation_id, subscriber_id, parking_code, entry_date, entry_time,
                   exit_date, exit_time, parking_spot
            FROM reservations
            WHERE TIMESTAMP(entry_date, entry_time) > NOW()
        """;

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Reservation reservation = new Reservation(
                    rs.getInt("reservation_id"),
                    rs.getString("subscriber_id"),
                    rs.getString("parking_code"),
                    rs.getDate("entry_date").toLocalDate(),
                    rs.getTime("entry_time").toLocalTime(),
                    rs.getDate("exit_date").toLocalDate(),
                    rs.getTime("exit_time").toLocalTime(),
                    rs.getInt("parking_spot")
                );
                futureReservations.add(reservation);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return futureReservations;
    }
    /**
     * Retrieves all currently active parking records from the database.
     * Each record includes entry and expected exit details, along with the
     * subscriber and spot information.
     *
     * @return A list of ActiveParking objects representing active parkings.
     */
    public static List<ActiveParking> getActiveParkings() {
        List<ActiveParking> activeList = new ArrayList<>();

        String sql = """
            SELECT parking_code, subscriber_id, entry_date, entry_time,
                   expected_exit_date, expected_exit_time, parking_spot, extended
            FROM active_parkings
        """;

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ActiveParking ap = new ActiveParking(
                    rs.getInt("parking_code"),
                    rs.getInt("subscriber_id"),
                    rs.getString("entry_date"),
                    rs.getString("entry_time"),
                    rs.getString("expected_exit_date"),
                    rs.getString("expected_exit_time"),
                    rs.getString("parking_spot"),
                    rs.getBoolean("extended")
                );
                activeList.add(ap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return activeList;
    }
    /**
     * Retrieves a list of site activity records from the database.
     * Each record includes the action, username, and timestamp of the activity.
     * Results are ordered by timestamp in descending order.
     *
     * @return A list of formatted strings representing site activity events.
     */

   
    /**
     * Retrieves current active parking sessions and upcoming reservations from the database.
     *
     * @return A list of formatted strings representing active parking and future reservation records.
     */
    public static List<String> getSiteActivityData() {
        List<String> result = new ArrayList<>();

        // 1. Active parkings
        String activeSql = "SELECT parking_code, subscriber_id, entry_date, entry_time, expected_exit_date, expected_exit_time, parking_spot, extended " +
                           "FROM active_parkings";

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(activeSql);
             ResultSet rs = stmt.executeQuery()) {

            result.add("=== Active Parkings ===");
            while (rs.next()) {
                String line = String.format("Code: %s | Subscriber: %s | Entry: %s %s | Exit: %s %s | Spot: %d | Extended: %s",
                        rs.getString("parking_code"),
                        rs.getString("subscriber_id"),
                        rs.getDate("entry_date"),
                        rs.getTime("entry_time"),
                        rs.getDate("expected_exit_date"),
                        rs.getTime("expected_exit_time"),
                        rs.getInt("parking_spot"),
                        rs.getBoolean("extended") ? "Yes" : "No"
                );
                result.add(line);
            }

        } catch (SQLException e) {
            result.add("Error retrieving active parking data.");
            e.printStackTrace();
        }

        // 2. Upcoming reservations
        String reservSql = "SELECT reservation_id, subscriber_id, parking_code, entry_date, entry_time, exit_date, exit_time, parking_spot " +
                           "FROM reservations WHERE entry_date > CURDATE() ORDER BY entry_date, entry_time";

        try (Connection conn = connectToDB();
             PreparedStatement stmt = conn.prepareStatement(reservSql);
             ResultSet rs = stmt.executeQuery()) {

            result.add("=== Upcoming Reservations ===");
            while (rs.next()) {
                String line = String.format("Reservation #%d | Subscriber: %s | Parking: %s | From: %s %s | To: %s %s | Spot: %d",
                        rs.getInt("reservation_id"),
                        rs.getString("subscriber_id"),
                        rs.getString("parking_code"),
                        rs.getDate("entry_date"),
                        rs.getTime("entry_time"),
                        rs.getDate("exit_date"),
                        rs.getTime("exit_time"),
                        rs.getInt("parking_spot")
                );
                result.add(line);
            }

        } catch (SQLException e) {
            result.add("Error retrieving reservation data.");
            e.printStackTrace();
        }

        return result;
    }


    
}
