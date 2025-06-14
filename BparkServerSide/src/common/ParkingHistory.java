package common;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a single parking history record for a subscriber.
 * Used to display past parking events in the client GUI.
 */
public class ParkingHistory implements Serializable {

    private int historyId;
    private String subscriberId;
    private String vehicleNumber;
    private LocalDate entryDate;
    private LocalTime entryTime;
    private LocalDate exitDate;
    private LocalTime exitTime;

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

    public int getHistoryId() {
        return historyId;
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public LocalTime getEntryTime() {
        return entryTime;
    }

    public LocalDate getExitDate() {
        return exitDate;
    }

    public LocalTime getExitTime() {
        return exitTime;
    }

    @Override
    public String toString() {
        return String.format("Vehicle: %s\nEntry: %s %s\nExit: %s %s\n",
                vehicleNumber, entryDate, entryTime, exitDate, exitTime);
    }
}
