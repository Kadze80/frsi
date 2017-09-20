package dataform;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ejb.PersistenceLocal;
import entities.Form;
import entities.FormHistory;
import entities.InputValueCheck;
import entities.ReportHistory;
import jaxb.Data;
import jaxb.Item;
import jaxb.Report;
import util.Convert;
import util.Validators;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.*;

public class XmlImporter {

    private String xml;
    private entities.Report report;

    public XmlImporter(String xml) {
        this.xml = xml;
    }

    public void createKvMap(PersistenceLocal persistence, String languageCode, Date reportDate) throws Exception {
        report = null;
        Report jaxbReport = null;
        JAXBContext context = JAXBContext.newInstance(Report.class, Data.class, Item.class);
        StringReader stringReader = new StringReader(xml);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        jaxbReport = (Report) unmarshaller.unmarshal(stringReader);

        if (jaxbReport != null && jaxbReport.data != null && jaxbReport.data.items != null && !jaxbReport.data.items.isEmpty()) {
            report = new entities.Report();
            report.setFormCode(jaxbReport.form);

            List<Form> forms = persistence.getFormsByCodeLanguageReportDate(report.getFormCode(), languageCode, reportDate, null);

            FormHistory dummyForm = persistence.getFormHistoryWithInputValueChecks(forms.get(0).getFormHistory().getId());
            Type typeListInputValueCheck = new TypeToken<List<InputValueCheck>>() {}.getType();
            List<InputValueCheck> inputValueChecks = new Gson().fromJson(dummyForm.getInputValueChecks(), typeListInputValueCheck);
            Map<String, String> kvMap = new HashMap<String, String>();

            Map<String, List<InputValueCheck>> dataMasks = new HashMap<String, List<InputValueCheck>>();
            for (InputValueCheck inputValueCheck : inputValueChecks) {
                if (inputValueCheck.getAuto() == null || !inputValueCheck.getAuto().booleanValue()) {
                    if (inputValueCheck.getMask() != null && !inputValueCheck.getMask().trim().isEmpty()) {
                        if (!dataMasks.containsKey(inputValueCheck.getKey()))
                            dataMasks.put(inputValueCheck.getKey(), new ArrayList<InputValueCheck>());
                        dataMasks.get(inputValueCheck.getKey()).add(inputValueCheck);
                    }
                }
            }

            for (Item item : jaxbReport.data.items) {
                /*for (InputValueCheck inputValueCheck : inputValueChecks) {
                    if(inputValueCheck.getAuto()!=null && inputValueCheck.getAuto().booleanValue())
                        continue;
                    if(item.key.equals(inputValueCheck.getKey()) && inputValueCheck.getMask() != null) {
                        item.value = Convert.getNumWithMaskFromStr(item.value,inputValueCheck.getMask());
                        if (!Validators.isValidMask(item.value,inputValueCheck.getMask())) {
                            throw new Exception("Значение параметра + " + item.key + " не соответствует заданному формату!");
                        }
                        break;
                    }
                }*/
                String finalKey;
                int dPos = item.key.indexOf("$D.");
                if (dPos == -1)
                    finalKey = item.key;
                else {
                    finalKey = item.key.substring(0, dPos) + "$DynamicRowId";
                }
                InputValueCheck inputValueCheck = null;
                List<InputValueCheck> inputValueCheckList = dataMasks.get(finalKey);
                if(inputValueCheckList!=null) {
                    if (inputValueCheckList.size() == 1)
                        inputValueCheck = inputValueCheckList.get(0);
                    else {
                        String groupId = item.key.substring(0, item.key.lastIndexOf(".") + 1);
                        for (InputValueCheck ivch : inputValueCheckList) {
                            if (ivch.getGroupId() == null)
                                inputValueCheck = ivch;
                            else if (ivch.getGroupId().equals(groupId)) {
                                inputValueCheck = ivch;
                                break;
                            }
                        }
                    }
                }
                if (inputValueCheck != null && inputValueCheck.getMask() != null) {
                    try {
                        item.value = Convert.getNumWithMaskFromStr(item.value, inputValueCheck.getMask());
                    } catch (Exception e) {
                        throw new Exception("Значение параметра " + item.key + " не соответствует заданному формату!");
                    }
                }
                kvMap.put(item.key, item.value);
            }

            ReportHistory reportHistory = new ReportHistory();
            reportHistory.setReport(report);
            reportHistory.setKvMap(kvMap);

            List<ReportHistory> reportHistoryList = new ArrayList<ReportHistory>();
            reportHistoryList.add(reportHistory);

            report.setReportHistory(reportHistoryList);
        }
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public entities.Report getReport() {
        return report;
    }
}
