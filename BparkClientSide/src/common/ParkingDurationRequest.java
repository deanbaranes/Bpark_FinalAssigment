package common;

import java.io.Serializable;

/**
 * Request sent from client to server asking for parking duration report
 * for a specific year and month...
 */
public class ParkingDurationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String year;
    private String month;

    public ParkingDurationRequest(String year, String month) {
        this.year = year;
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }
}
