package common;

import java.io.Serializable;
import java.util.List;

/**
 * Response sent from server to client containing parking duration records
 */
public class ParkingDurationResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<ParkingDurationRecord> records;

    public ParkingDurationResponse(List<ParkingDurationRecord> records) {
        this.records = records;
    }

    public List<ParkingDurationRecord> getRecords() {
        return records;
    }
}
