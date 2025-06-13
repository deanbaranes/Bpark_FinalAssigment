package common;

import java.io.Serializable;

/**
 * Represents an active parking record as stored in the database..
 */
public class ActiveParking implements Serializable {
    private static final long serialVersionUID = 1L;

    private int parkingCode;
    private int subscriberId;
    private String entryDate;
    private String entryTime;
    private String expectedExitDate;
    private String expectedExitTime;
    private String parkingSpot;
    private boolean extended;

    public ActiveParking(int parkingCode, int subscriberId, String entryDate, String entryTime,
                         String expectedExitDate, String expectedExitTime, String parkingSpot, boolean extended) {
        this.parkingCode = parkingCode;
        this.subscriberId = subscriberId;
        this.entryDate = entryDate;
        this.entryTime = entryTime;
        this.expectedExitDate = expectedExitDate;
        this.expectedExitTime = expectedExitTime;
        this.parkingSpot = parkingSpot;
        this.extended = extended;
    }

    public int getParkingCode() { return parkingCode; }
    public int getSubscriberId() { return subscriberId; }
    public String getEntryDate() { return entryDate; }
    public String getEntryTime() { return entryTime; }
    public String getExpectedExitDate() { return expectedExitDate; }
    public String getExpectedExitTime() { return expectedExitTime; }
    public String getParkingSpot() { return parkingSpot; }
    public boolean isExtended() { return extended; }

    @Override
    public String toString() {
        return String.format("Code: %d | Subscriber: %d | Entry: %s %s | Exit: %s %s | Spot: %s | Extended: %s",
                parkingCode, subscriberId, entryDate, entryTime, expectedExitDate, expectedExitTime,
                parkingSpot, extended ? "Yes" : "No");
    }
    public void setExtended(boolean extended) {
        this.extended = extended;
    }
    
    /**
     * Updates the expected exit time for the parking session.
     *
     * @param expectedExitTime The new expected exit time in HH:mm format.
     */
    public void setExpectedExitTime(String expectedExitTime) {
        this.expectedExitTime = expectedExitTime;
    }

}
