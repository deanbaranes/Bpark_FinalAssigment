package common;

import java.io.Serializable;

/**
 * Represents an order in the system.
 * This class is used to encapsulate all order data and send it as an object.
 */
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private int parkingSpace;
    private int orderNumber;
    private String orderDate;
    private int confirmationCode;
    private int subscriberId;
    private String dateOfPlacingOrder;

    public Order(int parkingSpace, int orderNumber, String orderDate,
                 int confirmationCode, int subscriberId, String dateOfPlacingOrder) {
        this.parkingSpace = parkingSpace;
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.confirmationCode = confirmationCode;
        this.subscriberId = subscriberId;
        this.dateOfPlacingOrder = dateOfPlacingOrder;
    }

    public int getParkingSpace() {
        return parkingSpace;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public int getConfirmationCode() {
        return confirmationCode;
    }

    public int getSubscriberId() {
        return subscriberId;
    }

    public String getDateOfPlacingOrder() {
        return dateOfPlacingOrder;
    }

    @Override
    public String toString() {
        return "Order# " + orderNumber +
               " | Parking: " + parkingSpace +
               " | Date: " + orderDate +
               " | Confirmation code: " + confirmationCode +
               " | Subscriber: " + subscriberId +
               " | Placed: " + dateOfPlacingOrder;
    }
}
