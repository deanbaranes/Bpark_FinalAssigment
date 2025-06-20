package common;

import java.io.Serializable;

/**
 * Represents an active parking record as stored in the database.
 * This class contains details about a current parking session, including
 * subscriber ID, entry time, expected exit time, parking spot, and whether the parking
 * duration has been extended.
 */
public class ActiveParking implements Serializable {
    private static final long serialVersionUID = 1L;

    private String parkingCode;
    private int subscriberId;
    private String entryDate;
    private String entryTime;
    private String expectedExitDate;
    private String expectedExitTime;
    private String parkingSpot;
    private boolean extended;

    /**
     * Constructs a new ActiveParking instance with the specified details.
     *
     * @param parkingCode        Unique code identifying the parking session.
     * @param subscriberId       The ID of the subscriber using the parking spot.
     * @param entryDate          The entry date in format yyyy-MM-dd.
     * @param entryTime          The entry time in format HH:mm.
     * @param expectedExitDate   The expected exit date in format yyyy-MM-dd.
     * @param expectedExitTime   The expected exit time in format HH:mm.
     * @param parkingSpot        The number or identifier of the parking spot.
     * @param extended           Whether the parking session has been extended.
     */
    public ActiveParking(String parkingCode, int subscriberId, String entryDate, String entryTime,
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

    /**
     * Returns the unique parking code.
     *
     * @return The parking code.
     */
    public String getParkingCode() { return parkingCode; }
    
    /**
     * Returns the ID of the subscriber who owns the parking session.
     *
     * @return The subscriber ID.
     */
    public int getSubscriberId() { return subscriberId; }
    
    /**
     * Returns the date the parking session started.
     *
     * @return The entry date.
     */
    public String getEntryDate() { return entryDate; }
    
    /**
     * Returns the time the parking session started.
     *
     * @return The entry time.
     */ 
    public String getEntryTime() { return entryTime; }
    
    /**
     * Returns the expected date of exit from the parking spot.
     *
     * @return The expected exit date.
     */
    public String getExpectedExitDate() { return expectedExitDate; }
    
    /**
     * Returns the expected time of exit from the parking spot.
     *
     * @return The expected exit time.
     */
    public String getExpectedExitTime() { return expectedExitTime; }
    
    /**
     * Returns the identifier of the parking spot used.
     *
     * @return The parking spot identifier.
     */
    public String getParkingSpot() { return parkingSpot; }
    
    /**
     * Indicates whether the parking session was extended.
     *
     * @return {@code true} if extended, {@code false} otherwise.
     */
    public boolean isExtended() { return extended; }

    
    /**
     * Returns a string representation of the active parking session.
     *
     * @return A formatted string with all the parking session details.
     */
    @Override
    public String toString() {
        return String.format("Code: %d | Subscriber: %d | Entry: %s %s | Exit: %s %s | Spot: %s | Extended: %s",
                parkingCode, subscriberId, entryDate, entryTime, expectedExitDate, expectedExitTime,
                parkingSpot, extended ? "Yes" : "No");
    }
    
    /**
     * Sets the extension status of the parking session.
     *
     * @param extended {@code true} if the session has been extended; {@code false} otherwise.
     */
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
