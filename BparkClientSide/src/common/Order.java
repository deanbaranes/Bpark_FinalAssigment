package common;

import java.io.Serializable;

/**
 * Represents an order in the parking management system.
 * This class is serializable so that it can be transmitted between server and client.
 */
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    private int parkingSpace;
    private int orderNumber;
    private String orderDate;
    private int confirmationCode;
    private int subscriberId;
    private String dateOfPlacingOrder;

    /**
     * Constructs a new Order object with the specified attributes.
     * @param parkingSpace The parking space number assigned to the order.
     * @param orderNumber The unique identifier of the order.
     * @param orderDate The date the order is for (reservation date).
     * @param confirmationCode A code used to confirm the order.
     * @param subscriberId The ID of the subscriber who placed the order.
     * @param dateOfPlacingOrder The date when the order was placed.
     */
    public Order(int parkingSpace, int orderNumber, String orderDate,
                 int confirmationCode, int subscriberId, String dateOfPlacingOrder) {
        this.parkingSpace = parkingSpace;
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.confirmationCode = confirmationCode;
        this.subscriberId = subscriberId;
        this.dateOfPlacingOrder = dateOfPlacingOrder;
    }

    /** @return The parking space number. */
    public int getParkingSpace() {
        return parkingSpace;
    }

    /** @return The unique order number. */
    public int getOrderNumber() {
        return orderNumber;
    }

    /** @return The reservation date of the order. */
    public String getOrderDate() {
        return orderDate;
    }

    /** @return The confirmation code associated with the order. */
    public int getConfirmationCode() {
        return confirmationCode;
    }

    /** @return The subscriber ID that placed the order. */
    public int getSubscriberId() {
        return subscriberId;
    }

    /** @return The date when the order was placed. */
    public String getDateOfPlacingOrder() {
        return dateOfPlacingOrder;
    }

    /**
     * Returns a string representation of the order.
     * @return A formatted string with all order details.
     */
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
