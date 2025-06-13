/*
Represents a login request sent by a subscriber client.
Contains the subscriber's ID, subscription code, and source (e.g., terminal/app).
Implements Serializable for transmission between client and server.
Used for authentication on both client and server sides..
*/

package common;

import java.io.Serializable;

public class LoginRequest implements Serializable {
    private String ID;
    private String subscriptionCode;
    private String source; // "terminal" or "app"

    public LoginRequest(String ID, String subscriptionCode, String source) {
        this.ID = ID;
        this.subscriptionCode = subscriptionCode;
        this.source = source;
    }

    public String getID() {
        return ID;
    }

    public String getSubscriptionCode() {
        return subscriptionCode;
    }

    public String getSource() {
        return source;
    }
}
