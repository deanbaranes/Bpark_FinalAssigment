package common;
 
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Represents a request to update an existing reservation's date and time.
 */
public class UpdateReservationRequest implements Serializable {
    private final int reservationId;
    private final LocalDate newDate;
    private final LocalTime newTime;

    public UpdateReservationRequest(int reservationId, LocalDate newDate, LocalTime newTime) {
        this.reservationId = reservationId;
        this.newDate = newDate;
        this.newTime = newTime;
    }

    public int getReservationId() {
        return reservationId;
    }

    public LocalDate getNewDate() {
        return newDate;
    }

    public LocalTime getNewTime() {
        return newTime;
    }
}
