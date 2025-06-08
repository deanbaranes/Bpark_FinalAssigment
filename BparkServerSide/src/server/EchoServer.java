package server;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import common.LoginManagement;
import common.LoginRequest;
import common.ParkingHistory;
import common.RegisterMemberRequest;
import common.Reservation;
import common.Subscriber;
import common.ActiveParking;
import common.UpdateSubscriberDetailsRequest;
import ocsf.server.*;


/**
 * EchoServer is a server-side class that listens for client connections
 * and handles messages sent by the client. It uses the OCSF framework
 * and communicates with a MySQL database to retrieve and update order data.
 */
public class EchoServer extends AbstractServer {

    /** Default port to listen on. */
    final public static int DEFAULT_PORT = 5555;
    private List<String> availableSpots;
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
            if (msg instanceof String command) {
                handleStringCommand(command, client);
            } else if (msg instanceof LoginRequest request) {
                handleLoginRequest(request, client);
            } else if (msg instanceof LoginManagement management) {
                handleManagementLogin(management, client);
            } else if (msg instanceof UpdateSubscriberDetailsRequest update) {
                handleSubscriberUpdate(update, client);
            } else if (msg instanceof RegisterMemberRequest request) {
                handleRegisterMember(request, client);  // ✅ נוספה התמיכה ברישום לקוח חדש
            } else if (msg instanceof Reservation req && req.getReservationId() == 0) { //new reservation
                handleNewReservationRequest(req, client);
            } else {
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
        } else if (command.startsWith("GET_RESERVATIONS|")) {
            String id = command.split("\\|")[1];
            client.sendToClient(mysqlConnection.getReservationsForSubscriber(id));
        } else if (command.equals("REQUEST_AVAILABLE_SPOTS")) {
            client.sendToClient(mysqlConnection.getAvailableSpots());
            
        } else if (command.equals("CHECK_PARKING_AVAILABILITY")) {
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

        } else {
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
                        client.sendToClient("APP_LOGIN_SUCCESS");
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
    private void handleManagementLogin(LoginManagement login, ConnectionToClient client) throws IOException {
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
                request.getVehicleNumber2(),
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
    
    private void handleNewReservationRequest(Reservation req, ConnectionToClient client) {
        try {
            int totalSpots = mysqlConnection.getTotalParkingSpots();
            int available = mysqlConnection.getAvailableSpotsCount();

            // Check if less than 40% of spots are available
            if (available < totalSpots * 0.4) {
                client.sendToClient("RESERVATION_FAILED|Less than 40% of parking spots are available at the moment.");
                return;
            }

            int spot = mysqlConnection.findAvailableSpot();

            if (spot == -1) {
                client.sendToClient("RESERVATION_FAILED|No available parking spot found at this time.");
                return;
            }

            String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            LocalDate exitDate = req.getEntryDate();
            LocalTime exitTime = req.getEntryTime().plusHours(RESERVATION_DURATION_HOURS); // usually 4 hours

            // Insert reservation into DB and update spot
            mysqlConnection.insertReservationAndUpdateSpot(
                req.getSubscriberId(), code,
                req.getEntryDate(), req.getEntryTime(),
                exitDate, exitTime, spot
            );

            // Send the new confirmed reservation back to client
            Reservation confirmed = new Reservation(
                0,
                req.getSubscriberId(),
                code,
                req.getEntryDate(),
                req.getEntryTime(),
                exitDate,
                exitTime,
                spot
            );

            client.sendToClient(confirmed);

        } catch (Exception e) {
            e.printStackTrace();
            try {
                client.sendToClient("RESERVATION_FAILED|A server error occurred while processing your request.");
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
}






/**
 * Handles messages sent from clients to the server.
 * Routes requests based on object type or command string.
 * Manages login logic, subscriber info requests, and update requests.
 *
 * @param msg Message received from the client.
 * @param client Reference to the client connection.
 */
/*
* 
*  @Override
public void handleMessageFromClient(Object msg, ConnectionToClient client) {
    System.out.println("Message received: " + msg + " from " + client);

    if (msg instanceof String) 
    {
        String command = (String) msg;

         if (command.equals("SHOW_ORDERS")) { /////PROTOTYPE
            try {
                String orders = mysqlConnection.getAllOrders();
                client.sendToClient(orders);
                client.sendToClient("Please choose another action if desired or press 'exit' to log out.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (command.startsWith("UPDATE_ORDER|")) {
            String[] parts = command.split("\\|");
            if (parts.length == 4) {
                String id = parts[1];
                String field = parts[2];
                String value = parts[3];
                boolean success = mysqlConnection.updateOrderField(id, field, value);
                try {
                    client.sendToClient(success ? "Order updated.\n" : "Failed to update.\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }*/
        /*else if (command.startsWith("CHECK")) { //// זה היה של האבטיפוס
        	
            String idStr = command.replace("CHECK", "");
            try {
                int orderId = Integer.parseInt(idStr);
                boolean exists = mysqlConnection.doesOrderExist(orderId);
                client.sendToClient(exists ? "ORDER_EXISTS:" + orderId : "ORDER_DOES_NOT_EXIST");
            } catch (NumberFormatException e) {
                try {
                    client.sendToClient("INVALID_ORDER_ID");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            catch (IOException e) 
            {
                e.printStackTrace();
            }
        }
        
        
        // Handles subscriber info request by ID and sends the data back to the client 
        if (command.startsWith("REQUEST_ID_DETAILS|")) {
            String id = command.split("\\|")[1]; 
            String subscriberInfo = mysqlConnection.getSubscriberInfo(id);

            try {
                client.sendToClient("SUBSCRIBER_INFO:" + subscriberInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        else if (command.startsWith("GET_HISTORY|")) {
            String id = command.split("\\|")[1];
            List<ParkingHistory> history = mysqlConnection.getHistoryForSubscriber(id); 

            try {
                client.sendToClient(history);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        else if (command.startsWith("GET_RESERVATIONS|")) {
            String id = command.split("\\|")[1];
            List<Reservation> reservations = mysqlConnection.getReservationsForSubscriber(id); //   
            try {
                client.sendToClient(reservations);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Retrieves available parking spots from the database and sends the list to the client 
            else if (msg.equals("REQUEST_AVAILABLE_SPOTS")) 
            {
                availableSpots = mysqlConnection.getAvailableSpots();
                try 
                {
                    client.sendToClient(availableSpots); 
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    		else 
    		{
                try {
                    client.sendToClient("Unrecognized command.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
    		}
        
    } 
    // Checks client login via terminal/application credentials and sends result to client 
    else if (msg instanceof LoginRequest) 
    {
        LoginRequest request = (LoginRequest) msg;
        boolean success = mysqlConnection.checkLogin(request.getID(), request.getSubscriptionCode());

        // print to console the source
        System.out.println("LoginRequest received from source: " + request.getSource());

        try {
            switch (request.getSource()) {
                case "terminal":
                    client.sendToClient(success ? "TERMINAL_LOGIN_SUCCESS" : "TERMINAL_LOGIN_FAILURE");
                    break;
                case "app":
                    if (success) {
                        Subscriber subscriber = mysqlConnection.getSubscriberById(request.getID());
                        if (subscriber != null) {
                            client.sendToClient("APP_LOGIN_SUCCESS");
                            client.sendToClient(subscriber);
                        } else {
                            client.sendToClient("APP_LOGIN_FAILED_NO_DATA");
                        }
                    } else {
                        client.sendToClient("APP_LOGIN_FAILURE");
                    }
                    break;
            
                default:
                    client.sendToClient("LOGIN_FAILURE"); // unknown source
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    // Checks manager login credentials and sends result to client 
    else if (msg instanceof LoginManagement) 
    {
    	LoginManagement loginData = (LoginManagement) msg;
        boolean LoginApproved = mysqlConnection.checkLoginManagement(loginData.getUsername(),loginData.getPassword());
        try 
        {
            client.sendToClient(LoginApproved ? "LOGIN_Management_SUCCESS" : "LOGIN_Management_FAILURE");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Handles an UpdateSubscriberDetailsRequest by updating DB and sending result
    else if (msg instanceof UpdateSubscriberDetailsRequest) {
        UpdateSubscriberDetailsRequest update = (UpdateSubscriberDetailsRequest) msg;
        boolean success = mysqlConnection.updateSubscriberContactInfo(
            update.getSubscriberId(),
            update.getNewEmail(),
            update.getNewPhone()
        );

        try {
            client.sendToClient(success ? "SUBSCRIBER_UPDATE_SUCCESS" : "SUBSCRIBER_UPDATE_FAILURE");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    else 
    {
        try {
            client.sendToClient("Unsupported message format.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}*/
