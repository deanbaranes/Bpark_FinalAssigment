package server;

import java.time.LocalDate;
import java.util.*;

import common.DailySubscriberCount;
import common.ParkingDurationRecord;
import java.sql.*;

/**
 * ReportGenerator is responsible for creating analytical reports based on parking history data.
 * It generates reports such as:
 * Daily parking duration statistics (including late and extended durations),
 * Daily count of active subscribers.
 * These reports are used for management and performance monitoring.
 */
public class ReportGenerator {
	
	 /**
     * Generates a parking duration report for a given month and year.
     * The report includes total parking time, late duration, and extended duration
     * for each day in the specified month. If no data exists for a day, the values are zeroed.
     *
     * @param conn  A valid Connection to the database.
     * @param year  The year for the report .
     * @param month The month for the report.
     * @return A list of ParkingDurationRecord objects, one for each day of the month.
     */
	public List<ParkingDurationRecord> generateParkingDurationReport(Connection conn, int year, int month) {
	    List<ParkingDurationRecord> report = new ArrayList<>();

	    String sql = """
	        SELECT DAY(entry_date) AS day,
	               SUM(TIMESTAMPDIFF(MINUTE, CONCAT(entry_date, ' ', entry_time), CONCAT(exit_date, ' ', exit_time))) AS total_duration,
	               SUM(late_duration) AS total_late,
	               SUM(extended_duration) AS total_extended
	        FROM parking_history
	        WHERE YEAR(entry_date) = ? AND MONTH(entry_date) = ?
	        GROUP BY DAY(entry_date)
	        ORDER BY DAY(entry_date)
	    """;

	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setInt(1, year);
	        stmt.setInt(2, month);
	        ResultSet rs = stmt.executeQuery();

	        Map<Integer, ParkingDurationRecord> durations = new HashMap<>();
	        while (rs.next()) {
	            int day = rs.getInt("day");
	            int total = rs.getInt("total_duration");
	            int late = rs.getInt("total_late");
	            int extended = rs.getInt("total_extended");

	            durations.put(day, new ParkingDurationRecord(day, total, late, extended));
	        }

	        int daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth();
	        for (int day = 1; day <= daysInMonth; day++) {
	            ParkingDurationRecord record = durations.getOrDefault(day, new ParkingDurationRecord(day, 0, 0, 0));
	            report.add(record);
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return report;
	}


	/**
     * Generates a member status report for a given month and year.
     * The report includes, for each day, the number of distinct subscribers
     * who entered the parking lot on that day.
     *
     * @param conn  A valid Connection to the database.
     * @param year  The year for the report.
     * @param month The month for the report.
     * @return A list of DailySubscriberCount objects, one for each day of the month.
     */
	public List<DailySubscriberCount> generateMemberStatusReport(Connection conn, int year, int month) {
	    List<DailySubscriberCount> report = new ArrayList<>();

	    String query = "SELECT entry_date, COUNT(DISTINCT subscriber_id) AS active_subscribers " +
	                   "FROM parking_history " +
	                   "WHERE MONTH(entry_date) = ? AND YEAR(entry_date) = ? " +
	                   "GROUP BY entry_date ORDER BY entry_date";

	    try (PreparedStatement stmt = conn.prepareStatement(query)) {
	        stmt.setInt(1, month);
	        stmt.setInt(2, year);

	        ResultSet rs = stmt.executeQuery();
	        Map<Integer, Integer> subscribersPerDay = new HashMap<>();

	        while (rs.next()) {
	            LocalDate date = rs.getDate("entry_date").toLocalDate();
	            subscribersPerDay.put(date.getDayOfMonth(), rs.getInt("active_subscribers"));
	        }

	        int daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth();
	        for (int day = 1; day <= daysInMonth; day++) {
	            int count = subscribersPerDay.getOrDefault(day, 0);
	            report.add(new DailySubscriberCount(day, count));
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return report;
	}

}
