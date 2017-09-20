package dataform;

import ejb.PersistenceLocal;
import ejb.Reference;
import ejb.ReferenceLocal;
import entities.RefBalanceAccItem;
import util.Convert;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class BalanceAccountsOutFormHtml extends BalanceAccountsFormHtml {

    public BalanceAccountsOutFormHtml(String html, Map<String, String> attributeValueMap, ReferenceLocal reference, Date reportDate) {
        super(html, attributeValueMap, reference, reportDate);
    }

    public BalanceAccountsOutFormHtml(ReferenceLocal reference){
        super(reference);
    }

    @Override
    protected String getDetailRowHtml(RefBalanceAccItem item) {
        StringBuilder sb = new StringBuilder();
        String name = Convert.htmlFormatWithTabsAndLineBreaks(item.getNameRu());
        sb.append("<tr id=\"balance_accounts_out_array:").append(item.getCode()).append("\">");
        sb.append("<td>").append(item.getParentCode()).append("</td><td>").append(item.getCode()).append("</td><td></td><td>");
        sb.append(name).append("</td><td>");
        sb.append("<input type=\"text\" id=\"balance_accounts_out_array*sum:code:").append(item.getCode()).append("\"");
        sb.append(" name=\"balance_accounts_out_array*sum:code:").append(item.getCode()).append("\"");
        sb.append(" class=\"maskMoney0\" readonly=\"true\" onchange=\"updateCalculatedFields();\" onkeydown=\"moveFocus(event,this);\" onfocus=\"this.select();\"/>");
        sb.append("</td><td></td></tr>");
        return sb.toString();
    }

    public String buildBalanceAccountsOutFormTemplate(int numVer, Date date) {
        StringBuilder sb = new StringBuilder();
        StringBuilder sbPart = new StringBuilder();
        sb.append(getPart0FormHeaderOut(numVer)).append(PART_1_OUT_TABLE_HEADER);

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
                    //if (account.getLevelCode().equals("Ad")) // Account with details
                    //    sbPart.append("<td><PlusMinusBaDetails/></td>");
                    //else // Account without details
                    sbPart.append("<td></td>");
                    sbPart.append("<td>").append(Convert.htmlFormatWithTabsAndLineBreaks(account.getNameRu())).append("</td>");
                    sbPart.append("<td><InputText name=\"sum\" key=\"code\" keyValue=\"").append(account.getCode()).append("\"");
                    sbPart.append(" readonly=\"true\"");
                    //if (account.getLevelCode().equals("Ad"))
                    if(haveChild)
                        sbPart.append(" auto=\"true\"");
                    sbPart.append(" mask=\"money0\"/></td>");
                    //if (account.getLevelCode().equals("Ad"))
                    if(haveChild)
                        sbPart.append("<td><Calculate/></td>");
                    else
                        sbPart.append("<td></td>");
                    sbPart.append("</tr>");
                    sb.append(sbPart.toString());
                }
            }
        }
        sb.append(PART_2_OUT_TABLE_FOOTER).append(PART_4_OUT_STYLE).append(PART_5_OUT_SCRIPT).append(BalanceAccountsFormHtml.PART_6_FORM_FOOTER);
        return sb.toString();
    }

    private String getPart0FormHeaderOut(int numVer){
        switch (numVer){
            case 1:
                return PART_0_OUT_FORM_HEADER;
            case 2:
                return PART_1_OUT_FORM_HEADER;
            default:
                return PART_0_OUT_FORM_HEADER;
        }
    }

    private static final String PART_0_OUT_FORM_HEADER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<Form\n" +
                    "\tname=\"balance_accounts_out\"\n" +
                    "\tlanguageCode=\"ru\" title=\"Перечень показателей для составления обзора финансового сектора (свод) (ИО)\"\n" +
                    "\tshortName=\"Перечень показ. для состав. обзора фин. сек. (свод) (ИО)\"\n" +
                    "\tbeginDate=\"2015-01-01\" endDate=\"2017-04-01\"\n" +
                    "\ttypeCode=\"CONSOLIDATED\"\n" +
                    "\tstyle=\"\" styleClass=\"\"\n" +
                    "\txmlVersion=\"1\">\n\n" +

                    "\t<h3 style=\"text-align: center;\">Перечень показателей для составления обзора финансового сектора</h3>\n" +
                    "\t<h3 style=\"text-align: center;\">СВОД</h3>\n" +
                    "\t<span style=\"display: block; text-align: center;\">\n" +
                    "\t\tпо состоянию на &#160;<StaticText name=\"$ReportDate\" value=\"Отчетная дата\"/>\n" +
                    "\t</span>\n<br/>\n";

    private static final String PART_1_OUT_FORM_HEADER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<Form\n" +
                    "\tname=\"balance_accounts_out\"\n" +
                    "\tlanguageCode=\"ru\" title=\"Сведения по показателям для составления обзора финансового сектора (свод) (ИО)\"\n" +
                    "\tshortName=\"Свед. по показ-м для сост обзора фин. сект. (свод) (ИО)\"\n" +
                    "\tbeginDate=\"2017-04-01\" endDate=\"\"\n" +
                    "\ttypeCode=\"CONSOLIDATED\"\n" +
                    "\tstyle=\"\" styleClass=\"\"\n" +
                    "\txmlVersion=\"1\">\n\n" +

                    "\t<h3 style=\"text-align: center;\">Сведения по показателям для составления обзора финансового сектора</h3>\n" +
                    "\t<h3 style=\"text-align: center;\">СВОД</h3>\n" +
                    "\t<span style=\"display: block; text-align: center;\">\n" +
                    "\t\tпо состоянию на &#160;<StaticText name=\"$ReportDate\" value=\"Отчетная дата\"/>\n" +
                    "\t</span>\n<br/>\n";



    private static final String PART_1_OUT_TABLE_HEADER =
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


    private static final String PART_2_OUT_TABLE_FOOTER =
            "</tbody>\n</table>\n</div>\n<br/><br/>\n" +
                    "<InputText name=\"sign\" style=\"font-weight:bold; width: 100em;border:0;margin-left:10em\" readonly=\"true\"/>\n";



    private static final String PART_4_OUT_STYLE =
            "<style>\n" +
                    "<![CDATA[\n" +
                    "\ttable.dataTable {\n" +
                    "\t\twidth:inherit ;\n" +
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
                    "\ttable.dataTable th:nth-child(3) {\n" +
                    "\t\tdisplay: none;\n" +
                    "\t}\n" +
                    "\ttable.dataTable td:nth-child(3) {\n" +
                    "\t\twidth: 4em;\n" +
                    "\t\ttext-align: center;\n" +
                    "\t\tdisplay: none;\n" +
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



    private static final String PART_5_OUT_SCRIPT =
            "<script>\n" +
                    "<![CDATA[\n" +
                    "function updateCalculatedFields() {\n" +
                    "}\n" +
                    "]]>\n" +
                    "</script>\n" +
                    "<script2>\n" +
                    "<![CDATA[\n" +
                    "function fixTable(fix){ \n" +
                    "    $(\"#balance_accounts_out_array\").tableHeadFixer({unFix:!fix, head:true}); \n" +
                    "}" +
                    "]]>\n" +
                    "</script2>\n";;
}
