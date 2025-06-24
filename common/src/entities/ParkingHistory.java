package entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a single parking history record for a subscriber.
 * This class contains data about a past parking event, including entry/exit
 * dates and times, vehicle number, and the subscriber ID. It is commonly used
 * to display past parking activity in the client application.
 */
public class ParkingHistory implements Serializable {

    private int historyId;
    private String subscriberId;
    private String vehicleNumber;
    private LocalDate entryDate;
    private LocalTime entryTime;
    private LocalDate exitDate;
    private LocalTime exitTime;

    /**
     * Constructs a ParkingHistory record with the provided values.
     *
     * @param historyId      Unique identifier of the history record.
     * @param subscriberId   The ID of the subscriber.
     * @param vehicleNumber  The vehicle's license plate number.
     * @param entryDate2     The date of entry to the parking lot.
     * @param entryTime2     The time of entry to the parking lot.
     * @param exitDate2      The date of exit from the parking lot.
     * @param exitTime2      The time of exit from the parking lot.
     */
    public ParkingHistory(int historyId, String subscriberId, String vehicleNumber,
                          LocalDate entryDate2, LocalTime entryTime2,
                          LocalDate exitDate2, LocalTime exitTime2) {
        this.historyId = historyId;
        this.subscriberId = subscriberId;
        this.vehicleNumber = vehicleNumber;
        this.entryDate = entryDate2;
        this.entryTime = entryTime2;
        this.exitDate = exitDate2;
        this.exitTime = exitTime2;
    }

    /**
     * Returns the unique history ID.
     *
     * @return The history ID.
     */
    public int getHistoryId() {
        return historyId;
    }

    /**
     * Returns the subscriber's ID.
     *
     * @return The subscriber ID.
     */
    public String getSubscriberId() {
        return subscriberId;
    }

    /**
     * Returns the vehicle's license plate number.
     *
     * @return The vehicle number.
     */
    public String getVehicleNumber() {
        return vehicleNumber;
    }

    /**
     * Returns the entry date of the parking session.
     *
     * @return The entry date.
     */
    public LocalDate getEntryDate() {
        return entryDate;
    }

    /**
     * Returns the entry time of the parking session.
     *
     * @return The entry time.
     */
    public LocalTime getEntryTime() {
        return entryTime;
    }

    /**
     * Returns the exit date of the parking session.
     *
     * @return The exit date.
     */
    public LocalDate getExitDate() {
        return exitDate;
    }

    /**
     * Returns the exit time of the parking session.
     *
     * @return The exit time.
     */
    public LocalTime getExitTime() {
        return exitTime;
    }

    /**
     * Returns a formatted string representing the parking history record.
     *
     * @return A string including vehicle number, entry, and exit times.
     */
    @Override
    public String toString() {
    	return String.format(
    	        "Vehicle Number: %s | Entry: %s at %s | Exit: %s at %s\n",
    	        vehicleNumber,
    	        entryDate,
    	        entryTime,
    	        exitDate,
    	        exitTime
    	    );
    }
}
