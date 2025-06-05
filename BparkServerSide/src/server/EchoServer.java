package server;


import java.io.*;
import java.util.List;

import common.LoginManagement;
import common.LoginRequest;
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
    /**
     * Constructs an EchoServer on the specified port.
     * @param port The port number to listen on.
     */
    public EchoServer(int port) {
        super(port);
    }

    /**
     * Handles messages received from a client.
     * @param msg The message sent by the client.
     * @param client The connection to the client.
     */
    @Override
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {
        System.out.println("Message received: " + msg + " from " + client);

        if (msg instanceof String) 
        {
            String command = (String) msg;

            if (command.equals("SHOW_ORDERS")) {
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
            }
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
            */
            
            /* Handles subscriber info request by ID and sends the data back to the client */
            else if (command.startsWith("REQUEST_ID_DETAILS|")) {
                String id = command.split("\\|")[1]; 
                String subscriberInfo = mysqlConnection.getSubscriberInfo(id);

                try {
                    client.sendToClient("SUBSCRIBER_INFO:" + subscriberInfo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
            /* Retrieves available parking spots from the database and sends the list to the client */
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
        /* Checks client login credentials and sends result to client */
        else if (msg instanceof LoginRequest) 
        {
            LoginRequest request = (LoginRequest) msg;
            boolean success = mysqlConnection.checkLogin(request.getFullName(), request.getSubscriptionCode());
            try 
            {
                client.sendToClient(success ? "LOGIN_SUCCESS" : "LOGIN_FAILURE");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        /* Checks manager login credentials and sends result to client */
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
        else 
        {
            try {
                client.sendToClient("Unsupported message format.");
            } catch (IOException e) {
                e.printStackTrace();
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
