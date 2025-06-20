package server;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Utility class that handles password and code retrieval based on email addresses.
 * This class provides static methods to fetch:
 * Employee passwords,
 * Subscriber subscription codes,
 * Parking codes for active parking sessions.
 * All methods execute database queries using DBExecutor to ensure proper connection handling.
 */
public class EmployeePassword {

	 /**
     * Retrieves the password associated with a given employee email.
     *
     * @param email The employee's email address.
     * @return The password as a string, or null if not found or on error.
     */
    public static String getPasswordForEmail(String email) {
        return DBExecutor.execute(conn -> {
            String sql = "SELECT password FROM employees WHERE email = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("password");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Retrieves the subscription code associated with a subscriber email.
     *
     * @param email The subscriber's email address.
     * @return The subscription code, or null if not found or on error.
     */
    public static String subscriptionCodeForEmail(String email) {
        return DBExecutor.execute(conn -> {
            String sql = "SELECT subscription_code FROM subscribers WHERE email = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("subscription_code");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Retrieves the parking code for an active parking session associated with a subscriber's email.
     *
     * @param email The subscriber's email address.
     * @return The active parking code, or null if not found or on error.
     */
    public static String parkingCodeForEmail(String email) {
        return DBExecutor.execute(conn -> {
            String sql = """
                SELECT ap.parking_code 
                FROM active_parkings ap
                JOIN subscribers s ON ap.subscriber_id = s.subscriber_id
                WHERE s.email = ?
                """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("parking_code");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
