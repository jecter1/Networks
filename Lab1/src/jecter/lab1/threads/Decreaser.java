package jecter.lab1.threads;

import jecter.lab1.Constants;
import jecter.lab1.TimeTable;

import java.util.Date;

public class Decreaser implements Runnable, Constants {
    private final TimeTable timeTable;
    private boolean running;

    public Decreaser(TimeTable timeTable) {
        this.timeTable = timeTable;
        this.running = true;
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while(running) {
            Date startTime = new Date();
            try {
                Thread.sleep(DECREASER_SLEEP_TIME_MS);
            } catch (InterruptedException exc) {
                exc.printStackTrace();
            }
            long time = new Date().getTime() - startTime.getTime();

            timeTable.decreaseTime(time);
        }
    }

    public void stop() {
        running = false;
    }
}
