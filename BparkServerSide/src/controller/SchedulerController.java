package controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

import jdbc.mysqlConnection;

/**
 * SchedulerTasks is responsible for initiating background tasks that run periodically.
 * The scheduled tasks are intended to automate key maintenance operations such as:
 * Towing vehicles that have overstayed their allowed time.
 * Cleaning up expired reservations that were not used.
 */
public class SchedulerController {

	/**
     * Starts all background scheduled tasks.
     * This includes:
     * Vehicle towing check (runs every minute).
     * Expired reservation cleanup (runs every minute).
     */
    public static void startAll() {
        startTowingCheck(); 
        startExpiredReservationCleaner();
        startMonthlyParkingReportGenerator();
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
    
    /**
     * Starts a scheduled task that generates monthly reports on the 1st of each month at 01:00 AM.
     * This task performs two main actions:
     * Generates and stores a parking duration report for the previous month.
     * Generates and stores a member status report for the previous month.
     * The task is first scheduled to run at the beginning of the next month at 01:00,
     * and then continues to repeat approximately every 30 days.
     */
    private static void startMonthlyParkingReportGenerator() {
        Timer timer = new Timer(true);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstOfNextMonth = now.withDayOfMonth(1).plusMonths(1).withHour(1).withMinute(0).withSecond(0).withNano(0);
        long initialDelay = Duration.between(now, firstOfNextMonth).toMillis();

        long oneMonthInMillis = 1000L * 60 * 60 * 24 * 30; 

        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                int year = now.getYear();
                int month = now.getMonthValue();

                if (month == 1) {
                    year -= 1;
                    month = 12;
                } else {
                    month -= 1;
                }

                System.out.println("Generating monthly reports for " + month + "/" + year);
                mysqlConnection.generateAndStoreParkingDurationReport();
                mysqlConnection.generateAndStoreMemberStatusReport();
            }
        }, initialDelay, oneMonthInMillis);
    }


}
