package entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a reservation made by a subscriber for a parking spot.
 * This class is used both to display upcoming reservations in the client GUI
 * and to send new reservation requests from the client to the server.
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

    /**
     * Constructs a fully populated Reservation instance.
     * This constructor is typically used when loading existing reservations from the database.
     *
     * @param reservationId The unique ID of the reservation.
     * @param subscriberId  The ID of the subscriber who made the reservation.
     * @param parkingCode   The parking code associated with the reservation.
     * @param entryDate     The date the reservation begins.
     * @param entryTime     The time the reservation begins.
     * @param exitDate      The date the reservation ends.
     * @param exitTime      The time the reservation ends.
     * @param parkingSpot   The parking spot number assigned to the reservation.
     */
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
    
    /**
     * Constructs a Reservation for sending a new reservation request
     * from the client to the server, with only subscriber ID and entry time.
     *
     * @param subscriberId The ID of the subscriber.
     * @param entryDate    The requested entry date.
     * @param entryTime    The requested entry time.
     */
    public Reservation(String subscriberId, LocalDate entryDate, LocalTime entryTime) {
        this.subscriberId = subscriberId;
        this.entryDate = entryDate;
        this.entryTime = entryTime;
    }

    /**
     * Returns the reservation ID.
     *
     * @return The reservation ID.
     */
    public int getReservationId() {
        return reservationId;
    }

    /**
     * Returns the subscriber ID.
     *
     * @return The subscriber ID.
     */
    public String getSubscriberId() {
        return subscriberId;
    }

    /**
     * Returns the parking code associated with the reservation.
     *
     * @return The parking code.
     */
    public String getParkingCode() {
        return parkingCode;
    }

    /**
     * Returns the entry date of the reservation.
     *
     * @return The entry date.
     */
    public LocalDate getEntryDate() {
        return entryDate;
    }

    /**
     * Returns the entry time of the reservation.
     *
     * @return The entry time.
     */
    public LocalTime getEntryTime() {
        return entryTime;
    }

    /**
     * Returns the exit date of the reservation.
     *
     * @return The exit date.
     */
    public LocalDate getExitDate() {
        return exitDate;
    }

    /**
     * Returns the exit time of the reservation.
     *
     * @return The exit time.
     */
    public LocalTime getExitTime() {
        return exitTime;
    }

    /**
     * Returns the assigned parking spot number.
     *
     * @return The parking spot number.
     */
    public int getParkingSpot() {
        return parkingSpot;
    }

    /**
     * Returns a formatted string summarizing the reservation.
     *
     * @return A multi-line string including reservation ID, parking code, spot, and times.
     */
    @Override
    public String toString() {
    	return String.format(
    	        "Parking Code: %s | From: %s at %s, until: %s at %s\n",
    	        parkingCode,
    	        entryDate,
    	        entryTime,
    	        exitDate,
    	        exitTime
    	    );
    }
}
