package common;
 
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a request to update the date and time of an existing reservation.
 * This request is typically sent from the client to the server when a subscriber
 * wishes to modify the timing of a previously made reservation.
 */
public class UpdateReservationRequest implements Serializable {
    private final int reservationId;
    private final LocalDate newDate;
    private final LocalTime newTime;

    /**
     * Constructs an UpdateReservationRequest with the reservation ID,
     * and the new desired date and time.
     *
     * @param reservationId The ID of the reservation to update.
     * @param newDate       The new entry date to set for the reservation.
     * @param newTime       The new entry time to set for the reservation.
     */
    public UpdateReservationRequest(int reservationId, LocalDate newDate, LocalTime newTime) {
        this.reservationId = reservationId;
        this.newDate = newDate;
        this.newTime = newTime;
    }

    /**
     * Returns the ID of the reservation to be updated.
     *
     * @return The reservation ID.
     */
    public int getReservationId() {
        return reservationId;
    }

    /**
     * Returns the new entry date for the reservation.
     *
     * @return The new date.
     */
    public LocalDate getNewDate() {
        return newDate;
    }

    /**
     * Returns the new entry time for the reservation.
     *
     * @return The new time.
     */
    public LocalTime getNewTime() {
        return newTime;
    }
}
