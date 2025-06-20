package common;

import java.io.Serializable;

/**
 * Represents a registered subscriber in the BPARK system.
 * This class includes personal details, contact information, vehicle data,
 * subscription code, and usage history such as late arrivals.
 * It is used throughout the system for login, profile display, and validation.
 */
public class Subscriber implements Serializable {
    private String subscriber_id;
    private String full_name;
    private String email;
    private String phone;
    private String vehicle_number1;
    private String subscription_code;
    private int late_count;
    private String credit_card;

    /**
     * Constructs a fully initialized Subscriber with all fields.
     * Typically used when retrieving subscriber data from the database.
     *
     * @param subscriber_id     The unique ID of the subscriber.
     * @param full_name         The subscriber's full name.
     * @param email             The subscriber's email address.
     * @param phone             The subscriber's phone number.
     * @param vehicle_number1   The license plate of the subscriber's vehicle.
     * @param subscription_code The unique subscription code for login.
     * @param late_count        The number of times the subscriber was late.
     * @param credit_card       The credit card number associated with the account.
     */
    public Subscriber(String subscriber_id, String full_name, String email, String phone,
                      String vehicle_number1, String subscription_code,
                      int late_count, String credit_card) {
        this.subscriber_id = subscriber_id;
        this.full_name = full_name;
        this.email = email;
        this.phone = phone;
        this.vehicle_number1 = vehicle_number1;
        this.subscription_code = subscription_code;
        this.late_count = late_count;
        this.credit_card = credit_card;
    }
    
    /**
     * Constructs a Subscriber using only ID and subscription code.
     * @param subscriber_id     The subscriber's ID.
     * @param subscription_code The subscriber's subscription code.
     */
    public Subscriber(String subscriber_id, String subscription_code)
    {
        this.subscriber_id = subscriber_id;
        this.subscription_code = subscription_code;
        this.full_name = null;
        this.email = null;
        this.phone = null;
        this.vehicle_number1 = null;
        this.late_count = 0;
        this.credit_card = null;
    }

    /**
     * Returns the subscriber's ID.
     *
     * @return The subscriber ID.
     */
    public String getSubscriber_id() {
        return subscriber_id;
    }

    /**
     * Returns the full name of the subscriber.
     *
     * @return The full name.
     */
    public String getFull_name() {
        return full_name;
    }

    /**
     * Returns the email address of the subscriber.
     *
     * @return The email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the phone number of the subscriber.
     *
     * @return The phone number.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Returns the primary vehicle's license plate number.
     *
     * @return The vehicle number.
     */
    public String getVehicle_number1() {
        return vehicle_number1;
    }

    /**
     * Returns the subscription code for the subscriber.
     *
     * @return The subscription code.
     */
    public String getSubscription_code() {
        return subscription_code;
    }

    /**
     * Returns the number of times the subscriber has arrived late.
     *
     * @return The late count.
     */
    public int getLateCount() {
        return late_count;
    }

    /**
     * Returns the subscriber's credit card number.
     *
     * @return The credit card number.
     */
    public String getCredit_card() {
        return credit_card;
    }

}
