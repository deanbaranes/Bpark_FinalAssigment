package response;

import java.io.Serializable;
import java.util.List;

/**
 * A response sent from the server to the client containing a list of
 *  ParkingDurationRecord objects.
 * This response provides parking duration data for a specific month,
 * including total duration, late times, and extensions per day.
 * It is typically used for generating managerial reports and charts.
 */
public class ParkingDurationResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<ParkingDurationRecord> records;

    /**
     * Constructs a ParkingDurationResponse with the given list of parking duration records.
     *
     * @param records A list of ParkingDurationRecord objects representing daily data.
     */
    public ParkingDurationResponse(List<ParkingDurationRecord> records) {
        this.records = records;
    }

    /**
     * Returns the list of parking duration records.
     *
     * @return A list of ParkingDurationRecord objects.
     */
    public List<ParkingDurationRecord> getRecords() {
        return records;
    }
}
