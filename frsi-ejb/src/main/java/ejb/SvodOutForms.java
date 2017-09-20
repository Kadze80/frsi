package ejb;

import entities.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Marat.Madybayev on 28.10.2014.
 */
public interface SvodOutForms {
    void init();

    Map<String, String> getFormsData(java.util.Date reportDate, Long respId, String formName, String key);
    List<ReportsFormula> getReportsFormula(String reportDate, String formName);
    Long generateAndGetId(Form form, Date reportDate, List<RefRespondentItem> respondents, PortalUser user, String idn, String initStatus, Date curDate, Date fromDate, Date toDate) throws Exception;
    void regenerate(ReportListItem reportListItem, List<RefRespondentItem> respondents, PortalUser user, Date curDate, Date fromDate, Date toDate) throws Exception;
}
