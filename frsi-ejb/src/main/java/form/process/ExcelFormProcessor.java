package form.process;

import ejb.PersistenceLocal;
import entities.RefRespondentItem;
import form.tag.Component;
import form.tag.Container;
import form.tag.*;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import util.Convert;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.List;

// Not used. Must be implemented properly
public class ExcelFormProcessor extends FormProcessor {
    private static final Logger logger = Logger.getLogger("fileLogger");
    private static Map<String, String> resMap = new HashMap<String, String>();
    private PersistenceLocal persistence;

    private RefRespondentItem respondent;
    private Date reportDate;
    private Map<String, String> attributeValueMap;
    private Map<String, Map<String, String>> referencesMap;

    private XSSFWorkbook workbook;
    private XSSFSheet sheet;

    private XSSFColor colorWhite;
    private XSSFColor colorGray;

    private XSSFFont fontHeader;
    private XSSFFont fontContent;

    private XSSFCellStyle styleHeader;
    private XSSFCellStyle styleLeft;
    private XSSFCellStyle styleCenter;
    private XSSFCellStyle styleRight;

    private Row row;
    private int rowNum;
    private String rowStyleClass;

    private Cell cell;
    private int cellNum;
    private String cellStyleClass;

    private Map<Integer,Short> lineCountInRow = new HashMap<Integer,Short>();

    // Dynamic row templates
    private Map<String, List<String>> drtMap; // key: tableName, value: drtRow (template cell values)
    private List<String> drtRow; // current template row
    private String drtCell; // current template cell
    private int drtCellNum;

    static {
        resMap.put("en_page1", "Sheet 1");
        resMap.put("kz_page1", "1 парақ");
        resMap.put("ru_page1", "Лист 1");
    }

    public ExcelFormProcessor(entities.Form formTemplate, RefRespondentItem respondent, Date reportDate, Map<String,String> attributeValueMap, PersistenceLocal persistence) {
        super(formTemplate.getFormHistory().getXml(), formTemplate.getFormHistory().getLanguageCode());
        this.respondent = respondent;
        this.reportDate = reportDate;
        this.attributeValueMap = attributeValueMap;
        this.persistence = persistence;

        referencesMap = new HashMap<String, Map<String, String>>();
        drtMap = new HashMap<String, List<String>>();
        drtRow = null;
        drtCell = "";
        drtCellNum = 0;
        if (form != null) createDynamicRowTemplate(form);

        if (form != null && attributeValueMap != null) {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet(resMap.get(lang + "_page1"));
            createStyles();
            rowNum = 0;
            cellNum = 0;
            row = sheet.createRow(rowNum);
            lineCountInRow.put(rowNum, (short) 1);
            cell = row.createCell(cellNum);
            createCells(form);
            autoSize();
        }
    }

    private void createStyles() {
        // Colors
        colorWhite = new XSSFColor(new Color(255, 255, 255));
        colorGray = new XSSFColor(new Color(192, 192, 192));

        // Fonts
        fontHeader = workbook.createFont();
        fontHeader.setBold(true);
        fontHeader.setFontHeightInPoints((short)10);
        fontHeader.setFontName("Times New Roman");

        fontContent = workbook.createFont();
        fontContent.setFontHeightInPoints((short)10);
        fontContent.setFontName("Times New Roman");

        // Styles
        styleHeader = workbook.createCellStyle();
        styleHeader.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        styleHeader.setFillForegroundColor(colorGray);
        styleHeader.setBorderTop(BorderStyle.HAIR);
        styleHeader.setBorderBottom(BorderStyle.HAIR);
        styleHeader.setBorderLeft(BorderStyle.HAIR);
        styleHeader.setBorderRight(BorderStyle.HAIR);
        styleHeader.setAlignment(HorizontalAlignment.CENTER);
        styleHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        styleHeader.setFont(fontHeader);

        styleLeft = workbook.createCellStyle();
        styleLeft.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        styleLeft.setFillForegroundColor(colorWhite);
        styleLeft.setBorderTop(BorderStyle.HAIR);
        styleLeft.setBorderBottom(BorderStyle.HAIR);
        styleLeft.setBorderLeft(BorderStyle.HAIR);
        styleLeft.setBorderRight(BorderStyle.HAIR);
        styleLeft.setAlignment(HorizontalAlignment.LEFT);
        styleLeft.setVerticalAlignment(VerticalAlignment.CENTER);
        styleLeft.setFont(fontContent);

        styleCenter = workbook.createCellStyle();
        //styleCenter.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        //styleCenter.setFillForegroundColor(colorWhite);
        styleCenter.setBorderTop(BorderStyle.HAIR);
        styleCenter.setBorderBottom(BorderStyle.HAIR);
        styleCenter.setBorderLeft(BorderStyle.HAIR);
        styleCenter.setBorderRight(BorderStyle.HAIR);
        styleCenter.setAlignment(HorizontalAlignment.CENTER);
        styleCenter.setVerticalAlignment(VerticalAlignment.CENTER);
        styleCenter.setFont(fontContent);

        styleRight = workbook.createCellStyle();
        //styleRight.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        //styleRight.setFillForegroundColor(colorWhite);
        styleRight.setBorderTop(BorderStyle.HAIR);
        styleRight.setBorderBottom(BorderStyle.HAIR);
        styleRight.setBorderLeft(BorderStyle.HAIR);
        styleRight.setBorderRight(BorderStyle.HAIR);
        styleRight.setAlignment(HorizontalAlignment.RIGHT);
        styleRight.setVerticalAlignment(VerticalAlignment.CENTER);
        styleRight.setFont(fontContent);
    }

    private void createCells(Container container) {
        for (Object element : container.elements) {
            String className = element.getClass().getSimpleName();

            // Create compound name for container using parent container's name
            if (element instanceof Container) {
                Container containerElement = (Container) element;
                containerElement.container = container; // set parent container
                containerElement.localName = containerElement.name;
                containerElement.name = (containerElement.name == null || containerElement.name.trim().isEmpty()) ?
                        container.name : container.name + "_" + containerElement.name;
            }

            String style=null;
            String styleClass = null;
            if (element instanceof Component) {
                Component component = (Component) element;
                component.container = container; // set parent container
                style = component.style;
                styleClass = component.styleClass;
                if (style != null && style.trim().isEmpty()) style = null;
                if (styleClass != null && styleClass.trim().isEmpty()) styleClass = null;
            }

            if (className.equals("String")) {
                String prevValue = cell.getStringCellValue();
                cell.setCellValue(prevValue + ((String) element).trim());
            }
            else if (className.equals("Br")) {
                String prevValue = cell.getStringCellValue();
                cell.setCellValue(prevValue + "\n");
                short lineCount = (short) (getLineCount(prevValue) + 1);
                if (lineCount > lineCountInRow.get(rowNum)) lineCountInRow.put(rowNum, lineCount);
            }
            else if (className.equals("P")) {
                newRow();
                createCells((Container) element);
                newRow();
            }
            else if (className.equals("Span")) {
                if (!cell.getStringCellValue().trim().isEmpty()) cell = row.createCell(++cellNum);
                createCells((Container) element);
            }
            else if (className.equals("H1") || className.equals("H2") || className.equals("H3") || className.equals("H4") || className.equals("H5") || className.equals("H6")) {
                newRow();
                createCells((Container) element);
                newRow();
            }
            else if (className.equals("Table")) {
                createCells((Container) element);
                newRow();
            }
            else if (className.equals("Tr")) {
                Tr tr = (Tr) element;
                row = sheet.createRow(++rowNum);
                lineCountInRow.put(rowNum, (short) 1);
                cellNum = 0;
                rowStyleClass = ((Container) element).styleClass;
                createCells((Container) element);
                // Dynamic rows
                if (tr.id != null && !tr.id.trim().isEmpty()) // && dynamicRowTemplates.containsKey(tr.name))
                    createDynamicRows(tr.name, tr.id);
            }
            else if (className.equals("Th")) {
                //Th th = (Th) element;
                //tagBuilder.append("<th");
                //if (th.colspan != null && !th.colspan.trim().isEmpty()) tagBuilder.append(" colspan=\"").append(th.colspan).append("\"");
                //if (th.rowspan != null && !th.rowspan.trim().isEmpty()) tagBuilder.append(" rowspan=\"").append(th.rowspan).append("\"");
                cell = row.createCell(cellNum);
                cell.setCellStyle(styleHeader);
                createCells((Container) element);
                cellNum++;
            }
            else if (className.equals("Td")) {
                //Td td = (Td) element;
                //tagBuilder.append("<td");
                //if (td.colspan != null && !td.colspan.trim().isEmpty()) tagBuilder.append(" colspan=\"").append(td.colspan).append("\"");
                //if (td.rowspan != null && !td.rowspan.trim().isEmpty()) tagBuilder.append(" rowspan=\"").append(td.rowspan).append("\"");
                cell = row.createCell(cellNum);

                if (rowStyleClass != null && rowStyleClass.equals("groupHeader")) {
                    cell.setCellStyle(styleHeader);
                } else {
                    Td tdElement = (Td) element;
                    List<String> cellsInnerHtml = new ArrayList<String>();
                    for (Object inputElement : tdElement.elements) {
                        if (inputElement instanceof InputText) {
                            // Create compound name for td using parent container's name
                            InputText inputText = (InputText) inputElement;
                            if (inputText.name.contains("sum") || inputText.name.contains("kolcb") ||
                                    inputText.name.contains("nominal_value_cb") || inputText.name.contains("currency_cb") ||
                                    inputText.name.contains("cb_avalsale") || inputText.name.contains("cb_fairval") ||
                                    inputText.name.contains("cb_hold") || inputText.name.contains("balval") ||
                                    inputText.name.contains("standard") || inputText.name.contains("uncertain") ||
                                    inputText.name.contains("vsego") || inputText.name.contains("hopeless") ||
                                    inputText.name.contains("cost") || inputText.name.contains("loan") ||
                                    inputText.name.contains("subs_debt") || inputText.name.contains("accpayable") ||
                                    inputText.name.contains("cb_repo") || inputText.name.contains("div_pay") ||
                                    inputText.name.contains("other_sources") || inputText.name.contains("rew") ||
                                    inputText.name.contains("balacc") || inputText.name.contains("rew")

                                    ) {
                                XSSFCellStyle styleNumber = styleRight;
                                DataFormat format = workbook.createDataFormat();
                                styleNumber.setDataFormat(format.getFormat("#,##0.00"));
                                cell.setCellStyle(styleNumber);
                                cell.setCellType(Cell.CELL_TYPE_NUMERIC);
                                //cell.
                            } else if (inputText.name.contains("code") || inputText.name.contains("deg_risk") ||
                                        inputText.name.contains("conv") || inputText.name.contains("num") ||
                                        inputText.name.contains("date")) {
                                cell.setCellStyle(styleCenter);
                            } else {
                                cell.setCellStyle(styleLeft);
                            }
                            //inputText.container = tdElement; // set parent container
                            //inputText.name = (td.name == null || td.name.trim().isEmpty()) ? dynamicRow.name : dynamicRow.name + "_" + td.name;
                            // Get inner Html of each cell
                            //cellsInnerHtml.add(getHtml(td));
                        } else {
                            XSSFCellStyle styleNum = styleLeft;
                            styleNum.setWrapText(true);
                            cell.setCellStyle(styleNum);
                        }
                    }
                    //cell.setCellStyle(styleLeft);
                }
                createCells((Container) element);
                cellNum++;
            }
            else if (className.equals("StaticText")) {
                StaticText staticText = (StaticText) element;
                String prevValue = cell.getStringCellValue();
                String newValue;
                if (staticText.name.trim().equals("$RespondentInfo") && respondent != null) newValue = respondent.getNameRu();
                else if (staticText.name.trim().equals("$ReportDate")) newValue = Convert.getDateStringFromDate(reportDate);
                else if (staticText.name.trim().equals("$ReportYear")) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(reportDate);
                    calendar.set(Calendar.DAY_OF_YEAR, 1);
                    calendar.set(Calendar.MONTH, 1);
                    newValue = Convert.getDateStringFromDate(calendar.getTime());
                }
                else newValue = staticText.value.trim();

                cell.setCellValue(prevValue + newValue);
                cell.setCellStyle(styleCenter);
            }
            else if (className.equals("InputText")) {
                InputText inputText = (InputText) element;

                StringBuilder sbCompoundName = new StringBuilder();
                if (container.name != null && !container.name.isEmpty()) sbCompoundName.append(container.name);
                sbCompoundName.append("*");
                if (inputText.name != null && !inputText.name.isEmpty()) sbCompoundName.append(inputText.name);
                sbCompoundName.append(":");
                if (inputText.key != null && !inputText.key.isEmpty()) sbCompoundName.append(inputText.key);
                sbCompoundName.append(":");
                if (inputText.keyValue != null && !inputText.keyValue.isEmpty()) sbCompoundName.append(inputText.keyValue);
                String compoundName = sbCompoundName.toString();

                String mapValue = attributeValueMap.get(compoundName);
                String cellValue;
                if (mapValue == null) {
                    cellValue = inputText.value == null || inputText.value.trim().isEmpty() ? "" : inputText.value;
                } else {
                    try {
                        double dValue = Double.parseDouble(mapValue);
                        cellValue = mapValue.trim().isEmpty() ? "" : Convert.getStringFromNumber(dValue);
                    } catch (NumberFormatException e) {
                        cellValue = "";
                    }
                }
                cell.setCellValue(cellValue);
            }
            else if (className.equals("InputDate")) {
                InputDate inputDate = (InputDate) element;

                StringBuilder sbCompoundName = new StringBuilder();
                if (container.name != null && !container.name.isEmpty()) sbCompoundName.append(container.name);
                sbCompoundName.append("*");
                if (inputDate.name != null && !inputDate.name.isEmpty()) sbCompoundName.append(inputDate.name);
                sbCompoundName.append(":");
                if (inputDate.key != null && !inputDate.key.isEmpty()) sbCompoundName.append(inputDate.key);
                sbCompoundName.append(":");
                if (inputDate.keyValue != null && !inputDate.keyValue.isEmpty()) sbCompoundName.append(inputDate.keyValue);
                String compoundName = sbCompoundName.toString();

                String mapValue = attributeValueMap.get(compoundName);
                String cellValue;
                if (mapValue == null) {
                    cellValue = inputDate.value == null || inputDate.value.trim().isEmpty() ? "" : inputDate.value;
                } else {
                    cellValue = mapValue.trim().isEmpty() ? "" : mapValue;
                }
                cell.setCellValue(cellValue);
            }
            else if (className.equals("InputTime")) {
                InputTime inputTime = (InputTime) element;

                StringBuilder sbCompoundName = new StringBuilder();
                if (container.name != null && !container.name.isEmpty()) sbCompoundName.append(container.name);
                sbCompoundName.append("*");
                if (inputTime.name != null && !inputTime.name.isEmpty()) sbCompoundName.append(inputTime.name);
                sbCompoundName.append(":");
                if (inputTime.key != null && !inputTime.key.isEmpty()) sbCompoundName.append(inputTime.key);
                sbCompoundName.append(":");
                if (inputTime.keyValue != null && !inputTime.keyValue.isEmpty()) sbCompoundName.append(inputTime.keyValue);
                String compoundName = sbCompoundName.toString();

                String mapValue = attributeValueMap.get(compoundName);
                String cellValue;
                if (mapValue == null) {
                    cellValue = inputTime.value == null || inputTime.value.trim().isEmpty() ? "" : inputTime.value;
                } else {
                    cellValue = mapValue.trim().isEmpty() ? "" : mapValue;
                }
                cell.setCellValue(cellValue);
            }
            else if (className.equals("InputSelect")) {
                InputSelect inputSelect = (InputSelect) element;

                // Здесь был код ЕССП

                StringBuilder sbCompoundName = new StringBuilder();
                if (container.name != null && !container.name.isEmpty()) sbCompoundName.append(container.name);
                sbCompoundName.append("*");
                if (inputSelect.name != null && !inputSelect.name.isEmpty()) sbCompoundName.append(inputSelect.name);
                sbCompoundName.append(":");
                if (inputSelect.key != null && !inputSelect.key.isEmpty()) sbCompoundName.append(inputSelect.key);
                sbCompoundName.append(":");
                if (inputSelect.keyValue != null && !inputSelect.keyValue.isEmpty()) sbCompoundName.append(inputSelect.keyValue);
                String compoundName = sbCompoundName.toString();

                String mapValue = attributeValueMap.get(compoundName);
                if (mapValue != null && !mapValue.trim().isEmpty()) {
                    Map<String, String> referenceMap = referencesMap.get(inputSelect.ref);
                    if (referenceMap != null) {
                        String refCaption = referenceMap.get(mapValue);
                        if (refCaption != null) cell.setCellValue(refCaption);
                    }
                }
            }
        }
    }

    private int getLineCount(String s) {
        String[] lines = s.split("\\n");
        return lines.length;
    }

    private void newRow() {
        row = sheet.createRow(++rowNum);
        lineCountInRow.put(rowNum, (short) 1);
        cellNum = 0;
        cell = row.createCell(cellNum);
    }

    private void createDynamicRows(String tableName, String groupId) {
        List<String> dynamicRowTemplate = drtMap.get(tableName);
        if (dynamicRowTemplate != null && !dynamicRowTemplate.isEmpty()) {

            String tablePrefix = tableName + ":";
            String groupPrefix = "$D." + groupId + ".";

            SortedSet<String> dynamicRowIds = new TreeSet<String>();
            for (Map.Entry<String, String> entry : attributeValueMap.entrySet()) {
                String[] keyParts = entry.getKey().split(":");
                if (keyParts.length > 2) {
                    String rowId = keyParts[2];
                    if (rowId.startsWith(groupPrefix)) dynamicRowIds.add(rowId);
                }
            }

            for (String dynamicRowId : dynamicRowIds) {
                newRow();
                for (int j = 0; j < dynamicRowTemplate.size(); j++) {
                    cell = row.createCell(j);
                    String rowIdText = dynamicRowId.replace("$D.", "").replace("group.", "");
                    String cellValue = dynamicRowTemplate.get(j).replace("$DynamicRowIdText", rowIdText).replace("$DynamicRowId", dynamicRowId);
                    cell.setCellValue(cellValue);
                }

                //htmlTr.id = tablePrefix + dynamicRowId;
                //htmlTr.ths = templateRow.ths;
                //htmlTr.tds = templateRow.tds;
            }
        }
    }

    /*
    private void createDynamicRowTemplate(Container container) {
        List<String> drt = new ArrayList<String>();
        drt.add("");
        drt.add("$DynamicRowIdText");
        drt.add("333");
        dynamicRowTemplates.put("testDynNoGroup_mainTable", drt);
    }
    */

    private void createDynamicRowTemplate(Container container) {
        /*
        StringBuilder stringBuilder = new StringBuilder();
        for (Object element : container.elements) {
            String className = element.getClass().getSimpleName();

            // Create compound name for container using parent container's name
            if (element instanceof Container) {
                Container containerElement = (Container) element;
                containerElement.container = container; // set parent container
                containerElement.localName = containerElement.name;
                containerElement.name = (containerElement.name == null || containerElement.name.trim().isEmpty()) ?
                        container.name : container.name + "_" + containerElement.name;
            }

            String style=null;
            String styleClass = null;
            if (element instanceof Component) {
                Component component = (Component) element;
                component.container = container; // set parent container
                style = component.style;
                styleClass = component.styleClass;
                if (style != null && style.trim().isEmpty()) style = null;
                if (styleClass != null && styleClass.trim().isEmpty()) styleClass = null;
            }

            if (className.equals("String")) {



                String prevValue = cell.getStringCellValue();
                cell.setCellValue(prevValue + ((String) element).trim());



                String s = (String) element;
                stringBuilder.append(s);
            }
            else if (className.equals("Br")) {
                stringBuilder.append("\n");
            }
            else if (className.equals("P")) {
                tagBuilder.append("<p").append(getStyleAttributes(style, styleClass)).append(">")
                        .append(getHtml((Container) element)).append("</p>");
            }
            else if (className.equals("Span")) {
                tagBuilder.append("<span").append(getStyleAttributes(style, styleClass)).append(">")
                        .append(getHtml((Container) element)).append("</span>");
            }
            else if (className.equals("H1")) {
                tagBuilder.append("<h1").append(getStyleAttributes(style, styleClass)).append(">")
                        .append(getHtml((Container) element)).append("</h1>");
            }
            else if (className.equals("H2")) {
                tagBuilder.append("<h2").append(getStyleAttributes(style, styleClass)).append(">")
                        .append(getHtml((Container) element)).append("</h2>");
            }
            else if (className.equals("H3")) {
                tagBuilder.append("<h3").append(getStyleAttributes(style, styleClass)).append(">")
                        .append(getHtml((Container) element)).append("</h3>");
            }
            else if (className.equals("H4")) {
                tagBuilder.append("<h4").append(getStyleAttributes(style, styleClass)).append(">")
                        .append(getHtml((Container) element)).append("</h4>");
            }
            else if (className.equals("H5")) {
                tagBuilder.append("<h5").append(getStyleAttributes(style, styleClass)).append(">")
                        .append(getHtml((Container) element)).append("</h5>");
            }
            else if (className.equals("H6")) {
                tagBuilder.append("<h6").append(getStyleAttributes(style, styleClass)).append(">")
                        .append(getHtml((Container) element)).append("</h6>");
            }
            else if (className.equals("Table")) {
                Table table = (Table) element;
                if (table.id == null || table.id.trim().isEmpty()) table.id = table.name;
                tagBuilder.append("<table");
                if (table.id != null && !table.id.trim().isEmpty()) tagBuilder.append(" id=\"" + table.id + "\"");
                tagBuilder.append(getStyleAttributes(style, styleClass)).append(">").append(getHtml((Container) element)).append("</table>");
            }
            else if (className.equals("Tr")) {
                Tr tr = (Tr) element;
                tagBuilder.append("<tr");
                if (tr.id != null && !tr.id.trim().isEmpty()) tagBuilder.append(" id=\"" + container.name + ":" + tr.id + "\"");
                tagBuilder.append(getStyleAttributes(style, styleClass)).append(">").append(getHtml((Container) element)).append("</tr>");
            }
            else if (className.equals("Th")) {
                Th th = (Th) element;
                tagBuilder.append("<th");
                if (th.colspan != null && !th.colspan.trim().isEmpty()) tagBuilder.append(" colspan=\"").append(th.colspan).append("\"");
                if (th.rowspan != null && !th.rowspan.trim().isEmpty()) tagBuilder.append(" rowspan=\"").append(th.rowspan).append("\"");
                tagBuilder.append(getStyleAttributes(style, styleClass)).append(">")
                        .append(getHtml((Container) element)).append("</th>");
            }
            else if (className.equals("Td")) {
                Td td = (Td) element;
                tagBuilder.append("<td");
                if (td.colspan != null && !td.colspan.trim().isEmpty()) tagBuilder.append(" colspan=\"").append(td.colspan).append("\"");
                if (td.rowspan != null && !td.rowspan.trim().isEmpty()) tagBuilder.append(" rowspan=\"").append(td.rowspan).append("\"");
                tagBuilder.append(getStyleAttributes(style, styleClass)).append(">")
                        .append(getHtml((Container) element)).append("</td>");
            }
            else if (className.equals("StaticText")) {
                StaticText staticText = (StaticText) element;
                tagBuilder.append("<span id=\"");
                if (container.name != null && !container.name.isEmpty()) tagBuilder.append(container.name);
                tagBuilder.append("*");
                if (staticText.name != null && !staticText.name.isEmpty()) tagBuilder.append(staticText.name);
                tagBuilder.append(":");
                if (staticText.key != null && !staticText.key.isEmpty()) tagBuilder.append(staticText.key);
                tagBuilder.append(":");
                if (staticText.keyValue != null && !staticText.keyValue.isEmpty()) tagBuilder.append(staticText.keyValue);
                tagBuilder.append("\"");
                tagBuilder.append(getStyleAttributes(style, styleClass));
                tagBuilder.append(">");
                if (staticText.value != null && !staticText.value.trim().isEmpty()) tagBuilder.append(staticText.value);
                tagBuilder.append("</span>");
            }
            else if (className.equals("InputText")) {
                InputText inputText = (InputText) element;
                tagBuilder.append("<input type=\"text\"");

                StringBuilder sbCompoundName = new StringBuilder();
                if (container.name != null && !container.name.isEmpty()) sbCompoundName.append(container.name);
                sbCompoundName.append("*");
                if (inputText.name != null && !inputText.name.isEmpty()) sbCompoundName.append(inputText.name);
                sbCompoundName.append(":");
                if (inputText.key != null && !inputText.key.isEmpty()) sbCompoundName.append(inputText.key);
                sbCompoundName.append(":");
                if (inputText.keyValue != null && !inputText.keyValue.isEmpty()) sbCompoundName.append(inputText.keyValue);
                String compoundName = sbCompoundName.toString();

                tagBuilder.append(" id=\"" + compoundName + "\"");
                tagBuilder.append(" name=\"" + compoundName + "\"");

                if (inputText.readonly != null && inputText.readonly.booleanValue()) tagBuilder.append(" readonly=\"readonly\"");
                if (inputText.auto == null || !inputText.auto.booleanValue()) tagBuilder.append(" onchange=\"updateCalculatedFields();\"");

                if (inputText.mask != null && !inputText.mask.trim().isEmpty()) {
                    if (inputText.mask.equals("money0")) styleClass = styleClass == null ? "maskMoney0" : styleClass + " maskMoney0";
                    if (inputText.mask.equals("money1")) styleClass = styleClass == null ? "maskMoney1" : styleClass + " maskMoney1";
                    if (inputText.mask.equals("money2")) styleClass = styleClass == null ? "maskMoney2" : styleClass + " maskMoney2";
                    if (inputText.mask.equals("money3")) styleClass = styleClass == null ? "maskMoney3" : styleClass + " maskMoney3";
                    if (inputText.mask.equals("money4")) styleClass = styleClass == null ? "maskMoney4" : styleClass + " maskMoney4";
                    if (inputText.mask.equals("money5")) styleClass = styleClass == null ? "maskMoney5" : styleClass + " maskMoney5";
                    if (inputText.mask.equals("money6")) styleClass = styleClass == null ? "maskMoney6" : styleClass + " maskMoney6";
                }
                tagBuilder.append(getStyleAttributes(style, styleClass));
                if (inputText.value != null && !inputText.value.trim().isEmpty()) {
                    tagBuilder.append(" value=\"").append(inputText.value).append("\"");
                    initialValuesMap.put(compoundName, inputText.value);
                }

                tagBuilder.append(" onkeydown=\"moveFocus(event, this);\"");
                tagBuilder.append(" onfocus=\"this.select();\""); // select all content on focus
                tagBuilder.append("/>");
            }
            else if (className.equals("InputDate")) {
                InputDate inputDate = (InputDate) element;
                tagBuilder.append("<input type=\"text\"");

                StringBuilder sbCompoundName = new StringBuilder();
                if (container.name != null && !container.name.isEmpty()) sbCompoundName.append(container.name);
                sbCompoundName.append("*");
                if (inputDate.name != null && !inputDate.name.isEmpty()) sbCompoundName.append(inputDate.name);
                sbCompoundName.append(":");
                if (inputDate.key != null && !inputDate.key.isEmpty()) sbCompoundName.append(inputDate.key);
                sbCompoundName.append(":");
                if (inputDate.keyValue != null && !inputDate.keyValue.isEmpty()) sbCompoundName.append(inputDate.keyValue);
                String compoundName = sbCompoundName.toString();

                tagBuilder.append(" id=\"" + compoundName + "\"");
                tagBuilder.append(" name=\"" + compoundName + "\"");

                if (inputDate.readonly != null && inputDate.readonly.booleanValue()) tagBuilder.append(" readonly=\"readonly\"");
                styleClass = styleClass == null ? "datepicker" : styleClass + " datepicker";

                tagBuilder.append(getStyleAttributes(style, styleClass));
                if (inputDate.value != null && !inputDate.value.trim().isEmpty()) {
                    tagBuilder.append(" value=\"").append(inputDate.value).append("\"");
                    initialValuesMap.put(compoundName, inputDate.value);
                }

                tagBuilder.append(" onkeydown=\"moveFocus(event, this);\"");
                //tagBuilder.append(" onfocus=\"this.select();\""); // select all content on focus
                tagBuilder.append("/>");
            }
            else if (className.equals("InputSelect")) {
                InputSelect inputSelect = (InputSelect) element;
                tagBuilder.append("<select");

                StringBuilder sbCompoundName = new StringBuilder();
                if (container.name != null && !container.name.isEmpty()) sbCompoundName.append(container.name);
                sbCompoundName.append("*");
                if (inputSelect.name != null && !inputSelect.name.isEmpty()) sbCompoundName.append(inputSelect.name);
                sbCompoundName.append(":");
                if (inputSelect.key != null && !inputSelect.key.isEmpty()) sbCompoundName.append(inputSelect.key);
                sbCompoundName.append(":");
                if (inputSelect.keyValue != null && !inputSelect.keyValue.isEmpty()) sbCompoundName.append(inputSelect.keyValue);
                String compoundName = sbCompoundName.toString();

                tagBuilder.append(" id=\"" + compoundName + "\"");
                tagBuilder.append(" name=\"" + compoundName + "\"");
                tagBuilder.append(getStyleAttributes(style, styleClass));
                tagBuilder.append(">");

                tagBuilder.append("<option value=\"$:" + inputSelect.ref + ":" + inputSelect.refCode + ":" + inputSelect.refCaption + "\"></option>");

                tagBuilder.append("</select>");
            }
            else if (className.equals("DynamicRow")) {
                DynamicRow dynamicRow = (DynamicRow) element;
                Container parent;
                // Retrieve closest table
                Table table = null;
                parent = dynamicRow.container;
                while (parent != null) {
                    if (parent instanceof Table) {
                        table = (Table) parent;
                        break;
                    } else {
                        parent = parent.container;
                    }
                }
                String tableId = table == null ? "" : table.id;

                // Generate inner HTML for each cell
                List<String> cellsInnerHtml = new ArrayList<String>();
                for (Object drElement : dynamicRow.elements) {
                    if (drElement instanceof Td) {
                        // Create compound name for td using parent container's name
                        Td td = (Td) drElement;
                        td.container = dynamicRow; // set parent container
                        td.name = (td.name == null || td.name.trim().isEmpty()) ? dynamicRow.name : dynamicRow.name + "_" + td.name;
                        // Get inner Html of each cell
                        String innerHtml = getHtml(td).replaceAll("\\s+", " ").trim();
                        cellsInnerHtml.add(innerHtml);
                    }
                }
                // Generate HTML for a template row
                StringBuilder sbRowHtml = new StringBuilder();
                for (String cellHtml : cellsInnerHtml) sbRowHtml.append("<td>").append(cellHtml).append("</td>");
                tagBuilder.append("<tr id=\"" + dynamicRow.name + ":" + dynamicRow.groupId + ".$DynamicRowIdMinor" + "\"");
                tagBuilder.append(getStyleAttributes(style, styleClass)).append(">").append(sbRowHtml.toString()).append("</tr>");

                // Generate dynamic function
                StringBuilder fb = new StringBuilder();
                fb.append("if (tableId === '" + tableId + "') {");
                for (int i = 0; i < cellsInnerHtml.size(); i++) {
                    fb.append("var cell" + i + " = row.insertCell(" + i + ");");
                    fb.append("cell" + i + ".innerHTML = '" + cellsInnerHtml.get(i) + "';");
                    // processing spans
                    fb.append("spans = cell" + i + ".getElementsByTagName('span');");
                    fb.append("for (var i = 0; i < spans.length; i++) {");
                    fb.append("  if (spans[i].innerHTML.indexOf(drIdValue) > -1) spans[i].innerHTML = rowId.replace('$D.','').replace('group.','');");
                    fb.append("}");
                    // processing inputs
                    fb.append("inputs = cell" + i + ".getElementsByTagName('input');");
                    fb.append("for (var i = 0; i < inputs.length; i++) {");
                    fb.append("  var inputType = inputs[i].getAttribute('type');");
                    fb.append("  if (inputs[i].id) inputs[i].id = inputs[i].id.replace(drIdValue, rowId);");
                    fb.append("  if (inputs[i].name) inputs[i].name = inputs[i].name.replace(drIdValue, rowId);");
                    fb.append("  if ($(inputs[i]).is('[class*=maskMoney]')) initMask(inputs[i]);");
                    fb.append("  if ($(inputs[i]).hasClass('datepicker')) initDatePicker(inputs[i]);");
                    fb.append("  if (inputType === 'button' && inputs[i].onclick) {");
                    fb.append("    var func = inputs[i].getAttribute('onclick');");
                    fb.append("    func = func.replace(drIdValue, rowId);");
                    fb.append("    inputs[i].setAttribute('onclick', func);");
                    fb.append("  }");
                    fb.append("}");
                    // processing selects
                    fb.append("selects = cell" + i + ".getElementsByTagName('select');");
                    fb.append("for (var i = 0; i < selects.length; i++) {");
                    fb.append("  if (selects[i].id) selects[i].id = selects[i].id.replace(drIdValue, rowId);");
                    fb.append("  if (selects[i].name) selects[i].name = selects[i].name.replace(drIdValue, rowId);");
                    fb.append("}");
                }
                fb.append("}");
                fbAddCells.append(fb.toString());
            }
            else if (className.equals("Style")) {
                tagBuilder.append("\n<style>").append(templateStyle).append(((Style) element).content).append("</style>");
            }
            else if (className.equals("Script")) {
                tagBuilder.append("\n<script>\n").append(templateScript).append(((Script) element).content);

                fbAddCells.append("}");
                dynamicFunctions.add(fbAddCells.toString());
                for (String dynamicFunction : dynamicFunctions) tagBuilder.append(dynamicFunction);
                tagBuilder.append("\n</script>\n");
            }
        }
        return tagBuilder.toString();
        */
    }

    private void autoSize() {
        for (Integer rn : lineCountInRow.keySet()) {
            short defaultRowHeight = sheet.getRow(rn.intValue()).getHeight();
            short newRowHeight = (short) (lineCountInRow.get(rn) * defaultRowHeight);
            sheet.getRow(rn.intValue()).setHeight(newRowHeight);
        }
        for (int i = 0; i < sheet.getRow(3).getPhysicalNumberOfCells(); i++) {
            sheet.autoSizeColumn(i);
        }
        sheet.setColumnWidth(0, 2000);
        sheet.setColumnWidth(1, 20000);
    }

    public byte[] getExcelFileContent() {
        byte[] result = null;
        if (workbook == null) return null;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            workbook.write(out);
            result = out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { out.close(); }
            catch (IOException e) { e.printStackTrace(); }
            return result;
        }
    }

    public boolean saveFile(String pathFileName) {
        boolean result = false;
        if (workbook == null) return result;

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(new File(pathFileName));
            workbook.write(out);
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }
    }

}
