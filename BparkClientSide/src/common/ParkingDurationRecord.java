package common;

import java.io.Serializable;

/**
 * Represents a record of parking duration for reporting purposes.
 * Includes total duration, late duration, and whether the parking was extended.
 */
public class ParkingDurationRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String parkingCode;
    private int duration;
    private int lateDuration;
    private int parkingSpot;
    private int extendedDuration; // Duration of extension in minutes


    public ParkingDurationRecord(String parkingCode, int duration, int lateDuration,  int extended, int parkingSpot) {
        this.parkingCode = parkingCode;
        this.duration = duration;
        this.lateDuration = lateDuration;
        this.extendedDuration = extended;
        this.parkingSpot = parkingSpot;
        
    }
    public int getParkingSpot() {
        return parkingSpot;
    }


    public String getParkingCode() {
        return parkingCode;
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
    private int extended;

    public int getExtended() {
        return extended;
    }

}
