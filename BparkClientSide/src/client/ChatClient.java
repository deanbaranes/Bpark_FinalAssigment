package client;
 
import ocsf.client.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import common.ChatIF;
import common.ParkingHistory;
import common.Reservation;
import common.Subscriber;
import javafx.application.Platform;

/**
 * ChatClient represents the client-side connection to the EchoServer.
 * It manages communication with the server, handles server responses,
 * and tracks the current menu state to guide user interaction.
 */
public class ChatClient extends AbstractClient {

    private ChatIF clientUI;
    private MenuState currentState = MenuState.IDLE;
    private String selectedOrderId;
    private String selectedField;
    private BaseController controller;


    /**
     * Constructs a ChatClient.
     * @param host The server host.
     * @param port The server port.
     * @param clientUI Reference to the user interface.
     * @throws IOException If connection fails.
     */
    public ChatClient(String host, int port, ChatIF clientUI) throws IOException {
        super(host, port);
        this.clientUI = clientUI;
        openConnection();
    }

    /**
     * Sets the UI controller.
     * @param controller The controller instance.
     */
  
    public void setController(BaseController controller) {
        this.controller = controller;
    }

    public MenuState getCurrentState() {
        return currentState;
    }
    
    public void setCurrentState(MenuState state) {
        this.currentState = state;
    }
    
    
    
    /**
     * Handles messages received from the server in a structured and maintainable way.
     * 
     * This method was refactored from a series of separate if/else chains
     * into a clean `switch` statement, which improves readability, scalability,
     * and centralizes response logic for different message types.
     *
     * It handles:
     * - Login results for terminal, app, and management.
     * - Subscriber info and update results.
     * - Lists (e.g., parking spots).
     * - Subscriber object (after login).
     *
     * GUI updates are safely wrapped in Platform.runLater to ensure they are
     * executed on the JavaFX Application Thread.
     *
     * If an unknown message is received, it is passed to the general clientUI.
     *
     * @param msg The message object sent by the server (String, List, or Subscriber).
     */

    
    @Override
    public void handleMessageFromServer(Object msg) {
        try {
            if (msg instanceof String message) {
                String baseMessage = message.contains("|") ? message.substring(0, message.indexOf("|")) : message;

                switch (baseMessage) {
                    case "TERMINAL_LOGIN_SUCCESS":
                    case "TERMINAL_LOGIN_FAILURE":
                        TerminalController.getInstance().handleLoginResponse(message);
                        break;

                    case "APP_LOGIN_SUCCESS":
                    case "APP_LOGIN_FAILURE":
                        ClientController.getInstance().handleLoginResponse(message);
                        break;

                    case "LOGIN_Management_SUCCESS":
                    case "LOGIN_Management_FAILURE":
                        ManagementController.getInstance().handleLoginManagementResponse(message);
                        break;

                    case "SUBSCRIBER_UPDATE_SUCCESS":
                        Platform.runLater(() -> {
                            ClientController.getInstance().showPopup("Details updated successfully.");
                        });
                        break;

                    case "SUBSCRIBER_UPDATE_FAILURE":
                        Platform.runLater(() -> {
                            ClientController.getInstance().showPopup("Failed to update details.");
                        });
                        break;
                        
                    case String s when s.startsWith("RESERVATION_FAILED|"):
                        String errorMsg = s.substring("RESERVATION_FAILED|".length());
                        Platform.runLater(() -> {
                            ClientController.getInstance().showPopup("Reservation failed:\n" + errorMsg);
                        });
                        break;

                    case "NO_SPOTS_AVAILABLE":
                        Platform.runLater(() ->
                            TerminalController.getInstance().showPopup("Sorry, there are currently no parking spots available.")
                        );
                        break;

                    case "SPOT_AVAILABLE":
                        Platform.runLater(() ->
                            TerminalController.getInstance().generateAndShowParkingCode()
                        );
                        break;

                    default:
                        if (message.startsWith("SUBSCRIBER_INFO:")) {
                            String info = message.substring("SUBSCRIBER_INFO:".length());
                            ManagementController.getInstance().displaySubscriberInfo(info);

                        } else if (message.startsWith("register_result:")) {
                            String result = message.substring("register_result:".length()).trim();
                            if (controller instanceof ManagementController mgrController) {
                                Platform.runLater(() -> {
                                    mgrController.showPopup(result);
                                    if (result.equalsIgnoreCase("Subscriber registered successfully.")) {
                                        mgrController.clearRegisterMemberForm(); // <- Clear fields after success
                                    }
                                });
                            } else {
                                if (clientUI != null) clientUI.display(result);
                            }
                        } else {
                            if (clientUI != null) clientUI.display(message);
                        }
                        break;
                }

            } else if (msg instanceof List<?> list) {
                if (!list.isEmpty()) {
                    Object first = list.get(0);

                    if (first instanceof String) {
                        if (controller instanceof TerminalController) {
                            ((TerminalController) controller).handleAvailableSpots((List<String>) list);
                        } else if (controller instanceof ClientController) {
                            ((ClientController) controller).handleAvailableSpots((List<String>) list);
                        }

                    } else if (first instanceof ParkingHistory) {
                        ClientController.getInstance().displayHistory((List<ParkingHistory>) list);

                    } else if (first instanceof Reservation) {
                        ClientController.getInstance().displayReservations((List<Reservation>) list);
                    }
                }

            } else if (msg instanceof Subscriber subscriber) {
                ClientController.getInstance().setSubscriber(subscriber);
            }
            else if (msg instanceof Reservation r) {
                Platform.runLater(() -> {
                    String message = String.format(
                        "Reservation confirmed!\n" +
                        "Entry: %s at %s\n" +
                        "Exit: %s at %s\n" +
                        "Parking Code: %s\n\n" +
                        "Please arrive on time.\nReservations are canceled if you're over 15 minutes late.",
                        r.getEntryDate(), r.getEntryTime(),
                        r.getExitDate(), r.getExitTime(),
                        r.getParkingCode()
                    );

                    if (controller instanceof ClientController clientController) {
                        clientController.showPopup(message);
                        clientController.showOnlyPostLoginMenu(); 
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Exception in handleMessageFromServer: " + e.getMessage());
        }
    }


    /**
     * Sends an order ID to the server to verify existence.
     * @param orderId The order ID string.
     */
    public void submitOrderId(String orderId) {
        try {
            Integer.parseInt(orderId);
            selectedOrderId = orderId;
            sendToServerSafe("CHECK" + orderId);
        } catch (NumberFormatException e) {
            clientUI.display("Invalid order ID. Must be a number.");
        }
    }

    /**
     * Sets the field that will be updated.
     * @param field The field name ("parking_space" or "order_date").
     */
    public void selectFieldToUpdate(String field) {
        if (field.equals("parking_space") || field.equals("order_date")) {
            selectedField = field;
            currentState = MenuState.UPDATE_ORDER_ENTER_VALUE;
        } else {
            clientUI.display("Invalid field selected.");
        }
    }

    /**
     * Sends the updated value to the server for the selected field.
     * @param newValue The new value for the field.
     */
    public void submitFieldUpdate(String newValue) {
        if (selectedField == null || selectedOrderId == null) {
            clientUI.display("No field or order selected.");
            return;
        }

        if (selectedField.equals("order_date")) {
            try {
                LocalDate newDate = LocalDate.parse(newValue);
                LocalDate tomorrow = LocalDate.now().plusDays(1);
                if (newDate.isBefore(tomorrow)) {
                    clientUI.display("Order Date must be from tomorrow onwards.");
                    return;
                }
            } catch (DateTimeParseException e) {
                clientUI.display("Invalid date format. Use yyyy-mm-dd.");
                return;
            }
        } else if (selectedField.equals("parking_space")) {
            if (!newValue.matches("-?\\d+")) {
                clientUI.display("Parking space must be a number.");
                return;
            }
        }

        String command = "UPDATE_ORDER|" + selectedOrderId + "|" + selectedField + "|" + newValue;
        sendToServerSafe(command);
        clientUI.display("Update sent.");
        currentState = MenuState.IDLE;
    }

    /**
     * Sends a request to display all orders.
     */
    public void showOrders() {
        sendToServerSafe("SHOW_ORDERS");
    }

    /**
     * Sends a message to the server with error handling.
     * @param msg The message string.
     */
    void sendToServerSafe(String msg) {
        try {
            sendToServer(msg);
        } catch (IOException e) {
            clientUI.display("Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Closes the connection and exits the application.
     */
    public void quit() {
        try {
            closeConnection();
        } catch (IOException ignored) {}
        System.exit(0);
    }
}



/**
 * Handles incoming messages from the server.
 * @param msg The message received.
 */
/*
 * 
 * 
@Override
public void handleMessageFromServer(Object msg) {
    if (msg instanceof String message) {
        /////// PROTOTYPE
         * if (message.equals("ORDER_DOES_NOT_EXIST")) {
            clientUI.display("Order does not exist. Please try again.");
            currentState = MenuState.CHECK_IF_EXISTS;
        } else if (message.equals("INVALID_ORDER_ID")) {
            clientUI.display("Invalid order ID format.");
            currentState = MenuState.CHECK_IF_EXISTS;
        } else if (message.startsWith("ORDER_EXISTS:")) {
            selectedOrderId = message.substring("ORDER_EXISTS:".length());
            clientUI.display("Order #" + selectedOrderId + " found. Select a field to update.");
            currentState = MenuState.UPDATE_ORDER_SELECT_FIELD;
           // controller.successfulFirstSubmit();
        }
    	
        // Passes login result message to TerminalController for handling 
        if (message.equals("TERMINAL_LOGIN_SUCCESS") || message.equals("TERMINAL_LOGIN_FAILURE")) {
            TerminalController.getInstance().handleLoginResponse(message);
        }
        
        // Passes login result message to ClientController for handling 
        else if (message.equals("APP_LOGIN_SUCCESS") || message.equals("APP_LOGIN_FAILURE")) {
            ClientController.getInstance().handleLoginResponse(message);
        }
        
        
        // Passes management login result message to ManagementController for handling 
        else if (message.equals("LOGIN_Management_SUCCESS") || message.equals("LOGIN_Management_FAILURE")) {
        	ManagementController.getInstance().handleLoginManagementResponse(message);
        }
        
        // Extracts subscriber information and displays it using ManagementController 
        else if (message.startsWith("SUBSCRIBER_INFO:")) {
            String info = message.substring("SUBSCRIBER_INFO:".length());
            ManagementController.getInstance().displaySubscriberInfo(info);
        }
        else if (message.equals("SUBSCRIBER_UPDATE_SUCCESS")) {
            ClientController.getInstance().showPopup("Details updated successfully.");
        }
        else if (message.equals("SUBSCRIBER_UPDATE_FAILURE")) {
            ClientController.getInstance().showPopup("Failed to update details.");
        }

        else {
            clientUI.display(message);
        }
    }
    // Passes a list of available parking spots to the Terminal or Client Controller for display 
    else if (msg instanceof List) {
        if (controller instanceof TerminalController) {
            ((TerminalController) controller).handleAvailableSpots((List<String>) msg);
        } else if (controller instanceof ClientController) {
            ((ClientController) controller).handleAvailableSpots((List<String>) msg);
        }
    }
    else if (msg instanceof Subscriber) {
        ClientController controller = ClientController.getInstance();
        controller.setSubscriber((Subscriber) msg);
    }
}
*/

