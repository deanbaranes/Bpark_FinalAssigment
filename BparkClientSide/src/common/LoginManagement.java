/**
 * A serializable data class used to encapsulate login credentials
 * (username and password) for management users.
 * 
 * This class was added to the 'common' package so it can be shared 
 * between both the client and server sides. Its purpose is to enable
 * the transfer of two parameters (username and password) as a single
 * object when performing authentication checks.
 */

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
