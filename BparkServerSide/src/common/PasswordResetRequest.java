package common;

import java.io.Serializable;

public class PasswordResetRequest implements Serializable {
    private final String email;
    public PasswordResetRequest(String email) { this.email = email; }
    public String getEmail() { return email; }
}
