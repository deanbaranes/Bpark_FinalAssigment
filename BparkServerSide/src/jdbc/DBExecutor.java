package jdbc;

import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;
   
/**
 * DBExecutor provides utility methods to simplify database access using a connection pool.
 * It ensures that connections are properly acquired from the pool and released after use,
 * minimizing boilerplate and reducing the risk of resource leaks.
 */
public class DBExecutor {    

    /**
     * Executes a database operation that returns a value (e.g. SELECT).
     * The connection is automatically managed (acquired and released).
     *
     * @param <T> The type of result the operation returns.
     * @param action A lambda function that receives a Connection and returns a result.
     * @return The result of the operation, or null if an exception occurred.
     */
    public static <T> T execute(Function<Connection, T> action) {
        Connection conn = null;
        try {
            // Acquire a connection from the pool
            conn = mysqlConnection.connectToDB();

            // Run the given function and return the result
            return action.apply(conn);

        } catch (Exception e) {
            // Print any exception that occurred during execution
            e.printStackTrace();
            return null;

        } finally {
            // Always release the connection back to the pool
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }

    /**
     * Executes a database operation that does not return a value (e.g. INSERT, UPDATE).
     * The connection is automatically managed (acquired and released).
     *
     * @param action A lambda function that receives a Connection and performs an action.
     */
    public static void executeVoid(Consumer<Connection> action) {
        Connection conn = null;
        try {
            // Acquire a connection from the pool
            conn = mysqlConnection.connectToDB();

            // Run the given action with the acquired connection
            action.accept(conn);

        } catch (Exception e) {
            // Print any exception that occurred during execution
            e.printStackTrace();

        } finally {
            // Always release the connection back to the pool
            ConnectionPool.getInstance().releaseConnection(conn);
        }
    }
}
