package clientSide;

import ocsf.client.*;
import response.*;

import java.io.IOException;
import java.util.*;

import controller.BaseController;
import controller.ClientController;
import controller.ManagementController;
import controller.TerminalController;
import entities.ActiveParking;
import entities.ParkingHistory;
import entities.Reservation;
import entities.Subscriber;
import interfaces.ChatIF;
import javafx.application.Platform;

/**
 * ChatClient represents the client-side connection to the server in the BPARK system.
 * It handles communication over the OCSF framework, dispatches messages to relevant controllers,
 * and manages user-specific session context such as active subscriber or controller.
 */
public class ChatClient extends AbstractClient {

    private ChatIF clientUI;
    private static ChatClient instance;
    
    /** The current controller that handles UI logic (can be client, terminal, or management). */
    private BaseController controller;
    
    String parkingCode = "1";

    /**
     * Constructs a ChatClient and opens a connection to the server.
     *
     * @param host     The server host.
     * @param port     The server port.
     * @param clientUI The UI interface for displaying messages.
     * @throws IOException if connection fails.
     */
    public ChatClient(String host, int port, ChatIF clientUI) throws IOException {
        super(host, port);
        this.clientUI = clientUI;
        instance = this;
        openConnection();
    }

    public static ChatClient getInstance() {
        return instance;
    }

    /**
     * Sets the current UI controller that the ChatClient will interact with.
     *
     * @param controller A controller implementing the BaseController interface.
     */
    public void setController(BaseController controller) {
        this.controller = controller;
    }

    
    /**
     * Handles incoming messages from the server and delegates them based on type.
     * Supported types include Strings, Lists, domain-specific responses, and objects.
     *
     * @param msg The message received from the server.
     */
    @Override
    public void handleMessageFromServer(Object msg) {
        System.out.println("== Received from server: " + msg.getClass());

        try {
            if (msg instanceof PasswordResetResponse resp) {
                handlePasswordReset(resp);
            } else if (msg instanceof String message) {
                handleStringMessage(message);
            } else if (msg instanceof Reservation reservation) {
                handleReservationMessage(reservation);
            } else if (msg instanceof Subscriber subscriber) {
                handleSubscriberMessage(subscriber);
            } else if (msg instanceof GetSiteActivityResponse response) {
                handleSiteActivity(response);
            } else if (msg instanceof ParkingDurationResponse response) {
                handleParkingDuration(response);
            } else if (msg instanceof MemberStatusReportResponse response) {
                handleMemberStatus(response);
            } else if (msg instanceof List<?> list) {
                handleListMessage(list);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Exception in handleMessageFromServer: " + e.getMessage());
        }
    }

    /**
     * Handles incoming password reset responses.
     *
     * @param resp The response object to process.
     */
    private void handlePasswordReset(PasswordResetResponse resp) {
        Platform.runLater(() -> {
            if (ManagementController.getInstance() != null)
                ManagementController.getInstance().handlePasswordResetResponse(resp);
            if (TerminalController.getInstance() != null)
                TerminalController.getInstance().handlePasswordResetResponse(resp);
            if (ClientController.getInstance() != null)
                ClientController.getInstance().handlePasswordResetResponse(resp);
        });
    }

    /**
     * Handles incoming string messages, such as status updates, error messages, or commands.
     * Delegates to the correct controller and triggers GUI updates when needed.
     *
     * @param message The string message from the server.
     */
    private void handleStringMessage(String message) {
        String baseMessage = message.contains("|") ? message.substring(0, message.indexOf("|")) : message;

        if (message.equals("No active parking found for the given member number or parking number.")) {
            if (controller instanceof ManagementController mgr) {
                Platform.runLater(() -> mgr.getConsoleParkingDetails().setText("No parking/member found."));
            }
            return;
        }

        if (message.startsWith("SUCSESSFUL_PARKING")) {
            String prefix = "SUCSESSFUL_PARKING";
            parkingCode = message.substring(prefix.length()).trim();
            baseMessage = prefix;
        }

        switch (baseMessage) {
            case "TERMINAL_LOGIN_SUCCESS" -> TerminalController.getInstance().handleLoginResponse(message);
            case "TERMINAL_LOGIN_FAILURE" -> {
                TerminalController.getInstance().handleLoginResponse(message);
                TerminalController.getInstance().subscriberNotFoundCase();
            }
            case "HAS ACTIVE PARKING", "NO ACTIVE PARKING" -> TerminalController.getInstance().handleActiveParkingResponse(message);
            case "ACTIVATION_RESULT" -> TerminalController.getInstance().handleReservationActivationResult(message);
            case "PICKUP_RESULT" -> TerminalController.getInstance().handlePickupResult(message);
            case "APP_LOGIN_FAILURE" -> Platform.runLater(() -> ClientController.getInstance().showPopup("Invalid ID or Subscriber Code."));
            case "APP_LOGIN_FAILED_NO_DATA" -> Platform.runLater(() -> ClientController.getInstance().showPopup("Login succeeded but no subscriber data found."));
            case "LOGIN_Management_SUCCESS", "LOGIN_Management_FAILURE" -> ManagementController.getInstance().handleLoginManagementResponse(message);
            case "SUBSCRIBER_UPDATE_SUCCESS" -> Platform.runLater(() -> ClientController.getInstance().showPopup("Details updated successfully."));
            case "SUBSCRIBER_UPDATE_FAILURE" -> Platform.runLater(() -> ClientController.getInstance().showPopup("Failed to update details."));
            case "RESERVATION_FAILED" -> Platform.runLater(() -> ClientController.getInstance().showPopup("Reservation failed:\nNo available spots at the selected time.\nPlease choose a different date and time."));
            case "RESERVATION_FAILED_SERVER_ERROR" -> Platform.runLater(() -> ClientController.getInstance().showPopup("Reservation failed:\nA server error occurred while processing your request."));
            case "RESERVATION_ALREADY_EXIST" -> Platform.runLater(() -> ClientController.getInstance().showPopup("Reservation failed:\nYou already have a reservation at that date and time."));
            case "NO_SPOTS_AVAILABLE" -> Platform.runLater(() -> TerminalController.getInstance().showPopup("Sorry, there are currently no parking spots available."));
            case "SUCSESSFUL_PARKING" -> Platform.runLater(() -> TerminalController.getInstance().handleSucsessfulParking(parkingCode));
            case "CAR_ALREADY_PARKED" -> Platform.runLater(() -> TerminalController.getInstance().handleCarAlreadyParked());
            case "EXTEND_SUCCESS" -> {
                String[] parts = message.split("\\|");
                String newTime = parts.length > 1 ? parts[1] : "";
                Platform.runLater(() -> ClientController.getInstance().showPopup("Parking duration was successfully extended.\nPickup time has been updated to: " + newTime));
            }
            case "EXTEND_ALREADY_DONE" -> Platform.runLater(() -> ClientController.getInstance().showPopup("Sorry, this parking session has already been extended.\nFurther extensions are not allowed."));
            case "EXTEND_FAILED_NO_ACTIVE_PARKING" -> Platform.runLater(() -> ClientController.getInstance().showPopup("Hi, no active parking was found."));
            case "EXTEND_FAILED_DB", "EXTEND_FAILED_UNKNOWN" -> Platform.runLater(() -> ClientController.getInstance().showPopup("Sorry, an error occurred.\nPlease try again."));
            case "CANCEL_SUCCESS" -> Platform.runLater(() -> {
                ClientController.getInstance().showPopup("Reservation was successfully cancelled.");
                ClientController.getInstance().refreshReservationList();
            });
            case "CANCEL_FAILED" -> Platform.runLater(() -> ClientController.getInstance().showPopup("Failed to cancel reservation. Please try again."));
            case "UPDATE_SUCCESS" -> Platform.runLater(() -> {
                ClientController.getInstance().showPopup("Reservation updated successfully.");
                ClientController.getInstance().refreshReservationList();
            });
            case "UPDATE_FAILED" -> Platform.runLater(() -> ClientController.getInstance().showPopup("Update failed. Please try again."));
            default -> {
                if (message.startsWith("SUBSCRIBER_INFO:")) {
                    String info = message.substring("SUBSCRIBER_INFO:".length());
                    ManagementController.getInstance().displaySubscriberInfo(info);
                } else if (message.startsWith("register_result:")) {
                    String result = message.substring("register_result:".length()).trim();
                    if (controller instanceof ManagementController mgrController) {
                        Platform.runLater(() -> {
                            mgrController.showPopup(result);
                            if (result.equalsIgnoreCase("Subscriber registered successfully.")) {
                                mgrController.clearRegisterMemberForm();
                            }
                        });
                    } else {
                        if (clientUI != null) clientUI.display(result);
                    }
                } else {
                    if (clientUI != null) clientUI.display(message);
                }
            }
        }
    }

    /**
     * Handles a Reservation object sent from the server (usually after a new reservation is made).
     *
     * @param r The reservation object.
     */
    private void handleReservationMessage(Reservation r) {
        Platform.runLater(() -> {
            String message = String.format(
                "Reservation confirmed!\nEntry: %s at %s\nExit: %s at %s\nParking Code: %s\n\nPlease arrive on time.\nReservations are canceled if you're over 15 minutes late.",
                r.getEntryDate(), r.getEntryTime(), r.getExitDate(), r.getExitTime(), r.getParkingCode()
            );
            if (controller instanceof ClientController clientController) {
                clientController.showPopup(message);
                clientController.showOnlyPostLoginMenu();
            }
        });
    }

    /**
     * Handles a Subscriber object sent from the server, typically after login.
     *
     * @param subscriber The subscriber object.
     */
    private void handleSubscriberMessage(Subscriber subscriber) {
        ClientController.getInstance().setSubscriber(subscriber);
    }

    /**
     * Handles site activity data (future reservations and active parking sessions).
     *
     * @param response The GetSiteActivityResponse object.
     */
    private void handleSiteActivity(GetSiteActivityResponse response) {
        List<Reservation> future = response.getFutureReservations();
        List<ActiveParking> active = response.getActiveParkings();
        Platform.runLater(() -> ManagementController.getInstance().displaySiteActivity(future, active));
    }

    /**
     * Handles parking duration reports and displays them in a bar chart.
     *
     * @param response The ParkingDurationResponse object.
     */
    private void handleParkingDuration(ParkingDurationResponse response) {
        List<ParkingDurationRecord> records = response.getRecords();
        Platform.runLater(() -> ManagementController.getInstance().displayParkingDurationBarChart(records));
    }

    /**
     * Handles member statistics reports and displays them in a chart.
     *
     * @param response The MemberStatusReportResponse object.
     */
    private void handleMemberStatus(MemberStatusReportResponse response) {
        List<DailySubscriberCount> records = response.getReport();
        Platform.runLater(() -> ManagementController.getInstance().displayMemberStatusBarChart(records));
    }

    /**
     * Handles a list of objects returned from the server.
     * The method identifies the type of content in the list and routes it accordingly.
     *
     * @param list A list of results (Reservation, ParkingHistory, etc.)
     */
    private void handleListMessage(List<?> list) {
        if (list.isEmpty()) {
            if (controller instanceof ClientController clientController) {
                Platform.runLater(() -> {
                    if (clientController.isShowingReservationView()) {
                        clientController.displayReservations(new ArrayList<>());
                    } else if (clientController.isShowingHistoryView()) {
                        clientController.displayHistory(new ArrayList<>());
                    }
                });
            } else if (controller instanceof ManagementController mgr) {
                Platform.runLater(() -> mgr.showPopup("No active parking records found for the given member number / parking number."));
            }
            return;
        }

        Object first = list.get(0);
        if (first instanceof String) {
            if (controller instanceof TerminalController terminalController) {
                terminalController.handleAvailableSpots((List<String>) list);
            } else if (controller instanceof ClientController clientController) {
                clientController.handleAvailableSpots((List<String>) list);
            }
        } else if (first instanceof ParkingHistory) {
            ClientController.getInstance().displayHistory((List<ParkingHistory>) list);
        } else if (first instanceof Reservation) {
            ClientController.getInstance().displayReservations((List<Reservation>) list);
        } else if (first instanceof ActiveParking) {
            if (controller instanceof ManagementController mgr) {
                Platform.runLater(() -> mgr.displayActiveParkingDetails((List<ActiveParking>) list));
            }
        }
    }

    /**
     * Sends a message to the server with internal exception handling.
     * In case of failure, an error message is displayed to the user.
     *
     * @param msg The message to send.
     */
    public void sendToServerSafe(String msg) {
        try {
            sendToServer(msg);
        } catch (IOException e) {
            clientUI.display("Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Closes the connection to the server and terminates the application.
     */
    public void quit() {
        try {
            closeConnection();
        } catch (IOException ignored) {}
        System.exit(0);
    }
}
