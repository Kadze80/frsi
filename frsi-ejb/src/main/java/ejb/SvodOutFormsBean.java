package ejb;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dataform.*;
import entities.*;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;
import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import util.DbUtil;
import util.ExceptionUtil;
import util.PeriodUtil;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Marat.Madybayev on 28.10.2014.
 */

@Stateless
public class SvodOutFormsBean implements SvodOutFormsLocal, SvodOutFormsRemote, IKeyHandler {
    public static final String MANDATORY_REPORT_PREFIX = "^";
    public static final String REFERENCE_DATA_PREFIX = "ref;";
    public static final String VALUE_TRUE = "Да";
    public static final String VALUE_FALSE = "Нет";
    private static final Logger logger = Logger.getLogger("fileLogger");
    private static final String JDBC_POOL_NAME = "jdbc/FrsiPool";

    private static final String RESPONDENT_REC_ID_KEY = "_reports_array*respondent_rec_id:num:$D.group.";

    @EJB
    private CoreLocal core;
    @EJB
    private PersistenceLocal persistence;
    @EJB
    private ReferenceLocal reference;

    private DataSource dataSource;
    private Gson gson;

    private DateEval dateEval = new DateEval();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyyMMdd");

    private Map<String, Map<String, String>> data;
    private Date reportDate;
    private String respondentIdn;
    private Set<Long> inputReportIds;
    private boolean noReport;
    private int currentRow;
    private String currentInputFormCode;
    private RefReportsRulesItem currentReportsRulesItem;

    private Map<String, Map<Long, LiteRefItem>> liteRefItems;
    private Iterator<LocalDate> dynamicDateIterator;
    private Date currentDynamicDate;
    private boolean currentFormulaHasData;

    // поля которые не должны использоваться при подсчете когда формируются сводные выходные формы
    /*private String[] FieldName = new String[] {"name_item", "code", "num_acc", "name_acc", "feature", "name_feature", "num", "name", "name_bank",
    "country", "name_emitter", "name_country_emitter", "name_cb", "nin_iin", "zaims", "assets", "name_jurper", "cont_oper", "vid_cb", "name_creditor",
    "code_branch", "is_ooovbo", "vid_oper", "purp_trans", "num_dog", "begdate_dog", "enddate_dog", "decision_sovdir", "bin", "name_class", "vid_curr",
    "date", "name1", "name2", "person", "date_acq", "name_contr", "datesattl_repo", "num_doc", "date_openrepo", "date_closerepo", "date_prolong", "deadlineoper_repo", "fee_rate",
    "currency_cb", "date_maturity", "categ_stockexch_dateacq", "categ_stockexch_repdate", "rating_dateacq", "rating_repdate",
            "report_date", "bank",
            // поля респондента
            "no", "weight", "is_organization_doc", "name_ru", "is_identification", "short_name_kz", "is_person_doc", "name_kz", "short_name_ru", "sign_count",
            "nokbdb_code", "short_name", "ruk", "main_buh", "date_begin_lic", "date_end_lic", "stop_lic", "vid_activity", "legal_address", "fact_address",
            "name_en",

            "ceo", "chief_accountant", "drafted_by", "signature_date"};*/

    @PostConstruct
    @Override
    public void init() {
        Date dateStart = new Date();

        // JDBC connection pool
        try {
            Context context = new InitialContext();
            dataSource = (DataSource) context.lookup(JDBC_POOL_NAME);
            logger.info("Connected to " + JDBC_POOL_NAME);
        } catch (NamingException e) {
            logger.error("Could not connect to " + JDBC_POOL_NAME);
            throw new EJBException(e);
        }

        gson = new Gson();
        //logger.debug("Entity = " + getEntity(new Long(560L)));

        Date dateEnd = new Date();
        long duration = dateEnd.getTime() - dateStart.getTime();
        logger.debug(MessageFormat.format("t: {0}, d: {1} ms", dateStart, duration));
    }

    public Map<String, String> getResultForm(Date reportDate, String formName, List<RefRespondentItem> respondents, Form consForm, Form inputForm) {
        //System.out.println("reportDate="+reportDate);
        //System.out.println("formName="+formName);
        /*
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        String dateInString = "01.11.2014";
        java.util.Date report_date = null;
        try {
            report_date = sdf.parse(dateInString);
        } catch (ParseException e) {
            logger.error("ParseException: " + e.getMessage());
            throw new EJBException(e);
        }
        */


        liteRefItems = new HashMap<String, Map<Long, LiteRefItem>>();
        FormHistory formHistory = persistence.getFormHistoryWithInputValueChecks(consForm.getFormHistory().getId());

        List<InputValueCheck> inputValueChecks;
        Gson gson = new Gson();
        Type typeListInputValueCheck = new TypeToken<List<InputValueCheck>>() {
        }.getType();
        inputValueChecks = gson.fromJson(formHistory.getInputValueChecks(), typeListInputValueCheck);
        if (inputValueChecks == null) inputValueChecks = new ArrayList<InputValueCheck>();
        final Map<String, List<InputValueCheck>> inputValueCheckIndex = makeIndexOfInputValueChecks(inputValueChecks);

        Set<String> numericFields = getNumericValueFields(formName, reportDate);

        //List<ApprovalItem> approvalItemList = persistence.getApprovalItemsByRepDateForm(reportDate, formName);
        List<Report> approvedReports = persistence.getReportByRepDateFormRespondentsStatusCode(reportDate, formName, respondents, ReportStatus.Status.APPROVED.toString());
        Map<String, String> svodForm = new HashMap<String, String>();
        ArrayList<ArrayList<SvodDinamicBean>> listOLists = new ArrayList<ArrayList<SvodDinamicBean>>();

        if (inputForm.getFormHistory().getFormTag() != null && inputForm.getFormHistory().getFormTag().hasDynamicRows) {
            for (Report report : approvedReports) {
                ArrayList<SvodDinamicBean> dinamicRow = new ArrayList<SvodDinamicBean>();
                //System.out.println("entityId:"+item.getEntityId());

                ReportHistory reportHistory = persistence.getLastReportHistoryByReportId(report.getId(), true, false, true, null);
                String jsonData = reportHistory.getData();
                if (jsonData == null || jsonData.isEmpty()) continue;
                Type typeMapStringString = new TypeToken<Map<String, String>>() {
                }.getType();
                Map<String, String> form = gson.fromJson(jsonData, typeMapStringString);

                for (Map.Entry<String, String> entry : form.entrySet()) {
                    String key = entry.getKey();
                    // заполняем список(List) только динамическими строками
                    if (key.contains("$D")) {
                        String num = key.substring(key.indexOf("$D.") + 3);
                        String field = key.substring(key.indexOf("*") + 1, key.indexOf(":"));
                        SvodDinamicBean svodDinamicBean = new SvodDinamicBean();
                        svodDinamicBean.setNum(num);
                        svodDinamicBean.setKey(key);
                        svodDinamicBean.setValue(entry.getValue());
                        svodDinamicBean.setNumericValue(numericFields.contains(field));
                        dinamicRow.add(svodDinamicBean);
                    } else { // не динамические строки добавляем в Map, суммируя значения
                        String value = svodForm.get(entry.getKey());
                        System.out.println("entry.getKey()" + " = " + value);
                        //System.out.println("value="+value);
                        double val = 0;

                        try {
                            if (value != null) {
                                try {
                                    val = Double.parseDouble(value);
                                } catch (NumberFormatException e) {
                                    val = 0;
                                }
                            }
                            if (entry.getValue() != null) {
                                try {
                                    val += Double.parseDouble(entry.getValue());
                                } catch (NumberFormatException e) {

                                }
                            }
                            svodForm.put(entry.getKey(), convertDecimalFormatSymbols(val));
                        } catch (Exception e) {
                            svodForm.put(entry.getKey(), entry.getValue());
                        }
                        /* работающий код
                        if (!Arrays.asList(FieldName).contains(parserKeyMap(entry.getKey()))) {
                            if (value != null) {
                                val = Double.parseDouble(value);
                            }
                            if (entry.getValue() != null) {
                                //String parsVal = UtilConvert.ConvertToNumberFormat(Double.parseDouble(entry.getValue()));
                                val += Double.parseDouble(entry.getValue());
                            }
                            //svodForm.put(entry.getKey(), new Integer(val).toString());
                            svodForm.put(entry.getKey(), new Double(val).toString());
                        } else {
                            svodForm.put(entry.getKey(), entry.getValue());
                        }
                        */
                    }
                }
                listOLists.add(dinamicRow);
            }
            /*
            //Sorting
            Collections.sort(dinamicRow, new Comparator<SvodDinamicBean>() {
                @Override
                public int compare(SvodDinamicBean dinamicRow1, SvodDinamicBean dinamicRow2)
                {
                    return dinamicRow1.getNum().compareTo(dinamicRow2.getNum());
                }
            });
            */
            List<SvodDinamicBean> dinamicRow = new ArrayList<SvodDinamicBean>();
            int i = 0;
            int k = 0;
            List<SvodDinamicBean> svodFullDinamicList = Lists.newArrayList();
            List<String> numFullList = Lists.newArrayList();
            for (ArrayList<SvodDinamicBean> listSingle : listOLists) {
                //Sorting
                Collections.sort(listSingle, new Comparator<SvodDinamicBean>() {
                    @Override
                    public int compare(SvodDinamicBean listSingle1, SvodDinamicBean listSingle2) {
                        return listSingle1.getNum().compareTo(listSingle2.getNum());
                    }
                });
                String prevNum = "";
                for (SvodDinamicBean item : listSingle) {
                    if (prevNum.equals(item.getNum())) continue;
                    String num = item.getNum();
                    // фильтруем
                    List<SvodDinamicBean> filtered = Lists.newArrayList();
                    for (SvodDinamicBean p : listSingle) {
                        if (num.equals(p.getNum())) {
                            filtered.add(p);
                        }
                    }
                    //Sorting
                    Collections.sort(svodFullDinamicList, new Comparator<SvodDinamicBean>() {
                        @Override
                        public int compare(SvodDinamicBean svodFullDinamicList1, SvodDinamicBean svodFullDinamicList2) {
                            return svodFullDinamicList1.getNum().compareTo(svodFullDinamicList2.getNum());
                        }
                    });
                    boolean isMatch = findElementInSvodFullDinamicList(svodFullDinamicList, filtered, formName);
                    if (!isMatch) {
                        String baseNumber = num.substring(0, num.lastIndexOf("."));
                        for (String n : numFullList) {
                            if (n.startsWith(baseNumber)) {
                                num = n;
                                break;
                            }
                        }
                        // определяем есть ли в окончательном списке такой Номер, если есть то увеличиваем на 1 до тех пор пока такого номера не будет
                        while (numFullList.contains(num)) {
                            String baseNum = num.substring(0, num.lastIndexOf("."));
                            int incNum = Integer.parseInt(num.substring(num.lastIndexOf(".") + 1)) + 1;
                            num = baseNum + "." + incNum;
                        }
                        // заменяем в key порядковый номер и добавляем в окончательный список
                        for (SvodDinamicBean filtItem : filtered) {
                            String key = filtItem.getKey();
                            // формируем новый ключ
                            String newKey = key.substring(0, key.indexOf("$D.")) + "$D." + num;
                            SvodDinamicBean svodDinamicBean = new SvodDinamicBean();
                            svodDinamicBean.setKey(newKey);
                            svodDinamicBean.setNum(num);
                            svodDinamicBean.setValue(filtItem.getValue());
                            svodFullDinamicList.add(svodDinamicBean);

                        }
                        // добавляем новый номер в список номеров
                        numFullList.add(num);
                    }
                    prevNum = item.getNum();
                }
                i++;
            }

            for (SvodDinamicBean item : svodFullDinamicList) {
                System.out.println(item.getNum() + " : " + item.getKey() + " : " + item.getValue());
                svodForm.put(item.getKey(), item.getValue());
            }

/*
            for (SvodDinamicBean item : dinamicRow) {
                String prevBaseNum = "";
                String num = item.getNum();
                String key = item.getKey();
                //int ind = num.lastIndexOf("\\.");
                String baseNum = num.substring(0, num.lastIndexOf("."));
                if (i == 0) prevBaseNum = baseNum;
                String newNum = "";
                if (!baseNum.equals(prevBaseNum)) {
                    k = 0;
                }
                // формируем новые порядковые номера для динамических строк
                newNum = baseNum + k++;
                // формируем новый ключ
                String newKey = key.substring(0, key.indexOf("$D.")) + newNum;
                // добавляем в Map, в котором уже находятся не динамические строки
                svodForm.put(newKey, item.getValue());

                prevBaseNum = baseNum;
                i++;

            }
*/
        } else {
            for (Report report : approvedReports) {
                ReportHistory reportHistory = persistence.getLastReportHistoryByReportId(report.getId(), true, false, true, null);
                String jsonData = reportHistory.getData();
                if (jsonData == null || jsonData.isEmpty()) continue;
                Type typeMapStringString = new TypeToken<Map<String, String>>() {
                }.getType();
                Map<String, String> form = gson.fromJson(jsonData, typeMapStringString);
                for (Map.Entry<String, String> entry : form.entrySet()) {
                    String field = entry.getKey().substring(entry.getKey().indexOf("*") + 1, entry.getKey().indexOf(":"));
                    if (!numericFields.contains(field))
                        continue;
                    String value = svodForm.get(entry.getKey());
                    System.out.println("entry.getKey()" + " = " + value);
                    //System.out.println("value="+value);
                    double val = 0;
                    //if (!Arrays.asList(FieldName).contains(parserKeyMap(entry.getKey()))) {
                    if (value != null) {
                        try {
                            val = Double.parseDouble(value);
                        } catch (NumberFormatException e) {
                            val = 0;
                        }
                    }
                    if (entry.getValue() != null) {
                        //String parsVal = UtilConvert.ConvertToNumberFormat(Double.parseDouble(entry.getValue()));
                        try {
                            val += Double.parseDouble(entry.getValue());
                        } catch (NumberFormatException e) {
                            val = 0;
                        }
                    }
                    //svodForm.put(entry.getKey(), new Integer(val).toString());
                    //if (!svodForm.containsKey(entry.getKey())) {
                    //svodForm.put(entry.getKey(), new Double(val).toString());
                    svodForm.put(entry.getKey(), convertDecimalFormatSymbols(val));// new Double(val).toString());
                    //} else {

                    //}
                    /*} else {
                        if (!svodForm.containsKey(entry.getKey())) {
                            svodForm.put(entry.getKey(), entry.getValue());
                        } else {
                            if (svodForm.containsValue(entry.getValue())) {
                                svodForm.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }*/
                }

            }
        }
        Map<String, String> svodOutForm = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : svodForm.entrySet()) {
            String key = entry.getKey();
            key = key.replaceAll(formName, formName + "_out");
            svodOutForm.put(key, entry.getValue());
            System.out.println("key=" + entry.getKey() + ";value=" + entry.getValue());
        }

        if (consForm.getFormHistory().getFormTag() != null && consForm.getFormHistory().getFormTag().sortFields != null) {
            for (SortField sortField : consForm.getFormHistory().getFormTag().sortFields) {
                String containerName = sortField.name.substring(0, sortField.name.indexOf("*"));
                String fieldName = sortField.name.substring(sortField.name.indexOf("*") + 1);
                sortDynamicList(svodOutForm, containerName, fieldName, sortField.desc != null ? sortField.desc : false, inputValueCheckIndex, reportDate);
            }
        }

        return svodOutForm;
    }

    private void sortDynamicList(final Map<String, String> svodForm, String containerName, String sortField, final boolean desc, final Map<String, List<InputValueCheck>> inputValueCheckIndex, final Date reportDate) {
        List<SvodDinamicBean> svodFullDynamycList = new ArrayList<SvodDinamicBean>();
        for (Map.Entry<String, String> entry : svodForm.entrySet()) {
            String key = entry.getKey();
            if (key.contains("$D")) {
                String num = key.substring(key.indexOf("$D.") + 3);
                SvodDinamicBean svodDinamicBean = new SvodDinamicBean();
                svodDinamicBean.setNum(num);
                svodDinamicBean.setKey(key);
                svodDinamicBean.setValue(entry.getValue());
                svodFullDynamycList.add(svodDinamicBean);
            }
        }

        Collections.sort(svodFullDynamycList, new Comparator<SvodDinamicBean>() {
            @Override
            public int compare(SvodDinamicBean svodFullDinamicList1, SvodDinamicBean svodFullDinamicList2) {
                return svodFullDinamicList1.getNum().compareTo(svodFullDinamicList2.getNum());
            }
        });

        Map<String, List<SvodDinamicBean>> idx = new HashMap<String, List<SvodDinamicBean>>();
        List<List<SvodDinamicBean>> lists = new ArrayList<List<SvodDinamicBean>>();
        String prevGroupNum = "";
        for (SvodDinamicBean bean : svodFullDynamycList) {
            String contName = bean.getKey().substring(0, bean.getKey().indexOf("*"));
            String fieldName = bean.getKey().substring(bean.getKey().indexOf("*") + 1, bean.getKey().indexOf(":"));
            if (contName.equals(containerName) && fieldName.equals(sortField)) {
                String groupNum = !bean.getNum().contains(".") ? "0" : bean.getNum().substring(0, bean.getNum().lastIndexOf("."));
                if (!prevGroupNum.equals(groupNum))
                    lists.add(new ArrayList<SvodDinamicBean>());
                lists.get(lists.size() - 1).add(bean);
                prevGroupNum = groupNum;
            }

            if (contName.equals(containerName)) {
                if (!idx.containsKey(bean.getNum()))
                    idx.put(bean.getNum(), new ArrayList<SvodDinamicBean>());
                idx.get(bean.getNum()).add(bean);
            }
        }

        for (List<SvodDinamicBean> list : lists) {
            List<String> sortedNums = new ArrayList<String>();
            for (SvodDinamicBean bean : list)
                sortedNums.add(bean.getNum());
            Collections.sort(sortedNums, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    int no1, no2;
                    try {
                        if (o1.contains(".")) {
                            no1 = Integer.valueOf(o1.substring(o1.lastIndexOf(".") + 1));
                            no2 = Integer.valueOf(o2.substring(o2.lastIndexOf(".") + 1));
                        } else {
                            no1 = Integer.valueOf(o1);
                            no2 = Integer.valueOf(o2);
                        }
                        return no1 - no2;
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
            });

            List<SvodDinamicBean> sortedByValue = new ArrayList<SvodDinamicBean>();
            sortedByValue.addAll(list);
            Collections.sort(sortedByValue, new Comparator<SvodDinamicBean>() {
                @Override
                public int compare(SvodDinamicBean o1, SvodDinamicBean o2) {
                    String v1 = getValue(o1, inputValueCheckIndex, reportDate);
                    String v2 = getValue(o2, inputValueCheckIndex, reportDate);
                    int res = String.CASE_INSENSITIVE_ORDER.compare(v1, v2);
                    if (res == 0) {
                        res = v1.compareTo(v2);
                    }
                    if (desc)
                        res = res * (-1);
                    return res;
                }
            });

            for (int i = 0; i < sortedByValue.size(); i++) {
                String oldNum = sortedByValue.get(i).getNum();
                String newNum = sortedNums.get(i);

                if (!oldNum.equals(newNum)) {
                    for (SvodDinamicBean changingBean : idx.get(oldNum)) {
                        String oldKey = changingBean.getKey();
                        String newKey = oldKey.substring(0, oldKey.lastIndexOf("$D.") + 3) + newNum;
                        changingBean.setKey(newKey);
                        changingBean.setNum(newNum);
                    }
                }
            }
        }

        for (SvodDinamicBean item : svodFullDynamycList) {
            svodForm.put(item.getKey(), item.getValue());
        }
    }

    private Map<String, List<InputValueCheck>> makeIndexOfInputValueChecks(List<InputValueCheck> inputValueChecks) {
        Map<String, List<InputValueCheck>> inputValueCheckIndex = new HashMap<String, List<InputValueCheck>>();
        for (InputValueCheck inputValueCheck : inputValueChecks) {
            if (inputValueCheck.getRef() != null && !inputValueCheck.getRef().trim().isEmpty() && inputValueCheck.getKey().contains("$D")) {
                if (!inputValueCheckIndex.containsKey(inputValueCheck.getKey()))
                    inputValueCheckIndex.put(inputValueCheck.getKey(), new ArrayList<InputValueCheck>());
                inputValueCheckIndex.get(inputValueCheck.getKey()).add(inputValueCheck);
            }
        }
        return inputValueCheckIndex;
    }

    private String getValue(SvodDinamicBean svodDinamicBean, Map<String, List<InputValueCheck>> inputValueCheckIndex, Date reportDate) {
        String value = svodDinamicBean.getValue();
        String finalKey;
        int dPos = svodDinamicBean.getKey().indexOf("$D.");
        if (dPos == -1)
            finalKey = svodDinamicBean.getKey();
        else {
            finalKey = svodDinamicBean.getKey().substring(0, dPos) + "$DynamicRowId";
        }
        if (inputValueCheckIndex.containsKey(finalKey)) {
            String groupId = "";
            if (svodDinamicBean.getKey().contains(".")) {
                groupId = svodDinamicBean.getKey().substring(0, svodDinamicBean.getKey().lastIndexOf(".") + 1);
            }
            for (InputValueCheck inputValueCheck : inputValueCheckIndex.get(finalKey)) {
                try {
                    Long recId = Long.parseLong(value);
                    value = reference.getRefItemNameByRecId(inputValueCheck.getRef(), inputValueCheck.getRefCaption(), recId, reportDate);
                } catch (NumberFormatException e) {
                    logger.error(e.getMessage());
                }
                if (!groupId.isEmpty() && inputValueCheck.getGroupId() != null && groupId.equals(inputValueCheck.getGroupId())) {
                    break;
                }
            }
        }
        return value;
    }

    private String getRefCaptionByRecId(String ref, String refCaption, Long recId) {
        if (!liteRefItems.containsKey(ref)) {
            liteRefItems.put(ref, new HashMap<Long, LiteRefItem>());
        }
        if (!liteRefItems.get(ref).containsKey(recId)) {
            liteRefItems.get(ref).put(recId, new LiteRefItem(ref, recId));
        }
        LiteRefItem refItem = liteRefItems.get(ref).get(recId);
        if (!refItem.captionFields.containsKey(refCaption)) {
            String value = reference.getRefItemNameByRecId(ref, refCaption, recId, reportDate);
            refItem.captionFields.put(refCaption, value);
        }
        return liteRefItems.get(ref).get(recId).captionFields.get(refCaption);
    }

    // поиск элементов в окончательном списке
    private boolean findElementInSvodFullDinamicList(List<SvodDinamicBean> svodFullDinamicList, List<SvodDinamicBean> filtered, String formName) {
        boolean result = false;
        Map<String, Boolean> resultCompareMap = new HashMap<String, Boolean>();
        for (SvodDinamicBean item : filtered) {
            String key = item.getKey();
            String num = item.getNum();
            String baseNum = num.substring(0, num.lastIndexOf("."));
            String field = key.substring(key.indexOf("*") + 1, key.indexOf(":"));
            //String partOfKey = key.substring(0, key.indexOf("$D."));
            if (formName.equals("fs_rv") && (key.contains("bank") || key.contains("country")) ||
                    formName.equals("fs_ikdu") && (key.contains("name_jurper")) ||
                    formName.equals("fs_repo") && (key.contains("nin_iin")) ||
                    formName.equals("fs_sscb") && (key.contains("name_emitter")) ||
                    formName.equals("fs_rv_apk_kp") && (key.contains("bank") || key.contains("country")) ||
                    formName.equals("fs_ikdu_apk_kp") && (key.contains("name_jurper")) ||
                    formName.equals("fs_repo_apk_kp") && (key.contains("nin_iin")) ||
                    formName.equals("fs_sscb_apk_kp") && (key.contains("name_emitter"))) {
                for (SvodDinamicBean svodItem : svodFullDinamicList) {
                    String key_ = svodItem.getKey();
                    String num_ = svodItem.getNum();
                    String baseNum_ = num_.substring(0, num_.lastIndexOf("."));
                    String field_ = key_.substring(key_.indexOf("*") + 1, key_.indexOf(":"));
                    //String partOfKey_ = key_.substring(0, key_.indexOf("$D."));
                    if (!baseNum.equals(baseNum_)) continue;
                    // сравниваем по части ключа(поле) и далее по значению
                    if (field.equals(field_)) {
                        if (svodItem.getValue().equalsIgnoreCase(item.getValue())) {
                            boolean isExsits = true;
                            // проверяем есть ли в Map значение не равное false
                            if (resultCompareMap.containsKey(num_))
                                isExsits = resultCompareMap.get(num_);
                            if (isExsits)
                                resultCompareMap.put(num_, true);
                            //result = true;
                        } else {
                            resultCompareMap.put(num_, false);
                        }
                    }
                }
            }
        }
        String mergeNum = "";
        for (Map.Entry<String, Boolean> entry : resultCompareMap.entrySet()) {
            if (entry.getValue()) {
                mergeNum = entry.getKey();
                result = true;
            }
        }
        if (result) {
            for (SvodDinamicBean item : filtered) {
                String key = item.getKey();
                String field = key.substring(key.indexOf("*") + 1, key.indexOf(":"));
                //if (!Arrays.asList(FieldName).contains(field)) {
                if (item.isNumericValue()) {
                    //int i = 0;
                    for (SvodDinamicBean svodItem : svodFullDinamicList) {
                        String key_ = svodItem.getKey();
                        String num_ = svodItem.getNum();
                        String field_ = key_.substring(key_.indexOf("*") + 1, key_.indexOf(":"));
                        if (mergeNum.equals(num_) && field.equals(field_)) {
                            double val = 0;
                            double val_ = 0;
                            //if (!Arrays.asList(FieldName).contains(field)) {
                            if (item.isNumericValue()) {
                                String value = item.getValue();
                                val = Double.parseDouble(value);

                                String value_ = svodItem.getValue();
                                val_ = Double.parseDouble(value_);
                                double sum = val + val_;
                                svodItem.setValue(convertDecimalFormatSymbols(sum));
                                //svodFullDinamicList.set(i, svodItem);
                            }
                        }
                        //i++;
                    }
                }
            }
        }
        return result;
    }


    @Override
    public Map<String, String> getFormsData(Date reportDate, Long respId, String formName, String key) {
        Connection connection = null;
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        String subject_type_code = null;
        List<RefRespondentItem> respondents = new ArrayList<RefRespondentItem>();
        Map<String, String> data = new HashMap<String, String>();
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();

            stmt = connection.prepareCall("{ call pkg_reporter.get_data(?,?,?,?,?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(OracleCallableStatement.class);
            java.sql.Date repDate = new java.sql.Date(reportDate.getTime());
            ocs.setLong(1, respId);
            ocs.setDate(2, repDate);
            ocs.setString(3, formName);
            ocs.setString(4, key);
            ocs.registerOutParameter(5, OracleTypes.CURSOR);
            ocs.execute();

            rs = ocs.getCursor(5);
            while (rs.next()) {
                data.put(rs.getString("path"), rs.getString("value"));
            }
        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, rs, ocs);
        }
        return data;
    }

    @Override
    public List<ReportsFormula> getReportsFormula(String reportDate, String formName) {
        Connection connection = null;
        //PreparedStatement ps = null;
        //ResultSet rs = null;
        List<ReportsFormula> result = new ArrayList<ReportsFormula>();
        CallableStatement stmt = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;
        String Err_Msg = "";
        int Err_Code = 0;

        try {
            connection = dataSource.getConnection();
            // вызов процедуры
            stmt = connection.prepareCall("{ call Read_Reports_Rules (?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ocs = stmt.unwrap(OracleCallableStatement.class);
            ocs.registerOutParameter(1, OracleTypes.CURSOR);
            ocs.setString(2, reportDate);
            ocs.setString(3, formName);
            ocs.registerOutParameter(4, OracleTypes.INTEGER);
            ocs.registerOutParameter(5, OracleTypes.VARCHAR);
            ocs.execute();
            Err_Code = ocs.getInt(4);

            cursor = ocs.getCursor(1);

            while (cursor.next()) {
                ReportsFormula item = new ReportsFormula();
                item.setFormName(cursor.getString("formname"));
                item.setFieldName(cursor.getString("fieldname"));
                item.setFormula(cursor.getString("formula"));
                //item.setIs_calc_other_field(cursor.getInt("is_calc_other_field"));
                item.setCoeff(cursor.getDouble("coeff"));
                item.setCondition(cursor.getString("condition"));
                result.add(item);
            }

        } catch (SQLException e) {
            throw new EJBException(e);
        } finally {
            DbUtil.closeConnection(connection, stmt, cursor);
        }
        return result;
    }

    @Override
    public Long generateAndGetId(Form form, Date reportDate, List<RefRespondentItem> respondents, PortalUser user, String idn, String initStatus, Date curDate, Date fromDate, Date toDate) throws Exception {
        if (form.getTypeCode().equals(Form.Type.CONSOLIDATED.name()))
            return generateConsolidatedReportAndGetId(form, reportDate, respondents, user, idn, initStatus, 0l, curDate);
        else if (form.getTypeCode().equals(Form.Type.OUTPUT.name()))
            return generateOutReportAndGetIt(form, reportDate, respondents, user, idn, initStatus, 0l, curDate, fromDate, toDate);
        throw new Exception("Выбрана неправильная форма");
    }

    @Override
    public void regenerate(ReportListItem reportListItem, List<RefRespondentItem> respondents, PortalUser user, Date curDate, Date fromDate, Date toDate) throws Exception {
        List<Form> forms = persistence.getFormsByCodeLanguageReportDate(reportListItem.getFormCode(), "ru", reportListItem.getReportDate(), null);
        if (forms.size() > 0) {
            Form form = forms.get(0);
            Report report = persistence.getReport(reportListItem.getId(), null);
            String initStatus = ReportStatus.Status.COMPLETED.name();
            if (form.getTypeCode().equals(Form.Type.CONSOLIDATED.name()))
                generateConsolidatedReportAndGetId(form, reportListItem.getReportDate(), respondents, user, report.getIdn(), initStatus, reportListItem.getId(), curDate);
            else if (form.getTypeCode().equals(Form.Type.OUTPUT.name()))
                generateOutReportAndGetIt(form, reportListItem.getReportDate(), respondents, user, report.getIdn(), initStatus, reportListItem.getId(), curDate, fromDate, toDate);
        } else {
            throw new Exception(MessageFormat.format("За дату {0} формы с кодом {1} не существует", reportListItem.getReportDate(), reportListItem.getFormCode()));
        }
    }

    private Long generateConsolidatedReportAndGetId(Form form, Date reportDate, List<RefRespondentItem> respondents, PortalUser user, String idn, String initStatus, Long reportId, Date curDate) throws Exception {
        if (respondents.size() == 0) {
            throw new Exception("Респонденты не выбраны");
        }

        int out = form.getCode().indexOf("_out");
        String formName = form.getCode().substring(0, out);
        Form inputForm = persistence.getFormsByCodeLanguageReportDate(formName, "ru", reportDate, null).get(0);
        Map<String, String> inputValues = getResultForm(reportDate, formName, respondents, form, inputForm);
        try {
            inputValues = persistence.updateCalculatedFields(form.getCode(), reportDate, inputValues, "ru", false);
        } catch (EJBException e) {
            Throwable t = ExceptionUtil.getRootCauseRecursive(e);
            throw new Exception(t.getMessage());
        }

        Set<Long> inputReportIds = new HashSet<Long>();
        List<Report> inputReports = persistence.getReportByRepDateFormRespondentsStatusCode(reportDate, formName, respondents, ReportStatus.Status.APPROVED.toString());
        for (Report inputReport : inputReports) {
            inputReportIds.add(inputReport.getId());
        }

        if (inputReports.size() == 0)
            throw new Exception("Нет входных отчетов");

        if (reportId != 0)
            updateReport(reportId, inputValues, inputReportIds, user, initStatus, curDate);
        else
            reportId = createReport(reportDate, form, inputValues, inputReportIds, user, idn, initStatus, curDate);

        return reportId;
    }

    private Long generateOutReportAndGetIt(Form form, Date reportDate, List<RefRespondentItem> respondents, PortalUser user, String idn, String initStatus, Long reportId, Date curDate, Date fromDate, Date toDate) throws Exception {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

        List<OutReportRow> outputRows = new ArrayList<OutReportRow>();
        /*int out = form.getCode().indexOf("_reports");
        String formName = form.getCode().substring(0, out);*/

        List<RefReportsRulesItem> reportsFormulas = reference.getRefReportsRulesListByFormCodeDate(form.getCode(), reportDate);
        String reportName = "";

        Collections.sort(reportsFormulas, new Comparator<RefReportsRulesItem>() {
            @Override
            public int compare(RefReportsRulesItem o1, RefReportsRulesItem o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });

        Form outputForm = persistence.getFormsByCodeLanguageReportDate(form.getCode(), "ru", reportDate, null).get(0);

        inputReportIds = new HashSet<Long>();
        Fields fields = new Fields();
        this.reportDate = reportDate;
        currentRow = 1;
        String periodDuration = "";
        for (RefRespondentItem respondent : respondents) {
            data = new HashMap<String, Map<String, String>>();
            respondentIdn = respondent.getIdn();
            noReport = false;
            Set<NoReportDataError> noDataErrors = new HashSet<NoReportDataError>();
            Map<String, String> currentInputValues = new HashMap<String, String>();

            for (RefReportsRulesItem reportsFormula : reportsFormulas) {
                currentReportsRulesItem = reportsFormula;

                String formulaResult;
                String formula = reportsFormula.getFormula();
                reportName = reportsFormula.getFormname();


                if (isDynamicCell()) {
                    periodDuration = reportsFormula.getDurCode();
                    buildDynamicDates(fromDate, toDate, periodDuration);
                }

                do {
                    if (isDynamicCell()) {
                        getNextDynamicDate();
                    }
                    currentFormulaHasData = false;

                    String currentFieldName = getFieldName(reportsFormula.getFieldname());
                    String keyValue;
                    String partialKey;
                    if (reportsFormula.getReportType() == 1) {
                        keyValue = reportsFormula.getKeyValue();
                        partialKey = form.getCode() + "_" + reportsFormula.getTableName() + "*" + currentFieldName + ":num:" + keyValue;
                    } else {
                        keyValue = "$D.group." + currentRow;
                        partialKey = form.getCode() + "_" + reportsFormula.getTableName() + "*" + currentFieldName + ":num:$D.group.";
                    }
                    String key = form.getCode() + "_" + reportsFormula.getTableName() + "*" + currentFieldName + ":num:" + keyValue;

                    fields.resetCurrentField();
                    if(reportsFormula.getReportType() == 2
                            && (reportsFormula.getKeyValue() != null && !reportsFormula.getKeyValue().isEmpty() && !reportsFormula.getKeyValue().equalsIgnoreCase("novalue"))){
                        fields.addField(form.getCode() + "_" + reportsFormula.getTableName() + "*" + currentFieldName + ":num:" + reportsFormula.getKeyValue());
                    }

                    FormulaParser parser = new FormulaParser(formula, this);
                    String parsedFormula = parser.getResult();

                    if (!validateMRP(reportsFormula)) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(isDynamicCell() ? getCurrentDynamicDate() : reportDate);
                        throw new Exception(MessageFormat.format("Не установлена величина МРП за {0} год", Integer.toString(calendar.get(Calendar.YEAR))));
                    }
                    if (parser.hasNoReportDataError()) {
                        noDataErrors.addAll(parser.getErrors());
                        noReport = true;
                    }
                    if (noReport) break;
                    if (!currentFormulaHasData) {
                        continue;
                    }

                    try {
                        Object expression = engine.eval(parsedFormula);
                        if (expression instanceof Double) {
                            double number = (Double) expression;
                            if (number == Double.NEGATIVE_INFINITY || number == Double.POSITIVE_INFINITY) {
                                throw new Exception(MessageFormat.format("Деление на ноль в формуле [{0}]", parsedFormula));
                            }
                            if (Double.isNaN(number)) {
                                throw new Exception(MessageFormat.format("Ошибка при расчете формулы <br/>{0}({1})." +
                                        "<br/>Возможная причина деление на ноль.",formula, parsedFormula));
                            }
                            formulaResult = convertDecimalFormatSymbols(number);
                        } else {
                            formulaResult = expression.toString();
                        }
                    } catch (ScriptException e) {
                        throw new Exception(MessageFormat.format("Ошибка в синтаксисе формулы [{0}]", parsedFormula));
                    }

                    currentInputValues.put(partialKey, formulaResult);
                    String cacheKey = form.getCode() + "_" + dateFormat.format(reportDate);
                    if (!data.containsKey(cacheKey)) data.put(cacheKey, new HashMap<String, String>());
                    data.get(cacheKey).put(key, formulaResult);

                    if (fields.hasCurrentField()) {
                        double sum = 0;
                        try {
                            sum = Double.parseDouble(formulaResult);
                        } catch (NumberFormatException e) {
                            throw new Exception(MessageFormat.format("Ошибка при конвертации данных из входного отчета {0}", getFormName(currentInputFormCode, "ru", reportDate)));
                        }

                        fields.addSum(sum);

                        /*double itogoSum = 0.0;
                        String itogoKeyPart = currentFieldName + ":num:" + reportsFormula.getKeyValue();
                        if (currentItogoMap.get(itogoKeyPart) != null) {
                            itogoSum = currentItogoMap.get(itogoKeyPart);
                        }
                        itogoSum += sum;
                        currentItogoMap.put(itogoKeyPart, itogoSum);*/
                    }

                } while (isDynamicCell() && hasNextDynamicDate());

                if (noReport) break;
            }

            if (noDataErrors.size() > 0) {
                NoReportDataError error = noDataErrors.iterator().next();
                throw new Exception(MessageFormat.format("Нет отчета \"{0}\" для организации {1} на {2}",
                        getFormName(error.getFormCode(), "ru", error.getReportDate()), respondent.getPersonName(),
                        dateFormat.format(isDynamicCell() ? getCurrentDynamicDate() : error.getReportDate())));
            }

            if (noReport) continue;

            currentInputValues.put(form.getCode() + "_array*respondent_rec_id:num:$D.group.", respondent.getRecId().toString());
            outputRows.add(new OutReportRow(currentRow, currentInputValues));
            /*for (Map.Entry<String, Double> e : currentItogoMap.entrySet()) {
                double value = e.getValue();
                if (itogoMap.containsKey(e.getKey()))
                    value += itogoMap.get(e.getKey());
                itogoMap.put(e.getKey(), value);
            }*/

            currentRow++;
        }

        if (inputReportIds.size() == 0)
            throw new Exception("Нет входных отчетов");

        List<SortField> sortFields;
        if (outputForm.getFormHistory().getFormTag() != null && outputForm.getFormHistory().getFormTag().sortFields != null) {
            sortFields = outputForm.getFormHistory().getFormTag().sortFields;
        } else {
            sortFields = new ArrayList<SortField>();
        }
        Collections.sort(outputRows, new OutReportRowComparator(sortFields, respondents));
        for (int i = 0; i < outputRows.size(); i++) {
            OutReportRow row = outputRows.get(i);
            row.rowId = i + 1;
        }

        Map<String, String> inputValues = new HashMap<String, String>();
        for (OutReportRow row : outputRows) {
            String rowId = Integer.toString(row.rowId);
            for (Map.Entry<String, String> d : row.data.entrySet()) {
                inputValues.put(d.getKey() + rowId, d.getValue());
//                inputValues.put(d.getKey(), d.getValue());
            }
        }

        /*if (reportName.equals("InfoIO") || reportName.equals("FinCondIO") || reportName.equals("InfoNBO") || reportName.equals("FinCondNBO")) {
            //inputValues.put(formName + "_reports_array*name:num:Itogo", "Итого:");
            for (Map.Entry<String, Double> entry : itogoMap.entrySet()) {
                inputValues.put(formName + "_reports_array*" + entry.getKey(), convertDecimalFormatSymbols(entry.getValue()));
            }
        }*/

        for (Map.Entry<String, Double> entry : fields.getFieldSum().entrySet()) {
            inputValues.put(entry.getKey(), convertDecimalFormatSymbols(entry.getValue()));
        }

        inputValues = persistence.updateCalculatedFields(form.getCode(), reportDate, inputValues, "ru", false);

        if (reportId != 0)
            updateReport(reportId, inputValues, inputReportIds, user, initStatus, curDate);
        else
            reportId = createReport(reportDate, form, inputValues, inputReportIds, user, idn, initStatus, curDate);

        if (fromDate != null && toDate != null && periodDuration != null && !periodDuration.isEmpty()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            Map<String, String> props = new HashMap<String, String>();
            props.put(ReportProps.START_PERIOD_KEY, dateFormat.format(fromDate));
            props.put(ReportProps.END_PERIOD_KEY, dateFormat.format(toDate));
            props.put(ReportProps.PERIOD_DURATION_KEY, periodDuration);
            long lastReportHistoryId = persistence.getLastReportHistoryIdByReportId(reportId, true, null);
            persistence.putReportProps(lastReportHistoryId, props);
        }

        return reportId;
    }

    private String getFieldName(String fieldName) {
        if (isDynamicCell())
            return fieldName + "@" + dateFormat2.format(getCurrentDynamicDate());
        else
            return fieldName;
    }

    private String getFormName(String formCode, String languageCode, Date reportDate) {
        if (formCode == null)
            return "";
        List<Form> forms = persistence.getFormsByCodeLanguageReportDate(formCode, languageCode, reportDate, null);
        if (forms.size() > 0)
            return forms.get(0).getFormHistory().getName();
        return "";
    }

    /**
     * Проверка на обновленную МРП
     *
     * @param reportsFormula
     * @throws Exception
     */
    private boolean validateMRP(RefReportsRulesItem reportsFormula) throws Exception {
        if (reportsFormula.getFormname().equals("ExecPrudOT")
                && reportsFormula.getFieldname().equals("result")) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(reportsFormula.getBeginDate());

            int formulaYear = calendar.get(Calendar.YEAR);
            calendar.setTime(isDynamicCell() ? getCurrentDynamicDate() : reportDate);

            int reportYear = calendar.get(Calendar.YEAR);
            if (formulaYear != reportYear) {
                return false;
            }
        }
        return true;
    }

    private Map<String, String> getReportData(Long reportId) {
        ReportHistory reportHistory = persistence.getLastReportHistoryByReportId(reportId, true, false, true, null);
        String jsonData = reportHistory.getData();
        if (jsonData == null || jsonData.isEmpty()) return new HashMap<String, String>();
        Type typeMapStringString = new TypeToken<Map<String, String>>() {
        }.getType();
        return gson.fromJson(jsonData, typeMapStringString);
    }

    private Long createReport(Date reportDate, Form form, Map<String, String> inputValues, Set<Long> inputReportIds, PortalUser user, String idn, String initStatus, Date curDate) {
        return persistence.createReportOutputReport(reportDate, form, inputValues, inputReportIds, user, idn, initStatus, curDate);
    }

    private void updateReport(Long reportId, Map<String, String> inputValues, Set<Long> inputReportIds, PortalUser user, String initStatus, Date curDate) {
        persistence.updateReportOutputReport(reportId, inputValues, inputReportIds, user, initStatus, curDate);
    }

    public String convertDecimalFormatSymbols(double sum) {
        return convertDecimalFormatSymbols(sum, 8);
    }

    public String convertDecimalFormatSymbols(double sum, int fractionDigits) {
        String result = "";
        //DecimalFormat df = new DecimalFormat("###############.######");
        DecimalFormat df = new DecimalFormat();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(dfs);
        df.setMinimumFractionDigits(fractionDigits);
        df.setGroupingUsed(false);
        result = df.format(sum);
        return result;
    }

    protected Set<String> getNumericValueFields(String formCode, Date reportDate) {
        Set<String> fields = new HashSet<String>();

        List<Form> forms = persistence.getFormsByCodeReportDate(formCode, reportDate);
        if (forms.size() == 0)
            return fields;

        FormHistory formHistory = persistence.getFormHistoryWithInputValueChecks(forms.get(0).getFormHistory().getId());
        List<InputValueCheck> inputValueChecks = new ArrayList<InputValueCheck>();
        Gson gson = new Gson();
        Type typeListInputValueCheck = new TypeToken<List<InputValueCheck>>() {
        }.getType();
        inputValueChecks = gson.fromJson(formHistory.getInputValueChecks(), typeListInputValueCheck);

        for (InputValueCheck item : inputValueChecks) {
            if (item.getRef() != null && !item.getRef().trim().isEmpty())
                continue;
            if (item.getValueType() == null || item.getInputType().trim().isEmpty() ||
                    (!item.getValueType().equals("int") && !item.getValueType().equals("float")))
                continue;
            String key = item.getKey();
            fields.add(key.substring(key.indexOf("*") + 1, key.indexOf(":")));
        }

        return fields;
    }

    @Override
    public String onKey(String key, int startIndex, int endIndex) throws FormulaSyntaxError, NoReportDataError {
        if (key.toLowerCase().startsWith(REFERENCE_DATA_PREFIX.toLowerCase())) {
            return onKeyReferenceData(key);
        } else {
            return onKeyReportData(key);
        }
    }

    private String onKeyReportData(String key) throws FormulaSyntaxError, NoReportDataError{
        boolean mandatoryReport = false;
        if (key.startsWith(MANDATORY_REPORT_PREFIX)) {
            mandatoryReport = true;
            key = key.substring(1);
        }
        String formCode = grabFormCode(key);
        currentInputFormCode = formCode;

        String key_;
        if (key.contains(";"))
            key_ = key.substring(key.indexOf(";") + 1);
        else
            key_ = key;

        Date contextDate = reportDate;

        if (isDynamicCell()) {
            contextDate = getCurrentDynamicDate();
        }

        if (key_.contains(";")) {
            String datePart = key_.substring(key_.indexOf(";") + 1);
            contextDate = dateEval.eval(datePart, reportDate);
            key_ = key_.substring(0, key_.indexOf(";"));
        }

        if (key_.endsWith("$D.group.n")) {
            key_ = key_.substring(0, key_.lastIndexOf("n")) + Integer.toString(currentRow);
        }

        String cacheKey = formCode + "_" + dateFormat.format(contextDate);
        if (!data.containsKey(cacheKey)) {
            boolean hasReport = loadData(formCode, mandatoryReport, contextDate);
            if (!hasReport)
                this.noReport = true;
        }
        String value = data.containsKey(cacheKey) && data.get(cacheKey).containsKey(key_) ? data.get(cacheKey).get(key_) : "0";
        if (!currentFormulaHasData && data.containsKey(cacheKey) && data.get(cacheKey).containsKey(key_)) {
            currentFormulaHasData = true;
        }
        if (value != null && (value.equals(VALUE_TRUE) || value.equals(VALUE_FALSE))) {
            value = "'" + value + "'";
        } else if (value != null) {
            value = "(" + value + ")";
        }
        return value;
    }

    /**
     * Парсит формулу для возварщения данных из справочника. Пока реализована только для справчника МРП
     * @param key
     * @return данные из справочника (пока только значение МРП)
     * @throws FormulaSyntaxError
     */
    private String onKeyReferenceData(String key) throws FormulaSyntaxError {
        if (key.equalsIgnoreCase("ref;ref_mrp")) {
            throw new FormulaSyntaxError();
        }
        double value = 0;
        // TODO: 5/12/16 Нужно вызвать метод Бактияра
        return convertDecimalFormatSymbols(value, 2);
    }

    private boolean isDynamicCell() {
        return currentReportsRulesItem.getReportKind() == 2;
    }

    private boolean loadData(String formCode, boolean throwException, Date contextDate) throws NoReportDataError {
        Report report = persistence.getReportByIdnDateForm(respondentIdn, contextDate, formCode);
        ReportStatus reportStatus = null;
        if(report != null) {
            reportStatus = persistence.getLastReportStatusByReportId(report.getId(), true, null);
        }
        if (report == null || reportStatus == null || !reportStatus.getStatusCode().equals(ReportStatus.Status.APPROVED.name())) {
            if (throwException)
                throw new NoReportDataError(contextDate, formCode, respondentIdn);
            DateTimeZone dtZone = DateTimeZone.forTimeZone(TimeZone.getDefault());
            LocalDate date1 = new LocalDate(contextDate, dtZone);
            LocalDate date2 = new LocalDate(reportDate, dtZone);
            if (date1.compareTo(date2) != 0) {
                // если за предедущий период отчета нету - игнорируем
                return true;
            }
            return false;
        } else {
            data.put(formCode + "_" + dateFormat.format(contextDate), getReportData(report.getId()));
            inputReportIds.add(report.getId());
            return true;
        }
    }

    private String grabFormCode(String key) throws FormulaSyntaxError {
        if (key.indexOf(";") < 0)
            throw new FormulaSyntaxError();

        String formCode_ = key.substring(0, key.indexOf(";"));
        if (formCode_.length() == 0)
            throw new FormulaSyntaxError();

        return formCode_;
    }

    private void buildDynamicDates(Date date1, Date date2, String periodDuration) throws Exception{
        DateTimeZone dtZone = DateTimeZone.forTimeZone(TimeZone.getDefault());

        if (date1 == null || date2 == null) {
            throw new Exception(MessageFormat.format("Для следующей формулы требуются начало и конец периода: {0}", currentReportsRulesItem.getFormula()));
        }

        if (periodDuration == null) {
            throw new Exception(MessageFormat.format("Для следующей формулы не указан тип периода: {0}", currentReportsRulesItem.getFormula()));
        }

        LocalDate localDate1 = new LocalDate(date1, dtZone);
        LocalDate localDate2 = new LocalDate(date2, dtZone);

        PeriodType pt = PeriodUtil.getPeriodTypeByName(periodDuration);
        localDate1 = PeriodUtil.floor(localDate1, pt);
        localDate2 = PeriodUtil.floor(localDate2, pt);

        Set<LocalDate> dates = new HashSet<LocalDate>();
        LocalDate inter = localDate1;
        dates.add(inter);
        while (inter.compareTo(localDate2) < 0) {
            inter = PeriodUtil.plusPeriod(inter, PeriodUtil.getPeriodTypeByName(periodDuration), 1);
            dates.add(inter);
        }

        dynamicDateIterator = dates.iterator();
    }

    private Date getCurrentDynamicDate() {
        return currentDynamicDate;
    }

    private Date getNextDynamicDate() {
        currentDynamicDate = dynamicDateIterator.next().toDate();
        return currentDynamicDate;
    }

    private boolean hasNextDynamicDate() {
        return dynamicDateIterator.hasNext();
    }

    class LiteRefItem {
        String refName;
        Long recId;
        Map<String, String> captionFields;

        public LiteRefItem(String refName, Long recId) {
            this.refName = refName;
            this.recId = recId;
            captionFields = new HashMap<String, String>();
        }
    }

    class Field {
        String name;
        boolean hasData;
        double sum;

        public Field(String name) {
            this.name = name;
            hasData = false;
            sum = 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Field field = (Field) o;

            return !(name != null ? !name.equals(field.name) : field.name != null);

        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }

    class Fields{
        private Map<String, Field> fieldMap = new HashMap<String, Field>();
        private Field currentField;

        void addField(String name) {
            if (!fieldMap.containsKey(name)) {
                currentField = new Field(name);
                fieldMap.put(name, currentField);
            } else {
                currentField = fieldMap.get(name);
            }
        }

        void resetCurrentField(){
            currentField = null;
        }

        boolean hasCurrentField() {
            return currentField != null;
        }

        void addSum(double sum) {
            currentField.hasData = true;
            currentField.sum += sum;
        }

        Map<String, Double> getFieldSum() {
            Map<String, Double> result = new HashMap<String, Double>();
            for (Field field : fieldMap.values()) {
                if (field.hasData)
                    result.put(field.name, field.sum);
            }
            return result;
        }
    }

    class OutReportRow {
        int rowId;
        Map<String, String> data;

        public OutReportRow(int rowId, Map<String, String> data) {
            this.rowId = rowId;
            this.data = data;
        }
    }

    class OutReportRowComparator implements Comparator<OutReportRow>{

        private List<SortField> sortFields;
        private List<RefRespondentItem> respondents;

        public OutReportRowComparator(List<SortField> sortFields, List<RefRespondentItem> respondents) {
            this.sortFields = sortFields;
            this.respondents = respondents;
        }

        @Override
        public int compare(OutReportRow o1, OutReportRow o2) {
            for (SortField sortField : sortFields) {
                String sortKey = sortField.name + ":num:$D.group.";
                String value1 = o1.data.get(sortKey);
                String value2 = o2.data.get(sortKey);
                int d = (sortField.desc != null && sortField.desc) ? -1 : 1;
                if (value1 == null)
                    return 1 * d;
                if (value2 == null)
                    return -1 * d;
                try {
                    double d1 = Double.parseDouble(value1);
                    double d2 = Double.parseDouble(value2);

                    if (d1 > d2)
                        return 1 * d;
                    else if (d1 < d2)
                        return -1 * d;
                } catch (NumberFormatException e) {
                    return value1.compareToIgnoreCase(value2);
                }
            }

            String respondentRecId1 = o1.data.get(RESPONDENT_REC_ID_KEY);
            String respondentRecId2 = o1.data.get(RESPONDENT_REC_ID_KEY);
            if (respondentRecId1 != null && respondentRecId2 != null) {
                try {
                    long recId1 = Long.parseLong(respondentRecId1);
                    long recId2 = Long.parseLong(respondentRecId2);

                    Map<Long, RefRespondentItem> respIndex = new HashMap<Long, RefRespondentItem>();
                    for (RefRespondentItem respondentItem : respondents) {
                        respIndex.put(respondentItem.getRecId(), respondentItem);
                    }

                    RefRespondentItem respondent1 = respIndex.get(recId1);
                    RefRespondentItem respondent2 = respIndex.get(recId2);
                    if (respondent1 == null) {
                        return 1;
                    }
                    if (respondent2 == null) {
                        return -1;
                    }
                    return respondent1.getNameRu().compareToIgnoreCase(respondent2.getNameRu());
                } catch (NumberFormatException e) {
                    return respondentRecId1.compareToIgnoreCase(respondentRecId2);
                }
            }
            return 0;
        }
    }
}
