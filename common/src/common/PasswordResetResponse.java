package common;

import java.io.Serializable;

/**
 * Represents the server's response to a PasswordResetRequest.
 * This response indicates whether the password reset was successful
 * and includes a message explaining the result.
 */
public class PasswordResetResponse implements Serializable {
    private final boolean success;
    private final String message;
    
    /**
     * Constructs a PasswordResetResponse with the given success flag and message.
     *
     * @param success true if the password reset was successful; false otherwise.
     * @param message A message providing details about the result.
     */
    public PasswordResetResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    /**
     * Returns whether the password reset was successful.
     *
     * @return true if successful; false otherwise.
     */
    public boolean isSuccess() { return success; }
    
    /**
     * Returns the message associated with the response.
     *
     * @return A descriptive message explaining the result.
     */
    public String getMessage() { return message; }
}
