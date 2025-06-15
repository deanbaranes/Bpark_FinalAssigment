package common;

import java.io.Serializable;

/**
 * Represents a record of parking duration for reporting purposes.
 * Includes total duration, late duration, and extension duration.
 */
public class ParkingDurationRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private int dayOfMonth;
    private int duration;          // in minutes
    private int lateDuration;      // in minutes
    private int extendedDuration;  // in minutes

    public ParkingDurationRecord(int dayOfMonth, int duration, int lateDuration, int extendedDuration) {
        this.dayOfMonth = dayOfMonth;
        this.duration = duration;
        this.lateDuration = lateDuration;
        this.extendedDuration = extendedDuration;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public int getDuration() {
        return duration;
    }

    public int getLateDuration() {
        return lateDuration;
    }

    public int getExtendedDuration() {
        return extendedDuration;
    }
}
