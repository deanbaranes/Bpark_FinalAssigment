package common;

import java.io.Serializable;

/**
 * Represents a password reset request sent from the client to the server.
 * This request includes the user's email address and the type of password to reset
 * (subscriber password, parking code, or employee password).
 */
public class PasswordResetRequest implements Serializable {
	private String PWtype;
    private final String email;
    
    /**
     * Constructs a PasswordResetRequest with the specified email and password type.
     *
     * @param email  The email address associated with the account.
     * @param PWtype The type of password to reset (e.g., "subscriber", "employee", "parking").
     */
    public PasswordResetRequest(String email,String PWtype)
    { 
    	this.email = email; 
    	this.PWtype = PWtype;
    	
    }
    
    /**
     * Returns the email address to which the reset is associated.
     * @return The email address.
     */
    public String getEmail() { return email; }
    
    /**
     * Returns the type of password that should be reset.
     * @return The password type string.
     */
    public String getPWtype() { return PWtype; }
}
