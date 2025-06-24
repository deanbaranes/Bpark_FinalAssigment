package server;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import common.ParkingHistory;
import common.Reservation;
import common.Subscriber;
import common.ActiveParking;
import common.DailySubscriberCount;
import common.EmailSender;
import common.ParkingDurationRecord;



/**
 * This class provides database-related utility methods for managing BPARK system data,
 * such as subscribers, reservations, parking spots, and active parkings.
 * 
 * Instead of directly managing connections in every method, the class uses a helper class
 * called DBExecutor, which encapsulates the connection management logic.
 *
 * How it works:
 * - Each database operation is executed using a lambda expression that receives an open {@code Connection} object.
 * - The DBExecutor.execute(...) method ensures the connection is automatically obtained from the connection pool,
 *   used, and then returned safely.
 * - This eliminates repetitive boilerplate code such as: 
 *   opening a connection, handling exceptions, and closing resources.
 * - The lambda body focuses only on what the query does – not how to manage the connection.
 * Benefits of this approach:
 * - Code is shorter and easier to read.
 * - Reduces the chance of forgetting to close connections.
 * - Promotes cleaner separation of concerns – logic vs. resource management.
 * - Uses Java functional programming to simplify repetitive patterns.
 */
public class mysqlConnection {


	public static Connection connectToDB() {
		return ConnectionPool.getInstance().getConnection();
	}

	/**
	 * Checks if a subscriber with the given ID number already exists in the
	 * database.
	 *
	 * @param idNumber The subscriber's ID number.
	 * @return true if the subscriber exists, false otherwise.
	 */
	public static boolean doesSubscriberExist(String idNumber) {
		return DBExecutor.execute(conn -> {
			String query = "SELECT 1 FROM subscribers WHERE subscriber_id = ?";

			try (PreparedStatement stmt = conn.prepareStatement(query)) {
				// Set the ID number as a parameter in the SQL query
				stmt.setString(1, idNumber);

				// Execute the query – if there's at least one result, the subscriber exists
				ResultSet rs = stmt.executeQuery();
				return rs.next();

			} catch (SQLException e) {
				// Print exception details and return false
				e.printStackTrace();
				return false;
			}
		});
	}
   
	/**
	 * . Generates a random subscription code for new subscribers. Format: SUB123456
	 *
	 * @return A unique subscription code string..
	 */
	private static String generateSubscriptionCode() {
		int randomNum = (int) (Math.random() * 1_000_000);
		return "SUB" + String.format("%06d", randomNum);
	}
    
	/**
	 * Registers a new subscriber in the database with the given details.
	 *
	 * @return true if the insertion was successful, false otherwise.
	 */
	public static boolean registerSubscriber(String firstName, String lastName, String idNumber, String email,
			String phone, String vehicleNumber1, String creditCard) {

		return DBExecutor.execute(conn -> {
			String query = "INSERT INTO subscribers (subscriber_id, full_name, email, phone, vehicle_number1, subscription_code, credit_card) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

			try (PreparedStatement stmt = conn.prepareStatement(query)) {

				String fullName = firstName + " " + lastName; // Combine first and last name
				String subscriptionCode = generateSubscriptionCode(); // Generate a random subscription code

				System.out.println("Registering subscriber: " + idNumber);

				// Set all query parameters
				stmt.setString(1, idNumber);
				stmt.setString(2, fullName);
				stmt.setString(3, email);
				stmt.setString(4, phone);
				stmt.setString(5, vehicleNumber1);
				stmt.setString(6, subscriptionCode);
				stmt.setString(7, creditCard);

				// Execute the INSERT query
				int rows = stmt.executeUpdate();

				System.out.println("Rows inserted: " + rows);

				// Return true if at least one row was inserted
				return rows > 0;

			} catch (SQLException e) {
				// Log any SQL exceptions and return false
				System.out.println("SQL ERROR: " + e.getMessage());
				e.printStackTrace();
				return false;
			}
		});
	}

	/**
	 * Retrieves subscriber information from the database based on the given
	 * subscriber ID. Builds a formatted string containing all relevant subscriber
	 * fields if found. If the subscriber does not exist, returns a message
	 * indicating so. In case of a database access error, returns an error message
	 * and logs the stack trace.
	 *
	 * @param subscriberId The ID of the subscriber to look up.
	 * @return A formatted string with subscriber details or an error message.
	 */
	public static String getSubscriberInfo(String subscriberId) {
	    return DBExecutor.execute(conn -> {
	        StringBuilder sb = new StringBuilder();
	        String query = "SELECT * FROM subscribers WHERE subscriber_id = ?";

	        try (PreparedStatement stmt = conn.prepareStatement(query)) {
	            // Set the ID parameter in the SQL query
	            stmt.setString(1, subscriberId);

	            // Execute the query
	            ResultSet rs = stmt.executeQuery();

	            if (rs.next()) {
	                // Append subscriber details line by line
	                sb.append("Subscriber ID: ").append(rs.getString("subscriber_id")).append("\n");
	                sb.append("Full Name: ").append(rs.getString("full_name")).append("\n");
	                sb.append("Email: ").append(rs.getString("email")).append("\n");
	                sb.append("Phone: ").append(rs.getString("phone")).append("\n");
	                sb.append("Vehicle #1: ").append(rs.getString("vehicle_number1")).append("\n");
	                sb.append("Subscription Code: ").append(rs.getString("subscription_code")).append("\n");
	                sb.append("Late Count: ").append(rs.getString("late_count")).append("\n");
	                sb.append("Credit Card: ").append(rs.getString("credit_card")).append("\n");
	            } else {
	                // If no subscriber found with the given ID
	                sb.append("Subscriber not found.");
	            }

	            rs.close(); // Close the ResultSet explicitly

	        } catch (SQLException e) {
	            // Log the error and return a general failure message
	            e.printStackTrace();
	            sb.append("Error accessing the database.");
	        }

	        // Return the final constructed message
	        return sb.toString();
	    });
	}

	/**
	 * Retrieves subscriber information from the database using a subscriber ID.
	 * This method queries the subscribers table and constructs a Subscriber object
	 * with all relevant fields if a matching record is found. If no record is found
	 * or an error occurs, the method returns null.
	 * 
	 * @param id The unique ID of the subscriber to retrieve.
	 * @return A Subscriber object containing the subscriber’s details, or null if
	 *         no such subscriber exists or an exception occurred.
	 */
	public static Subscriber getSubscriberById(String id) {
	    return DBExecutor.execute(conn -> {
	        String query = "SELECT * FROM subscribers WHERE subscriber_id = ?";
	        try (PreparedStatement stmt = conn.prepareStatement(query)) {
	            // Set the subscriber ID in the prepared statement
	            stmt.setString(1, id);
	            // Execute the SELECT query
	            ResultSet rs = stmt.executeQuery();
	            if (rs.next()) {
	                // If a subscriber is found, construct and return a Subscriber object
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
	            rs.close(); // Explicitly close ResultSet
	        } catch (SQLException e) {
	            // Log the SQL error if it occurs
	            e.printStackTrace();
	        }
	        // Return null if subscriber not found or error occurred
	        return null;
	    });
	}

	/**
	 * Updates email and phone number of a subscriber in the database.
	 *
	 * @param id    The subscriber ID to update.
	 * @param email The new email address.
	 * @param phone The new phone number.
	 * @return true if the update succeeded, false otherwise.
	 */
	public static boolean updateSubscriberContactInfo(String id, String email, String phone) {
	    return DBExecutor.execute(conn -> {
	        String query = "UPDATE subscribers SET email = ?, phone = ? WHERE subscriber_id = ?";
	        try (PreparedStatement stmt = conn.prepareStatement(query)) {
	            // Set the parameters: email, phone, and subscriber ID
	            stmt.setString(1, email);
	            stmt.setString(2, phone);
	            stmt.setString(3, id);
	            // Execute the UPDATE query
	            int rows = stmt.executeUpdate();
	            // Return true if at least one row was updated
	            return rows > 0;
	        } catch (SQLException e) {
	            // Print the error and return false in case of failure
	            e.printStackTrace();
	            return false;
	        }
	    });
	}

	/**
	 * Checks if the given subscriber ID has an active parking record.
	 *
	 * @param subscriberId The ID of the subscriber to check.
	 * @return "HAS ACTIVE PARKING" if found, otherwise "NO ACTIVE PARKING"
	 * @throws SQLException if a database access error occurs
	 */
	/**
	 * Checks if the given subscriber ID has an active parking record.
	 *
	 * @param subscriberId The ID of the subscriber to check.
	 * @return "HAS ACTIVE PARKING" if found, otherwise "NO ACTIVE PARKING"
	 */
	public static String isSubscriberInActiveParking(String subscriberId) {
	    return DBExecutor.execute(conn -> {
	        String sql = "SELECT 1 FROM active_parkings WHERE subscriber_id = ? LIMIT 1";

	        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	            // Bind the subscriber ID to the query
	            stmt.setString(1, subscriberId);

	            // Execute the query and check if any result exists
	            try (ResultSet rs = stmt.executeQuery()) {
	                if (rs.next()) {
	                    return "HAS ACTIVE PARKING";
	                }
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return "NO ACTIVE PARKING";
	    });
	}




	/**
	 * Retrieves the parking history records for a specific subscriber. This method
	 * queries the parking_history table in the database using the provided
	 * subscriber ID, and constructs a list of ParkingHistory objects representing
	 * the subscriber's past parking sessions.
	 * 
	 * @param subscriberId The ID of the subscriber whose history is to be
	 *                     retrieved.
	 * @return A list of ParkingHistory objects for the given subscriber. If no
	 *         records are found or an error occurs, an empty list is returned.
	 */
	public static List<ParkingHistory> getHistoryForSubscriber(String subscriberId) {
	    return DBExecutor.execute(conn -> {
	        List<ParkingHistory> historyList = new ArrayList<>();
	        String query = "SELECT * FROM parking_history WHERE subscriber_id = ?";
	        try (PreparedStatement stmt = conn.prepareStatement(query)) {
	            // Set the subscriber ID in the prepared statement
	            stmt.setString(1, subscriberId);
	            // Execute the query
	            ResultSet rs = stmt.executeQuery();
	            while (rs.next()) {
	                // Parse date and time fields from the result set
	                LocalDate entryDate = LocalDate.parse(rs.getString("entry_date"));
	                LocalTime entryTime = LocalTime.parse(rs.getString("entry_time"));
	                LocalDate exitDate = LocalDate.parse(rs.getString("exit_date"));
	                LocalTime exitTime = LocalTime.parse(rs.getString("exit_time"));
	                // Create a ParkingHistory object and add it to the list
	                ParkingHistory record = new ParkingHistory(
	                        rs.getInt("history_id"),
	                        rs.getString("subscriber_id"),
	                        rs.getString("vehicle_number"),
	                        entryDate, entryTime,
	                        exitDate, exitTime
	                );
	                historyList.add(record);
	            }
	            rs.close(); // Close ResultSet explicitly
	        } catch (SQLException e) {
	            // Log error and return empty list
	            e.printStackTrace();
	        }
	        // Return the completed list of history records
	        return historyList;
	    });
	}
	/**
	 * Retrieves all existing reservations for a specific subscriber. This method
	 * queries the reservations table in the database using the given subscriber ID
	 * and constructs a list of Reservation objects that represent each scheduled
	 * parking reservation. Dates and times are parsed explicitly to avoid time zone
	 * conversion issues.
	 * 
	 * @param subscriberId The ID of the subscriber whose reservations are
	 *                     requested.
	 * @return A list of Reservation objects for the specified subscriber. If the
	 *         subscriber has no reservations or an error occurs, an empty list is
	 *         returned.
	 */
	public static List<Reservation> getReservationsForSubscriber(String subscriberId) {
	    return DBExecutor.execute(conn -> {
	        List<Reservation> reservations = new ArrayList<>();
	        String query = "SELECT * FROM reservations WHERE subscriber_id = ?";
	        try (PreparedStatement stmt = conn.prepareStatement(query)) {
	            // Set the subscriber ID in the query
	            stmt.setString(1, subscriberId);
	            // Execute the SELECT query
	            ResultSet rs = stmt.executeQuery();
	            while (rs.next()) {
	                // Convert string values to LocalDate and LocalTime
	                LocalDate entryDate = LocalDate.parse(rs.getString("entry_date"));
	                LocalTime entryTime = LocalTime.parse(rs.getString("entry_time"));
	                LocalDate exitDate = LocalDate.parse(rs.getString("exit_date"));
	                LocalTime exitTime = LocalTime.parse(rs.getString("exit_time"));
	                // Create a new Reservation object from the result set
	                Reservation r = new Reservation(
	                    rs.getInt("reservation_id"),
	                    rs.getString("subscriber_id"),
	                    rs.getString("parking_code"),
	                    entryDate, entryTime,
	                    exitDate, exitTime,
	                    rs.getInt("parking_spot")
	                );
	                // Add it to the list
	                reservations.add(r);
	            }
	            rs.close(); // Close the ResultSet after use
	        } catch (SQLException e) {
	            // Log any errors that occur during the database operation
	            e.printStackTrace();
	        }
	        // Return the list of reservations (empty if none found or error occurred)
	        return reservations;
	    });
	}
	
	/**
	 * Processes a pickup request based on parking code.
	 *
	 * This method performs the following steps:
	 * 1. Checks if an active parking exists for the provided parking code.
	 *    - If found, the parking session is finalized, recorded in `parking_history`, and removed from `active_parkings`.
	 *    - Returns "SUCCESS".
	 * 2. If not found in active_parkings, checks if the vehicle was towed (`towed_vehicles`).
	 *    - If found, calculates the late duration in minutes, records it in `parking_history`, removes the entry from `towed_vehicles`, and returns "SENT_TOWED_VEHICLE_MSG".
	 * 3. If not found in either, returns "FATAL_ERROR".
	 *
	 * @param parkingCode The parking code to process.
	 * @return Result string indicating outcome ("SUCCESS", "SENT_TOWED_VEHICLE_MSG", or "FATAL_ERROR").
	 */
	public static String processPickupRequest(String parkingCode) {
	    String query = "SELECT * FROM active_parkings WHERE parking_code = ? FOR UPDATE";
	    int extendedDuration=0;
	    try (Connection conn = connectToDB()) {
	        conn.setAutoCommit(false);

	        try (PreparedStatement stmt = conn.prepareStatement(query)) {
	            stmt.setString(1, parkingCode);
	            ResultSet rs = stmt.executeQuery();

	            if (rs.next()) {
	                String subscriberId = rs.getString("subscriber_id");
	                int parkingSpot = rs.getInt("parking_spot");
	                LocalDate entryDate = rs.getDate("entry_date").toLocalDate();
	                LocalTime entryTime = rs.getTime("entry_time").toLocalTime();
	                LocalDate exitDate = LocalDate.now();
	                LocalTime exitTime = LocalTime.now();
	                String vehicleNumber = null;
	                boolean isExtended = rs.getString("extended").equalsIgnoreCase("1");
	                if (isExtended)
	                {
	                	long minutesTotal = ChronoUnit.MINUTES.between(LocalDateTime.of(entryDate, entryTime),LocalDateTime.of(exitDate, exitTime));
	                	extendedDuration = (int) Math.max(minutesTotal - 240, 0);
	               }
	                try (PreparedStatement vehicleStmt = conn.prepareStatement("SELECT vehicle_number1 FROM subscribers WHERE subscriber_id = ?")) {
	                    vehicleStmt.setString(1, subscriberId);
	                    ResultSet vehicleRs = vehicleStmt.executeQuery();
	                    if (vehicleRs.next()) {
	                        vehicleNumber = vehicleRs.getString("vehicle_number1");
	                    }
	                }

	                String insertHistory = "INSERT INTO parking_history (subscriber_id, vehicle_number, entry_date, entry_time, exit_date, exit_time, parking_spot,extended_duration) VALUES (?, ?, ?, ?, ?, ?, ?,?)";
	                try (PreparedStatement historyStmt = conn.prepareStatement(insertHistory)) {
	                    historyStmt.setString(1, subscriberId);
	                    historyStmt.setString(2, vehicleNumber);
	                    historyStmt.setDate(3, Date.valueOf(entryDate));
	                    historyStmt.setTime(4, Time.valueOf(entryTime));
	                    historyStmt.setDate(5, Date.valueOf(exitDate));
	                    historyStmt.setTime(6, Time.valueOf(exitTime));
	                    historyStmt.setInt(7, parkingSpot);
	                    historyStmt.setInt(8, extendedDuration);
	                    historyStmt.executeUpdate();
	                }

	                try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM active_parkings WHERE parking_code = ?")) {
	                    deleteStmt.setString(1, parkingCode);
	                    deleteStmt.executeUpdate();
	                }

	                updateParkingSpotStatus(parkingSpot, "available");
	                conn.commit();
	                return "SUCCESS";
	            } else {
	                String towedQuery = "SELECT * FROM towed_vehicles WHERE parking_code = ? FOR UPDATE";
	                try (PreparedStatement towedStmt = conn.prepareStatement(towedQuery)) {
	                    towedStmt.setString(1, parkingCode);
	                    ResultSet towedRs = towedStmt.executeQuery();

	                    if (towedRs.next()) {
	                        String subscriberId = towedRs.getString("subscriber_id");
	                        String vehicleNumber = towedRs.getString("vehicle_number");
	                        int parkingSpot = towedRs.getInt("parking_spot");
	                        Date entryDate = towedRs.getDate("entry_date");
	                        Time entryTime = towedRs.getTime("entry_time");
	                        Timestamp towedAt = towedRs.getTimestamp("towed_at");

	                        LocalDate exitDate = LocalDate.now();
	                        LocalTime exitTime = LocalTime.now();

	                        long minutesLate = ChronoUnit.MINUTES.between(towedAt.toInstant(), Instant.now());
	                        int lateDuration = (int) Math.max(minutesLate, 1);

	                        String insertHistory = """
	                            INSERT INTO parking_history 
	                            (subscriber_id, vehicle_number, entry_date, entry_time, exit_date, exit_time, late_duration, parking_spot) 
	                            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
	                        """;
	                        try (PreparedStatement insertStmt = conn.prepareStatement(insertHistory)) {
	                            insertStmt.setString(1, subscriberId);
	                            insertStmt.setString(2, vehicleNumber);
	                            insertStmt.setDate(3, entryDate);
	                            insertStmt.setTime(4, entryTime);
	                            insertStmt.setDate(5, Date.valueOf(exitDate));
	                            insertStmt.setTime(6, Time.valueOf(exitTime));
	                            insertStmt.setInt(7, lateDuration);
	                            insertStmt.setInt(8, parkingSpot);
	                            insertStmt.executeUpdate();
	                        }

	                        try (PreparedStatement deleteStmt = conn.prepareStatement(
	                                "DELETE FROM towed_vehicles WHERE parking_code = ?")) {
	                            deleteStmt.setString(1, parkingCode);
	                            deleteStmt.executeUpdate();
	                        }
	                        conn.commit();
	                        return "SENT_TOWED_VEHICLE_MSG";
	                    } else {
	                        conn.rollback();
	                        return "FATAL_ERROR"; 
	                    }
	                }
	            }
	        } catch (SQLException inner) {
	            conn.rollback();
	            throw inner;
	        } finally {
	            conn.setAutoCommit(true);
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        return "FAILURE";
	    }
	}



	/**
	 * Processes a reservation check-in request by validating the reservation timing and
	 * either moving it to active parking or returning a status indicating the next step.
	 * 
	 * This method performs the following steps:
	 * 1. Checks if a reservation exists for the given parking code.
	 * 2. If found, checks whether the current time is more than 15 minutes before the scheduled entry time.
	 *    - If yes, the reservation is considered too early and the method returns "ARRIVE_EARLY".
	 * 3. If the timing is valid, a new record is inserted into the active_parkings table with current timestamps.
	 * 4. Updates the parking spot status to 'occupied' and removes the reservation.
	 * 
	 * @param parkingCode The reservation code entered by the user.
	 * @return "SUCCESS" if the reservation is activated,
	 *         "ARRIVE_EARLY" if the user arrived too early (more than 15 minutes before reservation),
	 *         "INVALID_CODE" if no reservation was found,
	 *         or "ERROR" if an exception occurred during the process.
	 */

	public static String moveReservationToActive(String parkingCode) {
	    return DBExecutor.execute(conn -> {
	        String selectQuery = "SELECT * FROM reservations WHERE parking_code = ? FOR UPDATE";

	        try {
	            conn.setAutoCommit(false); 

	            try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
	                stmt.setString(1, parkingCode);
	                ResultSet rs = stmt.executeQuery();

	                if (!rs.next()) {
	                    conn.rollback();
	                    return "INVALID_CODE"; // Reservation not found
	                }
	                Date orderEntryDate = rs.getDate("entry_date");
	                Time orderEntryTime = rs.getTime("entry_time");
	                LocalDateTime reservationTime = LocalDateTime.of(
	                        orderEntryDate.toLocalDate(), orderEntryTime.toLocalTime());
	                LocalDateTime now = LocalDateTime.now();

	                Duration duration = Duration.between(now, reservationTime);
	                if (duration.toMinutes() > 15) {
	                    conn.rollback();
	                    return "ARRIVE_EARLY";
	                }


	                String subscriberId = rs.getString("subscriber_id");
	                int parkingSpot = rs.getInt("parking_spot");
	                LocalDateTime now1 = LocalDateTime.now();
	                LocalDateTime expectedExitDateTime = now1.plusHours(4);
	                LocalDate entryDate = now1.toLocalDate();
	                LocalTime entryTime = now1.toLocalTime();
	                LocalDate expectedExitDate = expectedExitDateTime.toLocalDate();
	                LocalTime expectedExitTime = expectedExitDateTime.toLocalTime();

	                String insertQuery = """
	                    INSERT INTO active_parkings 
	                    (parking_code, subscriber_id, entry_date, entry_time, expected_exit_date, expected_exit_time, parking_spot, extended) 
	                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)""";

	                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
	                    insertStmt.setString(1, parkingCode);
	                    insertStmt.setString(2, subscriberId);
	                    insertStmt.setDate(3, Date.valueOf(entryDate));
	                    insertStmt.setTime(4, Time.valueOf(entryTime));
	                    insertStmt.setDate(5, Date.valueOf(expectedExitDate));
	                    insertStmt.setTime(6, Time.valueOf(expectedExitTime));
	                    insertStmt.setInt(7, parkingSpot);
	                    insertStmt.setBoolean(8, false);
	                    insertStmt.executeUpdate();
	                }

	                try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE parking_spots SET status = 'occupied' WHERE spot_number = ?")) {
	                    updateStmt.setInt(1, parkingSpot);
	                    updateStmt.executeUpdate();
	                }
	                try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM reservations WHERE parking_code = ?")) {
	                    deleteStmt.setString(1, parkingCode);
	                    deleteStmt.executeUpdate();
	                }

	                conn.commit();  
	                return "SUCCESS";

	            } catch (SQLException innerEx) {
	                conn.rollback();
	                throw innerEx;
	            } finally {
	                conn.setAutoCommit(true);
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	            return "ERROR";
	        }
	    });
	}

    
    /**
     * Updates the status of a parking spot in the parking_spots table.
     *
     * @param parkingSpot The spot number to update.
     * @param newStatus The new status to assign (e.g., "available", "reserved", "occupied").
     */
	public static void updateParkingSpotStatus(int parkingSpot, String newStatus) {
	    DBExecutor.execute(conn -> {
	        String lockQuery = "SELECT status FROM parking_spots WHERE spot_number = ? FOR UPDATE";
	        String updateQuery = "UPDATE parking_spots SET status = ? WHERE spot_number = ?";

	        try {
	            conn.setAutoCommit(false); // Start transaction
	            // Lock the row
	            try (PreparedStatement lockStmt = conn.prepareStatement(lockQuery)) {
	                lockStmt.setInt(1, parkingSpot);
	                lockStmt.executeQuery(); // Just to acquire the lock
	            }
	            // Perform the update
	            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
	                updateStmt.setString(1, newStatus);
	                updateStmt.setInt(2, parkingSpot);
	                updateStmt.executeUpdate();
	            }
	            conn.commit(); // End transaction

	        } catch (SQLException e) {
	            e.printStackTrace();
	            try {
	                conn.rollback();
	            } catch (SQLException ex) {
	                ex.printStackTrace();
	            }
	        } finally {
	            try {
	                conn.setAutoCommit(true);
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	        return null; 
	    });
	}


   
    /**
     * Deletes a reservation from the reservations table based on the provided parking code.
     *
     * @param parkingCode The parking code of the reservation to delete.
     */
	public static void cancelReservation(String parkingCode) {
	    DBExecutor.execute(conn -> {
	        String deleteQuery = "DELETE FROM reservations WHERE parking_code = ?";

	        try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
	            stmt.setString(1, parkingCode);   // Set the parking code to identify which reservation to delete
	            stmt.executeUpdate();             // Execute the deletion
	        } catch (SQLException e) {
	            e.printStackTrace();              // Log error if the deletion fails
	        }

	        return null; // No return value needed since this is a void method
	    });
	}
	
	
	/*
	 * Checks if a subscriber with the provided full name and subscription code
	 * exists in the database.
	 * 
	 * @param fullName The full name of the subscriber.
	 * 
	 * @param code The subscription code associated with the subscriber.
	 * 
	 * @return true if a matching subscriber is found, false otherwise.
	 */
	public static boolean checkLogin(String ID, String code) {
	    return DBExecutor.execute(conn -> {
	        String query = "SELECT * FROM subscribers WHERE subscriber_id = ? AND subscription_code = ?";
	        try (PreparedStatement stmt = conn.prepareStatement(query)) {
	            // Set parameters for ID and subscription code
	            stmt.setString(1, ID);
	            stmt.setString(2, code);
	            // Execute the query
	            ResultSet rs = stmt.executeQuery();
	            // Return true if a match is found
	            return rs.next();
	        } catch (SQLException e) {
	            // Log error and return false in case of exception
	            e.printStackTrace();
	            return false;
	        }
	    });
	}

	/*
	 * Validates login credentials for a management user by checking the 'employees'
	 * table.
	 * 
	 * @param username The username entered by the manager.
	 * 
	 * @param password The password entered by the manager.
	 * 
	 * @return true if credentials are valid, false otherwise.
	 */
	public static String checkLoginManagement(String username, String password) {
	    return DBExecutor.execute(conn -> {
	        String query = "SELECT role FROM employees WHERE username = ? AND password = ?";
	        try (PreparedStatement stmt = conn.prepareStatement(query)) {
	            // Set the username and password in the query
	            stmt.setString(1, username);
	            stmt.setString(2, password);
	            // Execute the query
	            ResultSet rs = stmt.executeQuery();
	            if (rs.next()) {
	                // If credentials are valid, return the user's role in lowercase
	                return rs.getString("role").toLowerCase();  // e.g., "manager" or "attendant"
	            } else {
	                // No match found – return null
	                return null;
	            }
	        } catch (SQLException e) {
	            // Log the error and return null on failure
	            e.printStackTrace();
	            return null;
	        }
	    });
	}

	/*
	 * Retrieves all parking spots marked as 'available' from the database.
	 *
	 * @return A list of strings describing each available parking spot.
	 */

	public static List<String> getAvailableSpots() {
	    return DBExecutor.execute(conn -> {
	        List<String> availableSpots = new ArrayList<>();
	        String query = "SELECT spot_number FROM parking_spots WHERE status = 'available'";
	        try (PreparedStatement stmt = conn.prepareStatement(query);
	             ResultSet rs = stmt.executeQuery()) {
	            // Iterate over each row in the result set
	            while (rs.next()) {
	                // Format the parking spot as a string and add to the list
	                String spot = "Parking Spot #" + rs.getInt("spot_number") + " is available.";
	                availableSpots.add(spot);
	            }
	        } catch (SQLException e) {
	            // Log SQL exception and continue with empty list
	            e.printStackTrace();
	        }
	        // Return the list of available parking spots
	        return availableSpots;
	    });
	}


	/**
	 * Returns the total number of parking spots in the system.
	 * 
	 * @return total count of all parking spots
	 */
	public static int getTotalParkingSpots() {
	    return DBExecutor.execute(conn -> {
	        int count = 0;
	        String query = "SELECT COUNT(*) FROM parking_spots";
	        try (PreparedStatement stmt = conn.prepareStatement(query);
	             ResultSet rs = stmt.executeQuery()) {
	            // If the query returns a result, extract the count from the first column
	            if (rs.next()) {
	                count = rs.getInt(1);
	            }
	        } catch (SQLException e) {
	            // Log any errors and return 0 as a fallback
	            e.printStackTrace();
	        }
	        return count;
	    });
	}

	/**
	 * Returns the number of available (free) parking spots.
	 * 
	 * @return count of available spots
	 */
	public static int getAvailableSpotsCount() {
	    return DBExecutor.execute(conn -> {
	        int count = 0;
	        String query = "SELECT COUNT(*) FROM parking_spots WHERE status = 'available'";
	        try (PreparedStatement stmt = conn.prepareStatement(query);
	             ResultSet rs = stmt.executeQuery()) {
	            // Read the count from the first (and only) row in the result
	            if (rs.next()) {
	                count = rs.getInt(1);
	            }
	        } catch (SQLException e) {
	            // Log SQL error and keep count as 0
	            e.printStackTrace();
	        }
	        // Print the result for debugging
	        System.out.println(count);
	        // Return the final count
	        return count;
	    });
	}

	/**
	 * Finds the number of the first available parking spot.
	 * 
	 * @return spot_number or throws an error if none found
	 */
	public static int findAvailableSpot() {
	    return DBExecutor.execute(conn -> {
	        String query = "SELECT spot_number FROM parking_spots WHERE status = 'available' LIMIT 1";
	        try (PreparedStatement stmt = conn.prepareStatement(query);
	             ResultSet rs = stmt.executeQuery()) {
	            // If there is a result, return the spot number
	            if (rs.next()) {
	                int spot = rs.getInt("spot_number");
	                return spot;
	            } else {
	                // No available spot found – print debug info
	                System.out.println("No rows returned from query.");
	            }
	        } catch (SQLException e) {
	            // Log the SQL error
	            System.out.println("SQL Exception while searching for available spot:");
	            e.printStackTrace();
	        }
	        // If we reach here, no spot was found or an error occurred
	        throw new RuntimeException("No available parking spots found.");
	    });
	}

	/**
	 * Reserves a spot-inserts a new reservation.
	 * 
	 * @param subscriberId the subscriber making the reservation
	 * @param code         the generated reservation code
	 * @param entryDate    the reservation start date
	 * @param entryTime    the reservation start time
	 * @param exitDate     the reservation end date
	 * @param exitTime     the reservation end time
	 * @param spotNumber   the parking spot to reserve
	 */
	public static void insertReservationAndUpdateSpot(String subscriberId, String code,
	        LocalDate entryDate, LocalTime entryTime,
	        LocalDate exitDate, LocalTime exitTime, int spotNumber) {

	    DBExecutor.executeVoid(conn -> {
	        try {
	            // Disable auto-commit to manage the transaction manually
	            conn.setAutoCommit(false);
	            
	            // Step 2: Insert the reservation into the reservations table
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
	            // Step 3: Commit the transaction
	            conn.commit();
	        } catch (SQLException e) {
	            // In case of error, rollback changes
	            try {
	                conn.rollback();
	            } catch (SQLException rollbackEx) {
	                System.err.println("Failed to rollback transaction: " + rollbackEx.getMessage());
	            }
	            e.printStackTrace();
	        } finally {
	            try {
	                // Restore default auto-commit behavior
	                conn.setAutoCommit(true);
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	    });
	}

	/**
	 * Searches for active parking records by subscriber ID.
	 *
	 * Retrieves all active parking records from the database where the given
	 * subscriber ID matches and the expected exit date is today or later.
	 *
	 * @param subscriberId The subscriber ID to search for.
	 * @return A list of ActiveParking records matching the search criteria.
	 */
	public static List<ActiveParking> searchActiveParkingByMemberId(int subscriberId) {
	    return DBExecutor.execute(conn -> {
	        List<ActiveParking> result = new ArrayList<>();
	        String query = "SELECT * FROM active_parkings WHERE subscriber_id = ?";
	        try (PreparedStatement stmt = conn.prepareStatement(query)) {
	            // Set the subscriber ID in the query
	            stmt.setInt(1, subscriberId);
	            // Execute the query
	            ResultSet rs = stmt.executeQuery();
	            // Build the result list from the result set
	            while (rs.next()) {
	                result.add(new ActiveParking(
	                    rs.getString("parking_code"),
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
	            // Log and continue with empty result
	            e.printStackTrace();
	        }
	        return result;
	    });
	}

	/**
	 * Searches for active parking records by parking spot.
	 *
	 * Retrieves all active parking records from the database where the parking spot
	 * matches the provided value and the expected exit date is today or later.
	 *
	 * @param spot The parking spot identifier to search for.
	 * @return A list of ActiveParking records matching the search criteria.
	 */
	public static List<ActiveParking> searchActiveParkingBySpot(String spot) {
	    return DBExecutor.execute(conn -> {
	        List<ActiveParking> result = new ArrayList<>();
	        String query = "SELECT * FROM active_parkings WHERE parking_spot = ?";
	        try (PreparedStatement stmt = conn.prepareStatement(query)) {
	            // Bind the parking spot parameter
	            stmt.setString(1, spot);
	            // Execute the query and get the result set
	            ResultSet rs = stmt.executeQuery();
	            // Convert each row into an ActiveParking object and add to list
	            while (rs.next()) {
	                result.add(new ActiveParking(
	                    rs.getString("parking_code"),
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
	            // Log any SQL exceptions encountered
	            e.printStackTrace();
	        }
	        return result;
	    });
	}
	
	/**
	 * Extends the expected exit date and time of an active parking session by 4 hours.
	 * If the extension crosses midnight, the date is also updated accordingly.
	 * Updates the 'extended' flag to 1 in the database.
	 *
	 * @param ap The ActiveParking object representing the active session to be extended.
	 * @return true if the update succeeded, false otherwise.
	 */
	public static boolean extendParkingTime(ActiveParking ap) {
	    return DBExecutor.execute(conn -> {
	        String selectForUpdate = "SELECT expected_exit_date, expected_exit_time FROM active_parkings WHERE parking_code = ? FOR UPDATE";
	        String updateQuery = "UPDATE active_parkings SET expected_exit_date = ?, expected_exit_time = ?, extended = 1 WHERE parking_code = ?";

	        try {
	            conn.setAutoCommit(false);

	            try (
	                PreparedStatement lockStmt = conn.prepareStatement(selectForUpdate);
	                PreparedStatement updateStmt = conn.prepareStatement(updateQuery)
	            ) {
	                lockStmt.setString(1, ap.getParkingCode());
	                ResultSet rs = lockStmt.executeQuery();

	                if (!rs.next()) {
	                    conn.rollback();
	                    return false;
	                }

	                // Combine the date and time from the DB
	                LocalDate exitDate = rs.getDate("expected_exit_date").toLocalDate();
	                LocalTime exitTime = rs.getTime("expected_exit_time").toLocalTime();
	                LocalDateTime exitDateTime = LocalDateTime.of(exitDate, exitTime);

	                // Add 4 hours
	                LocalDateTime newExitDateTime = exitDateTime.plusHours(4);

	                // Split back to date and time
	                LocalDate newExitDate = newExitDateTime.toLocalDate();
	                LocalTime newExitTime = newExitDateTime.toLocalTime();

	                // Update database
	                updateStmt.setDate(1, Date.valueOf(newExitDate));
	                updateStmt.setTime(2, Time.valueOf(newExitTime));
	                updateStmt.setString(3, ap.getParkingCode());

	                int rows = updateStmt.executeUpdate();
	                if (rows > 0) {
	                    ap.setExtended(true);
	                    ap.setExpectedExitDate(newExitDate.toString());
	                    ap.setExpectedExitTime(newExitTime.toString());
	                    conn.commit();
	                    return true;
	                } else {
	                    conn.rollback();
	                    return false;
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	            try {
	                conn.rollback();
	            } catch (SQLException ex) {
	                ex.printStackTrace();
	            }
	            return false;
	        } finally {
	            try {
	                conn.setAutoCommit(true);
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	    });
	}


	/**
	 * Checks whether a reservation already exists in the database for a given
	 * subscriber ID, entry date, and entry time.
	 *
	 * This method is used to prevent duplicate reservations at the exact same date
	 * and time for the same subscriber. It queries the "reservations" table and
	 * returns true if such a reservation exists.
	 * 
	 * @param subscriberId The ID of the subscriber attempting to create a
	 *                     reservation.
	 * @param entryDate    The date of the requested reservation entry.
	 * @param entryTime    The time of the requested reservation entry.
	 * @return true if a reservation already exists for the given subscriber at the
	 *         specified date and time, false otherwise.
	 */
	public static boolean reservationExists(String subscriberId, LocalDate entryDate, LocalTime entryTime) {
	    return DBExecutor.execute(conn -> {
	        String query = "SELECT COUNT(*) FROM reservations WHERE subscriber_id = ? AND entry_date = ? AND entry_time = ?";
	        try (PreparedStatement stmt = conn.prepareStatement(query)) {
	            // Set the query parameters
	            stmt.setString(1, subscriberId);
	            stmt.setDate(2, java.sql.Date.valueOf(entryDate));
	            stmt.setTime(3, java.sql.Time.valueOf(entryTime));
	            // Execute the query
	            ResultSet rs = stmt.executeQuery();
	            // Return true if count > 0, meaning a reservation exists
	            if (rs.next()) {
	                return rs.getInt(1) > 0;
	            }
	        } catch (SQLException e) {
	            // Log the error and fall through to return false
	            e.printStackTrace();
	        }
	        return false;
	    });
	}

	/**
	 * Creates a new active parking entry for a subscriber who arrives without a
	 * prior reservation.
	 * 
	 * This method performs the following steps: - Checks if the subscriber already
	 * has an active parking record. - Searches for the first available parking
	 * spot. - Generates a unique parking code based on subscriber ID and parking
	 * spot. - Ensures the generated parking code does not collide with existing
	 * codes. - Calculates entry and expected exit times (current time + 4 hours). -
	 * Inserts a new record into the active_parkings table. - Updates the
	 * parking_spots table to mark the selected spot as occupied.
	 *
	 * @param subscriber The subscriber attempting to create a new parking entry.
	 * @return - "CAR_ALREADY_PARKED" if the subscriber already has an active
	 *         parking session. - "NO_SPOTS_AVAILABLE" if there are no available
	 *         parking spots. - "SUCSESSFUL_PARKING" followed by the generated
	 *         parking code if insertion succeeded. - "ERROR" if an exception
	 *         occurred during processing.
	 */
	public static String createNewActiveParking(Subscriber subscriber) {
	    return DBExecutor.execute(conn -> {
	        try {
	            // Step 1: Check if the subscriber already has an active parking session
	            String checkIfParkedQuery = "SELECT 1 FROM active_parkings WHERE subscriber_id = ?";
	            try (PreparedStatement checkStmt = conn.prepareStatement(checkIfParkedQuery)) {
	                checkStmt.setString(1, subscriber.getSubscriber_id());
	                ResultSet rs = checkStmt.executeQuery();
	                if (rs.next()) {
	                    return "CAR_ALREADY_PARKED";
	                }
	            }
	            
	         // Step 2: Check for an available parking spot
	            int parkingSpot = -1;
	            LocalDateTime now = LocalDateTime.now();
	            LocalDateTime nowPlusGrace = now.plusHours(8).plusMinutes(15);
	            String availableSpotsQuery = "SELECT spot_number FROM parking_spots WHERE status = 'available' ORDER BY spot_number ASC";
	            try (PreparedStatement spotStmt = conn.prepareStatement(availableSpotsQuery);
	                 ResultSet spotResult = spotStmt.executeQuery()) {

	                while (spotResult.next()) {
	                    int candidateSpot = spotResult.getInt("spot_number");

	                    String reservationQuery = "SELECT entry_date, entry_time FROM reservations WHERE parking_spot = ?";
	                    try (PreparedStatement resStmt = conn.prepareStatement(reservationQuery)) {
	                        resStmt.setInt(1, candidateSpot);
	                        ResultSet resRs = resStmt.executeQuery();

	                        boolean skipThisSpot = false;

	                        while (resRs.next()) {
	                            LocalDate resDate = resRs.getDate("entry_date").toLocalDate();
	                            LocalTime resTime = resRs.getTime("entry_time").toLocalTime();
	                            LocalDateTime reservationTime = LocalDateTime.of(resDate, resTime);

	                            if (!reservationTime.isAfter(nowPlusGrace)) {	
	                                skipThisSpot = true;
	                                break;
	                            }
	                        }

	                        if (!skipThisSpot) {
	                            parkingSpot = candidateSpot;
	                            break;
	                        }
	                    }
	                }

	                if (parkingSpot == -1) {
	                    return "NO_SPOTS_AVAILABLE";
	                }
	            }
	            // Step 3: Generate unique parking code (after collecting existing codes)
	            List<String> existingCodes = new ArrayList<>();
	            String existingCodesQuery = "SELECT parking_code FROM active_parkings";
	            try (PreparedStatement codeStmt = conn.prepareStatement(existingCodesQuery)) {
	                ResultSet codeResults = codeStmt.executeQuery();
	                while (codeResults.next()) {
	                    existingCodes.add(codeResults.getString("parking_code"));
	                }
	            }
	            String newParkingCode = generateUniqueParkingCode(subscriber.getSubscriber_id(), parkingSpot);
	            // Step 4: Calculate entry and expected exit times
	            LocalDateTime now1 = LocalDateTime.now();
	            LocalDateTime expectedExit = now1.plusHours(4);
	            // Step 5: Insert new active parking record
	            String insertQuery = "INSERT INTO active_parkings "
	                    + "(parking_code, subscriber_id, entry_date, entry_time, expected_exit_date, expected_exit_time, parking_spot, extended) "
	                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
	                insertStmt.setString(1, newParkingCode);
	                insertStmt.setString(2, subscriber.getSubscriber_id());
	                insertStmt.setDate(3, Date.valueOf(now1.toLocalDate()));
	                insertStmt.setTime(4, Time.valueOf(now1.toLocalTime()));
	                insertStmt.setDate(5, Date.valueOf(expectedExit.toLocalDate()));
	                insertStmt.setTime(6, Time.valueOf(expectedExit.toLocalTime()));
	                insertStmt.setInt(7, parkingSpot);
	                insertStmt.setInt(8, 0); // extended = false

	                insertStmt.executeUpdate();
	            }
	            // Step 6: Update the parking spot status to 'occupied'
	            String updateSpotQuery = "UPDATE parking_spots SET status = 'occupied' WHERE spot_number = ?";
	            try (PreparedStatement updateStmt = conn.prepareStatement(updateSpotQuery)) {
	                updateStmt.setInt(1, parkingSpot);
	                updateStmt.executeUpdate();
	            }
	            return "SUCSESSFUL_PARKING" + newParkingCode;
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return "ERROR";
	        }
	    });
	}
	/**
	 * Generates a unique parking code for a new active parking entry.
	 * 
	 * The generated code is built from: - The subscriber ID. - The selected parking
	 * spot number. - A random component to help avoid collisions. - A hash function
	 * is used to mix the input values into a consistent 4-digit code.
	 * 
	 * Final format: "BPARKxxxx" where xxxx is a 4-digit number.
	 *
	 * @param subscriberId The ID of the subscriber requesting the parking.
	 * @param parkingSpot  The allocated parking spot number.
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
	 * Generates a unique parking code by combining subscriber ID and parking spot
	 * number, ensuring the generated code does not already exist in either the
	 * reservations or active_parkings tables.
	 *
	 * The method repeatedly calls generateParkingCode and verifies uniqueness by
	 * checking against both reservations and active parkings until a valid, unused
	 * code is created.
	 *
	 * @param subscriberId The ID of the subscriber requesting the reservation or
	 *                     parking.
	 * @param parkingSpot  The number of the allocated parking spot.
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
	    return DBExecutor.execute(conn -> {
	        String query = "SELECT 1 FROM reservations WHERE parking_code = ?";

	        try (PreparedStatement stmt = conn.prepareStatement(query)) {
	            // Set the parameter value for the SQL query
	            stmt.setString(1, code);

	            // Execute the query and return true if any row exists
	            ResultSet rs = stmt.executeQuery();
	            return rs.next();

	        } catch (SQLException e) {
	            // Log the error and return false as fallback
	            e.printStackTrace();
	            return false;
	        }
	    });
	}

	/**
	 * Checks whether a given parking code already exists in the active_parkings
	 * table.
	 *
	 * @param code The parking code to check for existence.
	 * @return true if the code exists in the active_parkings table, false
	 *         otherwise.
	 */
	public static boolean activeParkingCodeExists(String code) {
	    return DBExecutor.execute(conn -> {
	        String query = "SELECT 1 FROM active_parkings WHERE parking_code = ?";
	        try (PreparedStatement stmt = conn.prepareStatement(query)) {
	            // Bind the input code to the SQL parameter
	            stmt.setString(1, code);
	            // Execute query and check if a row exists
	            ResultSet rs = stmt.executeQuery();
	            return rs.next(); // true if at least one result found
	        } catch (SQLException e) {
	            // Print exception and return false
	            e.printStackTrace();
	            return false;
	        }
	    });
	}

	public static boolean updateReservationDateTime(int reservationId, LocalDate newDate, LocalTime newTime) {
	    return DBExecutor.execute(conn -> {
	        String query = "UPDATE reservations SET entry_date = ?, entry_time = ?, exit_date = ?, exit_time = ? WHERE reservation_id = ?";
	        // Calculate new exit date and time as 4 hours after the new entry
	        LocalDateTime entryDateTime = LocalDateTime.of(newDate, newTime);
	        LocalDateTime exitDateTime = entryDateTime.plusHours(4);
	        LocalDate newExitDate = exitDateTime.toLocalDate();
	        LocalTime newExitTime = exitDateTime.toLocalTime();
	        try (PreparedStatement stmt = conn.prepareStatement(query)) {
	            // Bind all parameters to the prepared statement
	            stmt.setDate(1, Date.valueOf(newDate));
	            stmt.setTime(2, Time.valueOf(newTime));
	            stmt.setDate(3, Date.valueOf(newExitDate));
	            stmt.setTime(4, Time.valueOf(newExitTime));
	            stmt.setInt(5, reservationId);
	            // Execute the update and return whether any rows were affected
	            int rows = stmt.executeUpdate();
	            return rows > 0;
	        } catch (SQLException e) {
	            // Print the stack trace and return false on failure
	            e.printStackTrace();
	            return false;
	        }
	    });
	}

	/**
	 * Cancels a reservation by its ID. This method deletes the reservation from the
	 * `reservations` table and, if successful, updates the corresponding parking
	 * spot's status to 'available'.
	 *
	 * @param reservationId the ID of the reservation to cancel
	 * @return true if the cancellation and spot update were successful, false
	 *         otherwise
	 */
	public static boolean cancelReservationById(int reservationId) {
	    return DBExecutor.execute(conn -> {
	        String getSpotQuery = "SELECT parking_spot FROM reservations WHERE reservation_id = ? FOR UPDATE";
	        String deleteQuery = "DELETE FROM reservations WHERE reservation_id = ?";
	        String updateSpotQuery = "UPDATE parking_spots SET status = 'available' WHERE spot_number = ?";
	        try {
	            conn.setAutoCommit(false); // Begin transaction
	            int spotNumber;
	            // Step 1: Get the parking spot linked to the reservation
	            try (PreparedStatement getStmt = conn.prepareStatement(getSpotQuery)) {
	                getStmt.setInt(1, reservationId);
	                ResultSet rs = getStmt.executeQuery();

	                if (rs.next()) {
	                    spotNumber = rs.getInt("parking_spot");
	                } else {
	                    conn.rollback(); // No reservation found, rollback
	                    return false;
	                }
	            }
	            // Step 2: Delete the reservation
	            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
	                deleteStmt.setInt(1, reservationId);
	                int deletedRows = deleteStmt.executeUpdate();
	                if (deletedRows == 0) {
	                    conn.rollback(); // Nothing deleted, rollback
	                    return false;
	                }
	            }
	            // Step 3: Mark the parking spot as available again
	            try (PreparedStatement updateStmt = conn.prepareStatement(updateSpotQuery)) {
	                updateStmt.setInt(1, spotNumber);
	                updateStmt.executeUpdate();
	            }

	            conn.commit(); // All operations succeeded, commit the transaction
	            return true;
	        } catch (SQLException e) {
	            e.printStackTrace();
	            try {
	                conn.rollback(); // On any error, rollback the transaction
	            } catch (SQLException rollbackEx) {
	                rollbackEx.printStackTrace();
	            }
	            return false;
	        } finally {
	            try {
	                conn.setAutoCommit(true); // Reset auto-commit to default
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	    });
	}

	/**
	 * Retrieves all future reservations from the database. A future reservation is
	 * one where the entry date and time are after the current system timestamp.
	 *
	 * @return A list of Reservation objects representing upcoming reservations.
	 */
	public static List<Reservation> getFutureReservations() {
	    return DBExecutor.execute(conn -> {
	        List<Reservation> futureReservations = new ArrayList<>();
	        // SQL query to select future reservations by comparing entry date/time to NOW()
	        String sql = """
	            SELECT reservation_id, subscriber_id, parking_code, entry_date, entry_time,
	                   exit_date, exit_time, parking_spot
	            FROM reservations
	            WHERE TIMESTAMP(entry_date, entry_time) > NOW()
	        """;
	        try (
	            PreparedStatement stmt = conn.prepareStatement(sql);
	            ResultSet rs = stmt.executeQuery()
	        ) {
	            // Iterate through the result set and construct Reservation objects
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
	        } catch (SQLException e) {
	            // If an error occurs during the query, print the stack trace
	            e.printStackTrace();
	        }
	        return futureReservations;
	    });
	}

	/**
	 * Retrieves all currently active parking records from the database. Each record
	 * includes entry and expected exit details, along with the subscriber and spot
	 * information.
	 *
	 * @return A list of ActiveParking objects representing active parkings.
	 */
	public static List<ActiveParking> getActiveParkings() {
	    return DBExecutor.execute(conn -> {
	        List<ActiveParking> activeList = new ArrayList<>();
	        // SQL to fetch all currently active parking entries
	        String sql = """
	            SELECT parking_code, subscriber_id, entry_date, entry_time,
	                   expected_exit_date, expected_exit_time, parking_spot, extended
	            FROM active_parkings
	        """;

	        try (
	            PreparedStatement stmt = conn.prepareStatement(sql);
	            ResultSet rs = stmt.executeQuery()
	        ) {
	            // Iterate through the result set and create ActiveParking objects
	            while (rs.next()) {
	                ActiveParking ap = new ActiveParking(
	                    rs.getString("parking_code"),
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
	        } catch (SQLException e) {
	            // Print the error if any SQL issue occurs
	            e.printStackTrace();
	        }
	        return activeList;
	    });
	}
	/**
	 * Retrieves a list of site activity records from the database. Each record
	 * includes the action, username, and timestamp of the activity. Results are
	 * ordered by timestamp in descending order.
	 *
	 * @return A list of formatted strings representing site activity events.
	 */

	/**
	 * Retrieves current active parking sessions and upcoming reservations from the
	 * database.
	 *
	 * @return A list of formatted strings representing active parking and future
	 *         reservation records.
	 */
	public static List<String> getSiteActivityData() {
	    List<String> result = new ArrayList<>();

	    // First query: get active parkings
	    DBExecutor.execute(conn -> {
	        String activeSql = """
	            SELECT parking_code, subscriber_id, entry_date, entry_time,
	                   expected_exit_date, expected_exit_time, parking_spot, extended
	            FROM active_parkings
	        """;

	        try (
	            PreparedStatement stmt = conn.prepareStatement(activeSql);
	            ResultSet rs = stmt.executeQuery()
	        ) {
	            result.add("=== Active Parkings ===");

	            while (rs.next()) {
	                String line = String.format(
	                    "Code: %s | Subscriber: %s | Entry: %s %s | Exit: %s %s | Spot: %d | Extended: %s",
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
	            e.printStackTrace();
	            result.add("Error retrieving active parking data.");
	        }

	        return null; // This lambda does not return a value; we modify external list
	    });

	    // Second query: get upcoming reservations
	    DBExecutor.execute(conn -> {
	        String reservSql = """
	            SELECT reservation_id, subscriber_id, parking_code, entry_date, entry_time,
	                   exit_date, exit_time, parking_spot
	            FROM reservations
	            WHERE TIMESTAMP(entry_date, entry_time) > NOW()
	            ORDER BY entry_date, entry_time
	        """;

	        try (
	            PreparedStatement stmt = conn.prepareStatement(reservSql);
	            ResultSet rs = stmt.executeQuery()
	        ) {
	            result.add("=== Upcoming Reservations ===");

	            while (rs.next()) {
	                String line = String.format(
	                    "Reservation #%d | Subscriber: %s | Parking: %s | From: %s %s | To: %s %s | Spot: %d",
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
	            e.printStackTrace();
	            result.add("Error retrieving reservation data.");
	        }

	        return null; // Same as above: nothing returned, we just fill the list
	    });

	    return result;
	}
	
	/**
	 * Checks for overstayed vehicles in the parking lot and processes towing actions accordingly.
	 * This method scans the active_parkings table and selects all vehicles that have
	 * exceeded their allowed parking time based on whether they extended their parking:
	 * Vehicles with {extended = TRUE} are towed after 8 hours (480 minutes).
	 * Vehicles with {extended = FALSE} are towed after 4 hours (240 minutes).
	 *
	 * For each such vehicle:
	 * - It is inserted into the towed_vehicles table.
	 * - It is removed from active_parkings.
	 * - The associated parking spot is marked as available.
	 * - If an email is available:
	 *     - If this is the third late incident, a combined towing + late charge email is sent.
	 *     - Otherwise, a towing notice email is sent.
	 * - The subscriber's late_count is incremented by 1.
	 *   If late_count was already 2 (i.e., this is the third late incident),
	 *   a late charge is applied and late_count is reset to 0.
	 *
	 * This method is intended to run periodically as part of a scheduled task.
	 *
	 * @param none This is a static method and does not receive any parameters.
	 * @return void This method does not return a value.
	 * @throws SQLException if a database access error occurs during any query or update.
	 * @throws RuntimeException if unexpected errors occur in the database logic or email sending.
	 */
	
	public static void checkAndTowVehicles() {
	    DBExecutor.executeVoid(conn -> {
	        String query = """
	            SELECT ap.parking_code, ap.subscriber_id, ap.entry_date, ap.entry_time, 
	                   ap.parking_spot, ap.extended, s.vehicle_number1, s.email
	            FROM active_parkings ap
	            JOIN subscribers s ON ap.subscriber_id = s.subscriber_id
	            WHERE (
	                (ap.extended = TRUE AND TIMESTAMPDIFF(MINUTE, TIMESTAMP(ap.entry_date, ap.entry_time), NOW()) > 480)
	                OR
	                (ap.extended = FALSE AND TIMESTAMPDIFF(MINUTE, TIMESTAMP(ap.entry_date, ap.entry_time), NOW()) > 240)
	            )
	        """;

	        try (PreparedStatement stmt = conn.prepareStatement(query);
	             ResultSet rs = stmt.executeQuery()) {

	            while (rs.next()) {
	                String parkingCode = rs.getString("parking_code");
	                String subscriberId = rs.getString("subscriber_id");
	                Date entryDate = rs.getDate("entry_date");
	                Time entryTime = rs.getTime("entry_time");
	                int spot = rs.getInt("parking_spot");
	                String vehicleNumber = rs.getString("vehicle_number1");
	                String email = rs.getString("email");

	                try (PreparedStatement insertStmt = conn.prepareStatement("""
	                    INSERT INTO towed_vehicles 
	                    (parking_code, subscriber_id, vehicle_number, parking_spot, entry_date, entry_time) 
	                    VALUES (?, ?, ?, ?, ?, ?)
	                """)) {
	                    insertStmt.setString(1, parkingCode);
	                    insertStmt.setString(2, subscriberId);
	                    insertStmt.setString(3, vehicleNumber);
	                    insertStmt.setInt(4, spot);
	                    insertStmt.setDate(5, entryDate);
	                    insertStmt.setTime(6, entryTime);
	                    insertStmt.executeUpdate();
	                }

	                try (PreparedStatement deleteStmt = conn.prepareStatement(
	                        "DELETE FROM active_parkings WHERE parking_code = ?")) {
	                    deleteStmt.setString(1, parkingCode);
	                    deleteStmt.executeUpdate();
	                }

	                updateParkingSpotStatus(spot, "available");

	                boolean sentCombinedEmail = false;

	                try (PreparedStatement lateStmt = conn.prepareStatement("""
	                    SELECT late_count FROM subscribers WHERE subscriber_id = ?
	                """)) {
	                    lateStmt.setString(1, subscriberId);
	                    ResultSet lateRs = lateStmt.executeQuery();

	                    if (lateRs.next()) {
	                        int lateCount = lateRs.getInt("late_count");

	                        if (lateCount == 2) {
	                            try {
	                                new EmailSender().sendTowingWithLateChargeEmail(email, vehicleNumber, spot);
	                                sentCombinedEmail = true;
	                            } catch (Exception e) {
	                                System.err.println("[EmailSender] Failed to send combined towing/late email.");
	                                e.printStackTrace();
	                            }

	                            try (PreparedStatement resetLate = conn.prepareStatement("""
	                                UPDATE subscribers SET late_count = 0 WHERE subscriber_id = ?
	                            """)) {
	                                resetLate.setString(1, subscriberId);
	                                resetLate.executeUpdate();
	                            }
	                        } else {
	                            try (PreparedStatement incLate = conn.prepareStatement("""
	                                UPDATE subscribers SET late_count = late_count + 1 WHERE subscriber_id = ?
	                            """)) {
	                                incLate.setString(1, subscriberId);
	                                incLate.executeUpdate();
	                            }
	                        }
	                    }
	                }

	                if (email != null && !sentCombinedEmail) {
	                    try {
	                        new EmailSender().sendTowingNoticeEmail(email, vehicleNumber, spot);
	                        try (PreparedStatement updateStmt = conn.prepareStatement("""
	                            UPDATE towed_vehicles
	                            SET email_sent = TRUE
	                            WHERE parking_code = ?
	                        """)) {
	                            updateStmt.setString(1, parkingCode);
	                            updateStmt.executeUpdate();
	                        }
	                    } catch (Exception e) {
	                        System.err.println("[Towing] Failed to send email to: " + email);
	                        e.printStackTrace();
	                    }
	                }
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    });
	}


    /**
     * Finalizes towed vehicles that were not picked up within 24 hours.
     *
     * <p>This method performs the following:
     * 1. Selects all vehicles from `towed_vehicles` where `towed_at` is more than 24 hours ago.
     * 2. For each such vehicle:
     *    - Inserts a record into `parking_history` with a fixed `late_duration` of 1440 minutes (24 hours).
     *    - Deletes the vehicle from `towed_vehicles`.
     *    - Marks the associated parking spot as 'available'.
     *
     * <p>This method is intended to run periodically (e.g. every minute) as part of the background scheduled tasks.
     * It ensures that vehicles left in the trailer area for too long are automatically finalized in the system.
     *
     * @param none Static method – no parameters required.
     * @return void This method does not return a value.
     * @throws SQLException if a database access error occurs.
     * @throws RuntimeException if an unexpected error occurs during the process.
     */
    public static void finalizeTowedVehiclesLateTime() {
        DBExecutor.executeVoid(conn -> {
            String query = """
                SELECT * FROM towed_vehicles 
                WHERE towed_at < NOW() - INTERVAL 24 HOUR """;

            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String parkingCode = rs.getString("parking_code");
                    String subscriberId = rs.getString("subscriber_id");
                    String vehicleNumber = rs.getString("vehicle_number");
                    int parkingSpot = rs.getInt("parking_spot");
                    Date entryDate = rs.getDate("entry_date");
                    Time entryTime = rs.getTime("entry_time");
                    LocalDate exitDate = LocalDate.now();
                    LocalTime exitTime = LocalTime.now();
                    String insertHistory = """
                        INSERT INTO parking_history 
                        (subscriber_id, vehicle_number, entry_date, entry_time, exit_date, exit_time, late_duration, parking_spot) 
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """;

                    try (PreparedStatement insertStmt = conn.prepareStatement(insertHistory)) {
                        insertStmt.setString(1, subscriberId);
                        insertStmt.setString(2, vehicleNumber);
                        insertStmt.setDate(3, entryDate);
                        insertStmt.setTime(4, entryTime);
                        insertStmt.setDate(5, Date.valueOf(exitDate));
                        insertStmt.setTime(6, Time.valueOf(exitTime));
                        insertStmt.setInt(7, 1440); // 24 hours in minutes
                        insertStmt.setInt(8, parkingSpot);
                        insertStmt.executeUpdate();
                    }
                    try (PreparedStatement deleteStmt = conn.prepareStatement(
                            "DELETE FROM towed_vehicles WHERE parking_code = ?")) {
                        deleteStmt.setString(1, parkingCode);
                        deleteStmt.executeUpdate();
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

	
	/**
	 * Calculates the number of existing reservations that overlap with a given time range.
	 * The time range starts at the specified date and startTime, and extends for the given duration.
	 * This method accounts for reservations that cross over midnight into the next day.
	 *
	 * @param date The date of the requested reservation start.
	 * @param startTime The time of the requested reservation start.
	 * @param durationHours The duration (in hours) of the reservation + potential extension (usually 8).
	 * @return The number of overlapping reservations found within the specified time window.
	 */
	public static int getOverlappingReservationCount(LocalDate date, LocalTime startTime, int durationHours) {
	    return DBExecutor.execute(conn -> {
	    	String query = """
	    		    SELECT COUNT(*) AS overlap_count
	    		    FROM reservations
	    		    WHERE TIMESTAMP(entry_date, entry_time) < ?
	    		      AND TIMESTAMP(DATE_ADD(exit_date, INTERVAL 4 HOUR), exit_time) > ?
	    		""";


	        LocalDateTime start = LocalDateTime.of(date, startTime);
	        LocalDateTime end = start.plusHours(durationHours); 

	        try (PreparedStatement stmt = conn.prepareStatement(query)) {
	            stmt.setTimestamp(1, Timestamp.valueOf(end));
	            stmt.setTimestamp(2, Timestamp.valueOf(start));

	            try (ResultSet rs = stmt.executeQuery()) {
	                if (rs.next()) {
	                    return rs.getInt("overlap_count");
	                }
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return 0;
	    });
	}

	/**
	 * Removes expired reservations from the system and frees up the associated parking spots.
	 * A reservation is considered expired if its scheduled entry time is more than
	 * 15 minutes in the past and the vehicle has not yet arrived.
	 * For each expired reservation:
	 * - The reservation is deleted from the reservations table.
	 * - The associated parking spot is marked as 'available' in the `parking_spots` table.
	 * This method is intended to be run periodically by a scheduler,
	 * ensuring that abandoned reservations do not block future bookings.
	 */
	public static void removeExpiredReservations() {
	    DBExecutor.executeVoid(conn -> {
	        String selectQuery = """
	            SELECT reservation_id, parking_spot
	            FROM reservations
	            WHERE TIMESTAMP(entry_date, entry_time) < NOW() - INTERVAL 15 MINUTE
	        """;

	        try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
	             ResultSet rs = selectStmt.executeQuery()) {

	            while (rs.next()) {
	                int reservationId = rs.getInt("reservation_id");
	                int spot = rs.getInt("parking_spot");

	                try (PreparedStatement deleteStmt = conn.prepareStatement(
	                        "DELETE FROM reservations WHERE reservation_id = ?")) {
	                    deleteStmt.setInt(1, reservationId);
	                    deleteStmt.executeUpdate();
	                }
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    });
	}

	public static void generateAndStoreParkingDurationReport() {
	    YearMonth lastMonth = YearMonth.now().minusMonths(1);
	    int year = lastMonth.getYear();
	    int month = lastMonth.getMonthValue();

	    DBExecutor.executeVoid(conn -> {
	        List<ParkingDurationRecord> records = new ArrayList<>();

	        String selectQuery = "SELECT " +
	                "DAY(entry_date) AS day, " +
	                "SUM(TIMESTAMPDIFF(MINUTE, entry_time, exit_time)) AS duration, " +
	                "SUM(late_duration) AS lateDuration, " +
	                "SUM(extended_duration) AS extendedDuration " +
	                "FROM parking_history " +
	                "WHERE YEAR(entry_date) = ? AND MONTH(entry_date) = ? " +
	                "GROUP BY day " +
	                "ORDER BY day";

	        try (PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
	            stmt.setInt(1, year);
	            stmt.setInt(2, month);

	            try (ResultSet rs = stmt.executeQuery()) {
	                while (rs.next()) {
	                    int day = rs.getInt("day");
	                    int duration = rs.getInt("duration");
	                    int late = rs.getInt("lateDuration");
	                    int extended = rs.getInt("extendedDuration");

	                    records.add(new ParkingDurationRecord(day, duration, late, extended));
	                }
	            }

	            Gson gson = new Gson();
	            String jsonData = gson.toJson(records);

	            String insertQuery = "INSERT INTO monthly_reports (report_type, month, year, data) VALUES (?, ?, ?, ?)";

	            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
	                insertStmt.setString(1, "ParkingDuration");
	                insertStmt.setInt(2, month);
	                insertStmt.setInt(3, year);
	                insertStmt.setString(4, jsonData);
	                insertStmt.executeUpdate();
	                System.out.println("Stored ParkingDuration report for " + month + "/" + year);
	            }

	        } catch (SQLException e) {
	            System.err.println("Error generating report: " + e.getMessage());
	            e.printStackTrace();
	        }
	    });
	}

	public static List<ParkingDurationRecord> loadParkingDurationReport(int year, int month) {
        return DBExecutor.execute(conn -> {
            List<ParkingDurationRecord> records = new ArrayList<>();
            String sql = "SELECT data FROM monthly_reports WHERE report_type = 'ParkingDuration' AND year = ? AND month = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, year);
                stmt.setInt(2, month);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String json = rs.getString("data");
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<ParkingDurationRecord>>() {}.getType();
                    records = gson.fromJson(json, listType);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return records;
        });
    }

	
	/**
	 * Generates and stores a monthly member status report as a JSON string in the database.
	 *
	 * @param year  The year for which the report is generated.
	 * @param month The month for which the report is generated.
	 */
	public static void generateAndStoreMemberStatusReport() {
		YearMonth lastMonth = YearMonth.now().minusMonths(1);
	    int year = lastMonth.getYear();
	    int month = lastMonth.getMonthValue();
	    
	    String sql = """
	        SELECT entry_date, COUNT(DISTINCT subscriber_id) AS active_subscribers
	        FROM parking_history
	        WHERE MONTH(entry_date) = ? AND YEAR(entry_date) = ?
	        GROUP BY entry_date ORDER BY entry_date
	    """;

	    DBExecutor.executeVoid(conn -> {
	        List<DailySubscriberCount> report = new ArrayList<>();

	        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	            stmt.setInt(1, month);
	            stmt.setInt(2, year);

	            ResultSet rs = stmt.executeQuery();
	            Map<Integer, Integer> countPerDay = new HashMap<>();

	            while (rs.next()) {
	                LocalDate date = rs.getDate("entry_date").toLocalDate();
	                countPerDay.put(date.getDayOfMonth(), rs.getInt("active_subscribers"));
	            }

	            int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
	            for (int day = 1; day <= daysInMonth; day++) {
	                int count = countPerDay.getOrDefault(day, 0);
	                report.add(new DailySubscriberCount(day, count));
	            }

	            Gson gson = new Gson();
	            String json = gson.toJson(report);

	            String insertSql = """
	                INSERT INTO monthly_reports (report_type, year, month, data)
	                VALUES ('member_status', ?, ?, ?)
	            """;

	            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
	                insertStmt.setInt(1, year);
	                insertStmt.setInt(2, month);
	                insertStmt.setString(3, json);
	                insertStmt.executeUpdate();
	            }

	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    });
	}

	/**
	 * Loads a saved member status report from the database for a specific year and month.
	 * The report is stored as JSON in the "monthly_reports" table.
	 *
	 * @param year  The year of the report.
	 * @param month The month of the report.
	 * @return A list of DailySubscriberCount records.
	 */
	public static List<DailySubscriberCount> loadMemberStatusReport(int year, int month) {
	    String sql = """
	        SELECT data FROM monthly_reports
	        WHERE report_type = 'member_status' AND year = ? AND month = ?
	    """;

	    return DBExecutor.execute(conn -> {
	    	try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	    	    stmt.setInt(1, year);
	    	    stmt.setInt(2, month);

	    	    ResultSet rs = stmt.executeQuery();
	    	    if (rs.next()) {
	    	        String jsonData = rs.getString("data");

	    	        if (jsonData != null && !jsonData.isEmpty()) {
	    	            try {
	    	                Gson gson = new Gson();
	    	                Type listType = new TypeToken<List<DailySubscriberCount>>() {}.getType();
	    	                return gson.fromJson(jsonData, listType);
	    	            } catch (Exception parseEx) {
	    	                parseEx.printStackTrace();
	    	            }
	    	        }
	    	    }
	    	} catch (Exception e) {
	    	    e.printStackTrace();
	    	}

	        return Collections.emptyList();
	    });
	}


}
