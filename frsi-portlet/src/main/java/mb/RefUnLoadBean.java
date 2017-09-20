package mb;

import entities.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.primefaces.context.RequestContext;
import util.Convert;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@ManagedBean
@SessionScoped
public class RefUnLoadBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;

    private List<RefItem> RefList;
    private List<RefItem> selectedRef;
    private Long userId;
    private Date date;

    private Boolean allRecords;
    private Date dateExcel;
    private Boolean loadBank;
    private Boolean renderForBank;
    private Boolean renderForMfoReg;
    private String userLocation;
    private long depId;


    private List<RefPostItem> RefPostList;
    private List<RefPersonItem> RefPersonList;
    private List<RefLegalPersonItem> RefLegalPersonList;
    private List<RefCountryItem> RefCountryList;
    private List<RefManagersItem> RefManagersList;
    private List<RefTypeBusEntityItem> RefTypeBusEntityList;
    private List<RefRegionItem> RefRegionList;
    private List<RefRequirementItem> RefRequirementList;
    private List<RefTypeProvideItem> RefTypeProvideList;
    private List<RefTransTypeItem> RefTransTypeList;
    private List<RefBalanceAccItem> RefBalanceAccList;
    private List<RefConnOrgItem> RefConnOrgList;
    private List<RefDepartmentItem> RefDepartmentList;
    private List<RefBankItem> RefBankList;
    private List<RefRateAgencyItem> RefRateAgencyList;
    private List<RefCurrencyItem> RefCurrencyList;
    private List<RefCurrencyRateItem> RefCurrencyRateList;
    private List<RefSubjectTypeItem> RefSubjectTypeList;
    private List<RefRespondentItem> RefRespondentList;
    private List<RefDocTypeItem> RefDocTypeList;
    private List<RefDocumentItem> RefDocumentList;
    private List<RefIssuersItem> RefIssuersList;
    private List<RefSecuritiesItem> RefSecuritiesList;
    private List<RefVidOperItem> RefVidOperList;
    private List<RefBranchItem> RefBranchList;
    private List<RefListingEstimationItem> RefListingEstimationList;
    private List<RefRatingEstimationItem> RefRatingEstimationList;
    private List<RefRatingCategoryItem> RefRatingCategoryList;
    private List<SimpleReference> RefSimpleRefList;
    private List<RefMrpItem> RefMrpList;

    @PostConstruct
    public void init() { // preRenderView event listener
        try {
            if (sessionBean.isEjbNull()) sessionBean.init();
            date = sessionBean.getIntegration().getNewDateFromBackEndServer();
            userId = sessionBean.user.getUserId();
            userLocation = sessionBean.user.getLoginIP();
            refreshList();
        } catch (Exception e) { applicationBean.redirectToErrorPage(e); }
    }

    // At least dummy preRender event listener required to properly redirect to error pages when exceptions occur in PostConstruct methods.
    public void preRender() {
        boolean isPostBack = FacesContext.getCurrentInstance().isPostback();
        boolean isAjax = FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest();
        if (isPostBack || isAjax) return;
        try {
        } catch (Exception e) {
            applicationBean.redirectToErrorPage(e);
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('statusDialog').show()");
    }

    public void refreshList() {
        RefList = sessionBean.getReference().getRefList(userId, null);
    }

    public void unLoadRef() {
        String xml = null;
        String refName;

        byte[] buffer = new byte[1024];
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(bos);

            ZipEntry ze;
            InputStream is;
            int len;

            for (RefItem refItem : selectedRef) {
                refName = refItem.getCode();
                if (refName.equals(RefPostItem.REF_CODE)) {
                    xml = createRefPostDataFile(date, refName);
                } else if (refName.equals(RefPersonItem.REF_CODE)) {
                    xml = createRefPersonDataFile(date,refName);
                } else if (refName.equals(RefLegalPersonItem.REF_CODE)) {
                    xml = createRefLegalPersonDataFile(date,refName);
                } else if (refName.equals(RefCountryItem.REF_CODE)) {
                    xml = createRefCountryDataFile(date,refName);
                } else if (refName.equals(RefManagersItem.REF_CODE)) {
                    xml = createRefManagersDataFile(date,refName);
                } else if (refName.equals(RefTypeBusEntityItem.REF_CODE)) {
                    xml = createRefTypeBusEntityDataFile(date,refName);
                } else if (refName.equals(RefRegionItem.REF_CODE)) {
                    xml = createRefRegionDataFile(date,refName);
                } else if (refName.equals(RefRequirementItem.REF_CODE)) {
                    xml = createRefRequirementDataFile(date,refName);
                } else if (refName.equals(RefTypeProvideItem.REF_CODE)) {
                    xml = createRefTypeProvideDataFile(date,refName);
                } else if (refName.equals(RefTransTypeItem.REF_CODE)) {
                    xml = createRefTransTypeDataFile(date,refName);
                } else if (refName.equals(RefBalanceAccItem.REF_CODE)) {
                    xml = createRefBalanceAccountDataFile(date,refName);
                } else if (refName.equals(RefConnOrgItem.REF_CODE)) {
                    xml = createRefConnOrgDataFile(date,refName);
                } else if (refName.equals(RefDepartmentItem.REF_CODE)) {
                    xml = createRefDepartmentDataFile(date,refName);
                } else if (refName.equals(RefBankItem.REF_CODE)) {
                    xml = createRefBankDataFile(date,refName);
                } else if (refName.equals(RefRateAgencyItem.REF_CODE)) {
                    xml = createRefRateAgencyDataFile(date,refName);
                } else if (refName.equals(RefCurrencyItem.REF_CODE)) {
                    xml = createRefCurrencyDataFile(date,refName);
                } else if (refName.equals(RefCurrencyRateItem.REF_CODE)) {
                    xml = createRefCurrencyRateDataFile(date,refName);
                } else if (refName.equals(RefSubjectTypeItem.REF_CODE)) {
                    xml = createRefSubjectTypeDataFile(date,refName);
                } else if (refName.equals(RefRespondentItem.REF_CODE)) {
                    xml = createRefRespondentDataFile(date,refName);
                } else if (refName.equals(RefDocTypeItem.REF_CODE)) {
                    xml = createRefDocTypeDataFile(date,refName);
                } else if (refName.equals(RefDocumentItem.REF_CODE)) {
                    xml = createRefDocumentDataFile(date,refName);
                } else if (refName.equals(RefIssuersItem.REF_CODE)) {
                    xml = createRefIssuersDataFile(date,refName);
                } else if (refName.equals(RefSecuritiesItem.REF_CODE)) {
                    xml = createRefSecuritiesDataFile(date,refName);
                } else if (refName.equals(RefVidOperItem.REF_CODE)) {
                    xml = createRefVidOperDataFile(date,refName);
                } else if (refName.equals(RefBranchItem.REF_CODE)) {
                    xml = createRefBranchDataFile(date,refName);
                } else if (refName.equals(RefListingEstimationItem.REF_CODE)) {
                    xml = createRefListingEstimationDataFile(date,refName);
                } else if (refName.equals(RefRatingEstimationItem.REF_CODE)) {
                    xml = createRefRatingEstimationDataFile(date,refName);
                } else if (refName.equals(RefRatingCategoryItem.REF_CODE)) {
                    xml = createRefRatingCategoryDataFile(date,refName);
                } else if (refName.equals("ref_request_type")) {
                    xml = createSimpleRefDataFile(date, refName);
                } else if (refName.equals("ref_request_way")) {
                    xml = createSimpleRefDataFile(date, refName);
                } else if (refName.equals("ref_market_kind")) {
                    xml = createSimpleRefDataFile(date, refName);
                } else if (refName.equals("ref_category")) {
                    xml = createSimpleRefDataFile(date, refName);
                } else if (refName.equals("ref_subcategory")) {
                    xml = createSimpleRefDataFile(date, refName);
                } else if (refName.equals("ref_account_type")) {
                    xml = createSimpleRefDataFile(date, refName);
                } else if (refName.equals("ref_subaccount_type")) {
                    xml = createSimpleRefDataFile(date, refName);
                } else if (refName.equals("ref_type_holder_acc")) {
                    xml = createSimpleRefDataFile(date, refName);
                } else if (refName.equals("ref_request_feature")) {
                    xml = createSimpleRefDataFile(date, refName);
                } else if (refName.equals("ref_request_sts")) {
                    xml = createSimpleRefDataFile(date, refName);
                } else if (refName.equals("ref_repo_kind")) {
                    xml = createSimpleRefDataFile(date, refName);
                } else if (refName.equals("ref_market_type")) {
                    xml = createSimpleRefDataFile(date, refName);
                } else if (refName.equals("ref_trad_method")) {
                    xml = createSimpleRefDataFile(date, refName);
                } else if (refName.equals("ref_oper_type")) {
                    xml = createSimpleRefDataFile(date, refName);
                } else if (refName.equals("ref_deal_sts")) {
                    xml = createSimpleRefDataFile(date, refName);
                } else if (refName.equals(RefMrpItem.REF_CODE)) {
                    xml = createRefMrpDataFile(date,refName);
                }

                ze = new ZipEntry(refName + ".xml");

                zos.putNextEntry(ze);
                is = new ByteArrayInputStream(xml.getBytes());

                while ((len = is.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                is.close();

            }
            zos.closeEntry();
            zos.close();
            applicationBean.putFileContentToResponseOutputStream(bos.toByteArray(), "application/zip", "reference_" + Convert.dateFormatCompact.format(date) + ".zip");

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(null);
            auditEvent.setNameObject(null);
            auditEvent.setIdKindEvent(13L);
            auditEvent.setDateEvent(sessionBean.getIntegration().getNewDateFromBackEndServer());
            auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
            auditEvent.setDateIn(date);
            auditEvent.setRecId(null);
            auditEvent.setUserId(sessionBean.abstractUser.getId());
            auditEvent.setUserLocation(sessionBean.abstractUser.getLocation());
            sessionBean.getPersistence().insertAuditEvent(auditEvent);
        } catch (Exception e) {
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
        }
    }

    // region Выгрузка в Excell

    private String getRefNameFromValue(String value) {
        return value.substring(value.indexOf(":") + 1);
    }

    private String getRefCodeFromValue(String value) {
        return value.substring(0, value.indexOf(":"));
    }

    public void onUnloadExcel(List<RefItem> selectedRef){
        dateExcel = sessionBean.getIntegration().getNewDateFromBackEndServer();
        RefDepartmentList = (List<RefDepartmentItem>) sessionBean.getReference().getRefAbstractByFilterList(RefDepartmentItem.REF_CODE, new RefDepartmentItem(2L), dateExcel);
        allRecords = false;
        renderForBank = false;
        renderForMfoReg = false;

        if(selectedRef.size() > 1){
            for(RefItem item : selectedRef){
                if(item.getCode().equals(RefBankItem.REF_CODE))
                    renderForBank = true;
                else if (item.getCode().equals(RefMfoRegItem.REF_CODE))
                    renderForMfoReg = true;
            }
        } else if(selectedRef.size() == 1) {
            if(selectedRef.get(0).getCode().equals(RefBankItem.REF_CODE))
                renderForBank = true;
            else if(selectedRef.get(0).getCode().equals(RefMfoRegItem.REF_CODE))
                renderForMfoReg = true;
        }
    }

    public void unloadExcel(List<RefItem> selectedRef) {
        String nameObject = "";
        String codeObject = "";
        try {
            if(selectedRef.size() == 0) {
                throw new Exception("Ошибка при выгрузки в Excel!");
            }

            byte[] buffer = new byte[1024];

            Map<String, byte[]> xlsMap = new HashMap<String,byte[]>();
            byte[] xls = null;

            for (RefItem refContainer : selectedRef) {

                HSSFWorkbook workbook = new HSSFWorkbook();
                HSSFSheet sheet = workbook.createSheet(refContainer.getCode());

                HSSFFont font = workbook.createFont();
                font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

                // Заголовок
                HSSFCellStyle HeaderStyle = workbook.createCellStyle();
                HeaderStyle.setFont(font);
                HeaderStyle.setAlignment(HeaderStyle.ALIGN_CENTER);

                sheet.createRow(0);
                Row rowHeader = sheet.createRow(1);
                Cell cellHeader = rowHeader.createCell(1);
                cellHeader.setCellStyle(HeaderStyle);
                cellHeader.setCellValue(refContainer.getName());
                sheet.createRow(2);

                // Для наименования колонок
                HSSFCellStyle stylColumn = workbook.createCellStyle();
                stylColumn.setBorderBottom(stylColumn.BORDER_MEDIUM);
                stylColumn.setBorderTop(stylColumn.BORDER_MEDIUM);
                stylColumn.setBorderRight(stylColumn.BORDER_MEDIUM);
                stylColumn.setBorderLeft(stylColumn.BORDER_MEDIUM);
                stylColumn.setFont(font);
                stylColumn.setAlignment(stylColumn.ALIGN_CENTER);
                stylColumn.setWrapText(true);

                // Для данных
                HSSFCellStyle style = workbook.createCellStyle();
                style = workbook.createCellStyle();
                style.setBorderBottom(style.BORDER_THIN);
                style.setBorderTop(style.BORDER_THIN);
                style.setBorderRight(style.BORDER_THIN);
                style.setBorderLeft(style.BORDER_THIN);
                style.setWrapText(true);

                HSSFCellStyle styleDate = workbook.createCellStyle();
                styleDate = workbook.createCellStyle();
                styleDate.setBorderBottom(styleDate.BORDER_THIN);
                styleDate.setBorderTop(styleDate.BORDER_THIN);
                styleDate.setBorderRight(styleDate.BORDER_THIN);
                styleDate.setBorderLeft(styleDate.BORDER_THIN);
                styleDate.setAlignment(styleDate.ALIGN_CENTER);

                DataFormat format = workbook.createDataFormat();

                Map<String, Object[]> data = new TreeMap<String, Object[]>();
                int i = 0;
                int col = 0;

                if (refContainer.getCode().equals(RefPostItem.REF_CODE)) {
                    List<RefPostItem> items;
                    if(allRecords) {
                        items = (List<RefPostItem>) sessionBean.getReference().getRefAbstractByFilterList(refContainer.getCode(), null, dateExcel);
                    }else{
                        items = (List<RefPostItem>) sessionBean.getReference().getRefAbstractList(refContainer.getCode(), dateExcel);
                    }
                    for (RefPostItem rs : items) {
                        String id = String.valueOf(rs.getId());
                        String recId = String.valueOf(rs.getRecId());
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        boolean isActivity = rs.getIsActivity() == null ? false : rs.getIsActivity();
                        boolean isMainRuk = rs.getIsMainRuk() == null ? false : rs.getIsMainRuk();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getTypePostName(),
                                isActivity,
                                isMainRuk,
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Тип должности");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 5000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Активность");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Главный руководитель");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals(RefPersonItem.REF_CODE)) {
                    List<RefPersonItem> items;
                    if(allRecords) {
                        items = (List<RefPersonItem>) sessionBean.getReference().getRefAbstractByFilterList(refContainer.getCode(), null, dateExcel);
                    }else{
                        items = (List<RefPersonItem>) sessionBean.getReference().getRefAbstractList(refContainer.getCode(), dateExcel);
                    }
                    for (RefPersonItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getIdn(),
                                rs.getFm(),
                                rs.getNm(),
                                rs.getFt(),
                                rs.getFioRu(),
                                rs.getFioKz(),
                                rs.getFioEn(),
                                rs.getCountryName(),
                                rs.getPhoneWork(),
                                rs.getFax(),
                                rs.getAddressWork(),
                                rs.getNote(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("ИИН");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Фамилия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Отчество");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("ФИО на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 10000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("ФИО на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 10000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("ФИО на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 10000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Гражданство");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Телефон");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Факс");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Адрес");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Примечание");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals(RefLegalPersonItem.REF_CODE)) {
                    List<RefLegalPersonItem> items;
                    if(allRecords) {
                        items = (List<RefLegalPersonItem>) sessionBean.getReference().getRefAbstractByFilterList(refContainer.getCode(), null, dateExcel);
                    }else{
                        items = (List<RefLegalPersonItem>) sessionBean.getReference().getRefAbstractList(refContainer.getCode(), dateExcel);
                    }
                    for (RefLegalPersonItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        boolean isInvFund = rs.getIsInvFund() == null ? false : rs.getIsInvFund();
                        boolean isNonRezident = rs.getIsNonRezident() == null ? false : rs.getIsNonRezident();
                        boolean isAkimat = rs.getIsAkimat() == null ? false : rs.getIsAkimat();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getShortNameRu(),
                                rs.getShortNameKz(),
                                rs.getShortNameEn(),
                                isNonRezident,
                                rs.getIdn(),
                                isInvFund,
                                rs.getInvIdn(),
                                isAkimat,
                                rs.getManager(),
                                rs.getOrgTypeName(),
                                rs.getTypeBeName(),
                                rs.getCountryName(),
                                /*rs.getRegionName(),
                                rs.getPostalIndex(),*/
                                rs.getLegalAddress(),
                                rs.getFactAddress(),
                                rs.getNote(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Краткое наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Краткое наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Краткое наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Нерезидент");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("БИН");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Инвестиционный фонд");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("БИН управляющей компании");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Акимат");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Руководитель (Ф.И.О. полностью)");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Тип организации");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Организационно-правовая форма");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Гражданство");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    /*cell = row2.createCell(++col);
                    cell.setCellValue("Город/регион");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Почтовый индекс");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);*/

                    cell = row2.createCell(++col);
                    cell.setCellValue("Юр.адрес");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Факт.адрес");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Примечание");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals(RefCountryItem.REF_CODE)) {
                    List<RefCountryItem> items;
                    if(allRecords) {
                        items = (List<RefCountryItem>) sessionBean.getReference().getRefAbstractByFilterList(refContainer.getCode(), null, dateExcel);
                    }else{
                        items = (List<RefCountryItem>) sessionBean.getReference().getRefAbstractList(refContainer.getCode(), dateExcel);
                    }
                    for (RefCountryItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals(RefManagersItem.REF_CODE)) {
                    List<RefManagersItem> items;
                    if(allRecords) {
                        items = (List<RefManagersItem>) sessionBean.getReference().getRefAbstractByFilterList(refContainer.getCode(), null, dateExcel);
                    }else{
                        items = (List<RefManagersItem>) sessionBean.getReference().getRefAbstractList(refContainer.getCode(), dateExcel);
                    }
                    for (RefManagersItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();
                        boolean isExecutor = rs.getExecutor() == null ? false :rs.getExecutor();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getFm(),
                                rs.getNm(),
                                rs.getFt(),
                                rs.getFioRu(),
                                rs.getFioKz(),
                                rs.getFioEn(),
                                rs.getPostNameRu(),
                                rs.getPhone(),
                                isExecutor,
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Фамилия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Отчество");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("ФИО на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("ФИО на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("ФИО на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Должность");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Телефон");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Исполнитель");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals(RefTypeBusEntityItem.REF_CODE)) {
                    List<RefTypeBusEntityItem> items;
                    if(allRecords) {
                        items = (List<RefTypeBusEntityItem>)sessionBean.getReference().getRefAbstractByFilterList(RefTypeBusEntityItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefTypeBusEntityItem>)sessionBean.getReference().getRefAbstractList(RefTypeBusEntityItem.REF_CODE, dateExcel);
                    }
                    for (RefTypeBusEntityItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals(RefRegionItem.REF_CODE)) {
                    List<RefRegionItem> items;
                    if(allRecords) {
                        items = (List<RefRegionItem>)sessionBean.getReference().getRefAbstractByFilterList(RefRegionItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefRegionItem>)sessionBean.getReference().getRefAbstractList(RefRegionItem.REF_CODE, dateExcel);
                    }
                    for (RefRegionItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getOblastName(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Область");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals(RefRequirementItem.REF_CODE)) {
                    List<RefRequirementItem> items;
                    if(allRecords) {
                        items = (List<RefRequirementItem>)sessionBean.getReference().getRefAbstractByFilterList(RefRequirementItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefRequirementItem>)sessionBean.getReference().getRefAbstractList(RefRequirementItem.REF_CODE, dateExcel);
                    }
                    for (RefRequirementItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals(RefTypeProvideItem.REF_CODE)) {
                    List<RefTypeProvideItem> items;
                    if(allRecords) {
                        items = (List<RefTypeProvideItem>)sessionBean.getReference().getRefAbstractByFilterList(RefTypeProvideItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefTypeProvideItem>)sessionBean.getReference().getRefAbstractList(RefTypeProvideItem.REF_CODE, dateExcel);
                    }
                    for (RefTypeProvideItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals(RefTransTypeItem.REF_CODE)) {
                    List<RefTransTypeItem> items;
                    if(allRecords) {
                        items = (List<RefTransTypeItem>)sessionBean.getReference().getRefAbstractByFilterList(RefTransTypeItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefTransTypeItem>)sessionBean.getReference().getRefAbstractList(RefTransTypeItem.REF_CODE, dateExcel);
                    }
                    for (RefTransTypeItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getShortNameRu(),
                                rs.getKindOfActivity(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast

                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Краткое наименование");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Вид деятельности");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals(RefBalanceAccItem.REF_CODE)) {
                    List<RefBalanceAccItem> items;
                    if(allRecords) {
                        items = (List<RefBalanceAccItem>)sessionBean.getReference().getRefAbstractByFilterList(RefBalanceAccItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefBalanceAccItem>)sessionBean.getReference().getRefAbstractList(RefBalanceAccItem.REF_CODE, dateExcel);
                    }
                    for (RefBalanceAccItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getParentCode(),
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getShortNameRu(),
                                rs.getShortNameKz(),
                                rs.getShortNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код родителя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Краткое наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Краткое наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Краткое наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals(RefConnOrgItem.REF_CODE)) {
                    List<RefConnOrgItem> items;
                    if(allRecords) {
                        items = (List<RefConnOrgItem>)sessionBean.getReference().getRefAbstractByFilterList(RefConnOrgItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefConnOrgItem>) sessionBean.getReference().getRefAbstractList(RefConnOrgItem.REF_CODE, dateExcel);
                    }
                    for (RefConnOrgItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getShortNameRu(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Краткое наименование");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals(RefDepartmentItem.REF_CODE)) {
                    List<RefDepartmentItem> items;
                    if(allRecords) {
                        items = (List<RefDepartmentItem>)sessionBean.getReference().getRefAbstractByFilterList(RefDepartmentItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefDepartmentItem>)sessionBean.getReference().getRefAbstractList(RefDepartmentItem.REF_CODE, dateExcel);
                    }
                    for (RefDepartmentItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals(RefBankItem.REF_CODE)) {
                    RefBankItem item = new RefBankItem();
                    item.setIsLoad(loadBank);
                    List<RefBankItem> items = (List<RefBankItem>) sessionBean.getReference().getRefAbstractByFilterList(RefBankItem.REF_CODE, item, dateExcel);
                    for (RefBankItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();
                        boolean isNonRezident = rs.getIsNonRezident() == null ? false : rs.getIsNonRezident();
                        boolean isLoad = rs.getIsLoad() == null ? false : rs.getIsLoad();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getIdn(),
                                rs.getPostAddress(),
                                isNonRezident,
                                isLoad,
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast

                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("БИН");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Адрес");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Нерезидент");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3100);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Загружен с НСИ");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals(RefRateAgencyItem.REF_CODE)) {
                    List<RefRateAgencyItem> items;
                    if(allRecords) {
                        items = (List<RefRateAgencyItem>)sessionBean.getReference().getRefAbstractByFilterList(RefRateAgencyItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefRateAgencyItem>)sessionBean.getReference().getRefAbstractList(RefRateAgencyItem.REF_CODE, dateExcel);
                    }
                    for (RefRateAgencyItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals(RefCurrencyItem.REF_CODE)) {
                    List<RefCurrencyItem> items;
                    if(allRecords) {
                        items = (List<RefCurrencyItem>)sessionBean.getReference().getRefAbstractByFilterList(RefCurrencyItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefCurrencyItem>)sessionBean.getReference().getRefAbstractList(RefCurrencyItem.REF_CODE, dateExcel);
                    }
                    for (RefCurrencyItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                rs.getMinorUnits(),
                                rs.getRate(),
                                rs.getCurRateName(),
                                rs.getRateAgency(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Малая денежная ед.");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Ставка");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Рейтинг валюты");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Рейтинговое агентство");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals(RefCurrencyRateItem.REF_CODE)) {
                    List<RefCurrencyRateItem> items;
                    if(allRecords) {
                        items = (List<RefCurrencyRateItem>)sessionBean.getReference().getRefAbstractByFilterList(RefCurrencyRateItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefCurrencyRateItem>)sessionBean.getReference().getRefAbstractList(RefCurrencyRateItem.REF_CODE, dateExcel);
                    }
                    for (RefCurrencyRateItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                rs.getRateAgency(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Рейтинговое агентство");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 5000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals(RefSubjectTypeItem.REF_CODE)) {
                    List<RefSubjectTypeItem> items;
                    if(allRecords) {
                        items = (List<RefSubjectTypeItem>)sessionBean.getReference().getRefAbstractByFilterList(RefSubjectTypeItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefSubjectTypeItem>)sessionBean.getReference().getRefAbstractList(RefSubjectTypeItem.REF_CODE, dateExcel);
                    }
                    for (RefSubjectTypeItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        boolean isAdvance = rs.getIsAdvance() == null ? false : rs.getIsAdvance();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getShortNameRu(),
                                rs.getShortNameKz(),
                                rs.getShortNameEn(),
                                isAdvance,
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast

                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Краткое наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Краткое наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Краткое наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Подотчетное лицо");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals("ref_org_type") || refContainer.getCode().equals("ref_type_deals") || refContainer.getCode().equals("ref_type_activity") || refContainer.getCode().equals("ref_npa")
                        || refContainer.getCode().equals("ref_finrep_indic") || refContainer.getCode().equals("ref_degree_relation") || refContainer.getCode().equals("ref_sign_related") || refContainer.getCode().equals("ref_type_risk")) {
                    SimpleReference simpleReference = new SimpleReference();
                    simpleReference.setRefCode(refContainer.getCode());
                    List<SimpleReference> items = sessionBean.getReference().getRefSimpleListByParams(dateExcel, simpleReference);
                    for (SimpleReference rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast

                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals(RefRespondentItem.REF_CODE)) {
                    List<RefRespondentItem> items;
                    if(allRecords) {
                        items = (List<RefRespondentItem>) sessionBean.getReference().getRefAbstractByFilterList(RefRespondentItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefRespondentItem>) sessionBean.getReference().getRefAbstractList(RefRespondentItem.REF_CODE, dateExcel);
                    }
                    for (RefRespondentItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date bDateLic = rs.getDateBeginLic();
                        Date eDateLic = rs.getDateEndLic();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getPersonName(),
                                rs.getRefSubjectTypeName(),
                                rs.getRefDepartmentName(),
                                /*rs.getNokbdbCode(),
                                rs.getMainBuh(),
                                bDateLic,
                                eDateLic,
                                rs.getStopLic(),
                                rs.getVidActivity(),*/
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Тип субъекта");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Филиал");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    /*cell = row2.createCell(++col);
                    cell.setCellValue("Код не банк орг.");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Главный бухгалтер");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия лицензии");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия лицензии");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Причина прекращения лицензии");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Вид деятельности");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);*/

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals(RefDocTypeItem.REF_CODE)) {
                    List<RefDocTypeItem> items;
                    if(allRecords) {
                        items = (List<RefDocTypeItem>)sessionBean.getReference().getRefAbstractByFilterList(RefDocTypeItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefDocTypeItem>)sessionBean.getReference().getRefAbstractList(RefDocTypeItem.REF_CODE, dateExcel);
                    }
                    for (RefDocTypeItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        boolean isIdentification = rs.getIsIdentification() == null ? false : rs.getIsIdentification();
                        boolean isOrganization = rs.getIsOrganizationDoc() == null ? false : rs.getIsOrganizationDoc();
                        boolean isPerson = rs.getIsPersonDoc() == null ? false : rs.getIsPersonDoc();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                isIdentification,
                                isOrganization,
                                isPerson,
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast

                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Идентификационный");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 5000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Организационный");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 5000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Персональный");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 5000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals(RefDocumentItem.REF_CODE)) {
                    List<RefDocumentItem> items;
                    if(allRecords) {
                        items = (List<RefDocumentItem>)sessionBean.getReference().getRefAbstractByFilterList(RefDocumentItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefDocumentItem>)sessionBean.getReference().getRefAbstractList(RefDocumentItem.REF_CODE, dateExcel);
                    }
                    for (RefDocumentItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getDocTypeName(),
                                rs.getRespondentName(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Тип документа");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Респондент");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals(RefIssuersItem.REF_CODE)) {
                    List<RefIssuersItem> items;
                    if(allRecords) {
                        items = (List<RefIssuersItem>)sessionBean.getReference().getRefAbstractByFilterList(RefIssuersItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefIssuersItem>)sessionBean.getReference().getRefAbstractList(RefIssuersItem.REF_CODE, dateExcel);
                    }
                    for (RefIssuersItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        boolean isState = rs.getIsState() == null ? false : rs.getIsState();
                        boolean isRezident = rs.getIsResident() == null ? false : rs.getIsResident();
                        boolean isFromKase = rs.getIsFromKase() == null ? false : rs.getIsFromKase();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getSignName(),
                                isState,
                                isRezident,
                                rs.getListingEstimation(),
                                rs.getRatingEstimation(),
                                isFromKase,
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 5000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Признак Эмитента");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Признак государственной");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2100);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Признак резидента");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2100);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Листинг");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2100);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Рейтинг");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2100);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Признак загрузки с KASE");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2100);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals(RefSecuritiesItem.REF_CODE)) {
                    List<RefSecuritiesItem> items;
                    if(allRecords) {
                        items = (List<RefSecuritiesItem>)sessionBean.getReference().getRefAbstractByFilterList(RefSecuritiesItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefSecuritiesItem>)sessionBean.getReference().getRefAbstractList(RefSecuritiesItem.REF_CODE, dateExcel);
                    }
                    for (RefSecuritiesItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        boolean isRezident = rs.getIsResident() == null ? false : rs.getIsResident();
                        boolean isState = rs.getIsState() == null ? false : rs.getIsState();
                        Date circulDate = rs.getCirculDate();
                        Date maturityDate = rs.getMaturityDate();
                        boolean isBondProgram = rs.getIsBondProgram() == null ? false : rs.getIsBondProgram();
                        boolean isGarant = rs.getIsGarant() == null ? false : rs.getIsGarant();
                        boolean isPermit = rs.getIsPermit() == null ? false : rs.getIsPermit();
                        boolean isFromKase = rs.getIsFromKase() == null ? false : rs.getIsFromKase();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getIssuerName(),
                                rs.getSignCode(),
                                rs.getSignName(),
                                isRezident,
                                isState,
                                rs.getNominalValue(),
                                rs.getNin(),
                                circulDate,
                                maturityDate,
                                rs.getSecurityCnt(),
                                rs.getVarietyCode(),
                                rs.getVarietyName(),
                                rs.getTypeCode(),
                                rs.getTypeName(),
                                rs.getCurrencyCode(),
                                rs.getCurrencyName(),
                                rs.getIssueVolume(),
                                rs.getCirculPeriod(),
                                rs.getListingEstimation(),
                                rs.getRatingEstimation(),
                                isBondProgram,
                                rs.getBondProgramVolume(),
                                rs.getBondPrgCnt(),
                                isGarant,
                                rs.getGarant(),
                                isPermit,
                                isFromKase,
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4200);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Эмитент");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Признак эмитента(Код)");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Признак эмитента");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Нерезидент");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Государственная ЦБ");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Номинальная стоимость");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("НИН");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия обращения");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата погашения/аннулирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Количество ЦБ");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Вид ЦБ(код)");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Вид ЦБ");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Тип ЦБ(код)");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Тип ЦБ");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Валюта номинала(код)");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Валюта номинала");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Объем выпуска");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Срок обращения");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Листинг");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Рейтинг");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Признак облигационной программы");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Объем облигационной программы");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Количество выпуском в облигационной программе");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Признак гарантированных ЦБ");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Гарант");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Разрешены к приобретению за счет ПА");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Признак загрузки с KASE");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals(RefVidOperItem.REF_CODE)) {
                    List<RefVidOperItem> items;
                    if(allRecords) {
                        items = (List<RefVidOperItem>)sessionBean.getReference().getRefAbstractByFilterList(RefVidOperItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefVidOperItem>)sessionBean.getReference().getRefAbstractList(RefVidOperItem.REF_CODE, dateExcel);
                    }
                    for (RefVidOperItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                }else if (refContainer.getCode().equals(RefBranchItem.REF_CODE)) {
                    List<RefBranchItem> items;
                    if(allRecords) {
                        items = (List<RefBranchItem>)sessionBean.getReference().getRefAbstractByFilterList(RefBranchItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefBranchItem>)sessionBean.getReference().getRefAbstractList(RefBranchItem.REF_CODE, dateExcel);
                    }
                    for (RefBranchItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals("ref_branch_old")) {
                    SimpleReference simpleReference = new SimpleReference();
                    simpleReference.setRefCode(refContainer.getCode());
                    List<SimpleReference> items = sessionBean.getReference().getRefSimpleListByParams(dateExcel, simpleReference);

                    for (SimpleReference rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                }else if (refContainer.getCode().equals(RefListingEstimationItem.REF_CODE)) {
                    List<RefListingEstimationItem> items;
                    if(allRecords) {
                        items = (List<RefListingEstimationItem>)sessionBean.getReference().getRefAbstractByFilterList(RefListingEstimationItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefListingEstimationItem>)sessionBean.getReference().getRefAbstractList(RefListingEstimationItem.REF_CODE, dateExcel);
                    }
                    for (RefListingEstimationItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getPriority(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 1200);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Листинг");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Приоритет");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals(RefRatingEstimationItem.REF_CODE)) {
                    List<RefRatingEstimationItem> items;
                    if(allRecords) {
                        items = (List<RefRatingEstimationItem>)sessionBean.getReference().getRefAbstractByFilterList(RefRatingEstimationItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefRatingEstimationItem>)sessionBean.getReference().getRefAbstractList(RefRatingEstimationItem.REF_CODE, dateExcel);
                    }
                    for (RefRatingEstimationItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getPriority(),
                                rs.getRatingCategoryName(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 1200);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Оценка");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Приоритет");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Категория рейтинга");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals(RefRatingCategoryItem.REF_CODE)) {
                    List<RefRatingCategoryItem> items;
                    if(allRecords) {
                        items = (List<RefRatingCategoryItem>)sessionBean.getReference().getRefAbstractByFilterList(RefRatingCategoryItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefRatingCategoryItem>)sessionBean.getReference().getRefAbstractList(RefRatingCategoryItem.REF_CODE, dateExcel);
                    }

                    for (RefRatingCategoryItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getCode(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 1200);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Категория");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals("ref_request_type")) {
                    SimpleReference simpleReference = new SimpleReference();
                    simpleReference.setRefCode(refContainer.getCode());
                    List<SimpleReference> items = sessionBean.getReference().getRefSimpleListByParams(dateExcel, simpleReference);
                    for (SimpleReference rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals("ref_request_way")) {
                    SimpleReference simpleReference = new SimpleReference();
                    simpleReference.setRefCode(refContainer.getCode());
                    List<SimpleReference> items = sessionBean.getReference().getRefSimpleListByParams(dateExcel, simpleReference);
                    for (SimpleReference rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals("ref_market_kind")) {
                    SimpleReference simpleReference = new SimpleReference();
                    simpleReference.setRefCode(refContainer.getCode());
                    List<SimpleReference> items = sessionBean.getReference().getRefSimpleListByParams(dateExcel, simpleReference);
                    for (SimpleReference rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);

                } else if (refContainer.getCode().equals("ref_category")) {
                    SimpleReference simpleReference = new SimpleReference();
                    simpleReference.setRefCode(refContainer.getCode());
                    List<SimpleReference> items = sessionBean.getReference().getRefSimpleListByParams(dateExcel, simpleReference);
                    for (SimpleReference rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals("ref_subcategory")) {
                    SimpleReference simpleReference = new SimpleReference();
                    simpleReference.setRefCode(refContainer.getCode());
                    List<SimpleReference> items = sessionBean.getReference().getRefSimpleListByParams(dateExcel, simpleReference);
                    for (SimpleReference rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals("ref_account_type")) {
                    SimpleReference simpleReference = new SimpleReference();
                    simpleReference.setRefCode(refContainer.getCode());
                    List<SimpleReference> items = sessionBean.getReference().getRefSimpleListByParams(dateExcel, simpleReference);
                    for (SimpleReference rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals("ref_subaccount_type")) {
                    SimpleReference simpleReference = new SimpleReference();
                    simpleReference.setRefCode(refContainer.getCode());
                    List<SimpleReference> items = sessionBean.getReference().getRefSimpleListByParams(dateExcel, simpleReference);
                    for (SimpleReference rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals("ref_type_holder_acc")) {
                    SimpleReference simpleReference = new SimpleReference();
                    simpleReference.setRefCode(refContainer.getCode());
                    List<SimpleReference> items = sessionBean.getReference().getRefSimpleListByParams(dateExcel, simpleReference);
                    for (SimpleReference rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals("ref_request_feature")) {
                    SimpleReference simpleReference = new SimpleReference();
                    simpleReference.setRefCode(refContainer.getCode());
                    List<SimpleReference> items = sessionBean.getReference().getRefSimpleListByParams(dateExcel, simpleReference);
                    for (SimpleReference rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals("ref_request_sts")) {
                    SimpleReference simpleReference = new SimpleReference();
                    simpleReference.setRefCode(refContainer.getCode());
                    List<SimpleReference> items = sessionBean.getReference().getRefSimpleListByParams(dateExcel, simpleReference);
                    for (SimpleReference rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals("ref_repo_kind")) {
                    SimpleReference simpleReference = new SimpleReference();
                    simpleReference.setRefCode(refContainer.getCode());
                    List<SimpleReference> items = sessionBean.getReference().getRefSimpleListByParams(dateExcel, simpleReference);
                    for (SimpleReference rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals("ref_market_type")) {
                    SimpleReference simpleReference = new SimpleReference();
                    simpleReference.setRefCode(refContainer.getCode());
                    List<SimpleReference> items = sessionBean.getReference().getRefSimpleListByParams(dateExcel, simpleReference);
                    for (SimpleReference rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals("ref_trad_method")) {
                    SimpleReference simpleReference = new SimpleReference();
                    simpleReference.setRefCode(refContainer.getCode());
                    List<SimpleReference> items = sessionBean.getReference().getRefSimpleListByParams(dateExcel, simpleReference);
                    for (SimpleReference rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals("ref_oper_type")) {
                    SimpleReference simpleReference = new SimpleReference();
                    simpleReference.setRefCode(refContainer.getCode());
                    List<SimpleReference> items = sessionBean.getReference().getRefSimpleListByParams(dateExcel, simpleReference);
                    for (SimpleReference rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals("ref_deal_sts")) {
                    SimpleReference simpleReference = new SimpleReference();
                    simpleReference.setRefCode(refContainer.getCode());
                    List<SimpleReference> items = sessionBean.getReference().getRefSimpleListByParams(dateExcel, simpleReference);
                    for (SimpleReference rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 3000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals(RefMrpItem.REF_CODE)) {
                    List<RefMrpItem> items;
                    if(allRecords) {
                        items = (List<RefMrpItem>)sessionBean.getReference().getRefAbstractByFilterList(RefMrpItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefMrpItem>)sessionBean.getReference().getRefAbstractList(RefMrpItem.REF_CODE, dateExcel);
                    }
                    for (RefMrpItem rs : items) {
                        String id = String.valueOf(rs.getId());
                        String recId = String.valueOf(rs.getRecId());
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getValue(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Значение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                }else if (refContainer.getCode().equals(RefMfoRegItem.REF_CODE)) {
                    List<RefMfoRegItem> items;
                    RefMfoRegItem item = new RefMfoRegItem();
                    item.setRefDepartmentId(depId);
                    items = (List<RefMfoRegItem>)sessionBean.getReference().getRefAbstractByFilterList(RefMfoRegItem.REF_CODE, item, dateExcel);

                    for (RefMfoRegItem rs : items) {
                        String id = String.valueOf(rs.getId());
                        String recId = String.valueOf(rs.getRecId());
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getDepName(),
                                rs.getLpName(),
                                rs.getBase(),
                                rs.getNumReg(),
                                rs.getFioManager(),
                                rs.getAddress(),
                                rs.getContactDetails(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Филиал НБРК");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование юридического лица");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Основание");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("№ п/п в реестре Территориальных филиалов НБРК");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("ФИО руководителя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Адрес");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Номер телефона, факс, адрес электронной почты, интернет- ресурс (при наличии)");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                }else if (refContainer.getCode().equals(RefDealBAItem.REF_CODE)) {
                    List<RefDealBAItem> items;
                    if(allRecords) {
                        items = (List<RefDealBAItem>)sessionBean.getReference().getRefAbstractByFilterList(RefDealBAItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefDealBAItem>)sessionBean.getReference().getRefAbstractList(RefDealBAItem.REF_CODE, dateExcel);
                    }
                    for (RefDealBAItem rs : items) {
                        String id = String.valueOf(rs.getId());
                        String recId = String.valueOf(rs.getRecId());
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getShortNameRu(),
                                rs.getShortNameKz(),
                                rs.getShortNameEn(),
                                rs.getNumAcc(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Краткое наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Краткое наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Краткое наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Номер счета");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals(RefNpaItem.REF_CODE)) {
                    List<RefNpaItem> items;
                    if(allRecords) {
                        items = (List<RefNpaItem>)sessionBean.getReference().getRefAbstractByFilterList(RefNpaItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefNpaItem>)sessionBean.getReference().getRefAbstractList(RefNpaItem.REF_CODE, dateExcel);
                    }
                    for (RefNpaItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getShortNameRu(),
                                rs.getShortNameKz(),
                                rs.getShortNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Краткое наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Краткое наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Краткое наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals(RefTypeActivityItem.REF_CODE)) {
                    List<RefTypeActivityItem> items;
                    if(allRecords) {
                        items = (List<RefTypeActivityItem>)sessionBean.getReference().getRefAbstractByFilterList(RefTypeActivityItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefTypeActivityItem>)sessionBean.getReference().getRefAbstractList(RefTypeActivityItem.REF_CODE, dateExcel);
                    }
                    for (RefTypeActivityItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                } else if (refContainer.getCode().equals(RefBasisofControlItem.REF_CODE)) {
                    List<RefBasisofControlItem> items;
                    if(allRecords) {
                        items = (List<RefBasisofControlItem>)sessionBean.getReference().getRefAbstractByFilterList(RefBasisofControlItem.REF_CODE, null, dateExcel);
                    }else{
                        items = (List<RefBasisofControlItem>)sessionBean.getReference().getRefAbstractList(RefBasisofControlItem.REF_CODE, dateExcel);
                    }
                    for (RefBasisofControlItem rs : items) {
                        Long id = rs.getId();
                        Long recId = rs.getRecId();
                        Date bDate = rs.getBeginDate();
                        Date eDate = rs.getEndDate();
                        Date datlast = rs.getDatlast();

                        data.put(String.valueOf(++i), new Object[]{
                                id,
                                recId,
                                rs.getCode(),
                                bDate,
                                eDate,
                                rs.getNameRu(),
                                rs.getNameKz(),
                                rs.getNameEn(),
                                rs.getBasisControl(),
                                rs.getUserName(),
                                rs.getUserLocation(),
                                datlast
                        });
                    }

                    Row row2 = sheet.createRow(3);

                    Cell cell = row2.createCell(col);
                    cell.setCellValue("Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Rec_Id");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Код");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 2000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата начала действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата окончания действия");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4500);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на русском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на казахском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Наименование на английском");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Основание контроля");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 8000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Имя пользователя");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Местоположение");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 4000);

                    cell = row2.createCell(++col);
                    cell.setCellValue("Дата последнего редактирования");
                    cell.setCellStyle(stylColumn);
                    sheet.setColumnWidth(col, 6000);
                }

                if (data != null) {
                    Set<String> keyset = data.keySet();
                    int rownum = 4;
                    for (String key : keyset) {
                        Row rowValue = sheet.createRow(rownum++);
                        Object[] objArr = data.get(key);
                        int cellnum = 0;
                        for (Object obj : objArr) {
                            Cell cellValue = rowValue.createCell(cellnum++);
                            if (obj instanceof java.sql.Date) {
                                cellValue.setCellValue(Convert.getDateStringFromDate((java.sql.Date) obj));
                                cellValue.setCellStyle(styleDate);
                            } else if (obj instanceof java.sql.Timestamp) {
                                cellValue.setCellValue(Convert.getDateTimeStringFromDateRus((java.sql.Timestamp) obj));
                                cellValue.setCellStyle(styleDate);
                            } else if (obj instanceof Boolean) {
                                cellValue.setCellValue((Boolean) obj ? "Да" : "Нет");
                                cellValue.setCellStyle(styleDate);
                            } else if (obj instanceof String) {
                                cellValue.setCellValue((String) obj);
                                cellValue.setCellStyle(style);
                            } else if (obj instanceof Integer) {
                                cellValue.setCellValue((Integer) obj);
                                //style.setDataFormat(format.getFormat("#,##0"));
                                cellValue.setCellStyle(style);
                            } else if (obj instanceof Long) {
                                cellValue.setCellValue((Long) obj);
                                style.setDataFormat(format.getFormat("#,##0"));
                                cellValue.setCellStyle(style);
                            } else {
                                cellValue.setCellStyle(style);
                            }
                        }
                    }
                }

                sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, col));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                workbook.write(baos);
                xls = baos.toByteArray();
                xlsMap.put(refContainer.getCode() + ":" + refContainer.getName(), xls);
            }

            if(xlsMap.size() == 1) {
                for (Map.Entry <String,byte[]> entry : xlsMap.entrySet()){
                    ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
                    String fileName = getRefNameFromValue(entry.getKey());
                    nameObject = fileName;
                    fileName = Convert.getContentDespositionFilename(fileName + ".xls", externalContext.getRequestHeaderMap());
                    applicationBean.putFileContentToResponseOutputStream(entry.getValue(), "application/vnd.ms-excel", fileName);
                    codeObject = getRefCodeFromValue(entry.getKey());
                }
            }else if(xlsMap.size() > 1) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos);
                zos.setEncoding("CP866");
                ZipArchiveEntry zea;
                InputStream is;
                int len;
                String name;
                String code;
                for (Map.Entry <String,byte[]> entry : xlsMap.entrySet()){
                    code = getRefCodeFromValue(entry.getKey());
                    name = getRefNameFromValue(entry.getKey());

                    zea = new ZipArchiveEntry(name + ".xls");
                    zos.putArchiveEntry(zea);

                    is = new ByteArrayInputStream(entry.getValue());

                    while ((len = is.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    is.close();

                    nameObject = nameObject == "" ? name : nameObject + ", " + name;
                    codeObject = codeObject == "" ? code : codeObject + ", " + code;
                }
                zos.closeArchiveEntry();
                zos.close();
                applicationBean.putFileContentToResponseOutputStream(bos.toByteArray(), "application/zip", "reference_" + Convert.dateFormatCompact.format(dateExcel) + ".zip");
            }
        } catch (Exception e) {
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка формирования Excel файла!", e.getMessage()));
            return;
        }

        try {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setCodeObject(codeObject);
            auditEvent.setNameObject(nameObject);
            auditEvent.setIdKindEvent(5L);
            auditEvent.setDateEvent(dateExcel);
            auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
            auditEvent.setDateIn(dateExcel);
            auditEvent.setRecId(null);
            auditEvent.setUserId(userId);
            auditEvent.setUserLocation(userLocation);
            sessionBean.getPersistence().insertAuditEvent(auditEvent);
        } catch (Exception e) {
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
        }
    }

    // region новый метод, выгрузки в Excel - не выгружает больше справочники по несколько раз !
    /*
    public void unloadExcel() {
        String nameObject = "";
        String codeObject = "";
        boolean haveError = false;
        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();
        try {
            if(selectedRef.size() == 0) {
                throw new Exception("Ошибка при выгрузки в Excel!");
            }

            Map <String, byte[]> xlsMap = new HashMap<String,byte[]>();
            byte[] xls = null;

            for (RefItem refContainer : selectedRef) {
                FileWrapper fileWrapper = sessionBean.getReference().referenceToExcelFile(refContainer, allRecords, dateExcel, loadBank);

                xls = fileWrapper.getBytes();
                xlsMap.put(refContainer.getCode() + ":" + refContainer.getName(), xls);
            }

            if(xlsMap.size() == 1) {
                for (Map.Entry <String,byte[]> entry : xlsMap.entrySet()){
                    ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
                    String fileName = getRefNameFromValue(entry.getKey());
                    nameObject = fileName;
                    fileName = Convert.getContentDespositionFilename(fileName + ".xlsx", externalContext.getRequestHeaderMap());
                    applicationBean.putFileContentToResponseOutputStream(entry.getValue(), "application/vnd.ms-excel", fileName);
                    codeObject = getRefCodeFromValue(entry.getKey());
                }
            }else if(xlsMap.size() > 1) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ZipArchiveOutputStream zos = new ZipArchiveOutputStream(bos);
                zos.setEncoding("CP866");
                ZipArchiveEntry zea;
                InputStream is;
                int len;
                String name;
                String code;
                byte[] buffer = new byte[1024];
                for (Map.Entry <String,byte[]> entry : xlsMap.entrySet()){
                    code = getRefCodeFromValue(entry.getKey());
                    name = getRefNameFromValue(entry.getKey());

                    zea = new ZipArchiveEntry(name + ".xlsx");
                    zos.putArchiveEntry(zea);

                    is = new ByteArrayInputStream(entry.getValue());

                    while ((len = is.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    is.close();

                    nameObject = nameObject == "" ? name : nameObject + ", " + name;
                    codeObject = codeObject == "" ? code : codeObject + ", " + code;
                }
                zos.closeArchiveEntry();
                zos.close();
                applicationBean.putFileContentToResponseOutputStream(bos.toByteArray(), "application/zip", "reference_" + Convert.dateFormatCompact.format(dateExcel) + ".zip");
            }
        } catch (Exception e) {
            haveError = true;
        } finally {
            if(haveError) {
                applicationBean.putFileContentToResponseOutputStream(null, "application/zip", "");
            }else{
                try {
                    AuditEvent auditEvent = new AuditEvent();
                    auditEvent.setCodeObject(codeObject);
                    auditEvent.setNameObject(nameObject);
                    auditEvent.setIdKindEvent((long) 5);
                    auditEvent.setDateEvent(date);
                    auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
                    auditEvent.setDateIn(dateExcel);
                    auditEvent.setRecId(null);
                    auditEvent.setUserId(userId);
                    auditEvent.setUserLocation(userLocation);
                    sessionBean.getPersistence().insertAuditEvent(auditEvent);
                } catch (Exception e) {
                    RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
                    RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
                }
            }
        }
    }*/
    // endregion

    // endregion

    // region Выгрузка в XML
    private void createRefAbstractDataFile(AbstractReference abstractReference, Boolean withName, XMLStreamWriter xsw) throws XMLStreamException {

        if (abstractReference.getRecId() != null)
            xsw.writeAttribute("rec_id", String.valueOf(abstractReference.getRecId()));

        if (abstractReference.getCode() != null && !abstractReference.getCode().trim().isEmpty())
            xsw.writeAttribute("code", abstractReference.getCode());

        if (abstractReference.getBeginDate() != null)
            xsw.writeAttribute("begin_date", Convert.getDateStringFromDate(abstractReference.getBeginDate()));

        if (abstractReference.getEndDate() != null)
            xsw.writeAttribute("end_date", Convert.getDateStringFromDate(abstractReference.getEndDate()));

        if (withName) {
            if (abstractReference.getNameKz() != null && !abstractReference.getNameKz().trim().isEmpty())
                xsw.writeAttribute("name_kz", abstractReference.getNameKz());

            if (abstractReference.getNameRu() != null && !abstractReference.getNameRu().trim().isEmpty())
                xsw.writeAttribute("name_ru", abstractReference.getNameRu());

            if (abstractReference.getNameEn() != null && !abstractReference.getNameEn().trim().isEmpty())
                xsw.writeAttribute("name_en", abstractReference.getNameEn());

        }
    }

    private String createRefDocTypeDataFile(Date date, String refName) {
        String result = null;
        RefDocTypeList = (List<RefDocTypeItem>)sessionBean.getReference().getRefAbstractList(RefDocTypeItem.REF_CODE, date);

        if (RefDocTypeList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefDocTypeItem refItem : RefDocTypeList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
//                    if(refItem.getIsIdentification() != null)
//                        xsw.writeAttribute("is_identification", String.valueOf(refItem.getIsIdentification()));
//                    if(refItem.getIsOrganizationDoc() != null)
//                        xsw.writeAttribute("is_organization_doc", String.valueOf(refItem.getIsOrganizationDoc()));
//                    if(refItem.getIsPersonDoc() != null)
//                        xsw.writeAttribute("is_person_doc", String.valueOf(refItem.getIsPersonDoc()));
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }

                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefSubjectTypeDataFile(Date date, String refName) {
        String result = null;
        RefSubjectTypeList = (List<RefSubjectTypeItem>)sessionBean.getReference().getRefAbstractList(RefSubjectTypeItem.REF_CODE, date);

        if (RefSubjectTypeList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefSubjectTypeItem refItem: RefSubjectTypeList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
//                    if(refItem.getRepPerDurMonths() != null)
//                        xsw.writeAttribute("rep_per_dur_months", String.valueOf(refItem.getRepPerDurMonths()));
//                    if(refItem.getIsAdvance() != null)
//                        xsw.writeAttribute("is_advance", String.valueOf(refItem.getIsAdvance()));
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefTypeBusEntityDataFile(Date date, String refName) {
        String result = null;
        RefTypeBusEntityList = (List<RefTypeBusEntityItem>)sessionBean.getReference().getRefAbstractList(refName, date);

        if (RefTypeBusEntityList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefTypeBusEntityItem refItem: RefTypeBusEntityList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefCountryDataFile(Date date, String refName) {
        String result = null;
        RefCountryList = (List<RefCountryItem>)sessionBean.getReference().getRefAbstractList(refName, date);

        if (RefCountryList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefCountryItem refItem: RefCountryList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefRegionDataFile(Date date, String refName) {
        String result = null;
        RefRegionList = (List<RefRegionItem>)sessionBean.getReference().getRefAbstractList(RefRegionItem.REF_CODE, date);

        if (RefRegionList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefRegionItem  refItem : RefRegionList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
//                    if(refItem.getOblastName() != null)
//                        xsw.writeAttribute("oblast_name", String.valueOf(refItem.getOblastName()));
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefManagersDataFile(Date date, String refName) {
        String result = null;
        RefManagersList = (List<RefManagersItem>)sessionBean.getReference().getRefAbstractList(refName, date);

        if (RefManagersList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefManagersItem  refItem : RefManagersList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, false, xsw);
                    if(refItem.getFm() != null)
                        xsw.writeAttribute("fm", String.valueOf(refItem.getFm()));
                    if(refItem.getNm() != null)
                        xsw.writeAttribute("nm", String.valueOf(refItem.getNm()));
                    if(refItem.getFt() != null)
                        xsw.writeAttribute("ft", String.valueOf(refItem.getFt()));
                    if(refItem.getFioKz() != null)
                        xsw.writeAttribute("fio_kz", String.valueOf(refItem.getFioKz()));
                    if(refItem.getFioEn() != null)
                        xsw.writeAttribute("fio_en", String.valueOf(refItem.getFioEn()));
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefLegalPersonDataFile(Date date, String refName) {
        String result = null;
        RefLegalPersonList = (List<RefLegalPersonItem>) sessionBean.getReference().getRefAbstractList(refName, date);

        if (RefLegalPersonList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefLegalPersonItem  refItem : RefLegalPersonList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    if(refItem.getIdn() != null)
                        xsw.writeAttribute("idn", refItem.getIdn());
                    if(refItem.getShortNameKz() != null)
                        xsw.writeAttribute("short_name_kz", refItem.getShortNameKz());
                    if(refItem.getShortNameRu() != null)
                        xsw.writeAttribute("short_name_ru", refItem.getShortNameRu());
                    if(refItem.getShortNameEn() != null)
                        xsw.writeAttribute("short_name_en", refItem.getShortNameEn());
                    if(refItem.getRefOrgTypeRecId() != null)
                        xsw.writeAttribute("ref_subject_type", String.valueOf(refItem.getRefOrgTypeRecId()));
                    if(refItem.getRefTypeBusEntityRecId() != null)
                        xsw.writeAttribute("ref_type_bus_entity", String.valueOf(refItem.getRefTypeBusEntityRecId()));
                    if(refItem.getRefCountryRecId() != null)
                        xsw.writeAttribute("ref_country", String.valueOf(refItem.getRefCountryRecId()));
                    /*if(refItem.getRefRegion() != null)
                        xsw.writeAttribute("ref_region", String.valueOf(refItem.getRefRegion()));
                    if(refItem.getPostalIndex() != null)
                        xsw.writeAttribute("postal_index", String.valueOf(refItem.getPostalIndex()));*/
                    /*if(refItem.getAddressStreet() != null)
                        xsw.writeAttribute("address_street", refItem.getAddressStreet());*/
                    /*if(refItem.getAddressNumHouse() != null)
                        xsw.writeAttribute("address_num_house", refItem.getAddressNumHouse());*/
                    /*if(refItem.getManager() != null)
                        xsw.writeAttribute("manager", refItem.getManager());*/
                    if(refItem.getLegalAddress() != null)
                        xsw.writeAttribute("legal_address", refItem.getLegalAddress());
                    if(refItem.getFactAddress() != null)
                        xsw.writeAttribute("fact_address", refItem.getFactAddress());
                    if(refItem.getNote() != null)
                        xsw.writeAttribute("note", refItem.getNote());
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefRespondentDataFile(Date date, String refName) {
        String result = null;
        RefRespondentList = (List<RefRespondentItem>)sessionBean.getReference().getRefAbstractList(RefRespondentItem.REF_CODE, date);

        if (RefRespondentList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefRespondentItem  refItem : RefRespondentList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, false, xsw);
                    if (refItem.getPersonRecId() != null)
                        xsw.writeAttribute("ref_unionpersons", String.valueOf(refItem.getUnionPersonsId()));
//                    if(refItem.getNokbdbCode() != null)
//                        xsw.writeAttribute("nokbdb_code", refItem.getNokbdbCode());
//                    if(refItem.getMainBuh() != null)
//                        xsw.writeAttribute("main_buh", refItem.getMainBuh());
//                    if(refItem.getDateBeginLic() != null)
//                        xsw.writeAttribute("date_begin_lic", Convert.getDateStringFromDate(refItem.getDateBeginLic()));
//                    if(refItem.getDateEndLic() != null)
//                        xsw.writeAttribute("date_end_lic", Convert.getDateStringFromDate(refItem.getDateEndLic()));
//                    if(refItem.getStopLic() != null)
//                        xsw.writeAttribute("stop_lic", refItem.getStopLic());
//                    if(refItem.getVidActivity() != null)
//                        xsw.writeAttribute("vid_activity", refItem.getVidActivity());
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefDocumentDataFile(Date date, String refName) {
        String result = null;
        RefDocumentList = (List<RefDocumentItem>)sessionBean.getReference().getRefAbstractList(RefDocumentItem.REF_CODE, date);

        if (RefDocumentList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefDocumentItem  refItem : RefDocumentList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    if(refItem.getRefDocTypeRecId() != null)
                        xsw.writeAttribute("ref_doc_type", String.valueOf(refItem.getRefDocTypeRecId()));
                    if(refItem.getRefRespondentRecId() != null)
                        xsw.writeAttribute("ref_respondent", String.valueOf(refItem.getRefRespondentRecId()));
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefRateAgencyDataFile(Date date, String refName) {
        String result = null;
        RefRateAgencyList = (List<RefRateAgencyItem>)sessionBean.getReference().getRefAbstractList(RefRateAgencyItem.REF_CODE, date);

        if (RefRateAgencyList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefRateAgencyItem  refItem : RefRateAgencyList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefCurrencyRateDataFile(Date date, String refName) {
        String result = null;
        RefCurrencyRateList = (List<RefCurrencyRateItem>)sessionBean.getReference().getRefAbstractList(RefCurrencyRateItem.REF_CODE, date);

        if (RefCurrencyRateList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefCurrencyRateItem  refItem : RefCurrencyRateList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    if(refItem.getRefRateAgencyRecId() != null)
                        xsw.writeAttribute("ref_rate_agency", String.valueOf(refItem.getRefRateAgencyRecId()));
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefCurrencyDataFile(Date date, String refName) {
        String result = null;
        RefCurrencyList = (List<RefCurrencyItem>)sessionBean.getReference().getRefAbstractList(RefCurrencyItem.REF_CODE, date);

        if (RefCurrencyList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefCurrencyItem  refItem : RefCurrencyList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
//                    if(refItem.getMinorUnits() != null)
//                        xsw.writeAttribute("minor_units", String.valueOf(refItem.getMinorUnits()));
//                    if(refItem.getRate() != null)
//                        xsw.writeAttribute("rate", String.valueOf(refItem.getRate()));
                    if(refItem.getRefCurrencyRateRecId() != null)
                        xsw.writeAttribute("ref_currency_rate", String.valueOf(refItem.getRefCurrencyRateRecId()));
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefPostDataFile(Date date, String refName) {
        String result = null;
        RefPostList = (List<RefPostItem>) sessionBean.getReference().getRefAbstractList(refName, date);

        if (RefPostList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefPostItem  refItem : RefPostList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    if(refItem.getTypePostId() != null)
                        xsw.writeAttribute("type_post", String.valueOf(refItem.getTypePostId()));
                    if(refItem.getIsActivity() != null)
                        xsw.writeAttribute("is_activity", String.valueOf(refItem.getIsActivity()));
                    if(refItem.getIsMainRuk() != null)
                        xsw.writeAttribute("is_main_ruk", String.valueOf(refItem.getIsMainRuk()));
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefPersonDataFile(Date date, String refName) {
        String result = null;
        RefPersonList = (List<RefPersonItem>) sessionBean.getReference().getRefAbstractList(refName, date);

        if (RefPersonList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefPersonItem  refItem : RefPersonList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, false, xsw);
                    if(refItem.getIdn() != null)
                        xsw.writeAttribute("idn", String.valueOf(refItem.getIdn()));
                    if(refItem.getFm() != null)
                        xsw.writeAttribute("fm", String.valueOf(refItem.getFm()));
                    if(refItem.getNm() != null)
                        xsw.writeAttribute("nm", String.valueOf(refItem.getNm()));
                    if(refItem.getFt() != null)
                        xsw.writeAttribute("ft", String.valueOf(refItem.getFt()));
                    if(refItem.getFioKz() != null)
                        xsw.writeAttribute("fio_kz", String.valueOf(refItem.getFioKz()));
                    if(refItem.getFioEn() != null)
                        xsw.writeAttribute("fio_en", String.valueOf(refItem.getFioEn()));
                    if(refItem.getRefCountryRecId() != null)
                        xsw.writeAttribute("ref_country", String.valueOf(refItem.getRefCountryRecId()));
                    if(refItem.getPhoneWork() != null)
                        xsw.writeAttribute("phone_work", String.valueOf(refItem.getPhoneWork()));
                    if(refItem.getFax() != null)
                        xsw.writeAttribute("fax", String.valueOf(refItem.getFax()));
                    if(refItem.getAddressWork() != null)
                        xsw.writeAttribute("address_work", String.valueOf(refItem.getAddressWork()));
                    if(refItem.getNote() != null)
                        xsw.writeAttribute("note", String.valueOf(refItem.getNote()));
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefRequirementDataFile(Date date, String refName) {
        String result = null;
        RefRequirementList = (List<RefRequirementItem>)sessionBean.getReference().getRefAbstractList(refName, date);

        if (RefRequirementList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefRequirementItem  refItem : RefRequirementList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefTypeProvideDataFile(Date date, String refName) {
        String result = null;
        RefTypeProvideList = (List<RefTypeProvideItem>) sessionBean.getReference().getRefAbstractList(refName, date);

        if (RefTypeProvideList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefTypeProvideItem  refItem : RefTypeProvideList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefTransTypeDataFile(Date date, String refName) {
        String result = null;
        RefTransTypeList = (List<RefTransTypeItem>)sessionBean.getReference().getRefAbstractList(refName, date);

        if (RefTransTypeList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefTransTypeItem  refItem : RefTransTypeList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    if(refItem.getKindOfActivity() != null)
                        xsw.writeAttribute("kind_of_activity", refItem.getKindOfActivity());
                    if(refItem.getShortNameRu() != null)
                        xsw.writeAttribute("short_name", refItem.getShortNameRu());
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefBalanceAccountDataFile(Date date, String refName) {
        String result = null;
        RefBalanceAccList = sessionBean.getReference().getRefBalanceAccLastRecord(date);

        if (RefBalanceAccList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefBalanceAccItem  refItem : RefBalanceAccList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    if(refItem.getLevelCode() != null)
                        xsw.writeAttribute("level_code", String.valueOf(refItem.getLevelCode()));
                    if(refItem.getParentCode() != null)
                        xsw.writeAttribute("parent_code", String.valueOf(refItem.getParentCode()));
                    if(refItem.getParentCode() != null)
                        xsw.writeAttribute("parent_code", String.valueOf(refItem.getParentCode()));
                    if(refItem.getShortNameKz() != null)
                        xsw.writeAttribute("short_name_kz", refItem.getShortNameKz());
                    if(refItem.getShortNameRu() != null)
                        xsw.writeAttribute("short_name_ru", refItem.getShortNameRu());
                    if(refItem.getShortNameEn() != null)
                        xsw.writeAttribute("short_name_en", refItem.getShortNameEn());
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefConnOrgDataFile(Date date, String refName) {
        String result = null;
        RefConnOrgList = (List<RefConnOrgItem>)sessionBean.getReference().getRefAbstractList(RefConnOrgItem.REF_CODE, date);

        if (RefConnOrgList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefConnOrgItem  refItem : RefConnOrgList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    if (refItem.getShortNameRu() != null)
                        xsw.writeAttribute("short_name", String.valueOf(refItem.getShortNameRu()));
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefDepartmentDataFile(Date date, String refName) {
        String result = null;
        RefDepartmentList = (List<RefDepartmentItem>)sessionBean.getReference().getRefAbstractList(RefDepartmentItem.REF_CODE, date);

        if (RefDepartmentList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefDepartmentItem  refItem : RefDepartmentList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefBankDataFile(Date date, String refName) {
        String result = null;
        RefBankList = (List<RefBankItem>)sessionBean.getReference().getRefAbstractList(RefBankItem.REF_CODE, date);

        if (RefBankList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefBankItem  refItem : RefBankList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    /*if(refItem.getBic() != null)
                        xsw.writeAttribute("bic", refItem.getBic());
                    if(refItem.getBicHead() != null)
                        xsw.writeAttribute("bic_head", refItem.getBicHead());
                    if(refItem.getBicNbrk() != null)
                        xsw.writeAttribute("bic_nbrk", refItem.getBicNbrk());*/
                    if(refItem.getIdn() != null)
                        xsw.writeAttribute("idn", refItem.getIdn());
                    if(refItem.getPostAddress() != null)
                        xsw.writeAttribute("post_address", refItem.getPostAddress());
                    if(refItem.getRefCountryId() != null)
                        xsw.writeAttribute("ref_country", String.valueOf(refItem.getRefCountryId()));
                    if(refItem.getIsLoad() != null)
                        xsw.writeAttribute("is_load", String.valueOf(refItem.getIsLoad()));
                    if(refItem.getIsLoad() != null)
                        xsw.writeAttribute("is_non_rezident", String.valueOf(refItem.getIsNonRezident()));
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefIssuersDataFile(Date date, String refName) {
        String result = null;
        RefIssuersList = (List<RefIssuersItem>)sessionBean.getReference().getRefAbstractList(RefIssuersItem.REF_CODE, date);

        if (RefIssuersList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefIssuersItem  refItem : RefIssuersList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    if(refItem.getSignName() != null)
                        xsw.writeAttribute("sign_name", String.valueOf(refItem.getSignName()));
                    if(refItem.getIsState() != null)
                        xsw.writeAttribute("is_state", String.valueOf(refItem.getIsState()));
                    if (refItem.getIsResident() != null)
                        xsw.writeAttribute("is_resident", String.valueOf(refItem.getIsResident()));
                    if (refItem.getListingEstimation() != null)
                        xsw.writeAttribute("listing_estimation", String.valueOf(refItem.getListingEstimation()));
                    if (refItem.getRatingEstimation() != null)
                        xsw.writeAttribute("rating_estimation", String.valueOf(refItem.getRatingEstimation()));
                    if (refItem.getIsFromKase() != null)
                        xsw.writeAttribute("is_from_kase", String.valueOf(refItem.getIsFromKase()));
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefSecuritiesDataFile(Date date, String refName) {
        String result = null;
        RefSecuritiesList = (List<RefSecuritiesItem>)sessionBean.getReference().getRefAbstractList(RefSecuritiesItem.REF_CODE, date);

        if (RefSecuritiesList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefSecuritiesItem  refItem : RefSecuritiesList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    /*if(refItem.getsIssuer() != null)
                        xsw.writeAttribute("s_issuer", String.valueOf(refItem.getsIssuer()));*/
                    if(refItem.getIssuerName() != null)
                        xsw.writeAttribute("issuer_name", String.valueOf(refItem.getIssuerName()));
                    /*if(refItem.getSgIssuerSign() != null)
                        xsw.writeAttribute("s_g_issuer_sign", String.valueOf(refItem.getSgIssuerSign()));*/
                    if(refItem.getSignCode() != null)
                        xsw.writeAttribute("sign_code", String.valueOf(refItem.getSignCode()));
                    if(refItem.getSignName() != null)
                        xsw.writeAttribute("sign_name", String.valueOf(refItem.getSignName()));
                    if(refItem.getIsResident() != null)
                        xsw.writeAttribute("is_resident", String.valueOf(refItem.getIsResident()));
                    if(refItem.getIsState() != null)
                        xsw.writeAttribute("is_state", String.valueOf(refItem.getIsState()));
                    if(refItem.getNominalValue() != null)
                        xsw.writeAttribute("nominal_value", String.valueOf(refItem.getNominalValue()));
                    if(refItem.getNin() != null)
                        xsw.writeAttribute("nin", String.valueOf(refItem.getNin()));
                    if(refItem.getCirculDate() != null)
                        xsw.writeAttribute("circul_date", Convert.getDateStringFromDate(refItem.getCirculDate()));
                    if(refItem.getMaturityDate() != null)
                        xsw.writeAttribute("maturity_date", Convert.getDateStringFromDate(refItem.getMaturityDate()));
                    if(refItem.getSecurityCnt() != null)
                        xsw.writeAttribute("security_cnt", String.valueOf(refItem.getSecurityCnt()));
                    /*if(refItem.getSgSecurityVariety() != null)
                        xsw.writeAttribute("s_g_security_variety", String.valueOf(refItem.getSgSecurityVariety()));*/
                    if(refItem.getVarietyCode() != null)
                        xsw.writeAttribute("variety_code", String.valueOf(refItem.getVarietyCode()));
                    if(refItem.getVarietyName() != null)
                        xsw.writeAttribute("variety_name", String.valueOf(refItem.getVarietyName()));
                    /*if(refItem.getSgSecurityType() != null)
                        xsw.writeAttribute("s_g_security_type", String.valueOf(refItem.getSgSecurityType()));*/
                    if(refItem.getTypeCode() != null)
                        xsw.writeAttribute("type_code", String.valueOf(refItem.getTypeCode()));
                    if(refItem.getTypeName() != null)
                        xsw.writeAttribute("type_name", String.valueOf(refItem.getTypeName()));
                    if(refItem.getNominalCurrency() != null)
                        xsw.writeAttribute("nominal_currency", String.valueOf(refItem.getNominalCurrency()));
                    if(refItem.getCurrencyCode() != null)
                        xsw.writeAttribute("currency_code", String.valueOf(refItem.getCurrencyCode()));
                    if(refItem.getCurrencyName() != null)
                        xsw.writeAttribute("currency_name", String.valueOf(refItem.getCurrencyName()));
                    if(refItem.getIssueVolume() != null)
                        xsw.writeAttribute("issue_volume", String.valueOf(refItem.getIssueVolume()));
                    if(refItem.getCirculPeriod() != null)
                        xsw.writeAttribute("circul_period", String.valueOf(refItem.getCirculPeriod()));
                    if(refItem.getListingEstimation() != null)
                        xsw.writeAttribute("listing_estimation", String.valueOf(refItem.getListingEstimation()));
                    if(refItem.getRatingEstimation() != null)
                        xsw.writeAttribute("rating_estimation", String.valueOf(refItem.getRatingEstimation()));
                    if(refItem.getIsBondProgram() != null)
                        xsw.writeAttribute("is_bond_program", String.valueOf(refItem.getIsBondProgram()));
                    if(refItem.getBondProgramVolume() != null)
                        xsw.writeAttribute("bond_program_volume", String.valueOf(refItem.getBondProgramVolume()));
                    if(refItem.getBondPrgCnt() != null)
                        xsw.writeAttribute("bond_prg_cnt", String.valueOf(refItem.getBondPrgCnt()));
                    if(refItem.getIsGarant() != null)
                        xsw.writeAttribute("is_garant", String.valueOf(refItem.getIsGarant()));
                    if(refItem.getGarant() != null)
                        xsw.writeAttribute("garant", String.valueOf(refItem.getGarant()));
                    if(refItem.getIsPermit() != null)
                        xsw.writeAttribute("is_permit", String.valueOf(refItem.getIsPermit()));
                    if(refItem.getIsFromKase() != null)
                        xsw.writeAttribute("is_from_kase", String.valueOf(refItem.getIsFromKase()));
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefVidOperDataFile(Date date, String refName) {
        String result = null;
        RefVidOperList = (List<RefVidOperItem>)sessionBean.getReference().getRefAbstractList(RefVidOperItem.REF_CODE, date);

        if (RefVidOperList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefVidOperItem  refItem : RefVidOperList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefBranchDataFile(Date date, String refName) {
        String result = null;
        RefBranchList = (List<RefBranchItem>)sessionBean.getReference().getRefAbstractList(RefBranchItem.REF_CODE, date);

        if (RefBranchList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefBranchItem  refItem : RefBranchList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefListingEstimationDataFile(Date date, String refName) {
        String result = null;
        RefListingEstimationList = (List<RefListingEstimationItem>)sessionBean.getReference().getRefAbstractList(RefListingEstimationItem.REF_CODE, date);

        if (RefListingEstimationList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefListingEstimationItem refItem : RefListingEstimationList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    /*if(refItem.getPriority() != null)
                        xsw.writeAttribute("priority", String.valueOf(refItem.getPriority()));*/
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefRatingEstimationDataFile(Date date, String refName) {
        String result = null;
        RefRatingEstimationList = (List<RefRatingEstimationItem>)sessionBean.getReference().getRefAbstractList(RefRatingEstimationItem.REF_CODE, date);

        if (RefRatingEstimationList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefRatingEstimationItem refItem : RefRatingEstimationList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    if(refItem.getRefRatingCategory() != null)
                        xsw.writeAttribute("ref_rating_category", String.valueOf(refItem.getRefRatingCategoryRecId()));
                    /*if(refItem.getPriority() != null)
                        xsw.writeAttribute("priority", String.valueOf(refItem.getPriority()));*/
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefRatingCategoryDataFile(Date date, String refName) {
        String result = null;
        RefRatingCategoryList = (List<RefRatingCategoryItem>)sessionBean.getReference().getRefAbstractList(RefRatingCategoryItem.REF_CODE, date);

        if (RefRatingCategoryList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefRatingCategoryItem refItem : RefRatingCategoryList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createRefMrpDataFile(Date date, String refName) {
        String result = null;
        RefMrpList = (List<RefMrpItem>)sessionBean.getReference().getRefAbstractList(RefMrpItem.REF_CODE, date);

        if (RefMrpList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (RefMrpItem refItem : RefMrpList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    if(refItem.getValue() != null)
                        xsw.writeAttribute("value", String.valueOf(refItem.getValue()));
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }

    private String createSimpleRefDataFile(Date date, String refName) {
        String result = null;
        SimpleReference simpleReference = new SimpleReference();
        simpleReference.setRefCode(refName);
        RefSimpleRefList = sessionBean.getReference().getRefSimpleListByParams(date, simpleReference);

        if (RefSimpleRefList != null) {
            try {
                StringWriter sw = new StringWriter();
                XMLStreamWriter xsw = XMLOutputFactory.newInstance().createXMLStreamWriter(sw);
                xsw.writeStartDocument(); xsw.writeCharacters("\n");
                xsw.writeStartElement("reference");
                xsw.writeAttribute("name", refName);
                xsw.writeAttribute("date", Convert.getDateStringFromDate(date));
                xsw.writeCharacters("\n");

                xsw.writeCharacters("\t");
                xsw.writeStartElement("items");
                xsw.writeCharacters("\n");

                for (SimpleReference refItem : RefSimpleRefList) {
                    xsw.writeCharacters("\t\t");
                    xsw.writeStartElement("item");
                    createRefAbstractDataFile(refItem, true, xsw);
                    xsw.writeEndElement();//item
                    xsw.writeCharacters("\n");
                }
                xsw.writeCharacters("\t"); xsw.writeEndElement();//items
                xsw.writeCharacters("\n");
                xsw.writeEndElement();// reference
                xsw.writeEndDocument();
                xsw.close();
                result = sw.toString();
            }catch (XMLStreamException e){
                e.printStackTrace();
                return null;
            }
        }
        return result;
    }
    // endregion

    // region Getters and Setters
    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public List<RefItem> getRefList() {
        return RefList;
    }

    public void setRefList(List<RefItem> refList) {
        RefList = refList;
    }

    public List<RefItem> getSelectedRef() {
        return selectedRef;
    }

    public void setSelectedRef(List<RefItem> selectedRef) {
        this.selectedRef = selectedRef;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Boolean getAllRecords() {
        return allRecords;
    }

    public void setAllRecords(Boolean allRecords) {
        this.allRecords = allRecords;
    }

    public Date getDateExcel() {
        return dateExcel;
    }

    public void setDateExcel(Date dateExcel) {
        this.dateExcel = dateExcel;
    }

    public Boolean getLoadBank() {
        return loadBank;
    }

    public void setLoadBank(Boolean loadBank) {
        this.loadBank = loadBank;
    }

    public Boolean getRenderForBank() {
        return renderForBank;
    }

    public void setRenderForBank(Boolean renderForBank) {
        this.renderForBank = renderForBank;
    }

    public Boolean getRenderForMfoReg() {
        return renderForMfoReg;
    }

    public void setRenderForMfoReg(Boolean renderForMfoReg) {
        this.renderForMfoReg = renderForMfoReg;
    }

    public long getDepId() {
        return depId;
    }

    public void setDepId(long depId) {
        this.depId = depId;
    }

    // endregion
}
