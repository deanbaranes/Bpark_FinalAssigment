package common;

import java.io.Serializable;

/**
 * Represents a record of parking duration for a specific day, used for reporting and analytics.
 * This includes the total parking duration, late duration (if the vehicle overstayed),
 * and the duration of any approved extension.
 */
public class ParkingDurationRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private int dayOfMonth;
    private int duration;          // in minutes
    private int lateDuration;      // in minutes
    private int extendedDuration;  // in minutes

    /**
     * Constructs a ParkingDurationRecord with the given duration metrics.
     *
     * @param dayOfMonth       The day of the month this record refers to (1–31).
     * @param duration         The total parking duration in minutes.
     * @param lateDuration     The amount of time the vehicle overstayed, in minutes.
     * @param extendedDuration The approved extension duration, in minutes.
     */
    public ParkingDurationRecord(int dayOfMonth, int duration, int lateDuration, int extendedDuration) {
        this.dayOfMonth = dayOfMonth;
        this.duration = duration;
        this.lateDuration = lateDuration;
        this.extendedDuration = extendedDuration;
    }

    /**
     * Returns the day of the month this record refers to.
     *
     * @return The day of the month (1–31).
     */
    public int getDayOfMonth() {
        return dayOfMonth;
    }

    /**
     * Returns the total parking duration in minutes.
     *
     * @return Total duration in minutes.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Returns the amount of time the vehicle overstayed beyond the allowed duration.
     *
     * @return Late duration in minutes.
     */
    public int getLateDuration() {
        return lateDuration;
    }

    /**
     * Returns the duration that was officially extended and approved.
     *
     * @return Extended duration in minutes.
     */
    public int getExtendedDuration() {
        return extendedDuration;
    }
}
