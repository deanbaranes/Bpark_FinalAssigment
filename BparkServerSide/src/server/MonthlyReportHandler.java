// MonthlyReportHandler.java
package server;

import common.MemberStatusReportRequest;
import common.MemberStatusReportResponse;
import common.ParkingDurationRequest;
import common.ParkingDurationResponse;
import common.DailySubscriberCount;
import common.ParkingDurationRecord;
import java.util.List;

public class MonthlyReportHandler {

    private ReportGenerator reportGenerator = new ReportGenerator();

    /**
     * Handles a request for a Parking Duration Report.
     * @param request the request object containing year and month
     * @return a response object with a list of duration records
     */
    public ParkingDurationResponse handleParkingDurationRequest(ParkingDurationRequest request) {
        List<ParkingDurationRecord> report = DBExecutor.execute(
            conn -> reportGenerator.generateParkingDurationReport(conn, request.getYear(), request.getMonth())
        );
        return new ParkingDurationResponse(report);
    }

    /**
     * Handles a request for a Member Status Report.
     * @param request the request object containing year and month
     * @return a response object with a list of subscriber counts per day
     */
    public MemberStatusReportResponse handleMemberStatusRequest(MemberStatusReportRequest request) {
        List<DailySubscriberCount> report = DBExecutor.execute(
            conn -> reportGenerator.generateMemberStatusReport(conn, request.getYear(), request.getMonth())
        );
        return new MemberStatusReportResponse(report);
    }
}
