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

