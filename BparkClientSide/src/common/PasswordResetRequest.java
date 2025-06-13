package common;

import java.io.Serializable;

public class PasswordResetRequest implements Serializable {
	private String PWtype;
    private final String email;
    public PasswordResetRequest(String email,String PWtype)
    { 
    	this.email = email; 
    	this.PWtype = PWtype;
    	
    }
    public String getEmail() { return email; }
    public String getPWtype() { return PWtype; }
}
//*
