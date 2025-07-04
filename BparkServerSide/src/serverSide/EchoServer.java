package serverSide;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import controller.NotificationController;
import entities.ActiveParking;
import entities.ParkingHistory;
import entities.Reservation;
import entities.Subscriber;
import jdbc.mysqlConnection;
import ocsf.server.*;
import request.LoginManagementRequest;
import request.LoginRequest;
import request.MemberStatusReportRequest;
import request.ParkingDurationRequest;
import request.PasswordResetRequest;
import request.RegisterMemberRequest;
import request.UpdateReservationRequest;
import request.UpdateSubscriberDetailsRequest;
import response.DailySubscriberCount;
import response.GetSiteActivityResponse;
import response.MemberStatusReportResponse;
import response.ParkingDurationRecord;
import response.ParkingDurationResponse;
import response.PasswordResetResponse;

  
/**
 * EchoServer is a server-side class that listens for client connections
 * and handles messages sent by the client. It uses the OCSF framework
 * and communicates with a MySQL database to retrieve and update order data...
 */
public class EchoServer extends AbstractServer {

    /** Default port to listen on. */
    final public static int DEFAULT_PORT = 5555;
    final int RESERVATION_DURATION_HOURS = 4;
    /**
     * Constructs an EchoServer on the specified port.
     * @param port The port number to listen on.
     */
    public EchoServer(int port) {
        super(port);
    }
 
    /**
     * Handles all incoming messages from clients and delegates processing
     * based on the message type (String commands, Login requests, etc.).
     * This method acts as the main dispatcher for client requests.
     *
     * @param msg The message received from the client (String, LoginRequest, etc.)
     * @param client The client connection that sent the message
     */
    @Override
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {
        System.out.println("Message received: " + msg + " from " + client);

        try {
            if (msg instanceof PasswordResetRequest req) {
            	if(req.getPWtype().equals("sub"))
            	{
            		handleSubscriptionCodeReset(req,client);
            	}
            	else if(req.getPWtype().equals("pcode")) {
                    handleParkingCodeReset(req, client);
            	}
            	else {
            		   handlePasswordReset(req, client);
            	}
            }
            if (msg instanceof String command) {
                handleStringCommand(command, client);
            } else if (msg instanceof LoginRequest request) {
                handleLoginRequest(request, client);
            } else if (msg instanceof LoginManagementRequest management) {
                handleManagementLogin(management, client);
            } else if (msg instanceof UpdateSubscriberDetailsRequest update) {
                handleSubscriberUpdate(update, client);
            } else if (msg instanceof RegisterMemberRequest request) {
                handleRegisterMember(request, client);
            } else if (msg instanceof Reservation req && req.getReservationId() == 0) { //new reservation
                handleNewReservationRequest(req, client);
            } else if (msg instanceof Subscriber subscriber) {
            	  handleNewSubscriberDropoffNoReserv(subscriber, client);
            } else if (msg instanceof UpdateReservationRequest) {
                UpdateReservationRequest req = (UpdateReservationRequest) msg;
                boolean updated = mysqlConnection.updateReservationDateTime(
                    req.getReservationId(), req.getNewDate(), req.getNewTime());
                if (updated)
                    client.sendToClient("UPDATE_SUCCESS");
                else
                    client.sendToClient("UPDATE_FAILED");

            }  else if (msg instanceof ParkingDurationRequest request) {
                handleParkingDurationRequest(request, client);
            } else if (msg instanceof MemberStatusReportRequest request) {
                handleMemberStatusReportRequest(request, client);
            }

            else {
                client.sendToClient("Unsupported message format.");
            	 }
            

        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

 
    
    /**
     * Processes text-based commands sent from the client (usually via sendToServer(String)).
     * Each command represents a specific request (e.g., fetch history, get reservations).
     *
     * @param command The string command sent by the client
     * @param client The client connection to respond to
     * @throws IOException if sending a response to the client fails
     */
    private void handleStringCommand(String command, ConnectionToClient client) throws IOException {
        if (command.startsWith("REQUEST_ID_DETAILS|")) {
            String id = command.split("\\|")[1];
            client.sendToClient("SUBSCRIBER_INFO:" + mysqlConnection.getSubscriberInfo(id));
        } else if (command.startsWith("GET_HISTORY|")) {
            String id = command.split("\\|")[1];
            client.sendToClient(mysqlConnection.getHistoryForSubscriber(id));
        }
        else if (command.startsWith("GET_PARKING_HISTORY|")) {
            String id = command.split("\\|")[1];
            List<ParkingHistory> history = mysqlConnection.getHistoryForSubscriber(id);
            client.sendToClient(history);
        }
    	  else if (command.startsWith("CHECK_IF_ACTIVE_PARKING|")) {
	        String id = command.split("\\|")[1];
            String result1 = mysqlConnection.isSubscriberInActiveParking(id);
            client.sendToClient(result1);
 
        }  else if (command.startsWith("GET_RESERVATIONS|")) {
            String id = command.split("\\|")[1];
            client.sendToClient(mysqlConnection.getReservationsForSubscriber(id));
        } else if (command.equals("REQUEST_AVAILABLE_SPOTS")) {
            client.sendToClient(mysqlConnection.getAvailableSpots());
            
        }
        else if (command.startsWith("CHECK_PICKUP_CODE|")) {
            String pickupCode = command.split("\\|")[1].trim();
            String pickupResult = mysqlConnection.processPickupRequest(pickupCode);
            System.out.println(" my test" + pickupResult);
            client.sendToClient("PICKUP_RESULT|" + pickupResult);
        }
        else if (command.startsWith("ACTIVATE_RESERVATION_CODE|")) {
            String reservationCode = command.split("\\|")[1].trim();
            String result = mysqlConnection.moveReservationToActive(reservationCode);
            System.out.println("Reservation activation result from MySQL: " + result);

            client.sendToClient("ACTIVATION_RESULT|" + result);
        }

        else if (command.equals("CHECK_PARKING_AVAILABILITY")) {
            List<String> availableSpots = mysqlConnection.getAvailableSpots();
            if (availableSpots.isEmpty()) {
                client.sendToClient("NO_SPOTS_AVAILABLE");
            }
            
            else {
                client.sendToClient("SPOT_AVAILABLE");
            }

        } else if (command.startsWith("SEARCH_ACTIVE_PARKING|")) {
            String value = command.split("\\|")[1];
            List<ActiveParking> results = new ArrayList<>();

            // Try to interpret the value as a subscriber ID (most common case)
            try {
                int subscriberId = Integer.parseInt(value);
                results = mysqlConnection.searchActiveParkingByMemberId(subscriberId);
            } catch (NumberFormatException ignored) {}

            // If no results found, try interpreting the value as a parking spot
            if (results.isEmpty()) {
                results = mysqlConnection.searchActiveParkingBySpot(value);
            }

            // Send appropriate response based on search results
            if (results.isEmpty()) {
                client.sendToClient("No active parking found for the given member number or parking number.");
            } else {
                client.sendToClient(results);
            }

        } else if (command.contains("EXTEND_PARKING")) {
            String subscriberIdStr = command.split("\\|")[1];
            String currentCommand = command.split("\\|")[0];
            if (currentCommand.contains("TERMINAL")) {
            	try {
                    int subscriberId = Integer.parseInt(subscriberIdStr);

                    List<ActiveParking> list = mysqlConnection.searchActiveParkingByMemberId(subscriberId);

                    if (list == null || list.isEmpty()) {
                        client.sendToClient("EXTEND_FAILED_NO_ACTIVE_PARKING_TERMINAL");
                        return;
                    }

                    ActiveParking ap = list.get(0);

                    if (ap.isExtended()) {
                        client.sendToClient("EXTEND_ALREADY_DONE_TERMINAL");
                        return;
                    }
                    boolean success = mysqlConnection.extendParkingTime(ap);
                    if (success) {
                        client.sendToClient("EXTEND_SUCCESS_TERMINAL|" + ap.getExpectedExitTime());
                    } else {
                        client.sendToClient("EXTEND_FAILED_DB_TERMINAL");
                    }

                }  catch (Exception e) {
                    e.printStackTrace();
                    client.sendToClient("EXTEND_FAILED_UNKNOWN_TERMINAL");
                }
            }
            else
            {
            	try {
                    int subscriberId = Integer.parseInt(subscriberIdStr);

                    List<ActiveParking> list = mysqlConnection.searchActiveParkingByMemberId(subscriberId);

                    if (list == null || list.isEmpty()) {
                        client.sendToClient("EXTEND_FAILED_NO_ACTIVE_PARKING");
                        return;
                    }

                    ActiveParking ap = list.get(0);

                    if (ap.isExtended()) {
                        client.sendToClient("EXTEND_ALREADY_DONE");
                        return;
                    }

                    boolean success = mysqlConnection.extendParkingTime(ap);
                    if (success) {
                        client.sendToClient("EXTEND_SUCCESS|" + ap.getExpectedExitTime());
                    } else {
                        client.sendToClient("EXTEND_FAILED_DB");
                    }

                }  catch (Exception e) {
                    e.printStackTrace();
                    client.sendToClient("EXTEND_FAILED_UNKNOWN");
                }
            }
            
        
        } else if (command.startsWith("CANCEL_RESERVATION|")) {
        	int reservationId = Integer.parseInt(command.split("\\|")[1]);
            boolean success = mysqlConnection.cancelReservationById(reservationId);
            if (success)
                client.sendToClient("CANCEL_SUCCESS");
            else
                client.sendToClient("CANCEL_FAILED");
        } else if (command.equals("GET_SITE_ACTIVITY")) {
            	handleSiteActivityRequest(client);
    }else if (command.equals("GET_ALL_ACTIVE_PARKINGS")) {
        List<ActiveParking> allActive = mysqlConnection.getActiveParkings();
        client.sendToClient(allActive);
    }
        else {
        client.sendToClient("Unrecognized command.");
    }
        
}


    /**
     * Processes login requests from clients (e.g., mobile app or terminal).
     * If login is successful, a Subscriber object is sent back (for app logins).
     *
     * @param request The login credentials and source ("app" or "terminal")
     * @param client The client attempting to log in
     * @throws IOException if sending a login result or subscriber object fails
     */
    private void handleLoginRequest(LoginRequest request, ConnectionToClient client) throws IOException {
        boolean success = mysqlConnection.checkLogin(request.getID(), request.getSubscriptionCode());
        System.out.println("LoginRequest received from source: " + request.getSource());

        switch (request.getSource()) {
            case "terminal" -> client.sendToClient(success ? "TERMINAL_LOGIN_SUCCESS" : "TERMINAL_LOGIN_FAILURE");
            case "app" -> {
                if (success) {
                    Subscriber sub = mysqlConnection.getSubscriberById(request.getID());
                    if (sub != null) {
                       // client.sendToClient("APP_LOGIN_SUCCESS");
                        client.sendToClient(sub);
                    } else {
                        client.sendToClient("APP_LOGIN_FAILED_NO_DATA");
                    }
                } else {
                    client.sendToClient("APP_LOGIN_FAILURE");
                }
            }
            default -> client.sendToClient("LOGIN_FAILURE");
        }
    }

    /**
     * Processes login attempts from management clients.
     * Verifies username and password against the 'employees' table.
     *
     * @param login The login credentials provided by the management client
     * @param client The client attempting to log in
     * @throws IOException if sending the login result to the client fails
     */
    private void handleManagementLogin(LoginManagementRequest login, ConnectionToClient client) throws IOException {
        String role = mysqlConnection.checkLoginManagement(login.getUsername(), login.getPassword());

        if (role != null) {
            client.sendToClient("LOGIN_Management_SUCCESS|" + role);
        } else {
            client.sendToClient("LOGIN_Management_FAILURE");
        }
    }

    
    /**
     * Processes update requests to modify a subscriber's email and phone.
     * Updates the database and sends success/failure response back to the client.
     *
     * @param update The update request containing subscriber ID and new data
     * @param client The client that initiated the update request
     * @throws IOException if sending the update result to the client fails
     */
    private void handleSubscriberUpdate(UpdateSubscriberDetailsRequest update, ConnectionToClient client) throws IOException {
        boolean success = mysqlConnection.updateSubscriberContactInfo(update.getSubscriberId(), update.getNewEmail(), update.getNewPhone());
        client.sendToClient(success ? "SUBSCRIBER_UPDATE_SUCCESS" : "SUBSCRIBER_UPDATE_FAILURE");
    }

    /**
     * Handles a RegisterMemberRequest from the client and registers a new subscriber.
     * @param request The registration request containing subscriber data.
     * @param client The connection to the client who sent the request.
     */
    private void handleRegisterMember(RegisterMemberRequest request, ConnectionToClient client) {
        try {
            String id = request.getIdNumber();

            if (mysqlConnection.doesSubscriberExist(id)) {
                client.sendToClient("register_result: Subscriber with this ID already exists.");
                return;
            }
            boolean success = mysqlConnection.registerSubscriber(
                request.getFirstName(),
                request.getLastName(),
                request.getIdNumber(),
                request.getEmail(),
                request.getPhoneNumber(),
                request.getVehicleNumber(),
                request.getCreditCard()
            );

            client.sendToClient(success
                ? "register_result: Subscriber registered successfully."
                : "register_result: Failed to register subscriber.");
        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.sendToClient("register_result: Internal server error.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Handles drop-off requests from subscribers who do not have an existing reservation.
     * 
     * Receives a Subscriber object from the client, attempts to create a new active parking 
     * record for the subscriber in the database, and sends the result back to the client.
     *
     * @param subscriber The subscriber attempting to drop off a vehicle without a reservation.
     * @param client The connection to the client who sent the request.
     */
    private void handleNewSubscriberDropoffNoReserv(Subscriber subscriber,ConnectionToClient client)
    {
    	try 
    	{
			client.sendToClient(mysqlConnection.createNewActiveParking(subscriber));
		} 
    	catch (IOException e) 
    	{
						e.printStackTrace();
		}   	
    }
    
    
    /**
     * Handles a new reservation request sent by a subscriber from the client side.
     * 
     * The method performs the following steps:
     * 1. Calculates the extended time window (reservation duration + possible extension).
     * 2. Checks how many overlapping reservations exist in that time range.
     * 3. If the occupancy rate exceeds 60%, the reservation is denied.
     * 4. If a reservation already exists for the subscriber at the requested time, it is also denied.
     * 5. If valid, finds the next available spot and inserts the reservation into the database.
     * 6. Returns a confirmed Reservation object to the client.
     * 
     * @param req    The reservation request containing subscriber ID, entry date, and time.
     * @param client The client that initiated the reservation request.
     */
    private void handleNewReservationRequest(Reservation req, ConnectionToClient client) {
        try {
            int totalSpots = mysqlConnection.getTotalParkingSpots();

            // Step 1: Calculate extended window (reservation + possible 4-hour extension)
            LocalTime entryTime = req.getEntryTime();
            LocalDate date = req.getEntryDate();

            // Step 2: Check how many overlapping reservations exist in the extended time frame
            int overlappingCount = mysqlConnection.getOverlappingReservationCount(date, entryTime, 8);
            double occupancyRate = (double) overlappingCount / totalSpots;

            // Step 3: Deny the reservation if more than 60% of the lot is occupied
            if (occupancyRate > 0.6) {
                client.sendToClient("RESERVATION_FAILED");
                return;
            }

            // Step 4: Check if the subscriber already has a reservation at the same time
            if (mysqlConnection.reservationExists(req.getSubscriberId(), req.getEntryDate(), req.getEntryTime())) {
                client.sendToClient("RESERVATION_ALREADY_EXIST");
                return;
            }

            // Step 5: Find a free parking spot and register the new reservation
            int spot = mysqlConnection.findAvailableSpot();
            String code = mysqlConnection.generateUniqueParkingCode(req.getSubscriberId(), spot);

            LocalTime exitTime = entryTime.plusHours(RESERVATION_DURATION_HOURS);
            LocalDate exitDate = date;
            if (exitTime.isBefore(entryTime)) {
                exitDate = exitDate.plusDays(1);
            }

            mysqlConnection.insertReservationAndUpdateSpot(
                req.getSubscriberId(), code,
                date, entryTime,
                exitDate, exitTime, spot
            );

            // Step 6: Send the confirmed reservation back to the client
            Reservation confirmed = new Reservation(
                0,
                req.getSubscriberId(),
                code,
                date,
                entryTime,
                exitDate,
                exitTime,
                spot
            );

            client.sendToClient(confirmed);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.sendToClient("RESERVATION_FAILED_SERVER_ERROR");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    
    
    /**
     * Returns a formatted string describing all currently connected clients.
     * @return Client connection status summary.
     */
    public String getAllClientsInfo() {
        StringBuilder statusBuilder = new StringBuilder();
        Thread[] clients = getClientConnections();

        if (clients == null || clients.length == 0) {
            return "No clients are currently connected.\n";
        }
        statusBuilder.append("Connected clients info:\n");
        statusBuilder.append("=======================\n");
        boolean hasActiveClients = false;

        for (Thread clientThread : clients) {
            if (clientThread instanceof ConnectionToClient connectedClient && connectedClient.isAlive()) {
                hasActiveClients = true;
                String ip = connectedClient.getInetAddress().getHostAddress();
                String host = connectedClient.getInetAddress().getHostName();

                statusBuilder.append("Client: ")
                             .append(host)
                             .append(" (")
                             .append(ip)
                             .append(") - Status: Connected\n");
            }
        }
        if (!hasActiveClients) {
            return "No active clients are currently connected.\n";
        }

        return statusBuilder.toString();
    }
    
    /**
     * Called when the server starts listening for connections.
     */
    protected void serverStarted() {
        System.out.println("Server listening for connections on port " + getPort());
    }
    
    /**
     * Called when the server stops listening for connections.
     */
    protected void serverStopped() {
        System.out.println("Server has stopped listening for connections.");
    }

    /**
     * Main method to start the server from the command line.
     * @param args Command-line arguments (optional port number).
     */
    public static void main(String[] args) {
        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Throwable t) {
            port = DEFAULT_PORT;
        }

        EchoServer sv = new EchoServer(port);
        try {
            sv.listen();
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
        }
    }
    
    /**
     * Handles a password reset request for employees.
     * This method attempts to retrieve the password associated with the provided email.
     * If found, it sends the password to the user's email address using the EmailSender.
     * If no matching account is found, or if an error occurs, a PasswordResetResponse
     * with a corresponding failure message is sent back to the client.
     *
     * @param req    The PasswordResetRequest containing the user's email.
     * @param client The ConnectionToClient representing the connected client
     *               to whom the response should be sent.
     */
    private void handlePasswordReset(PasswordResetRequest req, ConnectionToClient client) {
        System.out.println("[EchoServer] → Got PasswordResetRequest for: " + req.getEmail());
        String email = req.getEmail();

        try {
            String password = mysqlConnection.getPasswordForEmail(email); 

            if (password == null) {
                client.sendToClient(new PasswordResetResponse(false, "No account found for that email."));
            } else {
                System.out.println("[EchoServer] → About to call sendPasswordEmail()");
                new NotificationController().sendPasswordEmail(email, password);
                System.out.println("[EchoServer] → sendPasswordEmail() completed successfully");
                client.sendToClient(new PasswordResetResponse(true,
                        "Your password has been sent to " + email));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                client.sendToClient(new PasswordResetResponse(false, "Server error."));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Handles a subscription code reset request by retrieving the subscription code associated
     * with the given email and sending it to the user via email.
     * If no account is found for the provided email, a failure response is sent to the client.
     * In case of an error during the process,an appropriate error message is returned to the client.
     *
     * @param req The PasswordResetRequest containing the user's email.
     * @param client The ConnectionToClient instance representing the connected client.
     */
    private void handleSubscriptionCodeReset(PasswordResetRequest req, ConnectionToClient client) {
        System.out.println("[EchoServer] → Got SubscriptionCodeResetRequest for: " + req.getEmail());
        String email = req.getEmail();

        try {
            String subscriptionCode = mysqlConnection.subscriptionCodeForEmail(email);

            if (subscriptionCode == null) {
                client.sendToClient(new PasswordResetResponse(false, "No account found for that email."));
            } else {
                System.out.println("[EchoServer] → About to call sendPasswordEmail()");
                new NotificationController().sendPasswordEmail(email, subscriptionCode);
                System.out.println("[EchoServer] → sendPasswordEmail() completed successfully");
                client.sendToClient(new PasswordResetResponse(true,
                        "Your subscription code has been sent to " + email));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                client.sendToClient(new PasswordResetResponse(false, "Server error."));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles a parking code reset request by retrieving the parking code associated
     * with the given email and sending it to the user via email.
     * If the email is not associated with any reservation, the client is notified accordingly.
     * In case of a server error during the process , an error response
     * is sent back to the client.
     *
     * @param req The PasswordResetRequest containing the user's email.
     * @param client The ConnectionToClient instance representing the connected client.
     */
    private void handleParkingCodeReset(PasswordResetRequest req, ConnectionToClient client) {
        System.out.println("[EchoServer] → Got ParkingCodeResetRequest for: " + req.getEmail());
        String email = req.getEmail();

        try {
            String parkingCode = mysqlConnection.parkingCodeForEmail(email);

            if (parkingCode == null) {
                client.sendToClient(new PasswordResetResponse(false, "No active parking found for that email."));
            } else {
                System.out.println("[EchoServer] → About to call sendParkingCodeEmail()");
                new NotificationController().sendParkingCodeEmail(email, parkingCode);
                System.out.println("[EchoServer] → sendParkingCodeEmail() completed successfully");
                client.sendToClient(new PasswordResetResponse(true,
                        "Your parking code has been sent to " + email));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                client.sendToClient(new PasswordResetResponse(false, "Server error."));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    
    /**
     * Handles a site activity request from the client.
     * This method retrieves the list of future reservations and currently active parkings
     * from the database via  mysqlConnection, and sends them back to the client
     * wrapped in a GetSiteActivityResponse object.
     *
     * If an error occurs during the process, a failure message ("SITE_ACTIVITY_FAILED") is sent instead.
     *
     * @param client The client that requested the site activity data.
     */
    private void handleSiteActivityRequest(ConnectionToClient client) {
        try {
            List<Reservation> futureReservations = mysqlConnection.getFutureReservations();
            List<ActiveParking> activeParkings = mysqlConnection.getActiveParkings();

            GetSiteActivityResponse response = new GetSiteActivityResponse(futureReservations, activeParkings);
            client.sendToClient(response);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.sendToClient("SITE_ACTIVITY_FAILED");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
    /**
     * Handles a ParkingDurationRequest from a client by querying the database
     * and returning a ParkingDurationResponse with detailed duration records.
     *
     * @param client The client connection that sent the request.
     * @param req    The request containing year and month filters.
     */
    private void handleParkingDurationRequest(ParkingDurationRequest req, ConnectionToClient client) {
        int year = req.getYear();
        int month = req.getMonth();

        List<ParkingDurationRecord> records = mysqlConnection.loadParkingDurationReport(year, month);
        ParkingDurationResponse response = new ParkingDurationResponse(records);

        try {
            client.sendToClient(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Handles a request from the client to generate a member status report.
     * This method delegates the report creation t  MonthlyReportHandler,
     * and sends the generated MemberStatusReportResponse back to the requesting client.
     *
     * @param req    The request object containing parameters for the report generation.
     * @param client The client that sent the request and will receive the response.
     */
    private void handleMemberStatusReportRequest(MemberStatusReportRequest req, ConnectionToClient client) {
        int year = req.getYear();
        int month = req.getMonth();

        List<DailySubscriberCount> records = mysqlConnection.loadMemberStatusReport(year, month);
        MemberStatusReportResponse response = new MemberStatusReportResponse(records);

        try {
            client.sendToClient(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}