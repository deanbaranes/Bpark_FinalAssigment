package controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jdbc.mysqlConnection;

/**
 * SchedulerTasks is responsible for initiating background tasks that run periodically.
 * The scheduled tasks are intended to automate key maintenance operations such as:
 * Towing vehicles that have overstayed their allowed time.
 * Cleaning up expired reservations that were not used.
 */
public class SchedulerController {
	
	/**
	 * A single-threaded scheduled executor used to run the monthly report generator task
	 * on the 1st of each month at 01:00 AM.
	 */
	private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


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
     * Initializes the scheduling of the monthly report generator task.
     * This task is designed to run exactly on the 1st day of each month at 01:00 AM.
     * The method calculates the delay until the next 1st of the month at 01:00,
     * and schedules a Runnable that:
     * Generates the parking duration and member status reports for the previous month.
     * Reschedules itself for the following month.
     * This method should be called once during system startup.
     */
    private static void startMonthlyParkingReportGenerator() {
        Runnable reportTask = () -> {
            LocalDateTime now = LocalDateTime.now();
            int year = now.minusMonths(1).getYear();
            int month = now.minusMonths(1).getMonthValue();

            System.out.println("Generating monthly reports for " + month + "/" + year);
            mysqlConnection.generateAndStoreParkingDurationReport();
            mysqlConnection.generateAndStoreMemberStatusReport();

            scheduleNextMonthlyReport(SchedulerController::startMonthlyParkingReportGenerator);
        };

        scheduleNextMonthlyReport(reportTask);
    }

    
    /**
     * Schedules the given task to run at the next 1st of the month at 01:00 AM.
     * This method calculates the exact delay in milliseconds from the current time
     * until the desired execution time and schedules the task accordingly
     * using the ScheduledExecutorService.
     * @param task The Runnable to be executed at the scheduled time.
     */
    private static void scheduleNextMonthlyReport(Runnable task) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withDayOfMonth(1)
                                   .plusMonths(1)
                                   .withHour(1)
                                   .withMinute(0)
                                   .withSecond(0)
                                   .withNano(0);

        long delay = Duration.between(now, nextRun).toMillis();
        scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
    }
}
