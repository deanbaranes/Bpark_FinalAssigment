package server;

import java.util.Timer;
import java.util.TimerTask;

/**
 * SchedulerTasks is responsible for initiating background tasks that run periodically.
 * The scheduled tasks are intended to automate key maintenance operations such as:
 * Towing vehicles that have overstayed their allowed time.
 * Cleaning up expired reservations that were not used.
 */
public class SchedulerTasks {

	/**
     * Starts all background scheduled tasks.
     * This includes:
     * Vehicle towing check (runs every minute).
     * Expired reservation cleanup (runs every minute).
     */
    public static void startAll() {
        startTowingCheck(); 
        startExpiredReservationCleaner();
    }   
       
    /**
     * Starts the recurring task that checks for vehicles that overstayed
     * their parking duration and should be towed.
     * Invokes mysqlConnection.checkAndTowVehicles() every 60 seconds.
     */
    private static void startTowingCheck() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
            	mysqlConnection.checkAndTowVehicles();
            }
        }, 0, 60 * 1000); // every minute
  }

    /**
     * Starts the recurring task that removes reservations which expired
     * (the reserved time passed and the reservation was not used).
     * Invokes mysqlConnection.removeExpiredReservations() every 60 seconds.
     */
    private static void startExpiredReservationCleaner() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                mysqlConnection.removeExpiredReservations(); 
            }
        }, 0, 60 * 1000);
    }
}
