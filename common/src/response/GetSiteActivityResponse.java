package response;

import java.io.Serializable;
import java.util.List;

import entities.ActiveParking;
import entities.Reservation;

/**
 * A response object sent from the server containing:
 * A list of future reservations.
 * A list of currently active parkings.
 * This object is used to provide an overview of the site's current and upcoming activity,
 * for management/monitoring purposes.
 */
public class GetSiteActivityResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Reservation> futureReservations;
    private List<ActiveParking> activeParkings;

    /**
     * Constructs a {@code GetSiteActivityResponse} with the given lists of future reservations
     * and active parking sessions.
     *
     * @param futureReservations A list of upcoming Reservation objects.
     * @param activeParkings     A list of currently active ActiveParking objects.
     */
    public GetSiteActivityResponse(List<Reservation> futureReservations, List<ActiveParking> activeParkings) {
        this.futureReservations = futureReservations;
        this.activeParkings = activeParkings;
    }

    /**
     * Returns the list of future reservations.
     *
     * @return A list of {@link Reservation} objects.
     */
    public List<Reservation> getFutureReservations() {
        return futureReservations;
    }

    /**
     * Returns the list of currently active parking sessions.
     *
     * @return A list of {@link ActiveParking} objects.
     */
    public List<ActiveParking> getActiveParkings() {
        return activeParkings;
    }
}
