// DailySubscriberCount.java
package common;

import java.io.Serializable;

public class DailySubscriberCount implements Serializable {
    private int day;
    private int subscriberCount;

    public DailySubscriberCount(int day, int subscriberCount) {
        this.day = day;
        this.subscriberCount = subscriberCount;
    }

    public int getDay() {
        return day;
    }

    public int getSubscriberCount() {
        return subscriberCount;
    }
}
