package common;

import java.io.Serializable;

public class LoginManagement implements Serializable {
    private String username;
    private String password;

    public LoginManagement(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
