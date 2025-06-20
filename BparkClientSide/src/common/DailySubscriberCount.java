package common;

import java.io.Serializable;

/**
 * Represents the number of subscribers that visited or were active on a specific day.
 * Typically used for generating daily reports or statistics.
 */
public class DailySubscriberCount implements Serializable {
    private int day;
    private int subscriberCount;

    /**
     * Constructs a new {@code DailySubscriberCount} with the given day and subscriber count.
     *
     * @param day              The day of the month (1–31) or another unit depending on context (e.g., day index).
     * @param subscriberCount  The number of subscribers associated with that day.
     */
    public DailySubscriberCount(int day, int subscriberCount) {
        this.day = day;
        this.subscriberCount = subscriberCount;
    }

    /**
     * Returns the day associated with this record.
     *
     * @return The day value.
     */
    public int getDay() {
        return day;
    }

    /**
     * Returns the number of subscribers for the given day.
     *
     * @return The subscriber count.
     */
    public int getSubscriberCount() {
        return subscriberCount;
    }
}
