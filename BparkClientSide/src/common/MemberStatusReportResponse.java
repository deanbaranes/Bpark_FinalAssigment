package common;

import java.io.Serializable;
import java.util.List;

/**
 * Represents the server's response to a MemberStatusReportRequest.
 * Contains a list of daily subscriber counts for a specific month,
 * used to generate subscriber activity reports.
 */
public class MemberStatusReportResponse implements Serializable {
    private List<DailySubscriberCount> report;

    /**
     * Constructs a new MemberStatusReportResponst with the given report data.
     *
     * @param report A list of DailySubscriberCount representing subscriber activity per day.
     */
    public MemberStatusReportResponse(List<DailySubscriberCount> report) {
        this.report = report;
    }

    /**
     * Returns the list of daily subscriber counts.
     *
     * @return A list of {@link DailySubscriberCount} objects.
     */
    public List<DailySubscriberCount> getReport() {
        return report;
    }
}