package common;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a reservation made by a subscriber.
 * Used to show upcoming or existing reservations in the client GUI.
 */
public class Reservation implements Serializable {

    private int reservationId;
    private String subscriberId;
    private String parkingCode;
    private LocalDate entryDate;
    private LocalTime entryTime;
    private LocalDate exitDate;
    private LocalTime exitTime;
    private int parkingSpot;

    public Reservation(int reservationId, String subscriberId, String parkingCode,
                       LocalDate entryDate, LocalTime entryTime,
                       LocalDate exitDate, LocalTime exitTime, int parkingSpot) {
        this.reservationId = reservationId;
        this.subscriberId = subscriberId;
        this.parkingCode = parkingCode;
        this.entryDate = entryDate;
        this.entryTime = entryTime;
        this.exitDate = exitDate;
        this.exitTime = exitTime;
        this.parkingSpot = parkingSpot;
    }
    
    // Constructor used for sending new reservation requests from the client
    public Reservation(String subscriberId, LocalDate entryDate, LocalTime entryTime) {
        this.subscriberId = subscriberId;
        this.entryDate = entryDate;
        this.entryTime = entryTime;
    }


    public int getReservationId() {
        return reservationId;
    }

    public String getSubscriberId() {
        return subscriberId;
    }

    public String getParkingCode() {
        return parkingCode;
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

    public int getParkingSpot() {
        return parkingSpot;
    }

    @Override
    public String toString() {
        return String.format("Reservation #%d\nParking: %s (Spot %d)\nFrom: %s %s\nTo: %s %s\n",
                reservationId, parkingCode, parkingSpot,
                entryDate, entryTime, exitDate, exitTime);
    }
}
