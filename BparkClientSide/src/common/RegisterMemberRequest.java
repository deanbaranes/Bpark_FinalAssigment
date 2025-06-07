package common;

import java.io.Serializable;

/**
 * This class represents a request to register a new subscriber.
 * It contains all necessary fields to create a new member entry in the database.
 * Sent from client to server through the OCSF architecture.
 */
public class RegisterMemberRequest implements Serializable {
    private String firstName;
    private String lastName;
    private String idNumber;
    private String email;
    private String phoneNumber;
    private String vehicleNumber;
    private String vehicleNumber2;
    private String creditCard;


    public RegisterMemberRequest(String firstName, String lastName, String idNumber,
            String email, String phoneNumber, String vehicleNumber, String vehicleNumber2,
            String creditCard) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.idNumber = idNumber;
			this.email = email;
			this.phoneNumber = phoneNumber;
			this.vehicleNumber = vehicleNumber;
		    this.vehicleNumber2 = vehicleNumber2;
			this.creditCard = creditCard;
}

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getIdNumber() { return idNumber; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getVehicleNumber2() {return vehicleNumber2;}
    public String getCreditCard() {return creditCard;}

}
