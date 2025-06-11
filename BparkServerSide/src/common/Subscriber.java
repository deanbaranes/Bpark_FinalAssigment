package common;

import java.io.Serializable;

public class Subscriber implements Serializable {
    private String subscriber_id;
    private String full_name;
    private String email;
    private String phone;
    private String vehicle_number1;
    private String subscription_code;
    private int late_count;
    private String credit_card;

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


    public String getSubscription_code() {
        return subscription_code;
    }

    public int getLateCount() {
        return late_count;
    }

    public String getCredit_card() {
        return credit_card;
    }

}
