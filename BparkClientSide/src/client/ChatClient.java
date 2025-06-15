package client;

import ocsf.client.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import common.*;
import javafx.application.Platform;

/**
 * ChatClient represents the client-side connection to the EchoServer.
 * It manages communication with the server, handles server responses,
 * and tracks the current menu state to guide user interaction..
 */
public class ChatClient extends AbstractClient {

    private ChatIF clientUI;
    private BaseController controller;
    String parkingCode = "1";

    public ChatClient(String host, int port, ChatIF clientUI) throws IOException {
        super(host, port);
        this.clientUI = clientUI;
        openConnection();
    }

    public void setController(BaseController controller) {
        this.controller = controller;
    }

    @Override
    public void handleMessageFromServer(Object msg) {
        System.out.println("== Received from server: " + msg.getClass());

        try {
            if (msg instanceof PasswordResetResponse resp) {
                Platform.runLater(() -> {
                    if (ManagementController.getInstance() != null)
                        ManagementController.getInstance().handlePasswordResetResponse(resp);
                    if (TerminalController.getInstance() != null)
                        TerminalController.getInstance().handlePasswordResetResponse(resp);
                    if (ClientController.getInstance() != null)
                        ClientController.getInstance().handlePasswordResetResponse(resp);
                });
                return;
            }

            if (msg instanceof String message) {
                String baseMessage = message.contains("|") ? message.substring(0, message.indexOf("|")) : message;

                if (message.equals("No active parking found for the given member number or parking number.")) {
                    if (controller instanceof ManagementController mgr) {
                        Platform.runLater(() -> {
                            mgr.getConsoleParkingDetails().setText("No parking/member found.");
                        });
                    }
                    return;
                }

                if (message.startsWith("SUCSESSFUL_PARKING")) {
                    String prefix = "SUCSESSFUL_PARKING";
                    String codePart = message.substring(prefix.length());
                    parkingCode = codePart.trim();
                    baseMessage = prefix;
                }

                switch (baseMessage) {
                    case "TERMINAL_LOGIN_SUCCESS":
                        TerminalController.getInstance().handleLoginResponse(message);
                        break;
                    case "TERMINAL_LOGIN_FAILURE":
                        TerminalController.getInstance().handleLoginResponse(message);
                        TerminalController.getInstance().subscriberNotFoundCase();
                        break;
                    case "ACTIVATION_RESULT":
                    	TerminalController.getInstance().handleReservationActivationResult(message);
                        break;
                    case "PICKUP_RESULT":
                    	TerminalController.getInstance().handlePickupResult(message);
                        break;
                    case "APP_LOGIN_FAILURE":
                        Platform.runLater(() -> ClientController.getInstance().showPopup("Invalid ID or Subscriber Code."));
                        break;
                    case "APP_LOGIN_FAILED_NO_DATA":
                        Platform.runLater(() -> ClientController.getInstance().showPopup("Login succeeded but no subscriber data found."));
                        break;
                    case "LOGIN_Management_SUCCESS":
                    case "LOGIN_Management_FAILURE":
                        ManagementController.getInstance().handleLoginManagementResponse(message);
                        break;
                    case "SUBSCRIBER_UPDATE_SUCCESS":
                        Platform.runLater(() -> ClientController.getInstance().showPopup("Details updated successfully."));
                        break;
                    case "SUBSCRIBER_UPDATE_FAILURE":
                        Platform.runLater(() -> ClientController.getInstance().showPopup("Failed to update details."));
                        break;
                    case "RESERVATION_FAILED":
                        Platform.runLater(() -> ClientController.getInstance().showPopup("Reservation failed:\nNo available spots at the selected time.\nPlease choose a different date and time."));
                        break;
                    case "RESERVATION_FAILED_SERVER_ERROR":
                        Platform.runLater(() -> ClientController.getInstance().showPopup("Reservation failed:\nA server error occurred while processing your request."));
                        break;
                    case "RESERVATION_ALREADY_EXIST":
                        Platform.runLater(() -> ClientController.getInstance().showPopup("Reservation failed:\nYou already have a reservation at that date and time."));
                        break;
                    case "NO_SPOTS_AVAILABLE":
                        Platform.runLater(() -> TerminalController.getInstance().showPopup("Sorry, there are currently no parking spots available."));
                        break;
                    case "SUCSESSFUL_PARKING":
                        Platform.runLater(() -> TerminalController.getInstance().handleSucsessfulParking(parkingCode));
                        break;
                    case "CAR_ALREADY_PARKED":
                        Platform.runLater(() -> TerminalController.getInstance().handleCarAlreadyParked());
                        break;
                    case "EXTEND_SUCCESS":
                        String[] parts = message.split("\\|");
                        String newTime = parts.length > 1 ? parts[1] : "";
                        Platform.runLater(() -> ClientController.getInstance().showPopup("Parking duration was successfully extended.\nPickup time has been updated to: " + newTime));
                        break;
                    case "EXTEND_ALREADY_DONE":
                        Platform.runLater(() -> ClientController.getInstance().showPopup("Sorry, this parking session has already been extended.\nFurther extensions are not allowed."));
                        break;
                    case "EXTEND_FAILED_NO_ACTIVE_PARKING":
                        Platform.runLater(() -> ClientController.getInstance().showPopup("Hi, no active parking was found."));
                        break;
                    case "EXTEND_FAILED_DB":
                        Platform.runLater(() -> ClientController.getInstance().showPopup("Sorry, an error occurred.\nPlease try again."));
                        break;
                    case "EXTEND_FAILED_UNKNOWN":
                        Platform.runLater(() -> ClientController.getInstance().showPopup("Sorry, an error occurred.\nPlease try again."));
                        break;
                    case "CANCEL_SUCCESS":
                        Platform.runLater(() -> {
                            ClientController.getInstance().showPopup("Reservation was successfully cancelled.");
                            ClientController.getInstance().refreshReservationList();
                        });
                        break;
                    case "CANCEL_FAILED":
                        Platform.runLater(() -> ClientController.getInstance().showPopup("Failed to cancel reservation. Please try again."));
                        break;
                    case "UPDATE_SUCCESS":
                        Platform.runLater(() -> {
                            ClientController.getInstance().showPopup("Reservation updated successfully.");
                            ClientController.getInstance().refreshReservationList();
                        });
                        break;
                    case "UPDATE_FAILED":
                        Platform.runLater(() -> ClientController.getInstance().showPopup("Update failed. Please try again."));
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
                                        mgrController.clearRegisterMemberForm();
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
            }

            else if (msg instanceof GetSiteActivityResponse response) {
                List<Reservation> future = response.getFutureReservations();
                List<ActiveParking> active = response.getActiveParkings();
                Platform.runLater(() -> ManagementController.getInstance().displaySiteActivity(future, active));
            }

            else if (msg instanceof ParkingDurationResponse response) {
                List<ParkingDurationRecord> records = response.getRecords();
                Platform.runLater(() -> ManagementController.getInstance().displayParkingDurationBarChart(records));
            }
            else if (msg instanceof MemberStatusReportResponse response) {
                List<DailySubscriberCount> records = response.getReport();
                Platform.runLater(() -> ManagementController.getInstance().displayMemberStatusBarChart(records));
            }

            else if (msg instanceof List<?> list) {
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
                    if (controller instanceof TerminalController) {
                        ((TerminalController) controller).handleAvailableSpots((List<String>) list);
                    } else if (controller instanceof ClientController) {
                        ((ClientController) controller).handleAvailableSpots((List<String>) list);
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

            else if (msg instanceof Subscriber subscriber) {
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

    void sendToServerSafe(String msg) {
        try {
            sendToServer(msg);
        } catch (IOException e) {
            clientUI.display("Failed to send message: " + e.getMessage());
        }
    }

    public void quit() {
        try {
            closeConnection();
        } catch (IOException ignored) {}
        System.exit(0);
    }
}
