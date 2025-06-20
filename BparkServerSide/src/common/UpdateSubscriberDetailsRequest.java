package common;

import java.io.Serializable;


/**
 * Represents a request sent by a client to update a subscriber's contact details.
 * This request includes the subscriber's ID and new values for their email and/or phone number.
 * It is sent to the server through the OCSF framework and handled by the server-side logic.
 */
public class UpdateSubscriberDetailsRequest implements Serializable {
    private String subscriber_id;
    private String newEmail;
    private String newPhone;

    /**
     * Constructs an UpdateSubscriberDetailsRequest with the given subscriber ID,
     * new email address, and new phone number.
     *
     * @param subscriber_id The ID of the subscriber whose details are to be updated.
     * @param newEmail      The new email address (can be {@code null} if unchanged).
     * @param newPhone      The new phone number (can be {@code null} if unchanged).
     */
    public UpdateSubscriberDetailsRequest(String subscriber_id, String newEmail, String newPhone) {
        this.subscriber_id = subscriber_id;
        this.newEmail = newEmail;
        this.newPhone = newPhone;
    }

    /**
     * Returns the ID of the subscriber to be updated.
     *
     * @return The subscriber ID.
     */
    public String getSubscriberId() {
        return subscriber_id;
    }

    /**
     * Returns the new email address for the subscriber.
     *
     * @return The new email address.
     */
    public String getNewEmail() {
        return newEmail;
    }

    /**
     * Returns the new phone number for the subscriber.
     *
     * @return The new phone number.
     */
    public String getNewPhone() {
        return newPhone;
    }
}
