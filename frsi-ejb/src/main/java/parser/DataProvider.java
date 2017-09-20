package parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dataform.NoReportDataError;
import ejb.Persistence;
import ejb.Reference;
import entities.*;

import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by nuriddin on 9/14/16.
 */
public class DataProvider extends AbstractDataProvider {
    private Gson gson = new Gson();
    private Persistence persistence;
    private Reference reference;

    public DataProvider(Persistence persistence, Reference reference) {
        this.persistence = persistence;
        this.reference = reference;
    }

    @Override
    protected List<SubjectType_Form> loadSubjectTypeForms(long stRecId) {
        return persistence.getSubjTypeForms(stRecId);
    }

    @Override
    public Map<String, String> getKeyColumns(String formCode, Date d) {
        List<Form> forms = persistence.getFormsByCodeReportDate(formCode, d);
        FormTag tag = forms.get(0).getFormHistory().getFormTag();
        if (tag == null || tag.keyFields == null)
            throw new IllegalStateException(MessageFormat.format("В форме {0} не описан ключевое поле", forms.get(0).getCode()));
        return tag.keyFields;
    }

    @Override
    protected Map<String, String> loadKvMap(ReportKey rk) throws NoReportDataError {
        Long reportId = persistence.getReportId(rk.getBin(), rk.getReportDate(), rk.getFormCode());
        if (reportId == null)
            throw new NoReportDataError();
        ReportStatus reportStatus = persistence.getLastReportStatusByReportId(reportId, true, null);
        if (reportStatus == null || !reportStatus.getStatusCode().equals(ReportStatus.Status.APPROVED.name())) {
            throw new NoReportDataError();
        }
        inputReports.add(reportId);
        Long lastReportHistoryItemId = persistence.getLastReportHistoryIdByReportId(reportId, true, null);
        ReportHistory reportHistoryItem = persistence.getReportHistory(lastReportHistoryItemId, true, false);
        String jsonData = reportHistoryItem.getData();
        if (jsonData == null || jsonData.isEmpty()) return new HashMap<String, String>();

        Type typeMapStringString = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> kvMap = gson.fromJson(jsonData, typeMapStringString);
        return kvMap;
    }

    @Override
    protected String getRefItemNameByRecId(String ref, String col, long recId, Date reportDate) {
        return reference.getRefItemNameByRecId(ref, col, recId, reportDate);
    }

    @Override
    protected RefRespondentItem loadRespondentByIdn(String idn, Date reportDate) {
        return reference.getRespondentByIdn(idn, reportDate);
    }

    @Override
    protected Form loadForm(String formCode, Date reportDate) {
        List<Form> forms = persistence.getFormsByCodeLanguageReportDate(formCode, "ru", reportDate, null);
        if (forms.size() == 0)
            return null;
        else
            return forms.get(0);
    }
}
