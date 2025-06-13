package common;

import java.io.Serializable;
import java.util.List;

/**
 * A response from the server containing:
 * - a list of future reservations
 * - a list of currently active parkings.
 */
public class GetSiteActivityResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Reservation> futureReservations;
    private List<ActiveParking> activeParkings;

    public GetSiteActivityResponse(List<Reservation> futureReservations, List<ActiveParking> activeParkings) {
        this.futureReservations = futureReservations;
        this.activeParkings = activeParkings;
    }

    public List<Reservation> getFutureReservations() {
        return futureReservations;
    }

    public List<ActiveParking> getActiveParkings() {
        return activeParkings;
    }
}
