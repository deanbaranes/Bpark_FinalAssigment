package server;
    
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * ConnectionPool is a manual implementation of a simple connection pooling mechanism.
 * It allows efficient reuse of a limited number of database connections to reduce overhead.
 * This version is suitable for small-scale applications like BPARK.
 */
public class ConnectionPool {
     
    // Singleton instance of the pool
    private static ConnectionPool instance;

    // Queue holding reusable Connection objects
    private final Queue<Connection> connectionPool = new LinkedList<>();

    // The maximum number of connections allowed in the pool
    private final int MAX_POOL_SIZE = 3;

    // Database credentials and connection URL
    private final String URL = "jdbc:mysql://localhost:3306/bpark?serverTimezone=Asia/Jerusalem&useSSL=false";
    private final String USER = "root";
    private final String PASSWORD = "Aa123456";

    /**
     * Private constructor to prevent direct instantiation.
     * Loads the JDBC driver and initializes the pool with 2 connections.
     */
    private ConnectionPool() {
        try {
            // Load the JDBC driver for MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace(); // Print error if the driver class isn't found
        }

        // Pre-populate the pool with 2 initial connections
        for (int i = 0; i < 2; i++) {
            connectionPool.add(createConnection());
        }

        System.out.println("Initialized connection pool with " + connectionPool.size() + " connections.");
    }

    /**
     * Returns the singleton instance of the ConnectionPool.
     * Thread-safe using 'synchronized' to avoid race conditions in multithreaded environments.
     */
    public static synchronized ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool(); // Create instance only once
        }
        return instance;
    }

    /**
     * Creates a new database connection.
     * @return A new Connection object
     * @throws RuntimeException if connection creation fails
     */
    private Connection createConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connection created and added to pool.");
            return conn;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create a DB connection");
        }
    }

    /**
     * Retrieves a connection from the pool.
     * If the pool is empty, creates a new connection instead.
     * @return An available Connection object
     */
    public synchronized Connection getConnection() {
        if (connectionPool.isEmpty()) {
            System.out.println("Pool empty, creating new connection.");
            return createConnection(); // fallback if pool is exhausted
        }

        Connection conn = connectionPool.poll(); // Take one from the front of the queue
        System.out.println("Connection retrieved from pool. Remaining: " + connectionPool.size());
        return conn;
    }

    /**
     * Returns a connection back to the pool if there's room.
     * If the pool is already full, closes the connection.
     * @param conn The connection to return
     */
    public synchronized void releaseConnection(Connection conn) {
        if (conn != null && connectionPool.size() < MAX_POOL_SIZE) {
            connectionPool.offer(conn); // Add to the back of the queue
            System.out.println("Connection released back to pool. Current size: " + connectionPool.size());
        } else {
            try {
                conn.close(); // If pool is full, release the resource completely
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Closes all connections currently held in the pool.
     * Typically used when the application shuts down.
     */
    public synchronized void closeAllConnections() {
        for (Connection conn : connectionPool) {
            try {
                conn.close(); // Properly release each connection
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        connectionPool.clear(); // Remove all from the queue
        System.out.println("Connection pool closed.");
    }
}
