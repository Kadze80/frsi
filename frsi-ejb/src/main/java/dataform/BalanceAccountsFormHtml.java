package dataform;

import ejb.PersistenceLocal;
import ejb.Reference;
import ejb.ReferenceLocal;
import entities.RefBalanceAccItem;
import jaxb.HtmlInput;
import jaxb.HtmlTd;
import jaxb.HtmlTr;
import org.apache.log4j.Logger;
import util.Convert;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;

public class BalanceAccountsFormHtml extends DataForm {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    private String html;
    private static final String FINISH_TAG = "<style";
    private Map<String,List<RefBalanceAccItem>> dynamicGroups;
    protected ReferenceLocal reference;
    private Date reportDate;

    public BalanceAccountsFormHtml(ReferenceLocal reference){
        this.reference = reference;
    }

    public BalanceAccountsFormHtml(String html, Map<String, String> attributeValueMap, ReferenceLocal reference, Date reportDate) {
        super(attributeValueMap);
        this.html = html;
        this.reference = reference;
        this.reportDate = reportDate;
        insertValuesIntoHtml();
    }

    private void insertValuesIntoHtml() {
        if (html == null || html.isEmpty()) return;
        try {
            // Processing order is important!
            findDynamicGroups();
            processDynamicRows();
            processInputTags();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private void findDynamicGroups() {
        dynamicGroups = new HashMap<String, List<RefBalanceAccItem>>();
        // Filling key set (parent codes)
        for (String key : attributeValueMap.keySet()) {
            int lastColonPos = key.lastIndexOf(":");
            if (0 <= lastColonPos && lastColonPos < key.length() - 1) {
                String code = key.substring(lastColonPos + 1);
                int codeLength = code.length();
                if (codeLength == 7 || codeLength == 8) { // to ensure that the code's format matches "DDDDDDD." (trailing dot is optional)
                    //String accountCode = code.substring(0, 4); // first 4 digits
                    String detailCode = code.substring(4,7); // last 3 digits excluding trailing dot
                    if (!detailCode.equals("000")) {
                        List<RefBalanceAccItem> rbas = reference.getRefBalanceAccountsByCode(reportDate, code);
                        if (!rbas.isEmpty()) dynamicGroups.put(rbas.get(0).getParentCode(), null);
                    }
                }
            }
        }
        // Obtaining details
        for (String parentCode : dynamicGroups.keySet()) {
            List<RefBalanceAccItem> details = reference.getRefBalanceAccountsByParentCode(reportDate, parentCode);
            dynamicGroups.put(parentCode, details);
        }
    }

    private void processDynamicRows() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(HtmlTr.class, HtmlTd.class, HtmlInput.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        StringBuilder sb = new StringBuilder();
        int pos, startPos, endPos, finishPos;
        String openTag = "<tr id=";
        String closeTag = "</tr>";
        pos = 0;
        finishPos = html.indexOf(FINISH_TAG);
        if (finishPos == -1) finishPos = html.length();
        while (pos < finishPos) {
            startPos = html.indexOf(openTag, pos);
            if (startPos >= 0) {
                endPos = html.indexOf(closeTag, startPos);
                if (endPos < 0) break;

                // Skip outer tags if tags nested, i.e. set startPos to the inner openTag which is closest to the closeTag
                int nestedTagPos = -1;
                do {
                    nestedTagPos = html.indexOf(openTag, startPos + openTag.length());
                    if (nestedTagPos > endPos) nestedTagPos = -1;
                    if (nestedTagPos >= 0) startPos = nestedTagPos;
                } while (nestedTagPos >= 0);

                sb.append(html.substring(pos, startPos));
                pos = endPos + closeTag.length();

                if (endPos >= 0) {
                    String srcHtmlTag = html.substring(startPos, pos);
                    //sb.append(srcHtmlTag);
                    InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
                    HtmlTr groupHtmlTr = (HtmlTr) unmarshaller.unmarshal(is);

                    // Process
                    boolean hasDetails = false;
                    if (groupHtmlTr != null && groupHtmlTr.id != null) {
                        String[] idParts = groupHtmlTr.id.split(":");
                        if (idParts.length > 1) {
                            String parentCode = idParts[1];
                            if (dynamicGroups.containsKey(parentCode)) {
                                hasDetails = true;
                                // change +/- buttons state
                                HtmlTd buttonsCell = groupHtmlTr.tds.get(2);
                                if (buttonsCell.elements.size() > 1) {
                                    HtmlInput buttonPlus = (HtmlInput) buttonsCell.elements.get(0);
                                    HtmlInput buttonMinus = (HtmlInput) buttonsCell.elements.get(1);
                                    buttonPlus.disabled = "disabled";
                                    buttonMinus.disabled = null;
                                }
                                StringWriter stringWriter = new StringWriter();
                                marshaller.marshal(groupHtmlTr, stringWriter);
                                String dstHtmlTag = stringWriter.toString();
                                sb.append(dstHtmlTag); // with changed +/- buttons state

                                List<RefBalanceAccItem> details = dynamicGroups.get(parentCode);
                                for (RefBalanceAccItem detail : details) {
                                    String detailRowHtml = getDetailRowHtml(detail);
                                    sb.append(detailRowHtml);
                                }
                            }
                        }
                    }
                    if (!hasDetails) sb.append(srcHtmlTag);
                }
            } else break;
        }
        sb.append(html.substring(pos));
        html = sb.toString();
    }

    protected String getDetailRowHtml(RefBalanceAccItem item) {
        StringBuilder sb = new StringBuilder();
        String name = Convert.htmlFormatWithTabsAndLineBreaks(item.getNameRu());
        sb.append("<tr id=\"balance_accounts_array:").append(item.getCode()).append("\">");
        sb.append("<td>").append(item.getParentCode()).append("</td><td>").append(item.getCode()).append("</td><td></td><td>");
        sb.append(name).append("</td><td>");
        sb.append("<input type=\"text\" id=\"balance_accounts_array*sum:code:").append(item.getCode()).append("\"");
        sb.append(" name=\"balance_accounts_array*sum:code:").append(item.getCode()).append("\"");
        sb.append(" class=\"maskMoney0\" onchange=\"updateCalculatedFields();makeDirty();\" onkeydown=\"moveFocus(event,this);\" onfocus=\"this.select();\"/>");
        sb.append("</td><td></td></tr>");
        return sb.toString();
    }

    private void processInputTags() throws  JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(HtmlInput.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        StringBuilder sb = new StringBuilder();
        int pos, startPos, endPos, finishPos;
        String openTag = "<input";
        String closeTag = "</input>";
        pos = 0;
        finishPos = html.indexOf(FINISH_TAG);
        if (finishPos == -1) finishPos = html.length();
        while (pos < finishPos) {
            startPos = html.indexOf(openTag, pos);
            if (startPos >= 0) {
                sb.append(html.substring(pos, startPos));
                endPos = html.indexOf("/>", startPos); // non-container tag, i.e. can be self-closing
                if (endPos >= 0) {
                    pos = endPos + 2;
                } else {
                    endPos = html.indexOf(closeTag, startPos);
                    if (endPos >= 0) {
                        pos = endPos + closeTag.length();
                    } else break;
                }
                if (endPos >= 0) {
                    String srcHtmlTag = html.substring(startPos, pos);
                    //logger.debug("src = " + srcHtmlTag);
                    InputStream is = new ByteArrayInputStream(srcHtmlTag.getBytes(StandardCharsets.UTF_8));
                    HtmlInput htmlInput = (HtmlInput) unmarshaller.unmarshal(is);

                    // Process
                    if (htmlInput.type.toLowerCase().equals("text")) {
							/*
							String[] nameParts = htmlInput.name.split("\\*");
							String containerName = nameParts[0];
							if (containerName.contains(":")) {
								containerName = containerName.replace(":", "_") + "_item";
							}
							String key = containerName + "*" + nameParts[1];
							*/
                        String value = attributeValueMap.get(htmlInput.name);
                        htmlInput.value = value == null ? null : value;

                        StringWriter stringWriter = new StringWriter();
                        marshaller.marshal(htmlInput, stringWriter);
                        String dstHtmlTag = stringWriter.toString();
                        sb.append(dstHtmlTag);
                    } else
                        sb.append(srcHtmlTag);
                }
            } else break;
        }
        sb.append(html.substring(pos));
        html = sb.toString();
    }

    public String buildBalanceAccountsFormTemplate(int numVer, Date date) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbPart = new StringBuilder();
        sb.append(getPart0FormHeader(numVer)).append(PART_1_TABLE_HEADER);

        List<RefBalanceAccItem> classes = reference.getRefBalanceAccountsByParentCode(date, null);
        for (RefBalanceAccItem clazz : classes) {
            sbPart.setLength(0);
            sbPart.append("<tr id=\"").append(clazz.getCode()).append("\" class=\"groupHeader\" style=\"background-color: #B0B0B0;\">");
            sbPart.append("<td></td>");
            sbPart.append("<td>").append(clazz.getCode()).append("</td>");
            sbPart.append("<td></td>");
            sbPart.append("<td>").append(Convert.htmlFormatWithTabsAndLineBreaks(clazz.getNameRu())).append("</td>");
            sbPart.append("<td><InputText name=\"sum\" key=\"code\" keyValue=\"").append(clazz.getCode()).append("\" readonly=\"true\" auto=\"true\" mask=\"money0\"/></td>");
            sbPart.append("<td><Calculate/></td>");
            sbPart.append("</tr>");
            sb.append(sbPart.toString());

            List<RefBalanceAccItem> groups = reference.getRefBalanceAccountsByParentCode(date, clazz.getCode());
            for (RefBalanceAccItem group : groups) {
                sbPart.setLength(0);
                sbPart.append("<tr id=\"").append(group.getCode()).append("\" class=\"groupHeader\">");
                sbPart.append("<td>").append(group.getParentCode()).append("</td>");
                sbPart.append("<td>").append(group.getCode()).append("</td>");
                sbPart.append("<td></td>");
                sbPart.append("<td>").append(Convert.htmlFormatWithTabsAndLineBreaks(group.getNameRu())).append("</td>");
                sbPart.append("<td><InputText name=\"sum\" key=\"code\" keyValue=\"").append(group.getCode()).append("\" readonly=\"true\" auto=\"true\" mask=\"money0\"/></td>");
                sbPart.append("<td><Calculate/></td>");
                sbPart.append("</tr>");
                sb.append(sbPart.toString());

                List<RefBalanceAccItem> accounts = reference.getRefBalanceAccountsByParentCode(date, group.getCode());
                for (RefBalanceAccItem account : accounts) {
                    Boolean haveChild = reference.getRefBalanceAccountsByParentCode(date, account.getCode()).size() > 0;
                    sbPart.setLength(0);
                    sbPart.append("<tr id=\"").append(account.getCode()).append("\">");
                    sbPart.append("<td>").append(account.getParentCode()).append("</td>");
                    sbPart.append("<td>").append(account.getCode()).append("</td>");
                    if(haveChild)
                        sbPart.append("<td><PlusMinusBaDetails/></td>");
                    else // Account without details
                        sbPart.append("<td></td>");
                    sbPart.append("<td>").append(Convert.htmlFormatWithTabsAndLineBreaks(account.getNameRu())).append("</td>");
                    sbPart.append("<td><InputText name=\"sum\" key=\"code\" keyValue=\"").append(account.getCode()).append("\"");
                    if(haveChild)
                        sbPart.append(" readonly=\"true\" auto=\"true\"");
                    sbPart.append(" mask=\"money0\"/></td>");
                    if(haveChild)
                        sbPart.append("<td><Calculate/></td>");
                    else
                        sbPart.append("<td></td>");
                    sbPart.append("</tr>");
                    sb.append(sbPart.toString());
                }
            }
        }
        sb.append(PART_2_TABLE_FOOTER).append(PART_3_SIGNS).append(PART_4_STYLE).append(PART_5_SCRIPT).append(PART_6_FORM_FOOTER);
        return sb.toString();
    }

    private String getPart0FormHeader(int numVer){
        switch (numVer){
            case 1:
                return PART_0_FORM_HEADER;
            case 2:
                return PART_1_FORM_HEADER;
            default:
                return PART_0_FORM_HEADER;
        }
    }

    private static final String PART_0_FORM_HEADER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<Form\n" +
                    "\tname=\"balance_accounts\"\n" +
                    "\tlanguageCode=\"ru\" title=\"Перечень показателей для составления обзора финансового сектора (ИО)\"\n" +
                    "\tshortName=\"Перечень показ. для состав. обзора фин. сек. (ИО)\"\n" +
                    "\tbeginDate=\"2015-01-01\" endDate=\"2017-04-01\"\n" +
                    "\ttypeCode=\"INPUT\"\n" +
                    "\tstyle=\"\" styleClass=\"\"\n" +
                    "\txmlVersion=\"1\">\n\n" +

                    "\t<h3 style=\"text-align: center;\">Перечень показателей для составления обзора финансового сектора</h3>\n" +
                    "\t<span style=\"display: block; text-align: center;\">\n" +
                    "\t\t<StaticText name=\"$RespondentInfo\" value=\"БИН, Полное наименование респондента\" class=\"\" style=\"\"/><br/>\n" +
                    "\t\tпо состоянию на &#160;<StaticText name=\"$ReportDate\" value=\"Отчетная дата\"/>\n" +
                    "\t</span>\n<br/>\n";

    private static final String PART_1_FORM_HEADER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<Form\n" +
                    "\tname=\"balance_accounts\"\n" +
                    "\tlanguageCode=\"ru\" title=\"Сведения по показателям для составления обзора финансового сектора (ИО)\"\n" +
                    "\tshortName=\"Свед. по показ-м для сост обзора фин. сект. (ИО)\"\n" +
                    "\tbeginDate=\"2017-04-01\" endDate=\"\"\n" +
                    "\ttypeCode=\"INPUT\"\n" +
                    "\tstyle=\"\" styleClass=\"\"\n" +
                    "\txmlVersion=\"1\">\n\n" +

                    "\t<h3 style=\"text-align: center;\">Сведения по показателям для составления обзора финансового сектора</h3>\n" +
                    "\t<span style=\"display: block; text-align: center;\">\n" +
                    "\t\t<StaticText name=\"$RespondentInfo\" value=\"БИН, Полное наименование респондента\" class=\"\" style=\"\"/><br/>\n" +
                    "\t\tпо состоянию на &#160;<StaticText name=\"$ReportDate\" value=\"Отчетная дата\"/>\n" +
                    "\t</span>\n<br/>\n";

    private static final String PART_2_TABLE_FOOTER =
            "</tbody>\n</table>\n</div>\n<br/><br/>\n";

    private static final String PART_3_SIGNS =
            "<table class=\"textLayoutTable\">\n" +
                    "\t<tr>\n" +
                    "\t\t<td>Первый руководитель:</td>\n" +
                    "\t\t<td><InputText name=\"ceo\" style=\"width: 20em;\"/></td>\n" +
                    "\t</tr>\n" +
                    "\t<tr>\n" +
                    "\t\t<td>Главный бухгалтер:</td>\n" +
                    "\t\t<td><InputText name=\"chief_accountant\" style=\"width: 20em;\"/></td>\n" +
                    "\t</tr>\n" +
                    "\t<tr>\n" +
                    "\t\t<td>Исполнитель:</td>\n" +
                    "\t\t<td><InputText name=\"drafted_by\" style=\"width: 20em;\"/></td>\n" +
                    "\t</tr>\n" +
                    "\t<tr>\n" +
                    "\t\t<td>Дата подписания отчета:</td>\n" +
                    "\t\t<td><InputText name=\"signature_date\" valueType=\"date\" style=\"width: 20em;\" valueFunc=\"LastSignDate\" readonly=\"true\"/></td>\n" +
                    "\t</tr>\n" +
                    "</table>\n";

    private static final String PART_4_STYLE =
            "<style>\n" +
                    "<![CDATA[\n" +
                    "\ttable.dataTable {\n" +
                    "\t\twidth:inherit ;\n" +
                    "\t}\n" +
                    "\ttable.dataTable th:nth-child(1) {\n" +
                    "\t\tdisplay: none;\n" +
                    "\t}\n" +
                    "\ttable.dataTable th:nth-child(1) {\n" +
                    "\t\tdisplay: none;\n" +
                    "\t}\n" +
                    "\ttable.dataTable td:nth-child(1) {\n" +
                    "\t\twidth: 4em;\n" +
                    "\t\tdisplay: none;\n" +
                    "\t}\n" +
                    "\ttable.dataTable td:nth-child(2) {\n" +
                    "\t\twidth: 4em;\n" +
                    "\t\ttext-align: left;\n" +
                    "\t}\n" +
                    "\ttable.dataTable td:nth-child(3) {\n" +
                    "\t\twidth: 4em;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t}\n" +
                    "\ttable.dataTable td:nth-child(4) {\n" +
                    "\t\twidth: 400px;\n" +
                    "\t\ttext-align: left;\n" +
                    "\t}\n" +
                    "\ttable.dataTable td:nth-child(5) {\n" +
                    "\t\twidth: 8em;\n" +
                    "\t\tpadding: 0 0 1px 1px;\n" +
                    "\t}\n" +
                    "\ttable.dataTable th:nth-child(6) {\n" +
                    "\t\tdisplay: none;\n" +
                    "\t}\n" +
                    "\ttable.dataTable td:nth-child(6) {\n" +
                    "\t\twidth: 2em;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t\tdisplay: none;\n" +
                    "\t}\n" +
                    "]]>\n" +
                    "</style>\n";

    private static final String PART_1_TABLE_HEADER =
            "<div><table name=\"array\" class=\"dataTable\">\n" +
                    "<thead>\n"+
                    "\t<tr>\n" +
                    "\t\t<th>ParentCode</th>\n" +
                    "\t\t<th>Код</th>\n" +
                    "\t\t<th></th>\n" +
                    "\t\t<th>Наименования классов, групп счетов и балансовых счетов</th>\n" +
                    "\t\t<th>Сумма</th>\n" +
                    "\t\t<th></th>\n" +
                    "\t</tr>\n" +
                    "</thead>\n" +
                    "<tbody>\n";

    private static final String PART_5_SCRIPT =
            "<script2>\n" +
                    "<![CDATA[\n" +
                    "function updateCalculatedFields() {\n" +
                    "    var table = document.getElementById(\"balance_accounts_array\");\n" +
                    "    for (var i = 0, row; row = table.rows[i]; i++) {\n" +
                    "        var parentCode = row.cells[0].innerHTML;\n" +
                    "        var code = row.cells[1].innerHTML;\n" +
                    "        if (!parentCode) updateCalculatedFieldsByParent(code, true);\n" +
                    "    }\n" +
                    "}" +
                    "function updateCalculatedFieldsByParent(parentCode, isRecursive) {\n" +
                    "   var table = document.getElementById(\"balance_accounts_array\");\n" +
                    "   var parentInputId = \"balance_accounts_array*sum:code:\" + parentCode;\n" +
                    "   var parentInput = document.getElementById(parentInputId);\n" +
                    "   if (parentInput) {\n" +
                    "       var sum = 0;\n" +
                    "       for (var i = 0, row; row = table.rows[i]; i++) {\n" +
                    "           var pCode = row.cells[0].innerHTML;\n" +
                    "           var code = row.cells[1].innerHTML;\n" +
                    "           if (pCode && pCode === parentCode) {\n" +
                    "               if (isRecursive) {\n" +
                    "                   var btnCalc = row.cells[5].querySelector('button[type=button]');\n" +
                    "                   if (btnCalc) updateCalculatedFieldsByParent(code, true);\n" +
                    "               }\n" +
                    "               var input = row.cells[4].querySelector('input[type=text]');\n" +
                    "               if (input) sum += getMaskedInputValue(input);\n" +
                    "           }\n" +
                    "       }\n" +
                    "       setInputValue(parentInput.id, sum);\n" +
                    "       if (!isRecursive) updateCalculatedFields();\n" +
                    "   }\n" +
                    "}" +
                    "function calculate(button) {\n" +
                    "   var groupRow = $(button).closest('tr')[0];\n" +
                    "   var groupCode = groupRow.cells[1].innerHTML;\n" +
                    "   updateCalculatedFieldsByParent(groupCode, false);\n" +
                    "}\n" +
                    "function fixTable(fix){ \n" +
                    "    $(\"#balance_accounts_array\").tableHeadFixer({unFix:!fix, head:true}); \n" +
                    "}" +
                    "]]>\n" +
                    "</script2>\n" +
                    "<serverscript>\n" +
                    "   function updateCalculatedFields() {\n" +
                    "       document.calculateBalanceAccounts();\n" +
                    "   }\n" +
                    "</serverscript>";

    public static final String PART_6_FORM_FOOTER =
            "</Form>";


    // region Getter and Setter
    public String getHtml() {
        return html;
    }
    // endregion

}
