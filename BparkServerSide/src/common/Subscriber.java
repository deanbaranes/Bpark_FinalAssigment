package common;

import java.io.Serializable;

public class Subscriber implements Serializable {
    private String subscriber_id;
    private String full_name;
    private String email;
    private String phone;
    private String vehicle_number1;
    private String vehicle_number2;
    private String subscription_code;
    private String notes;
    private String credit_card;

    public Subscriber(String subscriber_id, String full_name, String email, String phone,
                      String vehicle_number1, String vehicle_number2, String subscription_code,
                      String notes, String credit_card) {
        this.subscriber_id = subscriber_id;
        this.full_name = full_name;
        this.email = email;
        this.phone = phone;
        this.vehicle_number1 = vehicle_number1;
        this.vehicle_number2 = vehicle_number2;
        this.subscription_code = subscription_code;
        this.notes = notes;
        this.credit_card = credit_card;
    }

    public String getSubscriber_id() {
        return subscriber_id;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getVehicle_number1() {
        return vehicle_number1;
    }

    public String getVehicle_number2() {
        return vehicle_number2;
    }

    public String getSubscription_code() {
        return subscription_code;
    }

    public String getNotes() {
        return notes;
    }

    public String getCredit_card() {
        return credit_card;
    }

}
