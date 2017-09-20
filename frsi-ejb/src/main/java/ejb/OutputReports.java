package ejb;

import entities.*;

import java.util.Date;
import java.util.List;

/**
 * Created by nuriddin on 9/14/16.
 */
public interface OutputReports {
    void init();
    EjbResponse<Long> generateAndGetId(Form form, Date reportDate, List<RefRespondentItem> respondents, PortalUser user, String idn, String initStatus, Date curDate, Date fromDate, Date toDate) throws Exception;
    void regenerate(ReportListItem reportListItem, List<RefRespondentItem> respondents, PortalUser user, Date curDate, Date fromDate, Date toDate) throws Exception;
}
