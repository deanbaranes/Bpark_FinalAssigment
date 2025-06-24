package request;

import java.io.Serializable;

/**
 * Represents a request to generate a monthly report about subscriber activity.
 * This request includes the year and month for which the status report should be generated.
 */
public class MemberStatusReportRequest implements Serializable {
    private int year;
    private int month;

    /**
     * Constructs a new MemberStatusReportRequest with the specified year and month.
     *
     * @param year  The year for the report.
     * @param month The month for the report.
     */
    public MemberStatusReportRequest(int year, int month) {
        this.year = year;
        this.month = month;
    }

    /**
     * Returns the year for the report.
     *
     * @return The year value.
     */
    public int getYear() {
        return year;
    }

    /**
     * Returns the month for the report.
     *
     * @return The month value (1–12).
     */
    public int getMonth() {
        return month;
    }
}