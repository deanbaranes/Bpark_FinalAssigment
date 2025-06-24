package request;

import java.io.Serializable;

/**
 * A request sent from the client to the server, asking for a parking duration report
 * for a specific year and month.
 * This request is typically used by management to generate monthly reports
 * that analyze parking usage, late durations, and extension patterns.
 */ 
public class ParkingDurationRequest implements Serializable {

	private int year;
    private int month;

    /**
     * Constructs a {@code ParkingDurationRequest} with the specified year and month.
     *
     * @param year  The year of the requested report (e.g., 2025).
     * @param month The month of the requested report (1–12).
     */
    public ParkingDurationRequest(int year, int month) {
        this.year = year;
        this.month = month;
    }

    /**
     * Returns the year for which the report is requested.
     *
     * @return The year value.
     */
    public int getYear() {
        return year;
    }

    /**
     * Returns the month for which the report is requested.
     *
     * @return The month value (1–12).
     */
    public int getMonth() {
        return month;
    }
}
