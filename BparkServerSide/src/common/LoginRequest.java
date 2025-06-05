/*
Represents a login request sent by a subscriber client.
Contains the subscriber's full name and subscription code.
Implements Serializable for transmission between client and server.
Used for authentication on both client and server sides.
*/

package common;

import java.io.Serializable;

public class LoginRequest implements Serializable {
    private String fullName;
    private String subscriptionCode;

    public LoginRequest(String fullName, String subscriptionCode) {
        this.fullName = fullName;
        this.subscriptionCode = subscriptionCode;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSubscriptionCode() {
        return subscriptionCode;
    }
}

