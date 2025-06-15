// MemberStatusRequest.java
package common;

import java.io.Serializable;

public class MemberStatusReportRequest implements Serializable {
  
	private int year;
    private int month;

    public MemberStatusReportRequest(int year, int month) {
        this.year = year;
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }
}