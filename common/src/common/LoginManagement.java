package common;


import java.io.Serializable;

/**
 * Represents login credentials for a management-level user in the system.
 * This object is typically used to authenticate administrative or terminal users.
 */
public class LoginManagement implements Serializable {
    private String username;
    private String password;

    /**
     * Constructs a LoginManagement instance with the specified username and password.
     *
     * @param username The username used for login.
     * @param password The corresponding password.
     */
    public LoginManagement(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the username associated with the login.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the password associated with the login.
     *
     * @return The password.
     */
    public String getPassword() {
        return password;
    }
}
