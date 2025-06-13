package common;

import java.io.Serializable;


/**
 * Represents a client request to update subscriber email and/or phone.
 * Contains the subscriber_id, and new values for email and phone.
 * Sent from client to server via OCSF and processed in EchoServer..
 */
public class UpdateSubscriberDetailsRequest implements Serializable {
    private String subscriber_id;
    private String newEmail;
    private String newPhone;

    public UpdateSubscriberDetailsRequest(String subscriber_id, String newEmail, String newPhone) {
        this.subscriber_id = subscriber_id;
        this.newEmail = newEmail;
        this.newPhone = newPhone;
    }

    public String getSubscriberId() {
        return subscriber_id;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public String getNewPhone() {
        return newPhone;
    }
}
