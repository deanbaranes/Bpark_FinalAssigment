// MemberStatusResponse.java
package common;

import java.io.Serializable;
import java.util.List;

public class MemberStatusReportResponse implements Serializable {
    private List<DailySubscriberCount> report;

    public MemberStatusReportResponse(List<DailySubscriberCount> report) {
        this.report = report;
    }

    public List<DailySubscriberCount> getReport() {
        return report;
    }
}