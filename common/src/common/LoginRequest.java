package common;

import java.io.Serializable;

/**
 * Represents a login request sent from a client to the server.
 * This object contains the subscriber's ID, subscription code,
 * and the source of the login (from a terminal or a mobile app).
 */
public class LoginRequest implements Serializable {
    private String ID;
    private String subscriptionCode;
    private String source; // "terminal" or "app"

    /**
     * Constructs a new LoginRequest with the provided ID, subscription code, and source.
     *
     * @param ID                The unique subscriber ID attempting to log in.
     * @param subscriptionCode  The subscriber's access code.
     * @param source            The source of the request ("terminal" or "app").
     */
    public LoginRequest(String ID, String subscriptionCode, String source) {
        this.ID = ID;
        this.subscriptionCode = subscriptionCode;
        this.source = source;
    }

    /**
     * Returns the subscriber's ID.
     *
     * @return The ID string.
     */
    public String getID() {
        return ID;
    }

    /**
     * Returns the subscription code provided in the login request.
     *
     * @return The subscription code.
     */
    public String getSubscriptionCode() {
        return subscriptionCode;
    }

    /**
     * Returns the source of the login request.
     * Possible values: {@code "terminal"}, {@code "app"}.
     *
     * @return The login source string.
     */
    public String getSource() {
        return source;
    }
}
