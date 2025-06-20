package common;

import java.io.Serializable;

/**
 * Represents a request to register a new subscriber (member) in the system.
 * This request is sent from the client to the server (via OCSF),
 * and contains all personal and vehicle information required to create a new
 * subscriber entry in the database.
 */
public class RegisterMemberRequest implements Serializable {
    private String firstName;
    private String lastName;
    private String idNumber;
    private String email;
    private String phoneNumber;
    private String vehicleNumber;
    private String creditCard;

    /**
     * Constructs a new RegisterMemberRequest with the given subscriber information.
     *
     * @param firstName     The first name of the subscriber.
     * @param lastName      The last name of the subscriber.
     * @param idNumber      The subscriber's ID number.
     * @param email         The subscriber's email address.
     * @param phoneNumber   The subscriber's phone number.
     * @param vehicleNumber The subscriber's vehicle license plate number.
     * @param creditCard    The subscriber's credit card number.
     */
    public RegisterMemberRequest(String firstName, String lastName, String idNumber,
            String email, String phoneNumber, String vehicleNumber, 
            String creditCard) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.idNumber = idNumber;
			this.email = email;
			this.phoneNumber = phoneNumber;
			this.vehicleNumber = vehicleNumber;
			this.creditCard = creditCard;
}
    /**
     * Returns the subscriber's first name.
     *
     * @return The first name.
     */
    public String getFirstName() { return firstName; }
    
    /**
     * Returns the subscriber's last name.
     *
     * @return The last name.
     */
    public String getLastName() { return lastName; }
    
    /**
     * Returns the subscriber's ID number.
     *
     * @return The ID number.
     */
    public String getIdNumber() { return idNumber; }
    
    /**
     * Returns the subscriber's email address.
     *
     * @return The email address.
     */
    public String getEmail() { return email; }
    
    /**
     * Returns the subscriber's phone number.
     *
     * @return The phone number.
     */
    public String getPhoneNumber() { return phoneNumber; }
    
    /**
     * Returns the subscriber's vehicle license plate number.
     *
     * @return The vehicle number.
     */
    public String getVehicleNumber() { return vehicleNumber; }
    
    /**
     * Returns the subscriber's credit card number.
     *
     * @return The credit card number.
     */
    public String getCreditCard() {return creditCard;}

}
