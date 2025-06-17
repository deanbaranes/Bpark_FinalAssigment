package server;

import java.util.Timer;
import java.util.TimerTask;

public class SchedulerTasks {

    public static void startAll() {
        startTowingCheck(); 
        startReservationStatusUpdater();
    }   
       

    private static void startTowingCheck() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
            	mysqlConnection.checkAndTowVehicles();
            }
        }, 0, 60 * 1000); // every minute
  }

    private static void startReservationStatusUpdater() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
            	mysqlConnection.markUpcomingReservationsAsReserved();
            }
        }, 0, 60 * 1000); // every minute
    }

   
}
