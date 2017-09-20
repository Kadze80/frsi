package mb;

import entities.*;
import entities.Reference.LdmRefIssuers;
import entities.Reference.LdmRefSecurities;
import org.apache.log4j.Logger;
import org.primefaces.component.tabview.TabView;

import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;
import util.Convert;
import util.OracleException;
import util.Validators;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.portlet.PortletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by Ayupov.Bakhtiyar on 16.04.2015.
 */
@ManagedBean
@SessionScoped
public class RefBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    @ManagedProperty(value = "#{applicationBean}")
    private ApplicationBean applicationBean;
    @ManagedProperty(value = "#{sessionBean}")
    private SessionBean sessionBean;
    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    private String refCode;
    private String referenceCode;
    private boolean validationVisible;
    private TabView tabView;
    private String activeTabIndex;
    private String resultMessage;
    private String modalName;
    private List<Result> resultList;

    private Long userId;
    private String userName;
    private String userLocation;

    private Date datlast;
    private String editType;
    private String typeMM;
    private String editKind;
    private Boolean isVisibleEditKnd;
    private Boolean isLoad;
    private Boolean isVisibleBankLp;
    private Boolean isVisibleInsurLp;
    private Long entityId;
    private Boolean canEdit;
    private boolean disableIdn;
    private boolean disableInvIdn;
    private boolean disableBtnMajorMem;
    private boolean disableBtnMajorMemView;
    private boolean searchAllVer;
    private String typeUnionPersons;

    private List<Image> imageList;
    private Image curImage;
    private String loadSysName;

    // Общий список
    private List<RefItem> RefList;
    private List<RefItem> selectedRef = new ArrayList<RefItem>();
    private RefItem filterRefMainItem = new RefItem();
    private List<RefElements> refKndList;

    //region переменные справочников

    // Справочник должностей
    private List<RefPostItem> RefPostList;
    private List<RefPostItem> RefPostHstList;
    private List<RefPostItem> RefPostVerList;
    private RefPostItem selectedRefPost;
    private RefPostItem selectedRefPostVer;
    private boolean RefPostVisible;
    private RefPostItem refPostItem;
    private List<RefElements> typePostList;
    private RefPostItem filterRefPostItem = new RefPostItem();

    // Справочник физических лиц
    private List<RefPersonItem> RefPersonList;
    private List<RefPersonItem> RefPersonHstList;
    private List<RefPersonItem> RefPersonVerList;
    private RefPersonItem selectedRefPerson;
    private RefPersonItem selectedRefPersonVer;
    private boolean RefPersonVisible;
    private RefPersonItem refPersonItem;
    private RefPersonItem filterRefPersonItem = new RefPersonItem();

    // Справочник юридических лиц
    private List<RefLegalPersonItem> RefLegalPersonList;
    private List<RefLegalPersonItem> RefLegalPersonHstList;
    private List<RefLegalPersonItem> RefLegalPersonVerList;
    private RefLegalPersonItem selectedRefLegalPerson;
    private RefLegalPersonItem selectedSearchRefLegalPerson;
    private RefLegalPersonItem selectedRefLegalPersonVer;
    private boolean RefLegalPersonVisible;
    private RefLegalPersonItem refLegalPersonItem;
    //    private RefLegalPersonItem refSimpleLPItem;
    private RefLegalPersonItem filterRefLegalPersonItem = new RefLegalPersonItem();
    private String idnForSearch;
    private String nameForSearch;
    private List<RefLegalPersonItem> RefLPForSearchList;

    private String idnForSearchU;
    private String nameForSearchU;
    private List<RefUnionPersonItem> refUnionForSearchList;
    private RefUnionPersonItem selectedSearchRefUnionPerson;

    // Справочник стран
    private List<RefCountryItem> RefCountryList;
    private List<RefCountryItem> RefCountryVerList;
    private RefCountryItem selectedRefCountry;
    private RefCountryItem selectedRefCountryVer;
    private boolean RefCountryVisible;
    private RefCountryItem refCountryItem;
    private RefCountryItem filterRefCountryItem = new RefCountryItem();

    // Справочник работников
    private List<RefManagersItem> RefManagersList;
    private List<RefManagersItem> RefManagersHstList;
    private List<RefManagersItem> RefManagersVerList;
    private RefManagersItem selectedRefManagers;
    private RefManagersItem selectedRefManagersVer;
    private boolean RefManagersVisible;
    private RefManagersItem refManagersItem;
    private RefManagersItem filterRefManagersItem = new RefManagersItem();

    // Справочник организационно-правовых форм
    private List<RefTypeBusEntityItem> RefTypeBusEntityList;
    private List<RefTypeBusEntityItem> RefTypeBusEntityHstList;
    private List<RefTypeBusEntityItem> RefTypeBusEntityVerList;
    private RefTypeBusEntityItem selectedRefTypeBusEntity;
    private RefTypeBusEntityItem selectedRefTypeBusEntityVer;
    private boolean RefTypeBusEntityVisible;
    private RefTypeBusEntityItem refTypeBusEntityItem;
    private RefTypeBusEntityItem filterRefTypeBusEntityItem = new RefTypeBusEntityItem();

    // Справочник городов и регионов
    private List<RefRegionItem> RefRegionList;
    private List<RefRegionItem> RefRegionVerList;
    private RefRegionItem selectedRefRegion;
    private RefRegionItem selectedRefRegionVer;
    private boolean RefRegionVisible;
    private RefRegionItem refRegionItem;
    private RefRegionItem filterRefRegionItem = new RefRegionItem();

    // Справочник требований и обязательств
    private List<RefRequirementItem> RefRequirementList;
    private List<RefRequirementItem> RefRequirementHstList;
    private List<RefRequirementItem> RefRequirementVerList;
    private RefRequirementItem selectedRefRequirement;
    private RefRequirementItem selectedRefRequirementVer;
    private boolean RefRequirementVisible;
    private RefRequirementItem refRequirementItem;
    private RefRequirementItem filterRefRequirementItem = new RefRequirementItem();

    // Справочник видов обеспечения
    private List<RefTypeProvideItem> RefTypeProvideList;
    private List<RefTypeProvideItem> RefTypeProvideHstList;
    private List<RefTypeProvideItem> RefTypeProvideVerList;
    private RefTypeProvideItem selectedRefTypeProvide;
    private RefTypeProvideItem selectedRefTypeProvideVer;
    private boolean RefTypeProvideVisible;
    private RefTypeProvideItem refTypeProvideItem;
    private RefTypeProvideItem filterRefTypeProvideItem = new RefTypeProvideItem();

    // Справочник типов сделок
    private List<RefTransTypeItem> RefTransTypeList;
    private List<RefTransTypeItem> RefTransTypeHstList;
    private List<RefTransTypeItem> RefTransTypeVerList;
    private RefTransTypeItem selectedRefTransType;
    private RefTransTypeItem selectedRefTransTypeVer;
    private boolean RefTransTypeVisible;
    private RefTransTypeItem refTransTypeItem;
    private RefTransTypeItem filterRefTransTypeItem = new RefTransTypeItem();

    // Справочник балансовых счетов для отчетов о сделках
    private TreeNode balanceAccTree;
    private TreeNode selectedBalanceAcc;
    private List<RefBalanceAccItem> RefBalanceAccList;
    private List<RefBalanceAccItem> RefBalanceAccHstList;
    private List<RefBalanceAccItem> RefBalanceAccVerList;
    private RefBalanceAccItem selectedRefBalanceAcc;
    private RefBalanceAccItem selectedRefBalanceAccVer;
    private boolean RefBalanceAccVisible;
    private RefBalanceAccItem refBalanceAccItem;
    private RefBalanceAccItem filterRefBalanceAccItem = new RefBalanceAccItem();

    // Справочник признаков связанности с подотчетной организацией особыми отношениями
    private List<RefConnOrgItem> RefConnOrgList;
    private List<RefConnOrgItem> RefConnOrgHstList;
    private List<RefConnOrgItem> RefConnOrgVerList;
    private RefConnOrgItem selectedRefConnOrg;
    private RefConnOrgItem selectedRefConnOrgVer;
    private boolean RefConnOrgVisible;
    private RefConnOrgItem refConnOrgItem;
    private RefConnOrgItem filterRefConnOrgItem = new RefConnOrgItem();

    // Справочник подразделений НБ РК
    private List<RefDepartmentItem> RefDepartmentList;
    private List<RefDepartmentItem> RefDepartmentVerList;
    private RefDepartmentItem selectedRefDepartment;
    private RefDepartmentItem selectedRefDepartmentVer;
    private boolean RefDepartmentVisible;
    private RefDepartmentItem refDepartmentItem;
    private RefDepartmentItem filterRefDepartmentItem = new RefDepartmentItem();
    private List<SimpleReference> deptTypeList;

    // Справочник банков второго уровня
    private List<RefBankItem> RefBankList;
    private List<RefBankItem> RefBankHstList;
    private List<RefBankItem> RefBankVerList;
    private RefBankItem selectedRefBank;
    private RefBankItem selectedRefBankVer;
    private boolean RefBankVisible;
    private RefBankItem refBankItem;
    private RefBankItem filterRefBankItem = new RefBankItem();

    // Справочник рейтинговых агентств
    private List<RefRateAgencyItem> RefRateAgencyList;
    private List<RefRateAgencyItem> RefRateAgencyVerList;
    private RefRateAgencyItem selectedRefRateAgency;
    private RefRateAgencyItem selectedRefRateAgencyVer;
    private boolean RefRateAgencyVisible;
    private RefRateAgencyItem refRateAgencyItem;
    private RefRateAgencyItem filterRefRateAgencyItem = new RefRateAgencyItem();

    // Справочник валют
    private List<RefCurrencyItem> RefCurrencyList;
    private List<RefCurrencyItem> RefCurrencyVerList;
    private RefCurrencyItem selectedRefCurrency;
    private RefCurrencyItem selectedRefCurrencyVer;
    private boolean RefCurrencyVisible;
    private RefCurrencyItem refCurrencyItem;
    private RefCurrencyItem filterRefCurrencyItem = new RefCurrencyItem();

    // Справочник рейтингов валют
    private List<RefCurrencyRateItem> RefCurrencyRateList;
    private List<RefCurrencyRateItem> RefCurrencyRateVerList;
    private RefCurrencyRateItem selectedRefCurrencyRate;
    private RefCurrencyRateItem selectedRefCurrencyRateVer;
    private boolean RefCurrencyRateVisible;
    private RefCurrencyRateItem refCurrencyRateItem;
    private RefCurrencyRateItem filterRefCurrencyRateItem = new RefCurrencyRateItem();

    // Справочник типов субъектов
    private List<RefSubjectTypeItem> RefSubjectTypeList;
    private List<RefSubjectTypeItem> RefSubjectTypeHstList;
    private List<RefSubjectTypeItem> RefSubjectTypeVerList;
    private RefSubjectTypeItem selectedRefSubjectType;
    private RefSubjectTypeItem selectedRefSubjectTypeVer;
    private boolean RefSubjectTypeVisible;
    private RefSubjectTypeItem refSubjectTypeItem;
    private RefSubjectTypeItem filterRefSubjectTypeItem  = new RefSubjectTypeItem();
    private List<RefElements> repPerDurMonthsList;
    private List<SubjectTypePost> subjectTypePostList;

    // Справочник типов организаций
    private List<SimpleReference> RefOrgTypeList;
    private List<SimpleReference> RefOrgTypeHstList;
    private List<SimpleReference> RefOrgTypeVerList;
    private SimpleReference selectedRefOrgType;
    private SimpleReference selectedRefOrgTypeVer;
    private boolean RefOrgTypeVisible;
    private SimpleReference refOrgTypeItem;
    private SimpleReference filterRefOrgTypeItem = new SimpleReference();

    // Справочник респондентов(подотчетных организаций)
    private List<RefRespondentItem> RefRespondentList;
    private List<RefRespondentItem> RefRespondentHstList;
    private List<RefRespondentItem> RefRespondentVerList;
    private RefRespondentItem selectedRefRespondent;
    private RefRespondentItem selectedRefRespondentVer;
    private boolean RefRespondentVisible;
    private RefRespondentItem refRespondentItem;
    private RefRespondentItem filterRefRespondentItem = new RefRespondentItem();
    private List<RefRespondentItem> respondentResultList;
    private RefRespondentItem selectedResultResp;
    private List<RespondentWarrant> respondentWarrantList;
    private RespondentWarrant selectedRespWarrant;

    // Справочник типов документов
    private List<RefDocTypeItem> RefDocTypeList;
    private List<RefDocTypeItem> RefDocTypeHstList;
    private List<RefDocTypeItem> RefDocTypeVerList;
    private RefDocTypeItem selectedRefDocType;
    private RefDocTypeItem selectedRefDocTypeVer;
    private boolean RefDocTypeVisible;
    private RefDocTypeItem refDocTypeItem;
    private RefDocTypeItem filterRefDocTypeItem = new RefDocTypeItem();

    // Справочник документов
    private List<RefDocumentItem> RefDocumentList;
    private List<RefDocumentItem> RefDocumentHstList;
    private List<RefDocumentItem> RefDocumentVerList;
    private RefDocumentItem selectedRefDocument;
    private RefDocumentItem selectedRefDocumentVer;
    private boolean RefDocumentVisible;
    private RefDocumentItem refDocumentItem;
    private RefDocumentItem filterRefDocumentItem = new RefDocumentItem();

    // Справочник Эмитентов
    private List<RefIssuersItem> RefIssuersList;
    private LazyDataModel<RefIssuersItem> ldmRefIssuerListItem;
    private List<RefIssuersItem> RefIssuersVerList;

    private RefIssuersItem selectedRefIssuers;
    private RefIssuersItem selectedRefIssuersVer;
    private boolean RefIssuersVisible;
    private RefIssuersItem refIssuersItem;
    private RefIssuersItem filterRefIssuersItem = new RefIssuersItem();

    // Справочник Ценных бумаг
    private List<RefSecuritiesItem> RefSecuritiesList;
    private LazyDataModel<RefSecuritiesItem> ldmRefSecListItem;
    private List<RefSecuritiesItem> RefSecuritiesVerList;
    private RefSecuritiesItem selectedRefSecurities;
    private RefSecuritiesItem selectedRefSecuritiesVer;
    private boolean RefSecuritiesVisible;
    private RefSecuritiesItem refSecuritiesItem;
    private RefSecuritiesItem filterRefSecuritiesItem = new RefSecuritiesItem();

    // Справочник видов операций
    private List<RefVidOperItem> RefVidOperList;
    private List<RefVidOperItem> RefVidOperHstList;
    private List<RefVidOperItem> RefVidOperVerList;
    private RefVidOperItem selectedRefVidOper;
    private RefVidOperItem selectedRefVidOperVer;
    private boolean RefVidOperVisible;
    private RefVidOperItem refVidOperItem;
    private RefVidOperItem filterRefVidOperItem = new RefVidOperItem();

    // Справочник отраслей
    private List<RefBranchItem> RefBranchList;
    private List<RefBranchItem> RefBranchHstList;
    private List<RefBranchItem> RefBranchVerList;
    private RefBranchItem selectedRefBranch;
    private RefBranchItem selectedRefBranchVer;
    private boolean RefBranchVisible;
    private RefBranchItem refBranchItem;
    private RefBranchItem filterRefBranchItem = new RefBranchItem();

    // Справочник отраслей (старый)
    private List<SimpleReference> RefBranchOldList;
    private List<SimpleReference> RefBranchOldVerList;
    private SimpleReference selectedRefBranchOld;
    private SimpleReference selectedRefBranchOldVer;
    private boolean RefBranchOldVisible;
    private SimpleReference refBranchOldItem;
    private SimpleReference filterRefBranchOldItem = new SimpleReference();

    // Справочник Листинговых оценок
    private List<RefListingEstimationItem> RefListingEstimationList;
    private List<RefListingEstimationItem> RefListingEstimationVerList;
    private RefListingEstimationItem selectedRefListingEstimation;
    private RefListingEstimationItem selectedRefListingEstimationVer;
    private boolean RefListingEstimationVisible;
    private RefListingEstimationItem refListingEstimationItem;
    private RefListingEstimationItem filterRefListingEstimationItem = new RefListingEstimationItem();

    // Справочник Рейтинговых оценок
    private List<RefRatingEstimationItem> RefRatingEstimationList;
    private List<RefRatingEstimationItem> RefRatingEstimationVerList;
    private RefRatingEstimationItem selectedRefRatingEstimation;
    private RefRatingEstimationItem selectedRefRatingEstimationVer;
    private boolean RefRatingEstimationVisible;
    private RefRatingEstimationItem refRatingEstimationItem;
    private RefRatingEstimationItem filterRefRatingEstimationItem = new RefRatingEstimationItem();

    // Справочник Категорий рейтинговых оценок
    private List<RefRatingCategoryItem> RefRatingCategoryList;
    private List<RefRatingCategoryItem> RefRatingCategoryVerList;
    private RefRatingCategoryItem selectedRefRatingCategory;
    private RefRatingCategoryItem selectedRefRatingCategoryVer;
    private boolean RefRatingCategoryVisible;
    private RefRatingCategoryItem refRatingCategoryItem;
    private RefRatingCategoryItem filterRefRatingCategoryItem = new RefRatingCategoryItem();

    // Справочник алгоритмов расчета сроков предоставления


    // region Справочники KASE
    /*// Справочник типов заявок (KASE)
    private List<SimpleReference> RefRequestTypeList;
    private List<SimpleReference> RefRequestTypeHstList;
    private List<SimpleReference> RefRequestTypeVerList;
    private SimpleReference selectedRefRequestType;
    private SimpleReference selectedRefRequestTypeVer;
    private boolean RefRequestTypeVisible;
    private SimpleReference refRequestTypeItem;
    private SimpleReference filterRefRequestTypeItem = new SimpleReference();

    // Справочник направления заявок (KASE)
    private List<SimpleReference> RefRequestWayList;
    private List<SimpleReference> RefRequestWayHstList;
    private List<SimpleReference> RefRequestWayVerList;
    private SimpleReference selectedRefRequestWay;
    private SimpleReference selectedRefRequestWayVer;
    private boolean RefRequestWayVisible;
    private SimpleReference refRequestWayItem;
    private SimpleReference filterRefRequestWayItem = new SimpleReference();

    // Справочник видов рынка (KASE)
    private List<SimpleReference> RefMarketKindList;
    private List<SimpleReference> RefMarketKindHstList;
    private List<SimpleReference> RefMarketKindVerList;
    private SimpleReference selectedRefMarketKind;
    private SimpleReference selectedRefMarketKindVer;
    private boolean RefMarketKindVisible;
    private SimpleReference refMarketKindItem;
    private SimpleReference filterRefMarketKindItem = new SimpleReference();

    // Справочник категорий (KASE)
    private List<SimpleReference> RefCategoryList;
    private List<SimpleReference> RefCategoryHstList;
    private List<SimpleReference> RefCategoryVerList;
    private SimpleReference selectedRefCategory;
    private SimpleReference selectedRefCategoryVer;
    private boolean RefCategoryVisible;
    private SimpleReference refCategoryItem;
    private SimpleReference filterRefCategoryItem = new SimpleReference();

    // Справочник подкатегорий (KASE)
    private List<SimpleReference> RefSubCategoryList;
    private List<SimpleReference> RefSubCategoryHstList;
    private List<SimpleReference> RefSubCategoryVerList;
    private SimpleReference selectedRefSubCategory;
    private SimpleReference selectedRefSubCategoryVer;
    private boolean RefSubCategoryVisible;
    private SimpleReference refSubCategoryItem;
    private SimpleReference filterRefSubCategoryItem = new SimpleReference();

    // Справочник типов счетов (KASE)
    private List<SimpleReference> RefAccountTypeList;
    private List<SimpleReference> RefAccountTypeHstList;
    private List<SimpleReference> RefAccountTypeVerList;
    private SimpleReference selectedRefAccountType;
    private SimpleReference selectedRefAccountTypeVer;
    private boolean RefAccountTypeVisible;
    private SimpleReference refAccountTypeItem;
    private SimpleReference filterRefAccountTypeItem = new SimpleReference();

    // Справочник типов субсчетов (KASE)
    private List<SimpleReference> RefSubAccountTypeList;
    private List<SimpleReference> RefSubAccountTypeHstList;
    private List<SimpleReference> RefSubAccountTypeVerList;
    private SimpleReference selectedRefSubAccountType;
    private SimpleReference selectedRefSubAccountTypeVer;
    private boolean RefSubAccountTypeVisible;
    private SimpleReference refSubAccountTypeItem;
    private SimpleReference filterRefSubAccountTypeItem = new SimpleReference();

    // Справочник типов владельцев счетов (KASE)
    private List<SimpleReference> RefTypeHolderAccList;
    private List<SimpleReference> RefTypeHolderAccHstList;
    private List<SimpleReference> RefTypeHolderAccVerList;
    private SimpleReference selectedRefTypeHolderAcc;
    private SimpleReference selectedRefTypeHolderAccVer;
    private boolean RefTypeHolderAccVisible;
    private SimpleReference refTypeHolderAccItem;
    private SimpleReference filterRefTypeHolderAccItem = new SimpleReference();

    // Справочник признаков заявки (KASE)
    private List<SimpleReference> RefRequestFeatureList;
    private List<SimpleReference> RefRequestFeatureHstList;
    private List<SimpleReference> RefRequestFeatureVerList;
    private SimpleReference selectedRefRequestFeature;
    private SimpleReference selectedRefRequestFeatureVer;
    private boolean RefRequestFeatureVisible;
    private SimpleReference refRequestFeatureItem;
    private SimpleReference filterRefRequestFeatureItem = new SimpleReference();

    // Справочник статусов заявки (KASE)
    private List<SimpleReference> RefRequestStsList;
    private List<SimpleReference> RefRequestStsHstList;
    private List<SimpleReference> RefRequestStsVerList;
    private SimpleReference selectedRefRequestSts;
    private SimpleReference selectedRefRequestStsVer;
    private boolean RefRequestStsVisible;
    private SimpleReference refRequestStsItem;
    private SimpleReference filterRefRequestStsItem = new SimpleReference();

    // Справочник видов РЕПО (KASE)
    private List<SimpleReference> RefRepoKindList;
    private List<SimpleReference> RefRepoKindHstList;
    private List<SimpleReference> RefRepoKindVerList;
    private SimpleReference selectedRefRepoKind;
    private SimpleReference selectedRefRepoKindVer;
    private boolean RefRepoKindVisible;
    private SimpleReference refRepoKindItem;
    private SimpleReference filterRefRepoKindItem = new SimpleReference();

    // Справочник типов рынка (KASE)
    private List<SimpleReference> RefMarketTypeList;
    private List<SimpleReference> RefMarketTypeHstList;
    private List<SimpleReference> RefMarketTypeVerList;
    private SimpleReference selectedRefMarketType;
    private SimpleReference selectedRefMarketTypeVer;
    private boolean RefMarketTypeVisible;
    private SimpleReference refMarketTypeItem;
    private SimpleReference filterRefMarketTypeItem = new SimpleReference();

    // Справочник методов торгов (KASE)
    private List<SimpleReference> RefTradMethodList;
    private List<SimpleReference> RefTradMethodHstList;
    private List<SimpleReference> RefTradMethodVerList;
    private SimpleReference selectedRefTradMethod;
    private SimpleReference selectedRefTradMethodVer;
    private boolean RefTradMethodVisible;
    private SimpleReference refTradMethodItem;
    private SimpleReference filterRefTradMethodItem = new SimpleReference();

    // Справочник типов операций (KASE)
    private List<SimpleReference> RefOperTypeList;
    private List<SimpleReference> RefOperTypeHstList;
    private List<SimpleReference> RefOperTypeVerList;
    private SimpleReference selectedRefOperType;
    private SimpleReference selectedRefOperTypeVer;
    private boolean RefOperTypeVisible;
    private SimpleReference refOperTypeItem;
    private SimpleReference filterRefOperTypeItem = new SimpleReference();

    // Справочник статусов сделки (KASE)
    private List<SimpleReference> RefDealStsList;
    private List<SimpleReference> RefDealStsHstList;
    private List<SimpleReference> RefDealStsVerList;
    private SimpleReference selectedRefDealSts;
    private SimpleReference selectedRefDealStsVer;
    private boolean RefDealStsVisible;
    private SimpleReference refDealStsItem;
    private SimpleReference filterRefDealStsItem = new SimpleReference();*/

    // endregion

    // Справочник МРП
    private List<RefMrpItem> RefMrpList;
    private List<RefMrpItem> RefMrpHstList;
    private List<RefMrpItem> RefMrpVerList;
    private RefMrpItem selectedRefMrp;
    private RefMrpItem selectedRefMrpVer;
    private boolean RefMrpVisible;
    private RefMrpItem refMrpItem;
    private List<RefElements> typeMrpList;
    private RefMrpItem filterRefMrpItem = new RefMrpItem();

    // Справочник Реестр МФО
    private List<RefMfoRegItem> RefMfoRegList;
    private List<RefMfoRegItem> RefMfoRegHstList;
    private List<RefMfoRegItem> RefMfoRegVerList;
    private RefMfoRegItem selectedRefMfoReg;
    private RefMfoRegItem selectedRefMfoRegVer;
    private boolean RefMfoRegVisible;
    private RefMfoRegItem refMfoRegItem;
    private RefMfoRegItem filterRefMfoRegItem = new RefMfoRegItem();

    // Справочник балансовых счетов для отчетов о сделках
    private List<RefDealBAItem> RefDealBAList;
    private List<RefDealBAItem> RefDealBAHstList;
    private List<RefDealBAItem> RefDealBAVerList;
    private RefDealBAItem selectedRefDealBA;
    private RefDealBAItem selectedRefDealBAVer;
    private boolean RefDealBAVisible;
    private RefDealBAItem refDealBAItem;
    private RefDealBAItem filterRefDealBAItem = new RefDealBAItem();

     // для КН
    // Справочник видов деятельности
    private List<RefTypeActivityItem> RefTypeActivityList;
    private List<RefTypeActivityItem> RefTypeActivityHstList;
    private List<RefTypeActivityItem> RefTypeActivityVerList;
    private RefTypeActivityItem selectedRefTypeActivity;
    private RefTypeActivityItem selectedRefTypeActivityVer;
    private boolean RefTypeActivityVisible;
    private RefTypeActivityItem refTypeActivityItem;
    private RefTypeActivityItem filterRefTypeActivityItem = new RefTypeActivityItem();

     // Справочник НПА
    private List<RefNpaItem> RefNpaList;
    private List<RefNpaItem> RefNpaHstList;
    private List<RefNpaItem> RefNpaVerList;
    private RefNpaItem selectedRefNpa;
    private RefNpaItem selectedRefNpaVer;
    private boolean RefNpaVisible;
    private RefNpaItem refNpaItem;
    private RefNpaItem filterRefNpaItem = new RefNpaItem();

    //Справочник выходных и праздничных дней
    private List<RefWkdHolidayItem> RefWkdHolidayList;
    private List<RefWkdHolidayItem> RefWkdHolidayHstList;
    private RefWkdHolidayItem selectedRefWkdHoliday;
    private List<RefWkdHolidayItem> RefWkdHolidayVerList;
    private RefWkdHolidayItem selectedRefWkdHolidayVer;
    private boolean RefWkdHolidayVisible;
    private RefWkdHolidayItem refWkdHolidayItem;
    private RefWkdHolidayItem filterRefWkdHolidayItem = new RefWkdHolidayItem();

    private List<HolidayItem> RefHolidayList;
    private HolidayItem selectedRefHoliday;
    private List<HolidayItem> RefHolidayVerList;
    private HolidayItem selectedRefHolidayVer;
    private HolidayItem holidayItem;
    private HolidayItem filterRefHolidayItem = new HolidayItem();
    private Date filterDateForFillRef;

    //Справочник показателей финансовой отчетности
    private List<SimpleReference> RefFinRepIndicList;
    private List<SimpleReference> RefFinRepIndicHstList;
    private List<SimpleReference> RefFinRepIndicVerList;
    private SimpleReference selectedRefFinRepIndic;
    private SimpleReference selectedRefFinRepIndicVer;
    private boolean RefFinRepIndicVisible;
    private SimpleReference refFinRepIndicItem;
    private SimpleReference filterRefFinRepIndicItem = new SimpleReference();

    //Справочник видов сделок
    private List<SimpleReference> RefTypeDealsList;
    private List<SimpleReference> RefTypeDealsHstList;
    private List<SimpleReference> RefTypeDealsVerList;
    private SimpleReference selectedRefTypeDeals;
    private SimpleReference selectedRefTypeDealsVer;
    private boolean RefTypeDealsVisible;
    private SimpleReference refTypeDealsItem;
    private SimpleReference filterRefTypeDealsItem = new SimpleReference();

     //Справочник степени родства
    private List<SimpleReference> RefDegreeRelationList;
    private List<SimpleReference> RefDegreeRelationHstList;
    private List<SimpleReference> RefDegreeRelationVerList;
    private SimpleReference selectedRefDegreeRelation;
    private SimpleReference selectedRefDegreeRelationVer;
    private boolean RefDegreeRelationVisible;
    private SimpleReference refDegreeRelationItem;
    private SimpleReference filterRefDegreeRelationItem = new SimpleReference();

     //Справочник признаков связанности
    private List<SimpleReference> RefSignRelatedList;
    private List<SimpleReference> RefSignRelatedHstList;
    private List<SimpleReference> RefSignRelatedVerList;
    private SimpleReference selectedRefSignRelated;
    private SimpleReference selectedRefSignRelatedVer;
    private boolean RefSignRelatedVisible;
    private SimpleReference refSignRelatedItem;
    private SimpleReference filterRefSignRelatedItem = new SimpleReference();

     //Справочник видов риска
    private List<SimpleReference> RefTypeRiskList;
    private List<SimpleReference> RefTypeRiskHstList;
    private List<SimpleReference> RefTypeRiskVerList;
    private SimpleReference selectedRefTypeRisk;
    private SimpleReference selectedRefTypeRiskVer;
    private boolean RefTypeRiskVisible;
    private SimpleReference refTypeRiskItem;
    private SimpleReference filterRefTypeRiskItem = new SimpleReference();

    // Справочник основания контроля
    private List<RefBasisofControlItem> RefBasisofControlList;
    private List<RefBasisofControlItem> RefBasisofControlHstList;
    private List<RefBasisofControlItem> RefBasisofControlVerList;
    private RefBasisofControlItem selectedRefBasisofControl;
    private RefBasisofControlItem selectedRefBasisofControlVer;
    private boolean RefBasisofControlVisible;
    private RefBasisofControlItem refBasisofControlItem;
    private RefBasisofControlItem filterRefBasisofControlItem  = new RefBasisofControlItem();

    // Справочник форм собственности
    private List<SimpleReference> RefOwnershipList;

    //Справочник отраслей страхования
    private List<SimpleReference> RefBranchInsurList;

    // Справочник страховых организаций
    private List<RefInsurOrgItem> RefInsurOrgList;
    private RefInsurOrgItem selectedRefInsurOrg;
    private RefInsurOrgItem selectedRefInsurOrgVer;
    private boolean RefInsurOrgVisible;
    private RefInsurOrgItem refInsurOrgItem;
    private RefInsurOrgItem filterRefInsurOrgItem  = new RefInsurOrgItem();

    //Справочник межсистемного контроля
    private List<SimpleReference> extSysList;
    private List<SimpleReference> refStatusList;

    private List<RefExtIndicatorItem> RefExtIndList;
    private List<RefExtIndicatorItem> RefExtIndHstList;
    private List<RefExtIndicatorItem> RefExtIndVerList;
    private RefExtIndicatorItem selectedRefExtInd;
    private RefExtIndicatorItem selectedRefExtIndVer;
    private boolean RefExtIndVisible;
    private RefExtIndicatorItem refExtIndItem;
    private RefExtIndicatorItem filterRefExtIndItem = new RefExtIndicatorItem();

    private List<RefExtIndicatorParam> RefExtParamList;
    private RefExtIndicatorParam selectedRefExtParam;
    private boolean RefExtParamVisible;
    private RefExtIndicatorParam refExtParamItem;
    private RefExtIndicatorParam filterRefExtParamItem = new RefExtIndicatorParam();

    // Справочник крупных участников
    private List<RefUnionPersonItem> RefUnionPersonsList;
    private RefUnionPersonItem filterRefUnionPersonsItem = new RefUnionPersonItem();

    private List<RefMajorMemberItem> RefMajorMemberList;
    private List<RefMajorMemberItem> RefMajorMemberHstList;
    private List<RefMajorMemberItem> RefMajorMemberVerList;
    private RefMajorMemberItem selectedRefMajorMember;
    private RefMajorMemberItem selectedRefMajorMemberVer;
    private boolean RefMajorMemberVisible;
    private RefMajorMemberItem refMajorMemberItem;
    private RefMajorMemberItem filterRefMajorMemberItem = new RefMajorMemberItem();

    // Справочник алгоритмов расчета сроков предоставления
    private List<RefPeriodAlgItem> refPeriodAlgList;
    private List<RefPeriodAlgItem> refPeriodAlgHstList;
    private List<RefPeriodAlgItem> refPeriodAlgVerList;
    private RefPeriodAlgItem selectedRefPeriodAlg;
    private RefPeriodAlgItem selectedRefPeriodAlgVer;
    private boolean refPeriodAlgVisible;
    private RefPeriodAlgItem refPeriodAlgItem;
    private RefPeriodAlgItem filterRefPeriodAlgItem = new RefPeriodAlgItem();

    // Справочник сроков предоставления отчетности
    private List<RefPeriodItem> refPeriodList;
    private List<RefPeriodItem> refPeriodHstList;
    private List<RefPeriodItem> refPeriodVerList;
    private RefPeriodItem selectedRefPeriod;
    private RefPeriodItem selectedRefPeriodVer;
    private boolean refPeriodVisible;
    private RefPeriodItem refPeriodItem;
    private RefPeriodItem filterRefPeriodItem = new RefPeriodItem();

    private List<RefPeriodArgument> argumentList;
    private RefPeriodArgument selectedArgument;
    private RefPeriodArgument prepareArgument;
    private ValueType[] valueTypes = ValueType.values();
    private ValueType valueType;
    private String strValue;

    private List<RefMajorMemberOrgItem> RefMajorMemberOrgList;
    private List<RefMajorMemberOrgItem> RefMajorMemberOrgHstList;
    private List<RefMajorMemberOrgItem> RefMajorMemberOrgVerList;
    private RefMajorMemberOrgItem selectedRefMajorMemberOrg;
    private RefMajorMemberOrgItem selectedRefMajorMemberOrgVer;
    private boolean RefMajorMemberOrgVisible;
    private RefMajorMemberOrgItem refMajorMemberOrgItem;
    private RefMajorMemberOrgItem filterRefMajorMemberOrgItem = new RefMajorMemberOrgItem();


    private List<RefMajorMemDetailsItem> RefMajorMemDetailsList;
    private List<RefMajorMemDetailsItem> RefMajorMemDetailsHstList;
    private List<RefMajorMemDetailsItem> RefMajorMemDetailsVerList;
    private RefMajorMemDetailsItem selectedRefMajorMemDetails;
    private RefMajorMemDetailsItem selectedRefMajorMemDetailsVer;
    private boolean RefMajorMemDetailsVisible;
    private RefMajorMemDetailsItem refMajorMemDetailsItem;
    private RefMajorMemDetailsItem filterRefMajorMemDetailsItem = new RefMajorMemDetailsItem();

    private List<SimpleReference> RefValueTypeList;

    // Справочник страховых групп
   private TreeNode insurGroupsTree;
   private TreeNode selectedInsurGroups;
   private List<RefInsurGroupsItem> RefInsurGroupsList;
   private List<RefInsurGroupsItem> RefInsurGroupsHstList;
   private List<RefInsurGroupsItem> RefInsurGroupsVerList;
   private RefInsurGroupsItem selectedRefInsurGroups;
   private RefInsurGroupsItem selectedRefInsurGroupsVer;
   private boolean RefInsurGroupsVisible;
   private RefInsurGroupsItem refInsurGroupsItem;
   private RefInsurGroupsItem filterRefInsurGroupsItem = new RefInsurGroupsItem();

    // Справочник банковских конгломератов
   private TreeNode bankConglTree;
   private TreeNode selectedBankCongl;
   private List<RefBankConglomeratesItem> RefBankConglList;
   private List<RefBankConglomeratesItem> RefBankConglHstList;
   private List<RefBankConglomeratesItem> RefBankConglVerList;
   private RefBankConglomeratesItem selectedRefBankCongl;
   private RefBankConglomeratesItem selectedRefBankConglVer;
   private boolean RefBankConglVisible;
   private RefBankConglomeratesItem refBankConglItem;
   private RefBankConglomeratesItem filterRefBankConglItem = new RefBankConglomeratesItem();

    // Справочник держателей акций
     private TreeNode shareHoldTree;
     private TreeNode selectedShareHold;
     private List<RefShareHoldersItem> RefShareHoldList;
     private List<RefShareHoldersItem> RefShareHoldHstList;
     private List<RefShareHoldersItem> RefShareHoldVerList;
     private RefShareHoldersItem selectedRefShareHold;
     private RefShareHoldersItem selectedRefShareHoldVer;
     private boolean RefShareHoldVisible;
     private RefShareHoldersItem refShareHoldItem;
     private RefShareHoldersItem filterRefShareHoldItem = new RefShareHoldersItem();

    //endregion

    @PostConstruct
    public void init() { // preRenderView event listener
        Date dateStart = new Date();
        typeUnionPersons = "2";
        try {
            if (sessionBean.isEjbNull()) sessionBean.init();
            userId = sessionBean.user.getUserId();
            userLocation = sessionBean.user.getLoginIP();
            ldmRefSecListItem = new LdmRefSecurities();
            ldmRefIssuerListItem = new LdmRefIssuers();
            refreshRefList();
        } catch (Exception e) { applicationBean.redirectToErrorPage(e); }

        Date dateEnd = new Date();
        long duration = dateEnd.getTime() - dateStart.getTime();
        logger.debug(MessageFormat.format("t: {0}, d: {1} ms", dateStart, duration));
    }

    // At least dummy preRender event listener required to properly redirect to error pages when exceptions occur in PostConstruct methods.
    public void preRender() {
        boolean isPostBack = FacesContext.getCurrentInstance().isPostback();
        boolean isAjax = FacesContext.getCurrentInstance().getPartialViewContext().isAjaxRequest();
        if (isPostBack || isAjax) return;
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('statusDialog').show()");
    }

    public String getRefRightCode(String refName, String type) {
        String result = "SU:REF:";
        result = result + refName.toUpperCase() + ":" + type.toUpperCase();
        return result;
    }

    public void refreshRefList() {
        RefList = sessionBean.getReference().getRefList(userId, filterRefMainItem);
//        selectedRef = null;
    }

    public void refreshRefKnd(){
        refKndList = sessionBean.getReference().getRefKndList();
    }

    public void openReference(){
        tabVisible();
        referenceCode = selectedRef.get(0).getCode();
        isLoadRef(referenceCode, "insert_ref");
        canEdit = userBean.hasPermission(getRefRightCode(referenceCode.toUpperCase(), "EDIT")) && (!isLoad || referenceCode.equals(RefLegalPersonItem.REF_CODE));

        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();
        if(referenceCode.equals(RefRespondentItem.REF_CODE)){
            refreshDeps(date);
            refreshSTs(date);
        }else if (referenceCode.equals(RefLegalPersonItem.REF_CODE)){
            refreshOrgTypes(date);
        }else if (referenceCode.equals(RefManagersItem.REF_CODE)){
            RefPostList = (List<RefPostItem>) sessionBean.getReference().getRefAbstractList(RefPostItem.REF_CODE, date);
        }else if (referenceCode.equals(RefMfoRegItem.REF_CODE)){
            refreshDeps(date);
        }
    }

    public void refreshSimpleRefList(String type){
        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();
        Date maxDate = Convert.getDateFromString("01.01.3333");
        validationVisible = true;
        if (referenceCode.equals(RefPostItem.REF_CODE)){
            RefPostVisible = true;
            filterRefPostItem.setSearchAllVer(searchAllVer);
            RefPostList = (List<RefPostItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefPostItem, maxDate);
        }else if (referenceCode.equals(RefPersonItem.REF_CODE)){
            RefPersonVisible = true;
            filterRefPersonItem.setSearchAllVer(searchAllVer);
            RefPersonList = (List<RefPersonItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefPersonItem, maxDate);
        }else if (referenceCode.equals(RefLegalPersonItem.REF_CODE)){
            RefLegalPersonVisible = true;
            filterRefLegalPersonItem.setSearchAllVer(searchAllVer);
            filterRefLegalPersonItem.setCurrentRec(false);
            refreshOrgTypes(date);
            RefLegalPersonList = (List<RefLegalPersonItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefLegalPersonItem, maxDate);
        }else if (referenceCode.equals(RefCountryItem.REF_CODE)){
            RefCountryVisible = true;
            filterRefCountryItem.setSearchAllVer(searchAllVer);
            RefCountryList = (List<RefCountryItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefCountryItem, maxDate);
        }else if (referenceCode.equals(RefManagersItem.REF_CODE)){
            RefPostList = (List<RefPostItem>) sessionBean.getReference().getRefAbstractList(RefPostItem.REF_CODE, maxDate);
            RefManagersVisible = true;
            filterRefManagersItem.setSearchAllVer(searchAllVer);
            RefManagersList = (List<RefManagersItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefManagersItem, maxDate);
        }else if (referenceCode.equals(RefTypeBusEntityItem.REF_CODE)){
            RefTypeBusEntityVisible = true;
            filterRefTypeBusEntityItem.setSearchAllVer(searchAllVer);
            RefTypeBusEntityList = (List<RefTypeBusEntityItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefTypeBusEntityItem, maxDate);
        }else if (referenceCode.equals(RefRegionItem.REF_CODE)){
            RefRegionVisible = true;
            filterRefRegionItem.setSearchAllVer(searchAllVer);
            RefRegionList = (List<RefRegionItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefRegionItem, maxDate);
        }else if (referenceCode.equals(RefRequirementItem.REF_CODE)) {
            RefRequirementVisible = true;
            filterRefRequirementItem.setSearchAllVer(searchAllVer);
            RefRequirementList = (List<RefRequirementItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefRequirementItem, maxDate);
        }else if (referenceCode.equals(RefTypeProvideItem.REF_CODE)) {
            RefTypeProvideVisible = true;
            filterRefTypeProvideItem.setSearchAllVer(searchAllVer);
            RefTypeProvideList = (List<RefTypeProvideItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefTypeProvideItem, maxDate);
        }else if (referenceCode.equals(RefTransTypeItem.REF_CODE)) {
            RefTransTypeVisible = true;
            filterRefTransTypeItem.setSearchAllVer(searchAllVer);
            RefTransTypeList = (List<RefTransTypeItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefTransTypeItem, maxDate);
        }else if (referenceCode.equals(RefBalanceAccItem.REF_CODE)) {
            RefBalanceAccVisible = true;
            filterRefBalanceAccItem.setSearchAllVer(searchAllVer);
            RefBalanceAccList = (List<RefBalanceAccItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefBalanceAccItem, maxDate);;
            balanceAccTree = new DefaultTreeNode("Root", null);
            Map<String, TreeNode> nodes = new HashMap<String, TreeNode>();
            for (RefBalanceAccItem refBalanceAccItem1 : RefBalanceAccList) {
                TreeNode parentNode;
                if (refBalanceAccItem1.getParentCode() == null )
                    parentNode = balanceAccTree;
                else
                    parentNode = nodes.get(refBalanceAccItem1.getParentCode());
                TreeNode node;
                node = new DefaultTreeNode(refBalanceAccItem1, parentNode);
                nodes.put(refBalanceAccItem1.getCode(), node);
            }
        }else if (referenceCode.equals(RefConnOrgItem.REF_CODE)) {
            RefConnOrgVisible = true;
            filterRefConnOrgItem.setSearchAllVer(searchAllVer);
            RefConnOrgList = (List<RefConnOrgItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefConnOrgItem, maxDate);
        }else if (referenceCode.equals(RefDepartmentItem.REF_CODE)) {
            RefDepartmentVisible = true;
            filterRefDepartmentItem.setSearchAllVer(searchAllVer);
            RefDepartmentList = (List<RefDepartmentItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefDepartmentItem, maxDate);
            deptTypeList = sessionBean.getReference().getRefDeptType(date);
        }else if (referenceCode.equals(RefBankItem.REF_CODE)) {
            RefBankVisible = true;
            filterRefBankItem.setSearchAllVer(searchAllVer);
            RefBankList = (List<RefBankItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefBankItem, maxDate);
        }else if (referenceCode.equals(RefRateAgencyItem.REF_CODE)) {
            RefRateAgencyVisible = true;
            filterRefRateAgencyItem.setSearchAllVer(searchAllVer);
            RefRateAgencyList = (List<RefRateAgencyItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefRateAgencyItem, maxDate);
        }else if (referenceCode.equals(RefCurrencyItem.REF_CODE)) {
            RefCurrencyVisible = true;
            filterRefCurrencyItem.setSearchAllVer(searchAllVer);
            RefCurrencyList = (List<RefCurrencyItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefCurrencyItem, maxDate);
        }else if (referenceCode.equals(RefCurrencyRateItem.REF_CODE)) {
            RefCurrencyRateVisible = true;
            filterRefCurrencyRateItem.setSearchAllVer(searchAllVer);
            RefCurrencyRateList = (List<RefCurrencyRateItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefCurrencyRateItem, maxDate);
        }else if (referenceCode.equals(RefSubjectTypeItem.REF_CODE)) {
            RefSubjectTypeVisible = true;
            filterRefSubjectTypeItem.setSearchAllVer(searchAllVer);
            RefSubjectTypeList = (List<RefSubjectTypeItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefSubjectTypeItem, maxDate);
        }else if (referenceCode.equals(RefRespondentItem.REF_CODE)) {
            refreshDeps(date);
            refreshSTs(date);
            RefRespondentVisible = true;
            filterRefRespondentItem.setSearchAllVer(searchAllVer);
            RefRespondentList = (List<RefRespondentItem>)sessionBean.getReference().getRefAbstractByFilterList(RefRespondentItem.REF_CODE, filterRefRespondentItem, maxDate);
            for (RefRespondentItem refRespondentItem : RefRespondentList){
                if(refRespondentItem.getId() == 14){ // Исключаем Нац банк
                    RefRespondentList.remove(refRespondentItem);
                    break;
                }
            }
        }else if (referenceCode.equals(RefDocTypeItem.REF_CODE)) {
            RefDocTypeVisible = true;
            filterRefDocTypeItem.setSearchAllVer(searchAllVer);
            RefDocTypeList = (List<RefDocTypeItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefDocTypeItem, maxDate);
        }else if (referenceCode.equals(RefDocumentItem.REF_CODE)) {
            RefDocumentVisible = true;
            filterRefDocumentItem.setSearchAllVer(searchAllVer);
            RefDocumentList = (List<RefDocumentItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefDocumentItem, maxDate);
        }else if (referenceCode.equals(RefIssuersItem.REF_CODE)) {
            RefIssuersVisible = true;
            filterRefIssuersItem.setSearchAllVer(searchAllVer);
            RefIssuersList = (List<RefIssuersItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefIssuersItem, maxDate);
            ((LdmRefIssuers) ldmRefIssuerListItem).setSrcCollection(RefIssuersList);
        }else if (referenceCode.equals(RefSecuritiesItem.REF_CODE)) {
            RefSecuritiesVisible = true;
            filterRefSecuritiesItem.setSearchAllVer(searchAllVer);
            RefSecuritiesList = (List<RefSecuritiesItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefSecuritiesItem, maxDate);
            ((LdmRefSecurities) ldmRefSecListItem).setSrcCollection(RefSecuritiesList);
        }else if (referenceCode.equals(RefVidOperItem.REF_CODE)) {
            RefVidOperVisible = true;
            filterRefVidOperItem.setSearchAllVer(searchAllVer);
            RefVidOperList = (List<RefVidOperItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefVidOperItem, maxDate);
        }else if (referenceCode.equals(RefBranchItem.REF_CODE)) {
            RefBranchVisible = true;
            filterRefBranchItem.setSearchAllVer(searchAllVer);
            RefBranchList = (List<RefBranchItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefBranchItem, maxDate);
        }else if (referenceCode.equals("ref_branch_old")) {
            RefBranchOldVisible = true;
            filterRefBranchOldItem.setRefCode(referenceCode);
            filterRefBranchOldItem.setSearchAllVer(searchAllVer);
            RefBranchOldList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefBranchOldItem);
        }else if (referenceCode.equals(RefListingEstimationItem.REF_CODE)) {
            RefListingEstimationVisible = true;
            filterRefListingEstimationItem.setSearchAllVer(searchAllVer);
            RefListingEstimationList = (List<RefListingEstimationItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefListingEstimationItem, maxDate);
        }else if (referenceCode.equals(RefRatingEstimationItem.REF_CODE)) {
            RefRatingEstimationVisible = true;
            filterRefRatingEstimationItem.setSearchAllVer(searchAllVer);
            RefRatingEstimationList = (List<RefRatingEstimationItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefRatingEstimationItem, maxDate);
        }else if (referenceCode.equals(RefRatingCategoryItem.REF_CODE)) {
            RefRatingCategoryVisible = true;
            filterRefRatingCategoryItem.setSearchAllVer(searchAllVer);
            RefRatingCategoryList = (List<RefRatingCategoryItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefRatingCategoryItem, maxDate);
        }/*else if (referenceCode.equals("ref_request_type")) {
            RefRequestTypeVisible = true;
            filterRefRequestTypeItem.setRefCode(referenceCode);
            filterRefRequestTypeItem.setSearchAllVer(searchAllVer);
            RefRequestTypeList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefRequestTypeItem);
        }else if (referenceCode.equals("ref_request_way")) {
            RefRequestWayVisible = true;
            filterRefRequestWayItem.setRefCode(referenceCode);
            filterRefRequestWayItem.setSearchAllVer(searchAllVer);
            RefRequestWayList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefRequestWayItem);
        }else if (referenceCode.equals("ref_market_kind")) {
            RefMarketKindVisible = true;
            filterRefMarketKindItem.setRefCode(referenceCode);
            filterRefMarketKindItem.setSearchAllVer(searchAllVer);
            RefMarketKindList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefMarketKindItem);
        }else if (referenceCode.equals("ref_category")) {
            RefCategoryVisible = true;
            filterRefCategoryItem.setRefCode(referenceCode);
            filterRefCategoryItem.setSearchAllVer(searchAllVer);
            RefCategoryList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefCategoryItem);
        }else if (referenceCode.equals("ref_subcategory")) {
            RefSubCategoryVisible = true;
            filterRefSubCategoryItem.setRefCode(referenceCode);
            filterRefSubCategoryItem.setSearchAllVer(searchAllVer);
            RefSubCategoryList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefSubCategoryItem);
        }else if (referenceCode.equals("ref_account_type")) {
            RefAccountTypeVisible = true;
            filterRefAccountTypeItem.setRefCode(referenceCode);
            filterRefAccountTypeItem.setSearchAllVer(searchAllVer);
            RefAccountTypeList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefAccountTypeItem);
        }else if (referenceCode.equals("ref_subaccount_type")) {
            RefSubAccountTypeVisible = true;
            filterRefSubAccountTypeItem.setRefCode(referenceCode);
            filterRefSubAccountTypeItem.setSearchAllVer(searchAllVer);
            RefSubAccountTypeList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefSubAccountTypeItem);
        }else if (referenceCode.equals("ref_type_holder_acc")) {
            RefTypeHolderAccVisible = true;
            filterRefTypeHolderAccItem.setRefCode(referenceCode);
            filterRefTypeHolderAccItem.setSearchAllVer(searchAllVer);
            RefTypeHolderAccList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefTypeHolderAccItem);
        }else if (referenceCode.equals("ref_request_feature")) {
            RefRequestFeatureVisible = true;
            filterRefRequestFeatureItem.setRefCode(referenceCode);
            filterRefRequestFeatureItem.setSearchAllVer(searchAllVer);
            RefRequestFeatureList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefRequestFeatureItem);
        }else if (referenceCode.equals("ref_request_sts")) {
            RefRequestStsVisible = true;
            filterRefRequestStsItem.setRefCode(referenceCode);
            filterRefRequestStsItem.setSearchAllVer(searchAllVer);
            RefRequestStsList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefRequestStsItem);
        }else if (referenceCode.equals("ref_repo_kind")) {
            RefRepoKindVisible = true;
            filterRefRepoKindItem.setRefCode(referenceCode);
            filterRefRepoKindItem.setSearchAllVer(searchAllVer);
            RefRepoKindList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefRepoKindItem);
        }else if (referenceCode.equals("ref_market_type")) {
            RefMarketTypeVisible = true;
            filterRefMarketTypeItem.setRefCode(referenceCode);
            filterRefMarketTypeItem.setSearchAllVer(searchAllVer);
            RefMarketTypeList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefMarketTypeItem);
        }else if (referenceCode.equals("ref_trad_method")) {
            RefTradMethodVisible = true;
            filterRefTradMethodItem.setRefCode(referenceCode);
            filterRefTradMethodItem.setSearchAllVer(searchAllVer);
            RefTradMethodList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefTradMethodItem);
        }else if (referenceCode.equals("ref_oper_type")) {
            RefOperTypeVisible = true;
            filterRefOperTypeItem.setRefCode(referenceCode);
            filterRefOperTypeItem.setSearchAllVer(searchAllVer);
            RefOperTypeList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefOperTypeItem);
        }else if (referenceCode.equals("ref_deal_sts")) {
            RefDealStsVisible = true;
            filterRefDealStsItem.setRefCode(referenceCode);
            filterRefDealStsItem.setSearchAllVer(searchAllVer);
            RefDealStsList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefDealStsItem);
        }*/else if (referenceCode.equals(RefMrpItem.REF_CODE)) {
            RefMrpVisible = true;
            filterRefMrpItem.setSearchAllVer(searchAllVer);
            RefMrpList = (List<RefMrpItem>)sessionBean.getReference().getRefAbstractByFilterList(RefMrpItem.REF_CODE, filterRefMrpItem, maxDate);
        }else if (referenceCode.equals("ref_org_type")) {
            RefOrgTypeVisible = true;
            filterRefOrgTypeItem.setRefCode(referenceCode);
            filterRefOrgTypeItem.setSearchAllVer(searchAllVer);
            RefOrgTypeList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefOrgTypeItem);
        }else if (referenceCode.equals(RefMfoRegItem.REF_CODE)) {
            refreshDeps(date);
            RefMfoRegVisible = true;
            filterRefMfoRegItem.setSearchAllVer(searchAllVer);
            RefMfoRegList = (List<RefMfoRegItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefMfoRegItem, maxDate);
        }else if (referenceCode.equals(RefDealBAItem.REF_CODE)) {
            RefDealBAVisible = true;
            filterRefDealBAItem.setSearchAllVer(searchAllVer);
            RefDealBAList = (List<RefDealBAItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefDealBAItem, maxDate);
        } else if (referenceCode.equals("ref_type_activity")){
            setRefTypeActivityVisible(true);
            getFilterRefTypeActivityItem().setSearchAllVer(searchAllVer);
            //RefTypeActivityList = sessionBean.getReference().getRefTypeActivityListByParams(maxDate, filterRefTypeActivityItem);
            RefTypeActivityList = (List<RefTypeActivityItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefTypeActivityItem, maxDate);
        }else if (referenceCode.equals("ref_npa")) {
            setRefNpaVisible(true);
            filterRefNpaItem.setRefCode(referenceCode);
            filterRefNpaItem.setSearchAllVer(searchAllVer);
            //setRefNpaList(sessionBean.getReference().getRefNpaListByParams(maxDate, getFilterRefNpaItem()));
            RefNpaList = (List<RefNpaItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefNpaItem, maxDate);
        }else if (referenceCode.equals("ref_wkd_holidays")) {
            setRefWkdHolidayVisible(true);
            filterRefWkdHolidayItem.setSearchAllVer(searchAllVer);
            //setRefWkdHolidayList(sessionBean.getReference().getRefWkdHolidayListByParams(maxDate, filterRefWkdHolidayItem));
            RefWkdHolidayList = (List<RefWkdHolidayItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefWkdHolidayItem, maxDate);
        }else if (referenceCode.equals("ref_finrep_indic")) {
            setRefFinRepIndicVisible(true);
            filterRefFinRepIndicItem.setRefCode(referenceCode);
            filterRefFinRepIndicItem.setSearchAllVer(searchAllVer);
            RefFinRepIndicList = (sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefFinRepIndicItem));
        } else if (referenceCode.equals("ref_type_deals")) {
            setRefTypeDealsVisible(true);
            filterRefTypeDealsItem.setRefCode(referenceCode);
            filterRefTypeDealsItem.setSearchAllVer(searchAllVer);
            RefTypeDealsList = (sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefTypeDealsItem));
        } else if (referenceCode.equals("ref_degree_relation")) {
            setRefDegreeRelationVisible(true);
            filterRefDegreeRelationItem.setRefCode(referenceCode);
            filterRefDegreeRelationItem.setSearchAllVer(searchAllVer);
            RefDegreeRelationList = (sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefDegreeRelationItem));
        }else if (referenceCode.equals("ref_sign_related")) {
            setRefSignRelatedVisible(true);
            filterRefSignRelatedItem.setRefCode(referenceCode);
            filterRefSignRelatedItem.setSearchAllVer(searchAllVer);
            RefSignRelatedList = sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefSignRelatedItem);
         }else if (referenceCode.equals("ref_type_risk")) {
            setRefTypeRiskVisible(true);
            filterRefTypeRiskItem.setRefCode(referenceCode);
            filterRefTypeRiskItem.setSearchAllVer(searchAllVer);
             RefTypeRiskList = (sessionBean.getReference().getRefSimpleListByParams(maxDate, filterRefTypeRiskItem));
            //setRefTypeRiskList(sessionBean.getReference().getRefAbstractByFilterList(referenceCode, getFilterRefTypeRiskItem(), maxDate));
        }else if (referenceCode.equals("ref_basisofcontrol")) {
            setRefBasisofControlVisible(true);
            filterRefBasisofControlItem.setSearchAllVer(searchAllVer);
            RefBasisofControlList = (List<RefBasisofControlItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefBasisofControlItem, maxDate);
        } else if (referenceCode.equals(RefInsurOrgItem.REF_CODE)){
            RefInsurOrgVisible = true;
            filterRefInsurOrgItem.setSearchAllVer(searchAllVer);
            SimpleReference refOwnershipReference = new SimpleReference();
            refOwnershipReference.setRefCode("ref_ownership");
            SimpleReference refBranchInsurReference = new SimpleReference();
            refBranchInsurReference.setRefCode("ref_branch_insur");
            RefBranchInsurList = sessionBean.getReference().getRefSimpleListByParams(date,refBranchInsurReference);
            RefOwnershipList = sessionBean.getReference().getRefSimpleListByParams(date,refOwnershipReference);
            RefInsurOrgList = (List<RefInsurOrgItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefInsurOrgItem, maxDate);
        } else if (referenceCode.equals(RefExtIndicatorItem.REF_CODE)){
            RefExtIndVisible = true;
            filterRefExtIndItem.setSearchAllVer(searchAllVer);
            SimpleReference refExtSys = new SimpleReference();
            refExtSys.setRefCode("extsys");
            extSysList = sessionBean.getReference().getRefSimpleListByParams(date,refExtSys);
            RefExtIndList = (List<RefExtIndicatorItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefExtIndItem, maxDate);
        }  else if (referenceCode.equals(RefMajorMemberItem.REF_CODE)){
            RefMajorMemberVisible = true;
            filterRefMajorMemberItem.setSearchAllVer(searchAllVer);
           // filterRefMajorMemberItem.setRefUnionPersons(Long.parseLong(typeUnionPersons));
            RefMajorMemberList = (List<RefMajorMemberItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefMajorMemberItem, maxDate);
        }  else if (referenceCode.equals(RefMajorMemberOrgItem.REF_CODE)){
            SimpleReference refStatus = new SimpleReference();
            refStatus.setRefCode("ref_status");
            refStatusList = sessionBean.getReference().getRefSimpleListByParams(date,refStatus);
            filterRefMajorMemberOrgItem.setSearchAllVer(searchAllVer);
            RefMajorMemberOrgList = (List<RefMajorMemberOrgItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefMajorMemberOrgItem, maxDate);
        }else if (referenceCode.equals(RefMajorMemDetailsItem.REF_CODE)){
            filterRefMajorMemDetailsItem.setSearchAllVer(searchAllVer);
            RefMajorMemDetailsList = (List<RefMajorMemDetailsItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefMajorMemDetailsItem, maxDate);
        } else if (referenceCode.equals(RefPeriodAlgItem.REF_CODE)){
            refPeriodAlgVisible = true;
            filterRefPeriodAlgItem.setSearchAllVer(searchAllVer);
            refPeriodAlgList = (List<RefPeriodAlgItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefPeriodAlgItem, maxDate);
        } else if (referenceCode.equals(RefPeriodItem.REF_CODE)){
            refPeriodVisible = true;
            filterRefPeriodItem.setSearchAllVer(searchAllVer);
            refPeriodList = (List<RefPeriodItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefPeriodItem, maxDate);
        }else if (referenceCode.equals(RefInsurGroupsItem.REF_CODE)) {
            RefInsurGroupsVisible = true;
            filterRefInsurGroupsItem.setSearchAllVer(searchAllVer);
            RefInsurGroupsList = (List<RefInsurGroupsItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefInsurGroupsItem, maxDate);
            insurGroupsTree = new DefaultTreeNode("Root", null);
            Map<String, TreeNode> nodes = new HashMap<String, TreeNode>();
            for (RefInsurGroupsItem refInsurGroupsItem1 : RefInsurGroupsList) {
                TreeNode parentNode;
                if (refInsurGroupsItem1.getParentCode() == null )
                    parentNode = insurGroupsTree;
                else
                    parentNode = nodes.get(refInsurGroupsItem1.getParentCode());
                TreeNode node;
                node = new DefaultTreeNode(refInsurGroupsItem1, parentNode);
                nodes.put(refInsurGroupsItem1.getCode(), node);
            }
        } else if (referenceCode.equals(RefBankConglomeratesItem.REF_CODE)) {
            RefBankConglVisible = true;
            filterRefBankConglItem.setSearchAllVer(searchAllVer);
            RefBankConglList = (List<RefBankConglomeratesItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefBankConglItem, maxDate);
            bankConglTree = new DefaultTreeNode("Root", null);
            Map<String, TreeNode> nodes = new HashMap<String, TreeNode>();
            for (RefBankConglomeratesItem refBankCongl1 : RefBankConglList) {
                TreeNode parentNode;
                if (refBankCongl1.getParentCode() == null )
                    parentNode = bankConglTree;
                else
                    parentNode = nodes.get(refBankCongl1.getParentCode());
                TreeNode node;
                node = new DefaultTreeNode(refBankCongl1, parentNode);
                nodes.put(refBankCongl1.getCode(), node);
            }
        } else if (referenceCode.equals(RefShareHoldersItem.REF_CODE)) {
            RefShareHoldVisible = true;
            filterRefShareHoldItem.setSearchAllVer(searchAllVer);
            RefShareHoldList = (List<RefShareHoldersItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filterRefShareHoldItem, maxDate);
            shareHoldTree = new DefaultTreeNode("Root", null);
            Map<String, TreeNode> nodes = new HashMap<String, TreeNode>();
            for (RefShareHoldersItem refShareHold1 : RefShareHoldList) {
                TreeNode parentNode;
                if (refShareHold1.getParentCode() == null )
                    parentNode = shareHoldTree;
                else
                    parentNode = nodes.get(refShareHold1.getParentCode());
                TreeNode node;
                node = new DefaultTreeNode(refShareHold1, parentNode);
                nodes.put(refShareHold1.getCode(), node);
            }
        }
    }

    public void refreshSimpleRefHstList(){
        if (referenceCode.equals(RefPostItem.REF_CODE)){
            RefPostHstList = (List<RefPostItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefPostVer.getId());
        }else if (referenceCode.equals(RefPersonItem.REF_CODE)) {
            RefPersonHstList = (List<RefPersonItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefPersonVer.getId());
        }else if (referenceCode.equals(RefLegalPersonItem.REF_CODE)){
            RefLegalPersonHstList = (List<RefLegalPersonItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefLegalPersonVer.getId());
        }else if (referenceCode.equals(RefManagersItem.REF_CODE)){
            RefManagersHstList = (List<RefManagersItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefManagersVer.getId());
        }else if (referenceCode.equals(RefTypeBusEntityItem.REF_CODE)){
            RefTypeBusEntityHstList = (List<RefTypeBusEntityItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefTypeBusEntityVer.getId());
        }else if (referenceCode.equals(RefRequirementItem.REF_CODE)){
            RefRequirementHstList = (List<RefRequirementItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefRequirementVer.getId());
        }else if (referenceCode.equals(RefTypeProvideItem.REF_CODE)){
            RefTypeProvideHstList = (List<RefTypeProvideItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefTypeProvideVer.getId());
        }else if (referenceCode.equals(RefTransTypeItem.REF_CODE)){
            RefTransTypeHstList = (List<RefTransTypeItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefTransTypeVer.getId());
        }else if (referenceCode.equals(RefBalanceAccItem.REF_CODE)){
            selectedRefBalanceAcc = (RefBalanceAccItem) selectedBalanceAcc.getData();
            RefBalanceAccHstList = (List<RefBalanceAccItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefBalanceAccVer.getId());
        } else if (referenceCode.equals(RefConnOrgItem.REF_CODE)){
            RefConnOrgHstList = (List<RefConnOrgItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefConnOrgVer.getId());
        }else if (referenceCode.equals(RefBankItem.REF_CODE)){
            RefBankHstList = (List<RefBankItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefBankVer.getId());
        }else if (referenceCode.equals(RefSubjectTypeItem.REF_CODE)){
            RefSubjectTypeHstList = (List<RefSubjectTypeItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefSubjectTypeVer.getId());
        }else if (referenceCode.equals(RefRespondentItem.REF_CODE)){
            RefRespondentHstList = (List<RefRespondentItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefRespondentVer.getId());
        } else if (referenceCode.equals(RefDocTypeItem.REF_CODE)){
            RefDocTypeHstList = (List<RefDocTypeItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefDocTypeVer.getId());
        } else if (referenceCode.equals(RefDocumentItem.REF_CODE)){
            RefDocumentHstList = (List<RefDocumentItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefDocumentVer.getId());
        }else if (referenceCode.equals(RefVidOperItem.REF_CODE)) {
            RefVidOperHstList = (List<RefVidOperItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefVidOperVer.getId());
        }/*else if (referenceCode.equals("ref_request_type")){
            RefRequestTypeHstList = sessionBean.getReference().getRefSimpleHstList(selectedRefRequestTypeVer.getId(), referenceCode);
        }else if (referenceCode.equals("ref_request_way")){
            RefRequestWayHstList = sessionBean.getReference().getRefSimpleHstList(selectedRefRequestWayVer.getId(), referenceCode);
        }else if (referenceCode.equals("ref_market_kind")) {
            RefMarketKindHstList = sessionBean.getReference().getRefSimpleHstList(selectedRefMarketKindVer.getId(), referenceCode);
        }else if (referenceCode.equals("ref_category")) {
            RefCategoryHstList = sessionBean.getReference().getRefSimpleHstList(selectedRefCategoryVer.getId(), referenceCode);
        }else if (referenceCode.equals("ref_subcategory")) {
            RefSubCategoryHstList = sessionBean.getReference().getRefSimpleHstList(selectedRefSubCategoryVer.getId(), referenceCode);
        }else if (referenceCode.equals("ref_account_type")) {
            RefAccountTypeHstList = sessionBean.getReference().getRefSimpleHstList(selectedRefAccountTypeVer.getId(), referenceCode);
        }else if (referenceCode.equals("ref_subaccount_type")) {
            RefSubAccountTypeHstList = sessionBean.getReference().getRefSimpleHstList(selectedRefSubAccountTypeVer.getId(), referenceCode);
        }else if (referenceCode.equals("ref_type_holder_acc")) {
            RefTypeHolderAccHstList = sessionBean.getReference().getRefSimpleHstList(selectedRefTypeHolderAccVer.getId(), referenceCode);
        }else if (referenceCode.equals("ref_request_feature")) {
            RefRequestFeatureHstList = sessionBean.getReference().getRefSimpleHstList(selectedRefRequestFeatureVer.getId(), referenceCode);
        }else if (referenceCode.equals("ref_request_sts")) {
            RefRequestStsHstList = sessionBean.getReference().getRefSimpleHstList(selectedRefRequestStsVer.getId(), referenceCode);
        }else if (referenceCode.equals("ref_repo_kind")) {
            RefRepoKindHstList = sessionBean.getReference().getRefSimpleHstList(selectedRefRepoKindVer.getId(), referenceCode);
        }else if (referenceCode.equals("ref_market_type")) {
            RefMarketTypeHstList = sessionBean.getReference().getRefSimpleHstList(selectedRefMarketTypeVer.getId(), referenceCode);
        }else if (referenceCode.equals("ref_trad_method")) {
            RefTradMethodHstList = sessionBean.getReference().getRefSimpleHstList(selectedRefTradMethodVer.getId(), referenceCode);
        }else if (referenceCode.equals("ref_oper_type")) {
            RefOperTypeHstList = sessionBean.getReference().getRefSimpleHstList(selectedRefOperTypeVer.getId(), referenceCode);
        }else if (referenceCode.equals("ref_deal_sts")) {
            RefDealStsHstList = sessionBean.getReference().getRefSimpleHstList(selectedRefDealStsVer.getId(), referenceCode);
        }*/else if (referenceCode.equals(RefMrpItem.REF_CODE)){
            RefMrpHstList = (List<RefMrpItem>)sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefMrpVer.getId());
        }else if (referenceCode.equals("ref_org_type")) {
            RefOrgTypeHstList = sessionBean.getReference().getRefSimpleHstList(selectedRefOrgTypeVer.getId(), referenceCode);
        }else if (referenceCode.equals(RefMfoRegItem.REF_CODE)){
            RefMfoRegHstList = (List<RefMfoRegItem>)sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefMfoRegVer.getId());
        }else if (referenceCode.equals(RefDealBAItem.REF_CODE)){
            RefDealBAHstList = (List<RefDealBAItem>)sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefDealBAVer.getId());
        }else if (referenceCode.equals("ref_type_activity")){
           // RefTypeActivityHstList = sessionBean.getReference().getRefTypeActivityHstList(selectedRefTypeActivityVer.getId());
            RefTypeActivityHstList = (List<RefTypeActivityItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, getSelectedRefTypeActivityVer().getId());
        }else if (referenceCode.equals("ref_npa")) {
            //setRefNpaHstList(sessionBean.getReference().getRefNpaHstList(selectedRefNpaVer.getId()));
            RefNpaHstList = (List<RefNpaItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, getSelectedRefNpaVer().getId());
        }else if (referenceCode.equals("ref_wkd_holidays")) {
            RefWkdHolidayHstList = (List<RefWkdHolidayItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, getSelectedRefWkdHolidayVer().getId());
        }else if (referenceCode.equals("ref_finrep_indic")) {
            RefFinRepIndicHstList = sessionBean.getReference().getRefSimpleHstList(getSelectedRefFinRepIndicVer().getId(), referenceCode);
        }else if (referenceCode.equals("ref_type_deals")) {
            RefTypeDealsHstList = sessionBean.getReference().getRefSimpleHstList(getSelectedRefTypeDealsVer().getId(), referenceCode);
        }else if (referenceCode.equals("ref_degree_relation")) {
            RefDegreeRelationHstList = sessionBean.getReference().getRefSimpleHstList(getSelectedRefDegreeRelationVer().getId(), referenceCode);
        }else if (referenceCode.equals("ref_sign_related")) {
            RefSignRelatedHstList = sessionBean.getReference().getRefSimpleHstList(getSelectedRefSignRelatedVer().getId(), referenceCode);
        }else if (referenceCode.equals("ref_type_risk")) {
            RefTypeRiskHstList = sessionBean.getReference().getRefSimpleHstList(getSelectedRefTypeRiskVer().getId(), referenceCode);
        }else if (referenceCode.equals("ref_basisofcontrol")){
            RefBasisofControlHstList = (List<RefBasisofControlItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, getSelectedRefBasisofControlVer().getId());
        }else if (referenceCode.equals("ref_extind")){
            RefExtIndHstList = (List<RefExtIndicatorItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefExtIndVer.getId());
        }else if (referenceCode.equals("ref_major_member")){
            RefMajorMemberHstList = (List<RefMajorMemberItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefMajorMemberVer.getId());
        }else if (referenceCode.equals(RefPeriodAlgItem.REF_CODE)){
            refPeriodAlgHstList = (List<RefPeriodAlgItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefPeriodAlgVer.getId());
        }else if (referenceCode.equals(RefPeriodItem.REF_CODE)){
            refPeriodHstList = (List<RefPeriodItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefPeriodVer.getId());
        }else if (referenceCode.equals(RefInsurGroupsItem.REF_CODE)){
            RefInsurGroupsHstList = (List<RefInsurGroupsItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefInsurGroupsVer.getId());
        } else if (referenceCode.equals(RefBankConglomeratesItem.REF_CODE)){
            RefBankConglHstList = (List<RefBankConglomeratesItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefBankConglVer.getId());
        } else if (referenceCode.equals(RefShareHoldersItem.REF_CODE)){
            RefShareHoldHstList = (List<RefShareHoldersItem>) sessionBean.getReference().getRefAbstractHstList(referenceCode, selectedRefShareHoldVer.getId());
        }
    }

    public void refreshSimpleRefVerList(){
        RequestContext context = RequestContext.getCurrentInstance();
        String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();
        if (referenceCode.equals(RefPostItem.REF_CODE)) {
            selectedRefPostVer = null;
            RefPostItem refPostItem = new RefPostItem();
            refPostItem.setRecId(selectedRefPost.getRecId());
            RefPostVerList = (List<RefPostItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refPostItem, null);
            context.update(liferayNameSpace + ":frmRefPostVer");
            context.execute("PF('wDlgRefPostVer').show()");
        }else if (referenceCode.equals(RefPersonItem.REF_CODE)){
            selectedRefPersonVer = null;
            RefPersonItem refPersonItem = new RefPersonItem();
            refPersonItem.setRecId(selectedRefPerson.getRecId());
            RefPersonVerList = (List<RefPersonItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refPersonItem, null);
            context.update(liferayNameSpace + ":frmRefPersonVer");
            context.execute("PF('wDlgRefPersonVer').show()");
        }else if (referenceCode.equals(RefLegalPersonItem.REF_CODE)){
            selectedRefLegalPersonVer = null;
            RefLegalPersonItem refLegalPersonItem = new RefLegalPersonItem();
            refLegalPersonItem.setRecId(selectedRefLegalPerson.getRecId());
            RefLegalPersonVerList = (List<RefLegalPersonItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refLegalPersonItem, null);
            context.update(liferayNameSpace + ":frmRefLegalPersonVer");
            context.execute("PF('wDlgRefLegalPersonVer').show()");
        }else if (referenceCode.equals(RefCountryItem.REF_CODE)){
            selectedRefCountryVer = null;
            RefCountryItem refCountryItem = new RefCountryItem();
            refCountryItem.setRecId(selectedRefCountry.getRecId());
            RefCountryVerList = (List<RefCountryItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refCountryItem, null);
            context.update(liferayNameSpace + ":frmRefCountryVer");
            context.execute("PF('wDlgRefCountryVer').show()");
        }else if (referenceCode.equals(RefManagersItem.REF_CODE)){
            selectedRefManagersVer = null;
            RefManagersItem refManagersItem = new RefManagersItem();
            refManagersItem.setRecId(selectedRefManagers.getRecId());
            RefManagersVerList = (List<RefManagersItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refManagersItem, null);
            context.update(liferayNameSpace + ":frmRefManagersVer");
            context.execute("PF('wDlgRefManagersVer').show()");
        }else if (referenceCode.equals(RefTypeBusEntityItem.REF_CODE)){
            selectedRefTypeBusEntityVer = null;
            RefTypeBusEntityItem refTypeBusEntityItem = new RefTypeBusEntityItem();
            refTypeBusEntityItem.setRecId(selectedRefTypeBusEntity.getRecId());
            RefTypeBusEntityVerList = (List<RefTypeBusEntityItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refTypeBusEntityItem, null);
            context.update(liferayNameSpace + ":frmRefTypeBusEntityVer");
            context.execute("PF('wDlgRefTypeBusEntityVer').show()");
        }else if (referenceCode.equals(RefRegionItem.REF_CODE)){
            selectedRefRegionVer = null;
            RefRegionItem refRegionItem = new RefRegionItem();
            refRegionItem.setRecId(selectedRefRegion.getRecId());
            RefRegionVerList = (List<RefRegionItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refRegionItem, null);
            context.update(liferayNameSpace + ":frmRefRegionVer");
            context.execute("PF('wDlgRefRegionVer').show()");
        }else if (referenceCode.equals(RefRequirementItem.REF_CODE)){
            selectedRefRequirementVer = null;
            RefRequirementItem refRequirementItem = new RefRequirementItem();
            refRequirementItem.setRecId(selectedRefRequirement.getRecId());
            RefRequirementVerList = (List<RefRequirementItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refRequirementItem, null);
            context.update(liferayNameSpace + ":frmRefRequirementVer");
            context.execute("PF('wDlgRefRequirementVer').show()");
        }else if (referenceCode.equals(RefTypeProvideItem.REF_CODE)){
            selectedRefTypeProvideVer = null;
            RefTypeProvideItem refTypeProvideItem = new RefTypeProvideItem();
            refTypeProvideItem.setRecId(selectedRefTypeProvide.getRecId());
            RefTypeProvideVerList = (List<RefTypeProvideItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refTypeProvideItem, null);
            context.update(liferayNameSpace + ":frmRefTypeProvideVer");
            context.execute("PF('wDlgRefTypeProvideVer').show()");
        }else if (referenceCode.equals(RefTransTypeItem.REF_CODE)){
            selectedRefTransTypeVer = null;
            RefTransTypeItem refTransTypeItem = new RefTransTypeItem();
            refTransTypeItem.setRecId(selectedRefTransType.getRecId());
            RefTransTypeVerList = (List<RefTransTypeItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refTransTypeItem, null);
            context.update(liferayNameSpace + ":frmRefTransTypeVer");
            context.execute("PF('wDlgRefTransTypeVer').show()");
        }else if (referenceCode.equals(RefBalanceAccItem.REF_CODE)){
            selectedRefBalanceAccVer = null;
            RefBalanceAccItem refBalanceAccItem = new RefBalanceAccItem();
            refBalanceAccItem.setRecId(((RefBalanceAccItem) selectedBalanceAcc.getData()).getRecId());
            RefBalanceAccVerList = (List<RefBalanceAccItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refBalanceAccItem, null);
            context.update(liferayNameSpace + ":frmRefBalanceAccVer");
            context.execute("PF('wDlgRefBalanceAccVer').show()");
        }else if (referenceCode.equals(RefConnOrgItem.REF_CODE)){
            selectedRefConnOrgVer = null;
            RefConnOrgItem refConnOrgItem = new RefConnOrgItem();
            refConnOrgItem.setRecId(selectedRefConnOrg.getRecId());
            RefConnOrgVerList = (List<RefConnOrgItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refConnOrgItem, null);
            context.update(liferayNameSpace + ":frmRefConnOrgVer");
            context.execute("PF('wDlgRefConnOrgVer').show()");
        }else if (referenceCode.equals(RefDepartmentItem.REF_CODE)){
            selectedRefDepartmentVer = null;
            RefDepartmentItem refDepartmentItem = new RefDepartmentItem();
            refDepartmentItem.setRecId(selectedRefDepartment.getRecId());
            RefDepartmentVerList = (List<RefDepartmentItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refDepartmentItem, null);
            context.update(liferayNameSpace + ":frmRefDepartmentVer");
            context.execute("PF('wDlgRefDepartmentVer').show()");
        }else if (referenceCode.equals(RefBankItem.REF_CODE)){
            selectedRefBankVer = null;
            RefBankItem refBankItem = new RefBankItem();
            refBankItem.setRecId(selectedRefBank.getRecId());
            RefBankVerList = (List<RefBankItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refBankItem, null);
            context.update(liferayNameSpace + ":frmRefBankVer");
            context.execute("PF('wDlgRefBankVer').show()");
        }else if (referenceCode.equals(RefRateAgencyItem.REF_CODE)){
            selectedRefRateAgencyVer = null;
            RefRateAgencyItem refRateAgencyItem = new RefRateAgencyItem();
            refRateAgencyItem.setRecId(selectedRefRateAgency.getRecId());
            RefRateAgencyVerList = (List<RefRateAgencyItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refRateAgencyItem, null);
            context.update(liferayNameSpace + ":frmRefRateAgencyVer");
            context.execute("PF('wDlgRefRateAgencyVer').show()");
        }else if (referenceCode.equals(RefCurrencyItem.REF_CODE)){
            selectedRefCurrencyVer = null;
            RefCurrencyItem refCurrencyItem = new RefCurrencyItem();
            refCurrencyItem.setRecId(selectedRefCurrency.getRecId());
            RefCurrencyVerList = (List<RefCurrencyItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refCurrencyItem, null);
            context.update(liferayNameSpace + ":frmRefCurrencyVer");
            context.execute("PF('wDlgRefCurrencyVer').show()");
        }else if (referenceCode.equals(RefCurrencyRateItem.REF_CODE)){
            selectedRefCurrencyRateVer = null;
            RefCurrencyRateItem refCurrencyRateItem = new RefCurrencyRateItem();
            refCurrencyRateItem.setRecId(selectedRefCurrencyRate.getRecId());
            RefCurrencyRateVerList = (List<RefCurrencyRateItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refCurrencyRateItem, null);
            context.update(liferayNameSpace + ":frmRefCurrencyRateVer");
            context.execute("PF('wDlgRefCurrencyRateVer').show()");
        }else if (referenceCode.equals(RefSubjectTypeItem.REF_CODE)){
            selectedRefSubjectTypeVer = null;
            RefSubjectTypeItem refSubjectTypeItem = new RefSubjectTypeItem();
            refSubjectTypeItem.setRecId(selectedRefSubjectType.getRecId());
            RefSubjectTypeVerList = (List<RefSubjectTypeItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refSubjectTypeItem, null);
            context.update(liferayNameSpace + ":frmRefSubjectTypeVer");
            context.execute("PF('wDlgRefSubjectTypeVer').show()");
        }else if (referenceCode.equals(RefRespondentItem.REF_CODE)){
            selectedRefRespondentVer = null;
            RefRespondentItem refRespondentItem = new RefRespondentItem();
            refRespondentItem.setRecId(selectedRefRespondent.getRecId());
            RefRespondentVerList = (List<RefRespondentItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refRespondentItem, null);
            context.update(liferayNameSpace + ":frmRefRespondentVer");
            context.execute("PF('wDlgRefRespondentVer').show()");
        }else if (referenceCode.equals(RefDocTypeItem.REF_CODE)){
            selectedRefDocTypeVer = null;
            RefDocTypeItem refDocTypeItem = new RefDocTypeItem();
            refDocTypeItem.setRecId(selectedRefDocType.getRecId());
            RefDocTypeVerList = (List<RefDocTypeItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refDocTypeItem, null);
            context.update(liferayNameSpace + ":frmRefDocTypeVer");
            context.execute("PF('wDlgRefDocTypeVer').show()");
        }else if (referenceCode.equals(RefDocumentItem.REF_CODE)){
            selectedRefDocumentVer = null;
            RefDocumentItem refDocumentItem = new RefDocumentItem();
            refDocumentItem.setRecId(selectedRefDocument.getRecId());
            RefDocumentVerList = (List<RefDocumentItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refDocumentItem, null);
            context.update(liferayNameSpace + ":frmRefDocumentVer");
            context.execute("PF('wDlgRefDocumentVer').show()");
        }else if (referenceCode.equals(RefIssuersItem.REF_CODE)){
            selectedRefIssuersVer = null;
            RefIssuersItem refIssuersItem = new RefIssuersItem();
            refIssuersItem.setRecId(selectedRefIssuers.getRecId());
            RefIssuersVerList = (List<RefIssuersItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refIssuersItem, null);
            context.update(liferayNameSpace + ":frmRefIssuersVer");
            context.execute("PF('wDlgRefIssuersVer').show()");
        }else if (referenceCode.equals(RefSecuritiesItem.REF_CODE)){
            selectedRefSecuritiesVer = null;
            RefSecuritiesItem refSecuritiesItem = new RefSecuritiesItem();
            refSecuritiesItem.setRecId(selectedRefSecurities.getRecId());
            RefSecuritiesVerList = (List<RefSecuritiesItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refSecuritiesItem, null);
            context.update(liferayNameSpace + ":frmRefSecuritiesVer");
            context.execute("PF('wDlgRefSecuritiesVer').show()");
        }else if (referenceCode.equals(RefVidOperItem.REF_CODE)){
            selectedRefVidOperVer = null;
            RefVidOperItem refVidOperItem = new RefVidOperItem();
            refVidOperItem.setRecId(selectedRefVidOper.getRecId());
            RefVidOperVerList = (List<RefVidOperItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refVidOperItem, null);
            context.update(liferayNameSpace + ":frmRefVidOperVer");
            context.execute("PF('wDlgRefVidOperVer').show()");
        }else if (referenceCode.equals(RefBranchItem.REF_CODE)){
            selectedRefBranchVer = null;
            RefBranchItem refBranchItem = new RefBranchItem();
            refBranchItem.setRecId(selectedRefBranch.getRecId());
            RefBranchVerList = (List<RefBranchItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refBranchItem, null);
            context.update(liferayNameSpace + ":frmRefBranchVer");
            context.execute("PF('wDlgRefBranchVer').show()");
        }else if (referenceCode.equals("ref_branch_old")){
            selectedRefBranchOldVer = null;
            SimpleReference refBranchItem = new SimpleReference();
            refBranchItem.setRecId(selectedRefBranchOld.getRecId());
            refBranchItem.setRefCode(referenceCode);
            RefBranchOldVerList = sessionBean.getReference().getRefSimpleListByParams(null, refBranchItem);
            context.update(liferayNameSpace + ":frmRefBranchOldVer");
            context.execute("PF('wDlgRefBranchOldVer').show()");
        }else if (referenceCode.equals(RefListingEstimationItem.REF_CODE)){
            selectedRefListingEstimationVer = null;
            RefListingEstimationItem refListingEstimationItem = new RefListingEstimationItem();
            refListingEstimationItem.setRecId(selectedRefListingEstimation.getRecId());
            RefListingEstimationVerList = (List<RefListingEstimationItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refListingEstimationItem, null);
            context.update(liferayNameSpace + ":frmRefListingEstimationVer");
            context.execute("PF('wDlgRefListingEstimationVer').show()");
        }else if (referenceCode.equals(RefRatingEstimationItem.REF_CODE)){
            selectedRefRatingEstimationVer = null;
            RefRatingEstimationItem refRatingEstimationItem = new RefRatingEstimationItem();
            refRatingEstimationItem.setRecId(selectedRefRatingEstimation.getRecId());
            RefRatingEstimationVerList = (List<RefRatingEstimationItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refRatingEstimationItem, null);
            context.update(liferayNameSpace + ":frmRefRatingEstimationVer");
            context.execute("PF('wDlgRefRatingEstimationVer').show()");
        }else if (referenceCode.equals(RefRatingCategoryItem.REF_CODE)){
            selectedRefRatingCategoryVer = null;
            RefRatingCategoryItem refRatingCategoryItem = new RefRatingCategoryItem();
            refRatingCategoryItem.setRecId(selectedRefRatingCategory.getRecId());
            RefRatingCategoryVerList = (List<RefRatingCategoryItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refRatingCategoryItem, null);
            context.update(liferayNameSpace + ":frmRefRatingCategoryVer");
            context.execute("PF('wDlgRefRatingCategoryVer').show()");
        }/*else if (referenceCode.equals("ref_request_type")){
            selectedRefRequestTypeVer = null;
            SimpleReference refRequestTypeItem = new SimpleReference();
            refRequestTypeItem.setRecId(selectedRefRequestType.getRecId());
            refRequestTypeItem.setRefCode(referenceCode);
            RefRequestTypeVerList = sessionBean.getReference().getRefSimpleListByParams(null, refRequestTypeItem);
            context.update(liferayNameSpace + ":frmRefRequestTypeVer");
            context.execute("PF('wDlgRefRequestTypeVer').show()");
        }else if (referenceCode.equals("ref_request_way")){
            selectedRefRequestWayVer = null;
            SimpleReference refRequestWayItem = new SimpleReference();
            refRequestWayItem.setRecId(selectedRefRequestWay.getRecId());
            refRequestWayItem.setRefCode(referenceCode);
            RefRequestWayVerList = sessionBean.getReference().getRefSimpleListByParams(null, refRequestWayItem);
            context.update(liferayNameSpace + ":frmRefRequestWayVer");
            context.execute("PF('wDlgRefRequestWayVer').show()");
        }else if (referenceCode.equals("ref_market_kind")) {
            selectedRefMarketKindVer = null;
            SimpleReference refMarketKindItem = new SimpleReference();
            refMarketKindItem.setRecId(selectedRefMarketKind.getRecId());
            refMarketKindItem.setRefCode(referenceCode);
            RefMarketKindVerList = sessionBean.getReference().getRefSimpleListByParams(null, refMarketKindItem);
            context.update(liferayNameSpace + ":frmRefMarketKindVer");
            context.execute("PF('wDlgRefMarketKindVer').show()");
        }else if (referenceCode.equals("ref_category")){
            selectedRefCategoryVer = null;
            SimpleReference refCategoryItem = new SimpleReference();
            refCategoryItem.setRecId(selectedRefCategory.getRecId());
            refCategoryItem.setRefCode(referenceCode);
            RefCategoryVerList = sessionBean.getReference().getRefSimpleListByParams(null, refCategoryItem);
            context.update(liferayNameSpace + ":frmRefCategoryVer");
            context.execute("PF('wDlgRefCategoryVer').show()");
        }else if (referenceCode.equals("ref_subcategory")){
            selectedRefSubCategoryVer = null;
            SimpleReference refSubCategoryItem = new SimpleReference();
            refSubCategoryItem.setRecId(selectedRefSubCategory.getRecId());
            refSubCategoryItem.setRefCode(referenceCode);
            RefSubCategoryVerList = sessionBean.getReference().getRefSimpleListByParams(null, refSubCategoryItem);
            context.update(liferayNameSpace + ":frmRefSubCategoryVer");
            context.execute("PF('wDlgRefSubCategoryVer').show()");
        }else if (referenceCode.equals("ref_account_type")){
            selectedRefAccountTypeVer = null;
            SimpleReference refAccountTypeItem = new SimpleReference();
            refAccountTypeItem.setRecId(selectedRefAccountType.getRecId());
            refAccountTypeItem.setRefCode(referenceCode);
            RefAccountTypeVerList = sessionBean.getReference().getRefSimpleListByParams(null, refAccountTypeItem);
            context.update(liferayNameSpace + ":frmRefAccountTypeVer");
            context.execute("PF('wDlgRefAccountTypeVer').show()");
        }else if (referenceCode.equals("ref_subaccount_type")){
            selectedRefSubAccountTypeVer = null;
            SimpleReference refSubAccountTypeItem = new SimpleReference();
            refSubAccountTypeItem.setRecId(selectedRefSubAccountType.getRecId());
            refSubAccountTypeItem.setRefCode(referenceCode);
            RefSubAccountTypeVerList = sessionBean.getReference().getRefSimpleListByParams(null, refSubAccountTypeItem);
            context.update(liferayNameSpace + ":frmRefSubAccountTypeVer");
            context.execute("PF('wDlgRefSubAccountTypeVer').show()");
        }else if (referenceCode.equals("ref_type_holder_acc")){
            selectedRefTypeHolderAccVer = null;
            SimpleReference refTypeHolderAccItem = new SimpleReference();
            refTypeHolderAccItem.setRecId(selectedRefTypeHolderAcc.getRecId());
            refTypeHolderAccItem.setRefCode(referenceCode);
            RefTypeHolderAccVerList = sessionBean.getReference().getRefSimpleListByParams(null, refTypeHolderAccItem);
            context.update(liferayNameSpace + ":frmRefTypeHolderAccVer");
            context.execute("PF('wDlgRefTypeHolderAccVer').show()");
        }else if (referenceCode.equals("ref_request_feature")){
            selectedRefRequestFeatureVer = null;
            SimpleReference refRequestFeatureItem = new SimpleReference();
            refRequestFeatureItem.setRecId(selectedRefRequestFeature.getRecId());
            refRequestFeatureItem.setRefCode(referenceCode);
            RefRequestFeatureVerList = sessionBean.getReference().getRefSimpleListByParams(null, refRequestFeatureItem);
            context.update(liferayNameSpace + ":frmRefRequestFeatureVer");
            context.execute("PF('wDlgRefRequestFeatureVer').show()");
        }else if (referenceCode.equals("ref_request_sts")){
            selectedRefRequestStsVer = null;
            SimpleReference refRequestStsItem = new SimpleReference();
            refRequestStsItem.setRecId(selectedRefRequestSts.getRecId());
            refRequestStsItem.setRefCode(referenceCode);
            RefRequestStsVerList = sessionBean.getReference().getRefSimpleListByParams(null, refRequestStsItem);
            context.update(liferayNameSpace + ":frmRefRequestStsVer");
            context.execute("PF('wDlgRefRequestStsVer').show()");
        }else if (referenceCode.equals("ref_repo_kind")){
            selectedRefRepoKindVer = null;
            SimpleReference refRepoKindItem = new SimpleReference();
            refRepoKindItem.setRecId(selectedRefRepoKind.getRecId());
            refRepoKindItem.setRefCode(referenceCode);
            RefRepoKindVerList = sessionBean.getReference().getRefSimpleListByParams(null, refRepoKindItem);
            context.update(liferayNameSpace + ":frmRefRepoKindVer");
            context.execute("PF('wDlgRefRepoKindVer').show()");
        }else if (referenceCode.equals("ref_market_type")){
            selectedRefMarketTypeVer = null;
            SimpleReference refMarketTypeItem = new SimpleReference();
            refMarketTypeItem.setRecId(selectedRefMarketType.getRecId());
            refMarketTypeItem.setRefCode(referenceCode);
            RefMarketTypeVerList = sessionBean.getReference().getRefSimpleListByParams(null, refMarketTypeItem);
            context.update(liferayNameSpace + ":frmRefMarketTypeVer");
            context.execute("PF('wDlgRefMarketTypeVer').show()");
        }else if (referenceCode.equals("ref_trad_method")){
            selectedRefTradMethodVer = null;
            SimpleReference refTradMethodItem = new SimpleReference();
            refTradMethodItem.setRecId(selectedRefTradMethod.getRecId());
            refTradMethodItem.setRefCode(referenceCode);
            RefTradMethodVerList = sessionBean.getReference().getRefSimpleListByParams(null, refTradMethodItem);
            context.update(liferayNameSpace + ":frmRefTradMethodVer");
            context.execute("PF('wDlgRefTradMethodVer').show()");
        }else if (referenceCode.equals("ref_oper_type")){
            selectedRefOperTypeVer = null;
            SimpleReference refOperTypeItem = new SimpleReference();
            refOperTypeItem.setRecId(selectedRefOperType.getRecId());
            refOperTypeItem.setRefCode(referenceCode);
            RefOperTypeVerList = sessionBean.getReference().getRefSimpleListByParams(null, refOperTypeItem);
            context.update(liferayNameSpace + ":frmRefOperTypeVer");
            context.execute("PF('wDlgRefOperTypeVer').show()");
        }else if (referenceCode.equals("ref_deal_sts")){
            selectedRefDealStsVer = null;
            SimpleReference refDealStsItem = new SimpleReference();
            refDealStsItem.setRecId(selectedRefDealSts.getRecId());
            refDealStsItem.setRefCode(referenceCode);
            RefDealStsVerList = sessionBean.getReference().getRefSimpleListByParams(null, refDealStsItem);
            context.update(liferayNameSpace + ":frmRefDealStsVer");
            context.execute("PF('wDlgRefDealStsVer').show()");
        }*/else if (referenceCode.equals(RefMrpItem.REF_CODE)) {
            selectedRefMrpVer = null;
            RefMrpItem refMrpItem = new RefMrpItem();
            refMrpItem.setRecId(selectedRefMrp.getRecId());
            RefMrpVerList = (List<RefMrpItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refMrpItem, null);
            context.update(liferayNameSpace + ":frmRefMrpVer");
            context.execute("PF('wDlgRefMrpVer').show()");
        }else if (referenceCode.equals("ref_org_type")){
            selectedRefOrgTypeVer = null;
            SimpleReference refOrgTypeItem = new SimpleReference();
            refOrgTypeItem.setRecId(selectedRefOrgType.getRecId());
            refOrgTypeItem.setRefCode(referenceCode);
            RefOrgTypeVerList = sessionBean.getReference().getRefSimpleListByParams(null, refOrgTypeItem);
            context.update(liferayNameSpace + ":frmRefOrgTypeVer");
            context.execute("PF('wDlgRefOrgTypeVer').show()");
        }else if (referenceCode.equals(RefMfoRegItem.REF_CODE)) {
            selectedRefMfoRegVer = null;
            RefMfoRegItem refMfoRegItem = new RefMfoRegItem();
            refMfoRegItem.setRecId(selectedRefMfoReg.getRecId());
            RefMfoRegVerList = (List<RefMfoRegItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refMfoRegItem, null);
            context.update(liferayNameSpace + ":frmRefMfoRegVer");
            context.execute("PF('wDlgRefMfoRegVer').show()");
        }else if (referenceCode.equals(RefDealBAItem.REF_CODE)) {
            selectedRefDealBAVer = null;
            RefDealBAItem refDealBAItem = new RefDealBAItem();
            refDealBAItem.setRecId(selectedRefDealBA.getRecId());
            RefDealBAVerList = (List<RefDealBAItem>)sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refDealBAItem, null);
            context.update(liferayNameSpace + ":frmRefDealBAVer");
            context.execute("PF('wDlgRefDealBAVer').show()");
        }else if (referenceCode.equals("ref_type_activity")) {
            setSelectedRefTypeActivityVer(null);
            RefTypeActivityItem refTypeActivityItem = new RefTypeActivityItem();
            refTypeActivityItem.setRecId(getSelectedRefTypeActivity().getRecId());
            //RefTypeActivityVerList = sessionBean.getReference().getRefTypeActivityListByParams(null, refTypeActivityItem);
            setRefTypeActivityVerList((List<RefTypeActivityItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refTypeActivityItem, null));
            context.update(liferayNameSpace + ":frmRefTypeActivityVer");
            context.execute("PF('wDlgRefTypeActivityVer').show()");
        }else if (referenceCode.equals("ref_npa")){
            setSelectedRefNpaVer(null);
            RefNpaItem refNpaItem = new RefNpaItem();
            refNpaItem.setRecId(getSelectedRefNpa().getRecId());
            refNpaItem.setRefCode(referenceCode);
            //setRefNpaVerList(sessionBean.getReference().getRefNpaListByParams(null, refNpaItem));
            setRefNpaVerList((List<RefNpaItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refNpaItem, null));
            context.update(liferayNameSpace + ":frmRefNpaVer");
            context.execute("PF('wDlgRefNpaVer').show()");
        }else if (referenceCode.equals("ref_wkd_holidays")){
            setSelectedRefWkdHolidayVer(null);
            RefWkdHolidayItem refWkdHolidayItem = new RefWkdHolidayItem();
            refWkdHolidayItem.setRecId(getSelectedRefWkdHoliday().getRecId());
            refWkdHolidayItem.setRefCode(referenceCode);
            //setRefWkdHolidayVerList(sessionBean.getReference().getRefWkdHolidayListByParams(null, refWkdHolidayItem));
            setRefWkdHolidayVerList((List<RefWkdHolidayItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refWkdHolidayItem, null));
            context.update(liferayNameSpace + ":frmRefWkdHolidayVer");
            context.execute("PF('wDlgRefWkdHolidayVer').show()");
        }else if (referenceCode.equals("ref_finrep_indic")){
            setSelectedRefFinRepIndicVer(null);
            SimpleReference refFinRepIndicItem = new SimpleReference();
            refFinRepIndicItem.setRecId(getSelectedRefFinRepIndic().getRecId());
            refFinRepIndicItem.setRefCode(referenceCode);
            setRefFinRepIndicVerList(sessionBean.getReference().getRefSimpleListByParams(null, refFinRepIndicItem));
            context.update(liferayNameSpace + ":frmRefFinRepIndicVer");
            context.execute("PF('wDlgRefFinRepIndicVer').show()");
        }else if (referenceCode.equals("ref_type_deals")){
            setSelectedRefTypeDealsVer(null);
            SimpleReference refTypeDealsItem = new SimpleReference();
            refTypeDealsItem.setRecId(getSelectedRefTypeDeals().getRecId());
            refTypeDealsItem.setRefCode(referenceCode);
            setRefTypeDealsVerList(sessionBean.getReference().getRefSimpleListByParams(null, refTypeDealsItem));
            context.update(liferayNameSpace + ":frmRefTypeDealsVer");
            context.execute("PF('wDlgRefTypeDealsVer').show()");
        }else if (referenceCode.equals("ref_degree_relation")){
            setSelectedRefDegreeRelationVer(null);
            SimpleReference refDegreeRelationItem = new SimpleReference();
            refDegreeRelationItem.setRecId(getSelectedRefDegreeRelation().getRecId());
            refDegreeRelationItem.setRefCode(referenceCode);
            setRefDegreeRelationVerList(sessionBean.getReference().getRefSimpleListByParams(null, refDegreeRelationItem));
            context.update(liferayNameSpace + ":frmRefDegreeRelationVer");
            context.execute("PF('wDlgRefDegreeRelationVer').show()");
        }else if (referenceCode.equals("ref_sign_related")){
            setSelectedRefSignRelatedVer(null);
            SimpleReference refSignRelatedItem = new SimpleReference();
            refSignRelatedItem.setRecId(getSelectedRefSignRelated().getRecId());
            refSignRelatedItem.setRefCode(referenceCode);
            setRefSignRelatedVerList(sessionBean.getReference().getRefSimpleListByParams(null, refSignRelatedItem));
            context.update(liferayNameSpace + ":frmRefSignRelatedVer");
            context.execute("PF('wDlgRefSignRelatedVer').show()");
        }else if (referenceCode.equals("ref_type_risk")){
            setSelectedRefTypeRiskVer(null);
            SimpleReference refTypeRiskItem = new SimpleReference();
            refTypeRiskItem.setRecId(getSelectedRefTypeRisk().getRecId());
            refTypeRiskItem.setRefCode(referenceCode);
            setRefTypeRiskVerList(sessionBean.getReference().getRefSimpleListByParams(null, refTypeRiskItem));
            context.update(liferayNameSpace + ":frmRefTypeRiskVer");
            context.execute("PF('wDlgRefTypeRiskVer').show()");
        }else if (referenceCode.equals("ref_basisofcontrol")) {
            setSelectedRefBasisofControlVer(null);
            RefBasisofControlItem refBasisofControlItem = new RefBasisofControlItem();
            refBasisofControlItem.setRecId(getSelectedRefBasisofControl().getRecId());
            //RefPostVerList = sessionBean.getReference().getRefPostListByParams(null, refPostItem);
            RefBasisofControlVerList = (List<RefBasisofControlItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refBasisofControlItem, null);
            context.update(liferayNameSpace + ":frmRefBasisofControlVer");
            context.execute("PF('wDlgRefBasisofControlVer').show()");
        }else if (referenceCode.equals("ref_extind")) {
            setSelectedRefExtIndVer(null);
            RefExtIndicatorItem refExtIndicatorItem = new RefExtIndicatorItem();
            refExtIndicatorItem.setRecId(selectedRefExtInd.getRecId());
            RefExtIndVerList = (List<RefExtIndicatorItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refExtIndicatorItem, null);
            context.update(liferayNameSpace + ":frmRefExtIndVer");
            context.execute("PF('wDlgRefExtIndVer').show()");
        }else if (referenceCode.equals(RefMajorMemberItem.REF_CODE)){
            selectedRefMajorMemberVer = null;
            RefMajorMemberItem refMajorMemberItem = new RefMajorMemberItem();
            refMajorMemberItem.setRecId(selectedRefMajorMember.getRecId());
            RefMajorMemberVerList = (List<RefMajorMemberItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refMajorMemberItem, null);
            context.update(liferayNameSpace + ":frmRefMajorMemberVer");
            context.execute("PF('wDlgRefMajorMemberVer').show()");
        }else if (referenceCode.equals(RefPeriodAlgItem.REF_CODE)) {
            selectedRefPeriodAlgVer = null;
            RefPeriodAlgItem refPeriodAlgItem = new RefPeriodAlgItem();
            refPeriodAlgItem.setRecId(selectedRefPeriodAlg.getRecId());
            refPeriodAlgVerList = (List<RefPeriodAlgItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refPeriodAlgItem, null);
            context.update(liferayNameSpace + ":frmRefPeriodAlgVer");
            context.execute("PF('wDlgRefPeriodAlgVer').show()");
        }else if (referenceCode.equals(RefPeriodItem.REF_CODE)) {
            selectedRefPeriodVer = null;
            RefPeriodItem refPeriodItem = new RefPeriodItem();
            refPeriodItem.setRecId(selectedRefPeriod.getRecId());
            refPeriodVerList = (List<RefPeriodItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refPeriodItem, null);
            context.update(liferayNameSpace + ":frmRefPeriodVer");
            context.execute("PF('wDlgRefPeriodVer').show()");
        }else if (referenceCode.equals(RefInsurGroupsItem.REF_CODE)){
            selectedRefInsurGroupsVer = null;
            RefInsurGroupsItem refInsurGroupsItem = new RefInsurGroupsItem();
            refInsurGroupsItem.setRecId(((RefInsurGroupsItem) selectedInsurGroups.getData()).getRecId());
            RefInsurGroupsVerList = (List<RefInsurGroupsItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refInsurGroupsItem, null);
            context.update(liferayNameSpace + ":frmRefInsurGroupsVer");
            context.execute("PF('wDlgRefInsurGroupsVer').show()");
        }else if (referenceCode.equals(RefBankConglomeratesItem.REF_CODE)){
            selectedRefBankConglVer = null;
            RefBankConglomeratesItem refBankConglItem = new RefBankConglomeratesItem();
            refBankConglItem.setRecId(((RefBankConglomeratesItem) selectedBankCongl.getData()).getRecId());
            RefBankConglVerList = (List<RefBankConglomeratesItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refBankConglItem, null);
            context.update(liferayNameSpace + ":frmRefBankConglVer");
            context.execute("PF('wDlgRefBankConglVer').show()");
        } else if (referenceCode.equals(RefShareHoldersItem.REF_CODE)){
            selectedRefShareHoldVer = null;
            RefShareHoldersItem refShareHoldItem = new RefShareHoldersItem();
            refShareHoldItem.setRecId(((RefShareHoldersItem) selectedShareHold.getData()).getRecId());
            RefShareHoldVerList = (List<RefShareHoldersItem>) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, refShareHoldItem, null);
            context.update(liferayNameSpace + ":frmRefShareHoldVer");
            context.execute("PF('wDlgRefShareHoldVer').show()");
        }
    }

    public void deleteRefMajorMemOrgItem(){
       Long id = null;

       AuditEvent auditEvent = new AuditEvent();
       auditEvent.setCodeObject("ref_major_memorgs");
       auditEvent.setNameObject(null);
       auditEvent.setIdKindEvent(50L);
       auditEvent.setDateEvent(sessionBean.getIntegration().getNewDateFromBackEndServer());
       auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
       auditEvent.setUserId(userId);
       auditEvent.setUserLocation(userLocation);

        try {

            id = selectedRefMajorMemberOrg.getId();
            auditEvent.setDateIn(selectedRefMajorMemberOrg.getBeginDate());

            sessionBean.getReference().deleteRefAbstractItem("ref_major_memorgs", id, auditEvent);

        }catch (OracleException ex) {
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", ex.getMessage()));
            return;
        }

        refreshSimpleRefList("update_ref");

    }

    public void deleteSimpleRefItem(){
        Long id = null;

        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setCodeObject(referenceCode);
        auditEvent.setNameObject(null);
        auditEvent.setIdKindEvent(50L);
        auditEvent.setDateEvent(sessionBean.getIntegration().getNewDateFromBackEndServer());
        auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
        auditEvent.setUserId(userId);
        auditEvent.setUserLocation(userLocation);

        try {
            if (referenceCode.equals(RefPostItem.REF_CODE)) {
                id = selectedRefPost.getId();
                auditEvent.setDateIn(selectedRefPost.getBeginDate());
            } else if (referenceCode.equals(RefPersonItem.REF_CODE)) {
                id = selectedRefPerson.getId();
                auditEvent.setDateIn(selectedRefPerson.getBeginDate());
            } else if (referenceCode.equals(RefLegalPersonItem.REF_CODE)) {
                id = selectedRefLegalPerson.getId();
                auditEvent.setDateIn(selectedRefLegalPerson.getBeginDate());
            } else if (referenceCode.equals(RefManagersItem.REF_CODE)) {
                id = selectedRefManagers.getId();
                auditEvent.setDateIn(selectedRefManagers.getBeginDate());
            } else if (referenceCode.equals(RefTypeBusEntityItem.REF_CODE)) {
                id = selectedRefTypeBusEntity.getId();
                auditEvent.setDateIn(selectedRefTypeBusEntity.getBeginDate());
            } else if (referenceCode.equals(RefRequirementItem.REF_CODE)) {
                id = selectedRefRequirement.getId();
                auditEvent.setDateIn(selectedRefRequirement.getBeginDate());
            } else if (referenceCode.equals(RefTypeProvideItem.REF_CODE)) {
                id = selectedRefTypeProvide.getId();
                auditEvent.setDateIn(selectedRefTypeProvide.getBeginDate());
            } else if (referenceCode.equals(RefTransTypeItem.REF_CODE)) {
                id = selectedRefTransType.getId();
                auditEvent.setDateIn(selectedRefTransType.getBeginDate());
            } else if (referenceCode.equals(RefBalanceAccItem.REF_CODE)) {
                selectedRefBalanceAcc = (RefBalanceAccItem) selectedBalanceAcc.getData();
                auditEvent.setDateIn(selectedRefBalanceAcc.getBeginDate());
                id = selectedRefBalanceAcc.getId();
            } else if (referenceCode.equals(RefConnOrgItem.REF_CODE)) {
                id = selectedRefConnOrg.getId();
                auditEvent.setDateIn(selectedRefConnOrg.getBeginDate());
            } else if (referenceCode.equals(RefSubjectTypeItem.REF_CODE)) {
                id = selectedRefSubjectType.getId();
                auditEvent.setDateIn(selectedRefSubjectType.getBeginDate());
            } else if (referenceCode.equals(RefBankItem.REF_CODE)) {
                id = selectedRefBank.getId();
                auditEvent.setDateIn(selectedRefBank.getBeginDate());
            } else if (referenceCode.equals(RefRespondentItem.REF_CODE)) {
                id = selectedRefRespondent.getId();
                auditEvent.setDateIn(selectedRefRespondent.getBeginDate());
            } else if (referenceCode.equals(RefDocTypeItem.REF_CODE)) {
                id = selectedRefDocType.getId();
                auditEvent.setDateIn(selectedRefDocType.getBeginDate());
            } else if (referenceCode.equals(RefDocumentItem.REF_CODE)) {
                id = selectedRefDocument.getId();
                auditEvent.setDateIn(selectedRefDocument.getBeginDate());
            } else if (referenceCode.equals(RefVidOperItem.REF_CODE)) {
                id = selectedRefVidOper.getId();
                auditEvent.setDateIn(selectedRefVidOper.getBeginDate());
            }/*else if (referenceCode.equals("ref_reports_rules")) {
                id = selectedRefReportsRules.getId();
            }*//* else if (referenceCode.equals("ref_request_type")) {
                id = selectedRefRequestType.getId();
                auditEvent.setDateIn(selectedRefRequestType.getBeginDate());
            } else if (referenceCode.equals("ref_request_way")) {
                id = selectedRefRequestWay.getId();
                auditEvent.setDateIn(selectedRefRequestWay.getBeginDate());
            } else if (referenceCode.equals("ref_market_kind")) {
                id = selectedRefMarketKind.getId();
                auditEvent.setDateIn(selectedRefMarketKind.getBeginDate());
            } else if (referenceCode.equals("ref_category")) {
                id = selectedRefCategory.getId();
                auditEvent.setDateIn(selectedRefCategory.getBeginDate());
            } else if (referenceCode.equals("ref_subcategory")) {
                id = selectedRefSubCategory.getId();
                auditEvent.setDateIn(selectedRefSubCategory.getBeginDate());
            } else if (referenceCode.equals("ref_account_type")) {
                id = selectedRefAccountType.getId();
                auditEvent.setDateIn(selectedRefAccountType.getBeginDate());
            } else if (referenceCode.equals("ref_subaccount_type")) {
                id = selectedRefSubAccountType.getId();
                auditEvent.setDateIn(selectedRefSubAccountType.getBeginDate());
            } else if (referenceCode.equals("ref_type_holder_acc")) {
                id = selectedRefTypeHolderAcc.getId();
                auditEvent.setDateIn(selectedRefTypeHolderAcc.getBeginDate());
            } else if (referenceCode.equals("ref_request_feature")) {
                id = selectedRefRequestFeature.getId();
                auditEvent.setDateIn(selectedRefRequestFeature.getBeginDate());
            } else if (referenceCode.equals("ref_request_sts")) {
                id = selectedRefRequestSts.getId();
                auditEvent.setDateIn(selectedRefRequestSts.getBeginDate());
            } else if (referenceCode.equals("ref_repo_kind")) {
                id = selectedRefRepoKind.getId();
                auditEvent.setDateIn(selectedRefRepoKind.getBeginDate());
            } else if (referenceCode.equals("ref_market_type")) {
                id = selectedRefMarketType.getId();
                auditEvent.setDateIn(selectedRefMarketType.getBeginDate());
            } else if (referenceCode.equals("ref_trad_method")) {
                id = selectedRefTradMethod.getId();
                auditEvent.setDateIn(selectedRefTradMethod.getBeginDate());
            } else if (referenceCode.equals("ref_oper_type")) {
                id = selectedRefOperType.getId();
                auditEvent.setDateIn(selectedRefOperType.getBeginDate());
            } else if (referenceCode.equals("ref_deal_sts")) {
                id = selectedRefDealSts.getId();
                auditEvent.setDateIn(selectedRefDealSts.getBeginDate());
            }*/ else if (referenceCode.equals(RefMrpItem.REF_CODE)) {
                id = selectedRefMrp.getId();
                auditEvent.setDateIn(selectedRefMrp.getBeginDate());
            } else if (referenceCode.equals("ref_org_type")) {
                id = selectedRefOrgType.getId();
                auditEvent.setDateIn(selectedRefOrgType.getBeginDate());
            } else if (referenceCode.equals(RefMfoRegItem.REF_CODE)) {
                id = selectedRefMfoReg.getId();
                auditEvent.setDateIn(selectedRefMfoReg.getBeginDate());
            }else if (referenceCode.equals(RefDealBAItem.REF_CODE)) {
                id = selectedRefDealBA.getId();
                auditEvent.setDateIn(selectedRefDealBA.getBeginDate());
            } else if (referenceCode.equals("ref_type_activity")) {
                id = selectedRefTypeActivity.getId();
                auditEvent.setDateIn(selectedRefTypeActivity.getBeginDate());
            }else if (referenceCode.equals("ref_npa")) {
                id = selectedRefNpa.getId();
                auditEvent.setDateIn(selectedRefNpa.getBeginDate());
            }else if (referenceCode.equals("ref_wkd_holidays")) {
                id = selectedRefWkdHoliday.getId();
                auditEvent.setDateIn(selectedRefWkdHoliday.getBeginDate());
            }else if (referenceCode.equals("ref_finrep_indic")) {
                id = selectedRefFinRepIndic.getId();
                auditEvent.setDateIn(selectedRefFinRepIndic.getBeginDate());
            }else if (referenceCode.equals("ref_type_deals")) {
                id = selectedRefTypeDeals.getId();
                auditEvent.setDateIn(selectedRefTypeDeals.getBeginDate());
            }else if (referenceCode.equals("ref_degree_relation")) {
                id = selectedRefDegreeRelation.getId();
                auditEvent.setDateIn(selectedRefDegreeRelation.getBeginDate());
            }else if (referenceCode.equals("ref_sign_related")) {
                id = selectedRefSignRelated.getId();
                auditEvent.setDateIn(selectedRefSignRelated.getBeginDate());
            }else if (referenceCode.equals("ref_type_risk")) {
                id = selectedRefTypeRisk.getId();
                auditEvent.setDateIn(selectedRefTypeRisk.getBeginDate());
            }else if (referenceCode.equals("ref_basisofcontrol")) {
                id = selectedRefBasisofControl.getId();
                auditEvent.setDateIn(selectedRefBasisofControl.getBeginDate());
            }else if (referenceCode.equals("ref_extind")) {
                id = selectedRefExtInd.getId();
                auditEvent.setDateIn(selectedRefExtInd.getBeginDate());
            }else if (referenceCode.equals("ref_major_member")) {
                id = selectedRefMajorMember.getId();
                auditEvent.setDateIn(selectedRefMajorMember.getBeginDate());
            }else if (referenceCode.equals(RefPeriodAlgItem.REF_CODE)) {
                id = selectedRefPeriodAlg.getId();
                auditEvent.setDateIn(selectedRefPeriodAlg.getBeginDate());
            }else if (referenceCode.equals(RefPeriodItem.REF_CODE)) {
                id = selectedRefPeriod.getId();
                auditEvent.setDateIn(selectedRefPeriod.getBeginDate());
            }else if (referenceCode.equals(RefInsurGroupsItem.REF_CODE)) {
                selectedRefInsurGroups = (RefInsurGroupsItem) selectedInsurGroups.getData();
                auditEvent.setDateIn(selectedRefInsurGroups.getBeginDate());
                id = selectedRefInsurGroups.getId();
            }else if (referenceCode.equals(RefBankConglomeratesItem.REF_CODE)) {
                selectedRefBankCongl = (RefBankConglomeratesItem) selectedBankCongl.getData();
                auditEvent.setDateIn(selectedRefBankCongl.getBeginDate());
                id = selectedRefBankCongl.getId();
            }else if (referenceCode.equals(RefShareHoldersItem.REF_CODE)) {
                selectedRefShareHold = (RefShareHoldersItem) selectedShareHold.getData();
                auditEvent.setDateIn(selectedRefShareHold.getBeginDate());
                id = selectedRefShareHold.getId();
            }

            sessionBean.getReference().deleteRefAbstractItem(referenceCode, id, auditEvent);

        }catch (OracleException ex) {
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", ex.getMessage()));
            return;
        }
        refreshSimpleRefList("update_ref");
    }

    public void getSimpleRefItem(Boolean fromVer,String type){
        editType = type;
        editKind = null;
        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();
        RequestContext context = RequestContext.getCurrentInstance();
        String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();

        if(editType.equals("insert_item"))
            modalName = "Добавление новой записи";
        else if (editType.equals("update_item"))
            modalName = "Редактирование записи";
        else if (editType.equals("refresh_item"))
            modalName = "Обновление записи";
        else if (editType.equals("view_item"))
            modalName = "Просмотр записи";

        if (editType.equals("insert_item") || editType.equals("insert_child_item")){
            userName = null;
            userLocation = null;
            datlast = null;
        }

        if(editType.equals("update_item"))
            setIsVisibleEditKnd(true);
        else
            setIsVisibleEditKnd(false);

        if (referenceCode.equals(RefPostItem.REF_CODE)){
            typePostList = sessionBean.getReference().getRefElements("TYPE_POST", false);

            if (editType.equals("insert_item")){
                refPostItem = new RefPostItem();
                refPostItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefPostItem filter = new RefPostItem();
                filter.setId(fromVer ? selectedRefPostVer.getId() : selectedRefPost.getId());
                refPostItem = (RefPostItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refPostItem.getUserName();
                userLocation = refPostItem.getUserLocation();
                datlast = refPostItem.getDatlast();
            }

            context.update(liferayNameSpace + ":frmRefPostModal");
            context.execute("PF('wDlgRefPostModal').show()");
        }else if (referenceCode.equals(RefPersonItem.REF_CODE)){
            getPersonData(editType.equals("insert_item") ? null : (fromVer ? selectedRefPersonVer.getId() : selectedRefPerson.getId()), editType, date);
        }else if (referenceCode.equals(RefLegalPersonItem.REF_CODE)){
            getLPData(editType.equals("insert_item") ? null : (fromVer ? selectedRefLegalPersonVer.getId() : selectedRefLegalPerson.getId()), editType, date);
        }else if (referenceCode.equals(RefCountryItem.REF_CODE)){
            RefCountryItem filter = new RefCountryItem();
            filter.setId(fromVer ? selectedRefCountryVer.getId() : selectedRefCountry.getId());
            refCountryItem = (RefCountryItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
            userName = refCountryItem.getUserName();
            userLocation = refCountryItem.getUserLocation();
            datlast = refCountryItem.getDatlast();
            context.update(liferayNameSpace + ":frmRefCountryModal");
            context.execute("PF('wDlgRefCountryModal').show()");
        }else if (referenceCode.equals(RefManagersItem.REF_CODE)){
            if (editType.equals("insert_item")){
                refManagersItem = new RefManagersItem();
                refManagersItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefManagersItem filter = new RefManagersItem();
                filter.setId(fromVer ? selectedRefManagersVer.getId() : selectedRefManagers.getId());
                refManagersItem = (RefManagersItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refManagersItem.getUserName();
                userLocation = refManagersItem.getUserLocation();
                datlast = refManagersItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefManagersModal");
            context.execute("PF('wDlgRefManagersModal').show()");
        }else if (referenceCode.equals(RefTypeBusEntityItem.REF_CODE)){
            if (editType.equals("insert_item")){
                refTypeBusEntityItem = new RefTypeBusEntityItem();
                refTypeBusEntityItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefTypeBusEntityItem filter = new RefTypeBusEntityItem();
                filter.setId(fromVer ? selectedRefTypeBusEntityVer.getId() : selectedRefTypeBusEntity.getId());
                refTypeBusEntityItem  = (RefTypeBusEntityItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refTypeBusEntityItem.getUserName();
                userLocation = refTypeBusEntityItem.getUserLocation();
                datlast = refTypeBusEntityItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefTypeBusEntityModal");
            context.execute("PF('wDlgRefTypeBusEntityModal').show()");
        }else if (referenceCode.equals(RefRegionItem.REF_CODE)) {
            RefRegionItem filter = new RefRegionItem();
            filter.setId(fromVer ? selectedRefRegionVer.getId() : selectedRefRegion.getId());
            refRegionItem  = (RefRegionItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
            userName = refRegionItem.getUserName();
            userLocation = refRegionItem.getUserLocation();
            datlast = refRegionItem.getDatlast();
            context.update(liferayNameSpace + ":frmRefRegionModal");
            context.execute("PF('wDlgRefRegionModal').show()");
        }else if (referenceCode.equals(RefRequirementItem.REF_CODE)){
            if (editType.equals("insert_item")){
                refRequirementItem = new RefRequirementItem();
                refRequirementItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefRequirementItem filter = new RefRequirementItem();
                filter.setId(fromVer ? selectedRefRequirementVer.getId() : selectedRefRequirement.getId());
                refRequirementItem  = (RefRequirementItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refRequirementItem.getUserName();
                userLocation = refRequirementItem.getUserLocation();
                datlast = refRequirementItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefRequirementModal");
            context.execute("PF('wDlgRefRequirementModal').show()");
        }else if (referenceCode.equals(RefTypeProvideItem.REF_CODE)){
            if (editType.equals("insert_item")){
                refTypeProvideItem = new RefTypeProvideItem();
                refTypeProvideItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefTypeProvideItem filter = new RefTypeProvideItem();
                filter.setId(fromVer ? selectedRefTypeProvideVer.getId() : selectedRefTypeProvide.getId());
                refTypeProvideItem = (RefTypeProvideItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refTypeProvideItem.getUserName();
                userLocation = refTypeProvideItem.getUserLocation();
                datlast = refTypeProvideItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefTypeProvideModal");
            context.execute("PF('wDlgRefTypeProvideModal').show()");
        }else if (referenceCode.equals(RefTransTypeItem.REF_CODE)){
            if (editType.equals("insert_item")){
                refTransTypeItem = new RefTransTypeItem();
                refTransTypeItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefTransTypeItem filter = new RefTransTypeItem();
                filter.setId(fromVer ? selectedRefTransTypeVer.getId() : selectedRefTransType.getId());
                refTransTypeItem = (RefTransTypeItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refTransTypeItem.getUserName();
                userLocation = refTransTypeItem.getUserLocation();
                datlast = refTransTypeItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefTransTypeModal");
            context.execute("PF('wDlgRefTransTypeModal').show()");
        }else if (referenceCode.equals(RefBalanceAccItem.REF_CODE)){
            if (editType.equals("insert_item")){
                refBalanceAccItem = new RefBalanceAccItem();
                refBalanceAccItem.setBeginDate(date);
                if(selectedBalanceAcc != null) {
                    refBalanceAccItem.setParentCode(((RefBalanceAccItem) selectedBalanceAcc.getData()).getParentCode());
                    refBalanceAccItem.setLevelCode(((RefBalanceAccItem) selectedBalanceAcc.getData()).getLevelCode());
                }
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefBalanceAccItem filter = new RefBalanceAccItem();
                filter.setId(fromVer ? selectedRefBalanceAccVer.getId() : ((RefBalanceAccItem)selectedBalanceAcc.getData()).getId());
                refBalanceAccItem = (RefBalanceAccItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refBalanceAccItem.getUserName();
                userLocation = refBalanceAccItem.getUserLocation();
                datlast = refBalanceAccItem.getDatlast();
            }else if (editType.equals("insert_child_item")){
                refBalanceAccItem = new RefBalanceAccItem();
                refBalanceAccItem.setBeginDate(date);
                if(selectedBalanceAcc != null) {
                    refBalanceAccItem.setParentCode(((RefBalanceAccItem) selectedBalanceAcc.getData()).getCode());
                    refBalanceAccItem.setLevelCode(((RefBalanceAccItem) selectedBalanceAcc.getData()).getLevelCode());
                }
            }
            context.update(liferayNameSpace + ":frmRefBalanceAccModal");
            context.execute("PF('wDlgRefBalanceAccModal').show()");
        }else if (referenceCode.equals(RefConnOrgItem.REF_CODE)){
            if (editType.equals("insert_item")){
                refConnOrgItem = new RefConnOrgItem();
                refConnOrgItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefConnOrgItem filter = new RefConnOrgItem();
                filter.setId(fromVer ? selectedRefConnOrgVer.getId() : selectedRefConnOrg.getId());
                refConnOrgItem = (RefConnOrgItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refConnOrgItem.getUserName();
                userLocation = refConnOrgItem.getUserLocation();
                datlast = refConnOrgItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefConnOrgModal");
            context.execute("PF('wDlgRefConnOrgModal').show()");
        }else if (referenceCode.equals(RefBankItem.REF_CODE)){
            RefCountryList = (List<RefCountryItem>)sessionBean.getReference().getRefAbstractList(RefCountryItem.REF_CODE, date);
            if (editType.equals("insert_item")){
                refBankItem = new RefBankItem();
                refBankItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefBankItem filter = new RefBankItem();
                filter.setId(fromVer ? selectedRefBankVer.getId() : selectedRefBank.getId());
                refBankItem = (RefBankItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refBankItem.getUserName();
                userLocation = refBankItem.getUserLocation();
                datlast = refBankItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefBankModal");
            context.execute("PF('wDlgRefBankModal').show()");
        }else if (referenceCode.equals(RefDepartmentItem.REF_CODE)) {
            RefDepartmentItem filter = new RefDepartmentItem();
            filter.setId(fromVer ? selectedRefDepartmentVer.getId() : selectedRefDepartment.getId());
            refDepartmentItem  = (RefDepartmentItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
            userName = refDepartmentItem.getUserName();
            userLocation = refDepartmentItem.getUserLocation();
            datlast = refDepartmentItem.getDatlast();
            context.update(liferayNameSpace + ":frmRefDepartmentModal");
            context.execute("PF('wDlgRefDepartmentModal').show()");
        }else if (referenceCode.equals(RefRateAgencyItem.REF_CODE)) {
            RefRateAgencyItem filter = new RefRateAgencyItem();
            filter.setId(fromVer ? selectedRefRateAgencyVer.getId() : selectedRefRateAgency.getId());
            refRateAgencyItem = (RefRateAgencyItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
            userName = refRateAgencyItem.getUserName();
            userLocation = refRateAgencyItem.getUserLocation();
            datlast = refRateAgencyItem.getDatlast();
            context.update(liferayNameSpace + ":frmRefRateAgencyModal");
            context.execute("PF('wDlgRefRateAgencyModal').show()");
        }else if (referenceCode.equals(RefCurrencyItem.REF_CODE)) {
            RefCurrencyItem filter = new RefCurrencyItem();
            filter.setId(fromVer ? selectedRefCurrencyVer.getId() : selectedRefCurrency.getId());
            refCurrencyItem = (RefCurrencyItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
            userName = refCurrencyItem.getUserName();
            userLocation = refCurrencyItem.getUserLocation();
            datlast = refCurrencyItem.getDatlast();
            context.update(liferayNameSpace + ":frmRefCurrencyModal");
            context.execute("PF('wDlgRefCurrencyModal').show()");
        }else if (referenceCode.equals(RefCurrencyRateItem.REF_CODE)) {
            RefCurrencyRateItem filter = new RefCurrencyRateItem();
            filter.setId(fromVer ? selectedRefCurrencyRateVer.getId() : selectedRefCurrencyRate.getId());
            refCurrencyRateItem = (RefCurrencyRateItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
            userName = refCurrencyRateItem.getUserName();
            userLocation = refCurrencyRateItem.getUserLocation();
            datlast = refCurrencyRateItem.getDatlast();
            context.update(liferayNameSpace + ":frmRefCurrencyRateModal");
            context.execute("PF('wDlgRefCurrencyRateModal').show()");
        }else if (referenceCode.equals(RefSubjectTypeItem.REF_CODE)) {
            repPerDurMonthsList = sessionBean.getReference().getRefElements("REP_PER_DUR_MONTHS", false);

            if (editType.equals("insert_item")) {
                refSubjectTypeItem = new RefSubjectTypeItem();
                refSubjectTypeItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefSubjectTypeItem filter = new RefSubjectTypeItem();
                filter.setId(fromVer ? selectedRefSubjectTypeVer.getId() : selectedRefSubjectType.getId());
                refSubjectTypeItem = (RefSubjectTypeItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refSubjectTypeItem.getUserName();
                userLocation = refSubjectTypeItem.getUserLocation();
                datlast = refSubjectTypeItem.getDatlast();
            }
            subjectTypePostList = sessionBean.getReference().getSubjectTypePostList(refSubjectTypeItem.getRecId() == null ? 0 : refSubjectTypeItem.getRecId(), date);
            context.update(liferayNameSpace + ":frmRefSubjectTypeModal");
            context.execute("PF('wDlgRefSubjectTypeModal').show()");
        }else if (referenceCode.equals(RefRespondentItem.REF_CODE)) {
//            RefLegalPersonList = sessionBean.getReference().getRefLegalPersonList(date);
            if (editType.equals("insert_item")) {
                refRespondentItem = new RefRespondentItem();
                refRespondentItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefRespondentItem filter = new RefRespondentItem();
                filter.setId(fromVer ? selectedRefRespondentVer.getId() : selectedRefRespondent.getId());
                refRespondentItem = (RefRespondentItem) sessionBean.getReference().getRefAbstractByFilterList(referenceCode, filter, date).get(0);
                userName = refRespondentItem.getUserName();
                userLocation = refRespondentItem.getUserLocation();
                datlast = refRespondentItem.getDatlast();
            }

            refreshRespWarrant();

            context.update(liferayNameSpace + ":frmRefRespondentModal");
            context.execute("PF('wDlgRefRespondentModal').show()");
        }else if (referenceCode.equals(RefDocTypeItem.REF_CODE)) {
            if (editType.equals("insert_item")) {
                refDocTypeItem = new RefDocTypeItem();
                refDocTypeItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefDocTypeItem filter = new RefDocTypeItem();
                filter.setId(fromVer ? selectedRefDocTypeVer.getId() : selectedRefDocType.getId());
                refDocTypeItem = (RefDocTypeItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refDocTypeItem.getUserName();
                userLocation = refDocTypeItem.getUserLocation();
                datlast = refDocTypeItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefDocTypeModal");
            context.execute("PF('wDlgRefDocTypeModal').show()");
        }else if (referenceCode.equals(RefDocumentItem.REF_CODE)) {
            RefDocTypeList = (List<RefDocTypeItem>)sessionBean.getReference().getRefAbstractList(RefDocTypeItem.REF_CODE, date);
            RefRespondentList = (List<RefRespondentItem>)sessionBean.getReference().getRefAbstractList(RefRespondentItem.REF_CODE, date);
            for (RefRespondentItem refRespondentItem : RefRespondentList){
                if(refRespondentItem.getId() == 14){ // Исключаем Нац банк
                    RefRespondentList.remove(refRespondentItem);
                    break;
                }
            }
            if (editType.equals("insert_item")) {
                refDocumentItem = new RefDocumentItem();
                refDocumentItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefDocumentItem filter = new RefDocumentItem();
                filter.setId(fromVer ? selectedRefDocumentVer.getId() : selectedRefDocument.getId());
                refDocumentItem = (RefDocumentItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refDocumentItem.getUserName();
                userLocation = refDocumentItem.getUserLocation();
                datlast = refDocumentItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefDocumentModal");
            context.execute("PF('wDlgRefDocumentModal').show()");
        }else if (referenceCode.equals(RefIssuersItem.REF_CODE)){
            RefIssuersItem filter = new RefIssuersItem();
            filter.setId(fromVer ? selectedRefIssuersVer.getId() : selectedRefIssuers.getId());
            refIssuersItem = (RefIssuersItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
            userName = refIssuersItem.getUserName();
            userLocation = refIssuersItem.getUserLocation();
            datlast = refIssuersItem.getDatlast();
            context.update(liferayNameSpace + ":frmRefIssuersModal");
            context.execute("PF('wDlgRefIssuersModal').show()");
        }else if (referenceCode.equals(RefSecuritiesItem.REF_CODE)){
            RefSecuritiesItem filter = new RefSecuritiesItem();
            filter.setId(fromVer ? selectedRefSecuritiesVer.getId() : selectedRefSecurities.getId());
            refSecuritiesItem = (RefSecuritiesItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
            userName = refSecuritiesItem.getUserName();
            userLocation = refSecuritiesItem.getUserLocation();
            datlast = refSecuritiesItem.getDatlast();
            context.update(liferayNameSpace + ":frmRefSecuritiesModal");
            context.execute("PF('wDlgRefSecuritiesModal').show()");
        }else if (referenceCode.equals(RefVidOperItem.REF_CODE)) {
            if (editType.equals("insert_item")) {
                refVidOperItem = new RefVidOperItem();
                refVidOperItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefVidOperItem filter = new RefVidOperItem();
                filter.setId(fromVer ? selectedRefVidOperVer.getId() : selectedRefVidOper.getId());
                refVidOperItem = (RefVidOperItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refVidOperItem.getUserName();
                userLocation = refVidOperItem.getUserLocation();
                datlast = refVidOperItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefVidOperModal");
            context.execute("PF('wDlgRefVidOperModal').show()");
        }else if (referenceCode.equals(RefBranchItem.REF_CODE)) {
            if (editType.equals("insert_item")) {
                refBranchItem = new RefBranchItem();
                refBranchItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefBranchItem filter = new RefBranchItem();
                filter.setId(fromVer ? selectedRefBranchVer.getId() : selectedRefBranch.getId());
                refBranchItem = (RefBranchItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refBranchItem.getUserName();
                userLocation = refBranchItem.getUserLocation();
                datlast = refBranchItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefBranchModal");
            context.execute("PF('wDlgRefBranchModal').show()");
        }else if (referenceCode.equals("ref_branch_old")) {
            if (editType.equals("view_item")) {
                refBranchOldItem = sessionBean.getReference().getRefSimpleItem(fromVer ? selectedRefBranchOldVer.getId() : selectedRefBranchOld.getId(), referenceCode);
                userName = refBranchOldItem.getUserName();
                userLocation = refBranchOldItem.getUserLocation();
                datlast = refBranchOldItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefBranchOldModal");
            context.execute("PF('wDlgRefBranchOldModal').show()");
        }else if (referenceCode.equals(RefListingEstimationItem.REF_CODE)){
            RefListingEstimationItem filter = new RefListingEstimationItem();
            filter.setId(fromVer ? selectedRefListingEstimationVer.getId() : selectedRefListingEstimation.getId());
            refListingEstimationItem = (RefListingEstimationItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
            userName = refListingEstimationItem.getUserName();
            userLocation = refListingEstimationItem.getUserLocation();
            datlast = refListingEstimationItem.getDatlast();
            context.update(liferayNameSpace + ":frmRefListingEstimationModal");
            context.execute("PF('wDlgRefListingEstimationModal').show()");
        }else if (referenceCode.equals(RefRatingEstimationItem.REF_CODE)){
            RefRatingEstimationItem filter = new RefRatingEstimationItem();
            filter.setId(fromVer ? selectedRefRatingEstimationVer.getId() : selectedRefRatingEstimation.getId());
            refRatingEstimationItem = (RefRatingEstimationItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
            userName = refRatingEstimationItem.getUserName();
            userLocation = refRatingEstimationItem.getUserLocation();
            datlast = refRatingEstimationItem.getDatlast();
            context.update(liferayNameSpace + ":frmRefRatingEstimationModal");
            context.execute("PF('wDlgRefRatingEstimationModal').show()");
        }else if (referenceCode.equals(RefRatingCategoryItem.REF_CODE)) {
            RefRatingCategoryItem filter = new RefRatingCategoryItem();
            filter.setId(fromVer ? selectedRefRatingCategoryVer.getId() : selectedRefRatingCategory.getId());
            refRatingCategoryItem = (RefRatingCategoryItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
            userName = refRatingCategoryItem.getUserName();
            userLocation = refRatingCategoryItem.getUserLocation();
            datlast = refRatingCategoryItem.getDatlast();
            context.update(liferayNameSpace + ":frmRefRatingCategoryModal");
            context.execute("PF('wDlgRefRatingCategoryModal').show()");
        }/*else if (referenceCode.equals("ref_request_type")){
            if (editType.equals("insert_item")){
                refRequestTypeItem = new SimpleReference();
                refRequestTypeItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                refRequestTypeItem = sessionBean.getReference().getRefSimpleItem(fromVer ? selectedRefRequestTypeVer.getId() : selectedRefRequestType.getId(), referenceCode);
                userName = refRequestTypeItem.getUserName();
                userLocation = refRequestTypeItem.getUserLocation();
                datlast = refRequestTypeItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefRequestTypeModal");
            context.execute("PF('wDlgRefRequestTypeModal').show()");
        }else if (referenceCode.equals("ref_request_way")){
            if (editType.equals("insert_item")){
                refRequestWayItem = new SimpleReference();
                refRequestWayItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                refRequestWayItem = sessionBean.getReference().getRefSimpleItem(fromVer ? selectedRefRequestWayVer.getId() : selectedRefRequestWay.getId(), referenceCode);
                userName = refRequestWayItem.getUserName();
                userLocation = refRequestWayItem.getUserLocation();
                datlast = refRequestWayItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefRequestWayModal");
            context.execute("PF('wDlgRefRequestWayModal').show()");
        }else if (referenceCode.equals("ref_market_kind")) {
            if (editType.equals("insert_item")) {
                refMarketKindItem = new SimpleReference();
                refMarketKindItem.setBeginDate(date);
            } else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                refMarketKindItem = sessionBean.getReference().getRefSimpleItem(fromVer ? selectedRefMarketKindVer.getId() : selectedRefMarketKind.getId(), referenceCode);
                userName = refMarketKindItem.getUserName();
                userLocation = refMarketKindItem.getUserLocation();
                datlast = refMarketKindItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefMarketKindModal");
            context.execute("PF('wDlgRefMarketKindModal').show()");
        }else if (referenceCode.equals("ref_category")){
            if (editType.equals("insert_item")){
                refCategoryItem = new SimpleReference();
                refCategoryItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                refCategoryItem = sessionBean.getReference().getRefSimpleItem(fromVer ? selectedRefCategoryVer.getId() : selectedRefCategory.getId(),referenceCode);
                userName = refCategoryItem.getUserName();
                userLocation = refCategoryItem.getUserLocation();
                datlast = refCategoryItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefCategoryModal");
            context.execute("PF('wDlgRefCategoryModal').show()");
        }else if (referenceCode.equals("ref_subcategory")){
            if (editType.equals("insert_item")){
                refSubCategoryItem = new SimpleReference();
                refSubCategoryItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                refSubCategoryItem = sessionBean.getReference().getRefSimpleItem(fromVer ? selectedRefSubCategoryVer.getId() : selectedRefSubCategory.getId(),referenceCode);
                userName = refSubCategoryItem.getUserName();
                userLocation = refSubCategoryItem.getUserLocation();
                datlast = refSubCategoryItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefSubCategoryModal");
            context.execute("PF('wDlgRefSubCategoryModal').show()");
        }else if (referenceCode.equals("ref_account_type")){
            if (editType.equals("insert_item")){
                refAccountTypeItem = new SimpleReference();
                refAccountTypeItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                refAccountTypeItem = sessionBean.getReference().getRefSimpleItem(fromVer ? selectedRefAccountTypeVer.getId() : selectedRefAccountType.getId(),referenceCode);
                userName = refAccountTypeItem.getUserName();
                userLocation = refAccountTypeItem.getUserLocation();
                datlast = refAccountTypeItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefAccountTypeModal");
            context.execute("PF('wDlgRefAccountTypeModal').show()");
        }else if (referenceCode.equals("ref_subaccount_type")){
            if (editType.equals("insert_item")){
                refSubAccountTypeItem = new SimpleReference();
                refSubAccountTypeItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                refSubAccountTypeItem = sessionBean.getReference().getRefSimpleItem(fromVer ? selectedRefSubAccountTypeVer.getId() : selectedRefSubAccountType.getId(),referenceCode);
                userName = refSubAccountTypeItem.getUserName();
                userLocation = refSubAccountTypeItem.getUserLocation();
                datlast = refSubAccountTypeItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefSubAccountTypeModal");
            context.execute("PF('wDlgRefSubAccountTypeModal').show()");
        }else if (referenceCode.equals("ref_type_holder_acc")){
            if (editType.equals("insert_item")){
                refTypeHolderAccItem = new SimpleReference();
                refTypeHolderAccItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                refTypeHolderAccItem = sessionBean.getReference().getRefSimpleItem(fromVer ? selectedRefTypeHolderAccVer.getId() : selectedRefTypeHolderAcc.getId(),referenceCode);
                userName = refTypeHolderAccItem.getUserName();
                userLocation = refTypeHolderAccItem.getUserLocation();
                datlast = refTypeHolderAccItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefTypeHolderAccModal");
            context.execute("PF('wDlgRefTypeHolderAccModal').show()");
        }else if (referenceCode.equals("ref_request_feature")){
            if (editType.equals("insert_item")){
                refRequestFeatureItem = new SimpleReference();
                refRequestFeatureItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                refRequestFeatureItem = sessionBean.getReference().getRefSimpleItem(fromVer ? selectedRefRequestFeatureVer.getId() : selectedRefRequestFeature.getId(),referenceCode);
                userName = refRequestFeatureItem.getUserName();
                userLocation = refRequestFeatureItem.getUserLocation();
                datlast = refRequestFeatureItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefRequestFeatureModal");
            context.execute("PF('wDlgRefRequestFeatureModal').show()");
        }else if (referenceCode.equals("ref_request_sts")){
            if (editType.equals("insert_item")){
                refRequestStsItem = new SimpleReference();
                refRequestStsItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                refRequestStsItem = sessionBean.getReference().getRefSimpleItem(fromVer ? selectedRefRequestStsVer.getId() : selectedRefRequestSts.getId(),referenceCode);
                userName = refRequestStsItem.getUserName();
                userLocation = refRequestStsItem.getUserLocation();
                datlast = refRequestStsItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefRequestStsModal");
            context.execute("PF('wDlgRefRequestStsModal').show()");
        }else if (referenceCode.equals("ref_repo_kind")){
            if (editType.equals("insert_item")){
                refRepoKindItem = new SimpleReference();
                refRepoKindItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                refRepoKindItem = sessionBean.getReference().getRefSimpleItem(fromVer ? selectedRefRepoKindVer.getId() : selectedRefRepoKind.getId(),referenceCode);
                userName = refRepoKindItem.getUserName();
                userLocation = refRepoKindItem.getUserLocation();
                datlast = refRepoKindItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefRepoKindModal");
            context.execute("PF('wDlgRefRepoKindModal').show()");
        }else if (referenceCode.equals("ref_market_type")){
            if (editType.equals("insert_item")){
                refMarketTypeItem = new SimpleReference();
                refMarketTypeItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                refMarketTypeItem = sessionBean.getReference().getRefSimpleItem(fromVer ? selectedRefMarketTypeVer.getId() : selectedRefMarketType.getId(),referenceCode);
                userName = refMarketTypeItem.getUserName();
                userLocation = refMarketTypeItem.getUserLocation();
                datlast = refMarketTypeItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefMarketTypeModal");
            context.execute("PF('wDlgRefMarketTypeModal').show()");
        }else if (referenceCode.equals("ref_trad_method")){
            if (editType.equals("insert_item")){
                refTradMethodItem = new SimpleReference();
                refTradMethodItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                refTradMethodItem = sessionBean.getReference().getRefSimpleItem(fromVer ? selectedRefTradMethodVer.getId() : selectedRefTradMethod.getId(),referenceCode);
                userName = refTradMethodItem.getUserName();
                userLocation = refTradMethodItem.getUserLocation();
                datlast = refTradMethodItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefTradMethodModal");
            context.execute("PF('wDlgRefTradMethodModal').show()");
        }else if (referenceCode.equals("ref_oper_type")){
            if (editType.equals("insert_item")){
                refOperTypeItem = new SimpleReference();
                refOperTypeItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                refOperTypeItem = sessionBean.getReference().getRefSimpleItem(fromVer ? selectedRefOperTypeVer.getId() : selectedRefOperType.getId(),referenceCode);
                userName = refOperTypeItem.getUserName();
                userLocation = refOperTypeItem.getUserLocation();
                datlast = refOperTypeItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefOperTypeModal");
            context.execute("PF('wDlgRefOperTypeModal').show()");
        }else if (referenceCode.equals("ref_deal_sts")){
            if (editType.equals("insert_item")){
                refDealStsItem = new SimpleReference();
                refDealStsItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                refDealStsItem = sessionBean.getReference().getRefSimpleItem(fromVer ? selectedRefDealStsVer.getId() : selectedRefDealSts.getId(),referenceCode);
                userName = refDealStsItem.getUserName();
                userLocation = refDealStsItem.getUserLocation();
                datlast = refDealStsItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefDealStsModal");
            context.execute("PF('wDlgRefDealStsModal').show()");
        }*/else if (referenceCode.equals(RefMrpItem.REF_CODE)) {
            if (editType.equals("insert_item")) {
                refMrpItem = new RefMrpItem();
                refMrpItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefMrpItem filter = new RefMrpItem();
                filter.setId(fromVer ? selectedRefMrpVer.getId() : selectedRefMrp.getId());
                refMrpItem = (RefMrpItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refMrpItem.getUserName();
                userLocation = refMrpItem.getUserLocation();
                datlast = refMrpItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefMrpModal");
            context.execute("PF('wDlgRefMrpModal').show()");
        }else if (referenceCode.equals("ref_org_type")){
            if (editType.equals("insert_item")){
                refOrgTypeItem = new SimpleReference();
                refOrgTypeItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                refOrgTypeItem = sessionBean.getReference().getRefSimpleItem(fromVer ? selectedRefOrgTypeVer.getId() : selectedRefOrgType.getId(),referenceCode);
                userName = refOrgTypeItem.getUserName();
                userLocation = refOrgTypeItem.getUserLocation();
                datlast = refOrgTypeItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefOrgTypeModal");
            context.execute("PF('wDlgRefOrgTypeModal').show()");
        }else if (referenceCode.equals(RefMfoRegItem.REF_CODE)) {
            if (editType.equals("insert_item")) {
                refMfoRegItem = new RefMfoRegItem();
                refMfoRegItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefMfoRegItem filter = new RefMfoRegItem();
                filter.setId(fromVer ? selectedRefMfoRegVer.getId() : selectedRefMfoReg.getId());
                refMfoRegItem = (RefMfoRegItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refMfoRegItem.getUserName();
                userLocation = refMfoRegItem.getUserLocation();
                datlast = refMfoRegItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefMfoRegModal");
            context.execute("PF('wDlgRefMfoRegModal').show()");
        }else if (referenceCode.equals(RefDealBAItem.REF_CODE)) {
            if (editType.equals("insert_item")) {
                refDealBAItem = new RefDealBAItem();
                refDealBAItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefDealBAItem filter = new RefDealBAItem();
                filter.setId(fromVer ? selectedRefDealBAVer.getId() : selectedRefDealBA.getId());
                refDealBAItem = (RefDealBAItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refDealBAItem.getUserName();
                userLocation = refDealBAItem.getUserLocation();
                datlast = refDealBAItem.getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefDealBAModal");
            context.execute("PF('wDlgRefDealBAModal').show()");
        }else if (referenceCode.equals("ref_type_activity")){
            if (editType.equals("insert_item")){
                setRefTypeActivityItem(new RefTypeActivityItem());
                getRefTypeActivityItem().setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                //refTypeActivityItem  = sessionBean.getReference().getRefTypeActivityItem(fromVer ? selectedRefTypeActivityVer.getId() : selectedRefTypeActivity.getId());
                RefTypeActivityItem filter = new RefTypeActivityItem();
                filter.setId(fromVer ? getSelectedRefTypeActivityVer().getId() : getSelectedRefTypeActivity().getId());
                setRefTypeActivityItem((RefTypeActivityItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter));
                userName = getRefTypeActivityItem().getUserName();
                userLocation = getRefTypeActivityItem().getUserLocation();
                datlast = getRefTypeActivityItem().getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefTypeActivityModal");
            context.execute("PF('wDlgRefTypeActivityModal').show()");
        }else if (referenceCode.equals("ref_npa")){
            if (editType.equals("insert_item")){
                setRefNpaItem(new RefNpaItem());
                getRefNpaItem().setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                //refNpaItem  = sessionBean.getReference().getRefNpaItem(fromVer ? selectedRefNpaVer.getId() : selectedRefNpa.getId());
                RefNpaItem filter = new RefNpaItem();
                filter.setId(fromVer ? getSelectedRefNpaVer().getId() : getSelectedRefNpa().getId());
                setRefNpaItem((RefNpaItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter));
                userName = getRefNpaItem().getUserName();
                userLocation = getRefNpaItem().getUserLocation();
                datlast = getRefNpaItem().getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefNpaModal");
            context.execute("PF('wDlgRefNpaModal').show()");
        }else if (referenceCode.equals("ref_wkd_holidays")){
            if (editType.equals("insert_item")){
                setRefWkdHolidayItem(new RefWkdHolidayItem());
                getRefWkdHolidayItem().setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                //refWkdHolidayItem  = sessionBean.getReference().getRefWkdHolidayItem(fromVer ? getSelectedRefWkdHolidayVer().getId() : selectedRefWkdHoliday.getId());
                RefWkdHolidayItem filter = new RefWkdHolidayItem();
                filter.setId(fromVer ? getSelectedRefWkdHolidayVer().getId() : getSelectedRefWkdHoliday().getId());
                setRefWkdHolidayItem((RefWkdHolidayItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter));
                userName = getRefWkdHolidayItem().getUserName();
                userLocation = getRefWkdHolidayItem().getUserLocation();
                datlast = getRefWkdHolidayItem().getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefWkdHolidayModal");
            context.execute("PF('wDlgRefWkdHolidayModal').show()");
        }else if (referenceCode.equals("ref_finrep_indic")){
            if (editType.equals("insert_item")){
                setRefFinRepIndicItem(new SimpleReference());
                getRefFinRepIndicItem().setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                setRefFinRepIndicItem(sessionBean.getReference().getRefSimpleItem(fromVer ? getSelectedRefFinRepIndicVer().getId() : getSelectedRefFinRepIndic().getId(),referenceCode));
                userName = getRefFinRepIndicItem().getUserName();
                userLocation = getRefFinRepIndicItem().getUserLocation();
                datlast = getRefFinRepIndicItem().getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefFinRepIndicModal");
            context.execute("PF('wDlgRefFinRepIndicModal').show()");
        } else if (referenceCode.equals("ref_type_deals")){
            if (editType.equals("insert_item")){
                setRefTypeDealsItem(new SimpleReference());
                getRefTypeDealsItem().setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                setRefTypeDealsItem(sessionBean.getReference().getRefSimpleItem(fromVer ? getSelectedRefTypeDealsVer().getId() : getSelectedRefTypeDeals().getId(),referenceCode));
                userName = getRefTypeDealsItem().getUserName();
                userLocation = getRefTypeDealsItem().getUserLocation();
                datlast = getRefTypeDealsItem().getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefTypeDealsModal");
            context.execute("PF('wDlgRefTypeDealsModal').show()");
        } else if (referenceCode.equals("ref_degree_relation")){
            if (editType.equals("insert_item")){
                setRefDegreeRelationItem(new SimpleReference());
                getRefDegreeRelationItem().setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                setRefDegreeRelationItem(sessionBean.getReference().getRefSimpleItem(fromVer ? getSelectedRefDegreeRelationVer().getId() : getSelectedRefDegreeRelation().getId(),referenceCode));
                userName = getRefDegreeRelationItem().getUserName();
                userLocation = getRefDegreeRelationItem().getUserLocation();
                datlast = getRefDegreeRelationItem().getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefDegreeRelationModal");
            context.execute("PF('wDlgRefDegreeRelationModal').show()");
        } else if (referenceCode.equals("ref_sign_related")){
            if (editType.equals("insert_item")){
                setRefSignRelatedItem(new SimpleReference());
                getRefSignRelatedItem().setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                setRefSignRelatedItem(sessionBean.getReference().getRefSimpleItem(fromVer ? getSelectedRefSignRelatedVer().getId() : getSelectedRefSignRelated().getId(), referenceCode));
                userName = getRefSignRelatedItem().getUserName();
                userLocation = getRefSignRelatedItem().getUserLocation();
                datlast = getRefSignRelatedItem().getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefSignRelatedModal");
            context.execute("PF('wDlgRefSignRelatedModal').show()");
        } else if (referenceCode.equals("ref_type_risk")){
            if (editType.equals("insert_item")){
                setRefTypeRiskItem(new SimpleReference());
                getRefTypeRiskItem().setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                setRefTypeRiskItem(sessionBean.getReference().getRefSimpleItem(fromVer ? getSelectedRefTypeRiskVer().getId() : getSelectedRefTypeRisk().getId(), referenceCode));
                userName = getRefTypeRiskItem().getUserName();
                userLocation = getRefTypeRiskItem().getUserLocation();
                datlast = getRefTypeRiskItem().getDatlast();
            }
            context.update(liferayNameSpace + ":frmRefTypeRiskModal");
            context.execute("PF('wDlgRefTypeRiskModal').show()");
        }else if (referenceCode.equals("ref_basisofcontrol")){
            if (editType.equals("insert_item")){
                setRefBasisofControlItem(new RefBasisofControlItem());
                getRefBasisofControlItem().setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                //refPostItem = sessionBean.getReference().getRefPostItem(fromVer ? selectedRefPostVer.getId() : selectedRefPost.getId());
                RefBasisofControlItem filter = new RefBasisofControlItem();
                filter.setId(fromVer ? getSelectedRefBasisofControlVer().getId() : getSelectedRefBasisofControl().getId());
                setRefBasisofControlItem((RefBasisofControlItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter));
                userName = getRefBasisofControlItem().getUserName();
                userLocation = getRefBasisofControlItem().getUserLocation();
                datlast = getRefBasisofControlItem().getDatlast();
            }

            context.update(liferayNameSpace + ":frmRefBasisofControlModal");
            context.execute("PF('wDlgRefBasisofControlModal').show()");
        } else if (referenceCode.equals("ref_insur_org")){
            if (editType.equals("insert_item")){
                refInsurOrgItem = new RefInsurOrgItem();
                refInsurOrgItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                //refPostItem = sessionBean.getReference().getRefPostItem(fromVer ? selectedRefPostVer.getId() : selectedRefPost.getId());
                RefInsurOrgItem filter = new RefInsurOrgItem();
                filter.setId(fromVer ? getSelectedRefInsurOrgVer().getId() : getSelectedRefInsurOrg().getId());
                refInsurOrgItem = (RefInsurOrgItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refInsurOrgItem.getUserName();
                userLocation = refInsurOrgItem.getUserLocation();
                datlast = refInsurOrgItem.getDatlast();
            }

            context.update(liferayNameSpace + ":frmRefInsurOrgModal");
            context.execute("PF('wDlgRefInsurOrgModal').show()");
        } else if (referenceCode.equals("ref_extind")){
            SimpleReference refValueType = new SimpleReference();
            refValueType.setRefCode("ref_value_type");
            RefValueTypeList =  sessionBean.getReference().getRefSimpleListByParams(date,refValueType);
            if (editType.equals("insert_item")){
                refExtIndItem = new RefExtIndicatorItem();
                refExtIndItem.setBeginDate(date);
                if (RefExtParamList!=null) RefExtParamList.clear();

            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item")))  {
                if (RefExtParamList != null) RefExtParamList.clear();
                RefExtIndicatorItem filter = new RefExtIndicatorItem();
                filter.setId(fromVer ? getSelectedRefExtIndVer().getId() : getSelectedRefExtInd().getId());
                try {
                    RefExtParamList = sessionBean.getReference().getRefExtParamListByParams(date, selectedRefExtInd.getAlgorithm(), selectedRefExtInd.getId());
                } catch (OracleException e) {
                    e.printStackTrace();
                }
                refExtIndItem = (RefExtIndicatorItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = getRefExtIndItem().getUserName();
                userLocation = getRefExtIndItem().getUserLocation();
                datlast = getRefExtIndItem().getDatlast();
            }

            context.update(liferayNameSpace + ":frmRefExtIndModal");
            context.execute("PF('wDlgRefExtIndModal').show()");
        } else if (referenceCode.equals(RefMajorMemberItem.REF_CODE)){
            disableBtnMajorMem = true;
            disableBtnMajorMemView = true;


            if (editType.equals("insert_item")){
                if (RefMajorMemberOrgList != null)
                    RefMajorMemberOrgList.clear();

                if (RefMajorMemDetailsList != null)
                    RefMajorMemDetailsList.clear();

                refMajorMemberItem = new RefMajorMemberItem();
                refMajorMemberItem.setBeginDate(date);
                refMajorMemberOrgItem = new RefMajorMemberOrgItem();
                refMajorMemberOrgItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefMajorMemberItem filter = new RefMajorMemberItem();
                filter.setId(fromVer ? selectedRefMajorMemberVer.getId() : selectedRefMajorMember.getId());
                refMajorMemberItem = (RefMajorMemberItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refMajorMemberItem.getUserName();
                userLocation = refMajorMemberItem.getUserLocation();
                datlast = refMajorMemberItem.getDatlast();
                RefMajorMemberOrgItem filter1 = new RefMajorMemberOrgItem();
                filter1.setId(null);
                filter1.setRefMajorMember(selectedRefMajorMember.getId());
                RefMajorMemberOrgList = (List<RefMajorMemberOrgItem>) sessionBean.getReference().getRefAbstractByFilterList("ref_major_memorgs", filter1, date);

            }
            context.update(liferayNameSpace + ":frmRefMajorMemberModal");
            context.execute("PF('wDlgRefMajorMemberModal').show()");
        } else  if (referenceCode.equals(RefMajorMemberOrgItem.REF_CODE)){
             if (editType.equals("insert_item")){
                 refMajorMemberOrgItem = new RefMajorMemberOrgItem();
                 refMajorMemberOrgItem.setBeginDate(date);
             }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                 RefMajorMemberOrgItem filter1 = new RefMajorMemberOrgItem();
                 filter1.setId(fromVer ? selectedRefMajorMemberOrgVer.getId() : selectedRefMajorMemberOrg.getId());
                 refMajorMemberOrgItem = (RefMajorMemberOrgItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter1);
                 userName = refMajorMemberOrgItem.getUserName();
                 userLocation = refMajorMemberOrgItem.getUserLocation();
                 datlast = refMajorMemberOrgItem.getDatlast();
            }

            context.update(liferayNameSpace + ":frmRefMajorMemOrgsModal");
            context.execute("PF('wDlgRefMajorMemOrgsModal').show()");
        } else if (referenceCode.equals(RefPeriodAlgItem.REF_CODE)){
            if (editType.equals("insert_item")){
                refPeriodAlgItem = new RefPeriodAlgItem();
                refPeriodAlgItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefPeriodAlgItem filter = new RefPeriodAlgItem();
                filter.setId(fromVer ? selectedRefPeriodAlgVer.getId() : selectedRefPeriodAlg.getId());
                refPeriodAlgItem = (RefPeriodAlgItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refPeriodAlgItem.getUserName();
                userLocation = refPeriodAlgItem.getUserLocation();
                datlast = refPeriodAlgItem.getDatlast();
            }

            context.update(liferayNameSpace + ":frmRefPeriodAlgModal");
            context.execute("PF('wDlgRefPeriodAlgModal').show()");
        } else if (referenceCode.equals(RefPeriodItem.REF_CODE)){
            selectedArgument = null;
            refPeriodAlgList = (List<RefPeriodAlgItem>) sessionBean.getReference().getRefAbstractList(RefPeriodAlgItem.REF_CODE, date);
            if (editType.equals("insert_item")){
                refPeriodItem = new RefPeriodItem();
                refPeriodItem.setBeginDate(date);
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefPeriodItem filter = new RefPeriodItem();
                filter.setId(fromVer ? selectedRefPeriodVer.getId() : selectedRefPeriod.getId());
                refPeriodItem = (RefPeriodItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refPeriodItem.getUserName();
                userLocation = refPeriodItem.getUserLocation();
                datlast = refPeriodItem.getDatlast();
            }
            if(refPeriodItem.getId() != null && refPeriodItem.getId() != 0) {
                argumentList = sessionBean.getReference().getRefPeriodArguments(refPeriodItem.getId());
            }

            context.update(liferayNameSpace + ":frmRefPeriodModal");
            context.execute("PF('wDlgRefPeriodModal').show()");
        }  else if (referenceCode.equals(RefInsurGroupsItem.REF_CODE)){
               RefCountryList = (List<RefCountryItem>) sessionBean.getReference().getRefAbstractList(RefCountryItem.REF_CODE, date);
               refreshOrgTypes(date);
               SimpleReference refBranchInsurReference = new SimpleReference();
               refBranchInsurReference.setRefCode("ref_branch_insur");
               RefBranchInsurList = sessionBean.getReference().getRefSimpleListByParams(date,refBranchInsurReference);

            if (editType.equals("insert_item")){
                refInsurGroupsItem = new RefInsurGroupsItem();
                refInsurGroupsItem.setBeginDate(date);
                if(selectedInsurGroups != null) {
                    refInsurGroupsItem.setParentCode(((RefInsurGroupsItem) selectedInsurGroups.getData()).getParentCode());
                    refInsurGroupsItem.setLevelCode(((RefInsurGroupsItem) selectedInsurGroups.getData()).getLevelCode());
                }
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefInsurGroupsItem filter = new RefInsurGroupsItem();
                filter.setId(fromVer ? selectedRefInsurGroupsVer.getId() : ((RefInsurGroupsItem)selectedInsurGroups.getData()).getId());
                refInsurGroupsItem = (RefInsurGroupsItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refInsurGroupsItem.getUserName();
                userLocation = refInsurGroupsItem.getUserLocation();
                datlast = refInsurGroupsItem.getDatlast();
            }else if (editType.equals("insert_child_item")){
                refInsurGroupsItem = new RefInsurGroupsItem();
                refInsurGroupsItem.setBeginDate(date);
                if(selectedInsurGroups != null) {
                    refInsurGroupsItem.setParentCode(((RefInsurGroupsItem) selectedInsurGroups.getData()).getCode());
                    refInsurGroupsItem.setLevelCode(((RefInsurGroupsItem) selectedInsurGroups.getData()).getLevelCode());
                }
            }
            context.update(liferayNameSpace + ":frmRefInsurGroupsModal");
            context.execute("PF('wDlgRefInsurGroupsModal').show()");
        } else if (referenceCode.equals(RefBankConglomeratesItem.REF_CODE)){
           RefCountryList = (List<RefCountryItem>) sessionBean.getReference().getRefAbstractList(RefCountryItem.REF_CODE, date);
           refreshOrgTypes(date);
           SimpleReference refstatus = new SimpleReference();
           refstatus.setRefCode("ref_status");
           refStatusList = sessionBean.getReference().getRefSimpleListByParams(date,refstatus);
           RefTypeActivityList = (List<RefTypeActivityItem>) sessionBean.getReference().getRefAbstractList(RefTypeActivityItem.REF_CODE, date);

            if (editType.equals("insert_item")){
                refBankConglItem = new RefBankConglomeratesItem();
                refBankConglItem.setBeginDate(date);
                if(selectedBankCongl != null) {
                    refBankConglItem.setParentCode(((RefBankConglomeratesItem) selectedBankCongl.getData()).getParentCode());
                    refBankConglItem.setLevelCode(((RefBankConglomeratesItem) selectedBankCongl.getData()).getLevelCode());
                }
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefBankConglomeratesItem filter = new RefBankConglomeratesItem();
                filter.setId(fromVer ? selectedRefBankConglVer.getId() : ((RefBankConglomeratesItem)selectedBankCongl.getData()).getId());
                refBankConglItem = (RefBankConglomeratesItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refBankConglItem.getUserName();
                userLocation = refBankConglItem.getUserLocation();
                datlast = refBankConglItem.getDatlast();
            }else if (editType.equals("insert_child_item")){
                refBankConglItem = new RefBankConglomeratesItem();
                refBankConglItem.setBeginDate(date);
                if(selectedBankCongl != null) {
                    refBankConglItem.setParentCode(((RefBankConglomeratesItem) selectedBankCongl.getData()).getCode());
                    refBankConglItem.setLevelCode(((RefBankConglomeratesItem) selectedBankCongl.getData()).getLevelCode());
                }
            }
            context.update(liferayNameSpace + ":frmRefBankConglModal");
            context.execute("PF('wDlgRefBankConglModal').show()");
        } else if (referenceCode.equals(RefShareHoldersItem.REF_CODE)){
           RefCountryList = (List<RefCountryItem>) sessionBean.getReference().getRefAbstractList(RefCountryItem.REF_CODE, date);

            if (editType.equals("insert_item")){
                refShareHoldItem = new RefShareHoldersItem();
                refShareHoldItem.setBeginDate(date);
                if(selectedShareHold != null) {
                    refShareHoldItem.setParentCode(((RefShareHoldersItem) selectedShareHold.getData()).getParentCode());
                    refShareHoldItem.setLevelCode(((RefShareHoldersItem) selectedShareHold.getData()).getLevelCode());
                }
            }else if ((editType.equals("update_item")) || (editType.equals("refresh_item")) || (editType.equals("view_item"))) {
                RefShareHoldersItem filter = new RefShareHoldersItem();
                filter.setId(fromVer ? selectedRefShareHoldVer.getId() : ((RefShareHoldersItem)selectedShareHold.getData()).getId());
                refShareHoldItem = (RefShareHoldersItem) sessionBean.getReference().getRefAbstractItem(referenceCode, filter);
                userName = refShareHoldItem.getUserName();
                userLocation = refShareHoldItem.getUserLocation();
                datlast = refShareHoldItem.getDatlast();
            }else if (editType.equals("insert_child_item")){
                refShareHoldItem = new RefShareHoldersItem();
                refShareHoldItem.setBeginDate(date);
                if(selectedShareHold != null) {
                    refShareHoldItem.setParentCode(((RefShareHoldersItem) selectedShareHold.getData()).getCode());
                    refShareHoldItem.setLevelCode(((RefShareHoldersItem) selectedShareHold.getData()).getLevelCode());
                }
            }
            context.update(liferayNameSpace + ":frmRefShareHoldModal");
            context.execute("PF('wDlgRefShareHoldModal').show()");
        }
    }

     //open ref_holidays window
    public void getFillRefItem() {
         Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();
        Date maxDate =  Convert.getDateFromString("01.01.3333");
         RequestContext context = RequestContext.getCurrentInstance();
         String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();
         //holidayItem = sessionBean.getReference().getRefHolidayItem(false ? selectedRefHolidayVer.getId() : selectedRefHoliday.getId());
         setRefHolidayList(sessionBean.getReference().getRefHolidayList(date));
         getFilterRefHolidayItem().setSearchAllVer(searchAllVer);
         RefHolidayList = sessionBean.getReference().getRefHolidayListByParams(maxDate, getFilterRefHolidayItem());
         context.update(liferayNameSpace + ":frmCorrectRefHolModal");
         context.execute("PF('wDlgCorrectRefHolModal').show()");
    }

    public void saveHolidayChanges() {
        Date maxDate =  Convert.getDateFromString("01.01.3333");
        try {
            Date dateIn = sessionBean.getIntegration().getNewDateFromBackEndServer();
            Date datlast = sessionBean.getIntegration().getNewDateFromBackEndServer();
            userId = (long) sessionBean.user.getUserId();
            userLocation = sessionBean.user.getLoginIP();

            RequestContext context = RequestContext.getCurrentInstance();
            getHolidayItem().setUserId(userId);
            getHolidayItem().setUserLocation(userLocation);
            getHolidayItem().setDatlast(datlast);
            sessionBean.getReference().updateRefHolidayItem(getHolidayItem());

          //  recId = sessionBean.getReference().getRefRecIdById(referenceCode, id != null ? id : holidayItem.getId());
            dateIn = getHolidayItem().getBeginDate();
            context.execute("PF('holDialog').hide()");
        } catch (OracleException ex) {
              FacesMessage message = new FacesMessage();
              message.setSeverity(FacesMessage.SEVERITY_ERROR);
              message.setSummary(ex.getMessage());
              FacesContext.getCurrentInstance().addMessage(null, message);
              return;
        }

        getFilterRefHolidayItem().setSearchAllVer(searchAllVer);
        setRefHolidayList(sessionBean.getReference().getRefHolidayListByParams(maxDate, getFilterRefHolidayItem()));
    }

    public void fillWkdHolidays(){
       try {
            RequestContext context = RequestContext.getCurrentInstance();
            Date dateYear = getFilterDateForFillRef();
            String err_msg = "";
            userId = (long) sessionBean.user.getUserId();
            userLocation = sessionBean.user.getLoginIP();
            Date datlast = sessionBean.getIntegration().getNewDateFromBackEndServer();

            if(dateYear==null)
               err_msg = "Введите год для заполнения!";

            if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

             HolidayItem holidayItem = new HolidayItem();
             holidayItem.setUserId(userId);
             holidayItem.setUserLocation(userLocation);
             holidayItem.setDatlast(datlast);

            sessionBean.getReference().fillRefWkdHolidays(holidayItem, dateYear);

            context.execute("PF('wDlgCorrectRefHolModal').hide()");
       } catch (OracleException ex) {
           FacesMessage message = new FacesMessage();
           message.setSeverity(FacesMessage.SEVERITY_ERROR);
           message.setSummary(ex.getMessage());
           FacesContext.getCurrentInstance().addMessage(null, message);
           return;
       }
        refreshSimpleRefList("update_ref");
    }

    public void saveSimpleRefItem(){
        Long kindEvent = null;

        if(editKind != null)
            editType = editKind;

        if(editType.equals("insert_item") || editType.equals("insert_child_item"))
            kindEvent = 49L;
        else if (editType.equals("update_item"))
            kindEvent = 51L;
        else if (editType.equals("refresh_item"))
            kindEvent = 6L;

        String err_msg = "";
        Long recId = null;
        Long id = null;
        Date dateIn = sessionBean.getIntegration().getNewDateFromBackEndServer();
        Date datlast = sessionBean.getIntegration().getNewDateFromBackEndServer();
        userId = sessionBean.user.getUserId();
        userLocation = sessionBean.user.getLoginIP();

        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setCodeObject(referenceCode);
        auditEvent.setNameObject(null);
        auditEvent.setIdKindEvent(kindEvent);
        auditEvent.setDateEvent(datlast);
        auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
        auditEvent.setUserId(userId);
        auditEvent.setUserLocation(userLocation);

        try {
            RequestContext context = RequestContext.getCurrentInstance();
            if (referenceCode.equals(RefPostItem.REF_CODE)){

                if(refPostItem.getNameRu()==null || refPostItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                 if(refPostItem.getBeginDate()==null)
                    err_msg = "Введите дату начала действия!";
                 if(refPostItem.getTypePostId()==0)
                    err_msg = "Выберите тип должности!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refPostItem.setUserId(userId);
                refPostItem.setUserLocation(userLocation);
                refPostItem.setDatlast(datlast);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refPostItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refPostItem, auditEvent);
                }
                context.execute("PF('wDlgRefPostModal').hide()");
            }else if (referenceCode.equals(RefPersonItem.REF_CODE)){

                if(refPersonItem.getIdn() == null || refPersonItem.getIdn().trim().isEmpty())
                    err_msg = "Введите ИИН!!";
                else if (!Validators.validateIDN(refPersonItem.getIdn()))
                    err_msg = "Введен не корректный ИИН!";
                if (refPersonItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия !";
                if (refPersonItem.getFm() == null || refPersonItem.getFm().trim().isEmpty())
                    err_msg = "Введите фамилию!";
                if (refPersonItem.getNm() == null || refPersonItem.getNm().trim().isEmpty())
                    err_msg = "Введите имя!";
                if (refPersonItem.getRefCountry()==0)
                    err_msg = "Выберите Гражданство!";
                if (refPersonItem.getAddressWork() == null || refPersonItem.getAddressWork().trim().isEmpty() )
                    err_msg = "Введите рабочий адрес!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refPersonItem.setUserId(userId);
                refPersonItem.setUserLocation(userLocation);
                refPersonItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refPersonItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refPersonItem, auditEvent);
                }
                context.execute("PF('wDlgRefPersonModal').hide()");
            }else if (referenceCode.equals(RefLegalPersonItem.REF_CODE)){
                if(refLegalPersonItem.getNameRu() == null || refLegalPersonItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                if (refLegalPersonItem.getBeginDate() == null)
                    err_msg = "Введите дату начала дейтсвия!";
                if (refLegalPersonItem.getRefOrgType()==0)
                    err_msg = "Выберите тип организации !";
                if (refLegalPersonItem.getRefTypeBusEntity()==0)
                    err_msg = "Выберите организационно-правовую форму !";
                if (refLegalPersonItem.getRefCountry()==0)
                    err_msg = "Выберите гражданство !";
                if (!refLegalPersonItem.getIsNonRezident() && !refLegalPersonItem.getIsInvFund() && !refLegalPersonItem.getIsAkimat()){
                    if(refLegalPersonItem.getIdn() == null)
                        err_msg = "Введите БИН";
                }
                if (refLegalPersonItem.getIdn() != null) {
                    if (!Validators.validateIDN(refLegalPersonItem.getIdn()))
                        err_msg = "Введен не корректный БИН";
                }
                if (refLegalPersonItem.getInvIdn() != null) {
                    if (!Validators.validateIDN(refLegalPersonItem.getInvIdn()))
                        err_msg = "Введен не корректный БИН управляющей компании";

                }
                if (refLegalPersonItem.getIdn() != null && refLegalPersonItem.getInvIdn() != null) {
                    if (refLegalPersonItem.getIdn().equals(refLegalPersonItem.getInvIdn()))
                        err_msg = "БИН и БИН управляющей компании не может быть одинаковым!";
                }
                if (refLegalPersonItem.getLegalAddress() == null || refLegalPersonItem.getLegalAddress().trim().isEmpty())
                    err_msg = "Введите Юр. адрес !";
                if (refLegalPersonItem.getFactAddress() == null || refLegalPersonItem.getFactAddress().trim().isEmpty())
                    err_msg = "Введите Факт. адрес !";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refLegalPersonItem.setUserId(userId);
                refLegalPersonItem.setUserLocation(userLocation);
                refLegalPersonItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refLegalPersonItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refLegalPersonItem, auditEvent);
                }
                context.execute("PF('wDlgRefLegalPersonModal').hide()");
            } else if (referenceCode.equals(RefManagersItem.REF_CODE)){

                if (refManagersItem.getFm() == null || refManagersItem.getFm().trim().isEmpty())
                    err_msg = "Введите фамилию!";
                if (refManagersItem.getNm() == null || refManagersItem.getNm().trim().isEmpty())
                    err_msg = "Введите имя!";
                if (refManagersItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if (refManagersItem.getRefPostId() == null)
                    err_msg = "Выберите должность";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refManagersItem.setUserId(userId);
                refManagersItem.setUserLocation(userLocation);
                refManagersItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refManagersItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refManagersItem, auditEvent);
                }
                context.execute("PF('wDlgRefManagersModal').hide()");
            } else if (referenceCode.equals(RefTypeBusEntityItem.REF_CODE)){

                if (refTypeBusEntityItem.getNameRu() == null || refTypeBusEntityItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                if (refTypeBusEntityItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refTypeBusEntityItem.setUserId(userId);
                refTypeBusEntityItem.setUserLocation(userLocation);
                refTypeBusEntityItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refTypeBusEntityItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refTypeBusEntityItem, auditEvent);
                }
                context.execute("PF('wDlgRefTypeBusEntityModal').hide()");
            }else if (referenceCode.equals(RefRequirementItem.REF_CODE)){

                if (refRequirementItem.getNameRu() == null || refRequirementItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                if (refRequirementItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refRequirementItem.setUserId(userId);
                refRequirementItem.setUserLocation(userLocation);
                refRequirementItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refRequirementItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refRequirementItem, auditEvent);
                }
                context.execute("PF('wDlgRefRequirementModal').hide()");
            }else if (referenceCode.equals(RefTypeProvideItem.REF_CODE)){

                if (refTypeProvideItem.getCode() == null || refTypeProvideItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if (refTypeProvideItem.getNameRu() == null || refTypeProvideItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                if (refTypeProvideItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refTypeProvideItem.setUserId(userId);
                refTypeProvideItem.setUserLocation(userLocation);
                refTypeProvideItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refTypeProvideItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refTypeProvideItem, auditEvent);
                }
                context.execute("PF('wDlgRefTypeProvideModal').hide()");
            }else if (referenceCode.equals(RefTransTypeItem.REF_CODE)){

                if (refTransTypeItem.getNameRu() == null || refTransTypeItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                if (refTransTypeItem.getKindOfActivity() == null || refTransTypeItem.getKindOfActivity().trim().isEmpty())
                    err_msg = "Введите вид деятельности!";
                if (refTransTypeItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refTransTypeItem.setUserId(userId);
                refTransTypeItem.setUserLocation(userLocation);
                refTransTypeItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refTransTypeItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refTransTypeItem, auditEvent);
                }
                context.execute("PF('wDlgRefTransTypeModal').hide()");
            }else if (referenceCode.equals(RefBalanceAccItem.REF_CODE)){

                if (refBalanceAccItem.getCode() == null || refBalanceAccItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if (refBalanceAccItem.getNameRu() == null || refBalanceAccItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                if (refBalanceAccItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refBalanceAccItem.setUserId(userId);
                refBalanceAccItem.setUserLocation(userLocation);
                refBalanceAccItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item") || editType.equals("insert_child_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refBalanceAccItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refBalanceAccItem, auditEvent);
                }
                context.execute("PF('wDlgRefBalanceAccModal').hide()");
            }else if (referenceCode.equals(RefConnOrgItem.REF_CODE)){

                if (refConnOrgItem.getCode() == null || refConnOrgItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if (refConnOrgItem.getNameRu() == null || refConnOrgItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                if (refConnOrgItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refConnOrgItem.setUserId(userId);
                refConnOrgItem.setUserLocation(userLocation);
                refConnOrgItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refConnOrgItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refConnOrgItem, auditEvent);
                }
                context.execute("PF('wDlgRefConnOrgModal').hide()");
            }else if (referenceCode.equals(RefBankItem.REF_CODE)){
                refBankItem.setUserId(userId);
                refBankItem.setUserLocation(userLocation);
                refBankItem.setDatlast(datlast);

                if(refBankItem.getIdn() != null && !refBankItem.getIdn().trim().isEmpty()) {
                    if (!Validators.validateIDN(refBankItem.getIdn())) {
                        err_msg = "Введен не корректный БИН";
                    }
                }

                if (refBankItem.getNameRu()== null || refBankItem.getNameRu().trim().isEmpty()){
                    err_msg = "Введите наименование на русском!";
                }

                if (refBankItem.getRefCountryId()==0){
                    err_msg = "Выберите страну!";
                }

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refBankItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refBankItem, auditEvent);
                }
                context.execute("PF('wDlgRefBankModal').hide()");
            }else if (referenceCode.equals(RefSubjectTypeItem.REF_CODE)){

                if (refSubjectTypeItem.getNameRu() == null || refSubjectTypeItem.getNameRu().trim().isEmpty())
                    err_msg ="Введите наименование!";
                if (refSubjectTypeItem.getBeginDate() == null)
                    err_msg ="Введите дату начала действия!!";
                /*if (refSubjectTypeItem.getRepPerDurMonths() == 0)
                    err_msg ="Выберите периодичность сдачи!";*/

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refSubjectTypeItem.setUserId(userId);
                refSubjectTypeItem.setUserLocation(userLocation);
                refSubjectTypeItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refSubjectTypeItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refSubjectTypeItem, auditEvent);
                }

                Long stRecId = refSubjectTypeItem.getRecId();
                if(stRecId == null || stRecId == 0) {
                    RefSubjectTypeItem item = new RefSubjectTypeItem();
                    item.setId(id);
                    stRecId = (sessionBean.getReference().getRefAbstractItem(RefSubjectTypeItem.REF_CODE, item)).getRecId();
                }
                sessionBean.getReference().updateSubjectTypePost(subjectTypePostList, stRecId);

                context.execute("PF('wDlgRefSubjectTypeModal').hide()");
            }else if (referenceCode.equals(RefRespondentItem.REF_CODE)){

                if (refRespondentItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                else if (refRespondentItem.getUnionPersonsId() == null || refRespondentItem.getUnionPersonsId()==0)
                    err_msg = "Выберите Юр лицо !";
                else if (refRespondentItem.getRefSubjectType() == null || refRespondentItem.getRefSubjectType()==0)
                    err_msg = "Выберите тип субъекта !";
                else if (refRespondentItem.getRefSubjectType() != null && refRespondentItem.getRefSubjectType() != 0){
                    RefSubjectTypeItem refSubjectTypeItem = new RefSubjectTypeItem();
                    refSubjectTypeItem.setId(refRespondentItem.getRefSubjectType());
                    String shortName = sessionBean.getReference().getRefAbstractItem(RefSubjectTypeItem.REF_CODE, refSubjectTypeItem).getShortNameRu();
                    if(shortName != null && shortName.equalsIgnoreCase("МФО")) {
                        if (refRespondentItem.getRefDepartment() == null || refRespondentItem.getRefDepartment() == 0) {
                            err_msg = "Выберите филиал!";
                        }
                    }
                }

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refRespondentItem.setUserId(userId);
                refRespondentItem.setUserLocation(userLocation);
                refRespondentItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refRespondentItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refRespondentItem, auditEvent);
                }
                context.execute("PF('wDlgRefRespondentModal').hide()");
            }else if (referenceCode.equals(RefDocTypeItem.REF_CODE)){

                if (refDocTypeItem.getNameRu() == null || refDocTypeItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                if (refDocTypeItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refDocTypeItem.setUserId(userId);
                refDocTypeItem.setUserLocation(userLocation);
                refDocTypeItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refDocTypeItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refDocTypeItem, auditEvent);
                }
                context.execute("PF('wDlgRefDocTypeModal').hide()");
            }else if (referenceCode.equals(RefDocumentItem.REF_CODE)){

                if(refDocumentItem.getCode() == null || refDocumentItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refDocumentItem.getNameRu() == null || refDocumentItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                if (refDocumentItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if (refDocumentItem.getRefDocType()==0)
                    err_msg = "Выберите тип документа !";
                if(refDocumentItem.getRefRespondent()==0)
                    err_msg = "Выберите респондента !";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refDocumentItem.setUserId(userId);
                refDocumentItem.setUserLocation(userLocation);
                refDocumentItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refDocumentItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refDocumentItem, auditEvent);
                }
                context.execute("PF('wDlgRefDocumentModal').hide()");
            }else if (referenceCode.equals(RefVidOperItem.REF_CODE)){
                if(refVidOperItem.getCode() == null || refVidOperItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refVidOperItem.getNameRu() == null || refVidOperItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                if (refVidOperItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refVidOperItem.setUserId(userId);
                refVidOperItem.setUserLocation(userLocation);
                refVidOperItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refVidOperItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refVidOperItem, auditEvent);
                }
                context.execute("PF('wDlgRefVidOperModal').hide()");
            }/*else if (referenceCode.equals("ref_request_type")) {

                if(refRequestTypeItem.getCode() == null || refRequestTypeItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refRequestTypeItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refRequestTypeItem.getNameRu() == null || refRequestTypeItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refRequestTypeItem.setUserId(userId);
                refRequestTypeItem.setUserLocation(userLocation);
                refRequestTypeItem.setDatlast(datlast);
                refRequestTypeItem.setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(refRequestTypeItem);
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(refRequestTypeItem);
                }

                context.execute("PF('wDlgRefRequestTypeModal').hide()");
            }else if (referenceCode.equals("ref_request_way")) {

                if(refRequestWayItem.getCode() == null || refRequestWayItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refRequestWayItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refRequestWayItem.getNameRu() == null || refRequestWayItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refRequestWayItem.setUserId(userId);
                refRequestWayItem.setUserLocation(userLocation);
                refRequestWayItem.setDatlast(datlast);
                refRequestWayItem.setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(refRequestWayItem);
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(refRequestWayItem);
                }

                context.execute("PF('wDlgRefRequestWayModal').hide()");
            }else if (referenceCode.equals("ref_market_kind")) {

                if(refMarketKindItem.getCode() == null || refMarketKindItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refMarketKindItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refMarketKindItem.getNameRu() == null || refMarketKindItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refMarketKindItem.setUserId(userId);
                refMarketKindItem.setUserLocation(userLocation);
                refMarketKindItem.setDatlast(datlast);
                refMarketKindItem.setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(refMarketKindItem);
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(refMarketKindItem);
                }
                context.execute("PF('wDlgRefMarketKindModal').hide()");
            }else if (referenceCode.equals("ref_category")) {

                if(refCategoryItem.getCode() == null || refCategoryItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refCategoryItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refCategoryItem.getNameRu() == null || refCategoryItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refCategoryItem.setUserId(userId);
                refCategoryItem.setUserLocation(userLocation);
                refCategoryItem.setDatlast(datlast);
                refCategoryItem.setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(refCategoryItem);
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(refCategoryItem);
                }
                context.execute("PF('wDlgRefCategoryModal').hide()");
            }else if (referenceCode.equals("ref_subcategory")) {

                if(refSubCategoryItem.getCode() == null || refSubCategoryItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refSubCategoryItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refSubCategoryItem.getNameRu() == null || refSubCategoryItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refSubCategoryItem.setUserId(userId);
                refSubCategoryItem.setUserLocation(userLocation);
                refSubCategoryItem.setDatlast(datlast);
                refSubCategoryItem.setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(refSubCategoryItem);
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(refSubCategoryItem);
                }
                context.execute("PF('wDlgRefSubCategoryModal').hide()");
            }else if (referenceCode.equals("ref_account_type")) {

                if(refAccountTypeItem.getCode() == null || refAccountTypeItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refAccountTypeItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refAccountTypeItem.getNameRu() == null || refAccountTypeItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refAccountTypeItem.setUserId(userId);
                refAccountTypeItem.setUserLocation(userLocation);
                refAccountTypeItem.setDatlast(datlast);
                refAccountTypeItem.setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(refAccountTypeItem);
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(refAccountTypeItem);
                }
                context.execute("PF('wDlgRefAccountTypeModal').hide()");
            }else if (referenceCode.equals("ref_subaccount_type")) {

                if(refSubAccountTypeItem.getCode() == null || refSubAccountTypeItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refSubAccountTypeItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refSubAccountTypeItem.getNameRu() == null || refSubAccountTypeItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refSubAccountTypeItem.setUserId(userId);
                refSubAccountTypeItem.setUserLocation(userLocation);
                refSubAccountTypeItem.setDatlast(datlast);
                refSubAccountTypeItem.setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(refSubAccountTypeItem);
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(refSubAccountTypeItem);
                }
                context.execute("PF('wDlgRefSubAccountTypeModal').hide()");
            }else if (referenceCode.equals("ref_type_holder_acc")) {

                if(refTypeHolderAccItem.getCode() == null || refTypeHolderAccItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refTypeHolderAccItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refTypeHolderAccItem.getNameRu() == null || refTypeHolderAccItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refTypeHolderAccItem.setUserId(userId);
                refTypeHolderAccItem.setUserLocation(userLocation);
                refTypeHolderAccItem.setDatlast(datlast);
                refTypeHolderAccItem.setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(refTypeHolderAccItem);
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(refTypeHolderAccItem);
                }
                context.execute("PF('wDlgRefTypeHolderAccModal').hide()");
            }else if (referenceCode.equals("ref_request_feature")) {

                if(refRequestFeatureItem.getCode() == null || refRequestFeatureItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refRequestFeatureItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refRequestFeatureItem.getNameRu() == null || refRequestFeatureItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refRequestFeatureItem.setUserId(userId);
                refRequestFeatureItem.setUserLocation(userLocation);
                refRequestFeatureItem.setDatlast(datlast);
                refRequestFeatureItem.setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(refRequestFeatureItem);
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(refRequestFeatureItem);
                }
                context.execute("PF('wDlgRefRequestFeatureModal').hide()");
            }else if (referenceCode.equals("ref_request_sts")) {

                if(refRequestStsItem.getCode() == null || refRequestStsItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refRequestStsItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refRequestStsItem.getNameRu() == null || refRequestStsItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refRequestStsItem.setUserId(userId);
                refRequestStsItem.setUserLocation(userLocation);
                refRequestStsItem.setDatlast(datlast);
                refRequestStsItem.setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(refRequestStsItem);
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(refRequestStsItem);
                }
                context.execute("PF('wDlgRefRequestStsModal').hide()");
            }else if (referenceCode.equals("ref_repo_kind")) {

                if(refRepoKindItem.getCode() == null || refRepoKindItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refRepoKindItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refRepoKindItem.getNameRu() == null || refRepoKindItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refRepoKindItem.setUserId(userId);
                refRepoKindItem.setUserLocation(userLocation);
                refRepoKindItem.setDatlast(datlast);
                refRepoKindItem.setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(refRepoKindItem);
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(refRepoKindItem);
                }
                context.execute("PF('wDlgRefRepoKindModal').hide()");
            }else if (referenceCode.equals("ref_market_type")) {

                if(refMarketTypeItem.getCode() == null || refMarketTypeItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refMarketTypeItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refMarketTypeItem.getNameRu() == null || refMarketTypeItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refMarketTypeItem.setUserId(userId);
                refMarketTypeItem.setUserLocation(userLocation);
                refMarketTypeItem.setDatlast(datlast);
                refMarketTypeItem.setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(refMarketTypeItem);
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(refMarketTypeItem);
                }
                context.execute("PF('wDlgRefMarketTypeModal').hide()");
            }else if (referenceCode.equals("ref_trad_method")) {

                if(refTradMethodItem.getCode() == null || refTradMethodItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refTradMethodItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refTradMethodItem.getNameRu() == null || refTradMethodItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refTradMethodItem.setUserId(userId);
                refTradMethodItem.setUserLocation(userLocation);
                refTradMethodItem.setDatlast(datlast);
                refTradMethodItem.setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(refTradMethodItem);
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(refTradMethodItem);
                }
                context.execute("PF('wDlgRefTradMethodModal').hide()");
            }else if (referenceCode.equals("ref_oper_type")) {

                if(refOperTypeItem.getCode() == null || refOperTypeItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refOperTypeItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refOperTypeItem.getNameRu() == null || refOperTypeItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refOperTypeItem.setUserId(userId);
                refOperTypeItem.setUserLocation(userLocation);
                refOperTypeItem.setDatlast(datlast);
                refOperTypeItem.setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(refOperTypeItem);
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(refOperTypeItem);
                }
                context.execute("PF('wDlgRefOperTypeModal').hide()");
            }else if (referenceCode.equals("ref_deal_sts")) {

                if(refDealStsItem.getCode() == null || refDealStsItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refDealStsItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refDealStsItem.getNameRu() == null || refDealStsItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refDealStsItem.setUserId(userId);
                refDealStsItem.setUserLocation(userLocation);
                refDealStsItem.setDatlast(datlast);
                refDealStsItem.setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(refDealStsItem);
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(refDealStsItem);
                }
                context.execute("PF('wDlgRefDealStsModal').hide()");
            }*/else if (referenceCode.equals(RefMrpItem.REF_CODE)){

                if(refMrpItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refMrpItem.getNameRu() == null || refMrpItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                if(refMrpItem.getValue() == null)
                    err_msg = "Введите значение!";
                if (refMrpItem.getValue() != null && !Validators.IsValidLong(String.valueOf(refMrpItem.getValue())))
                    err_msg = "Не правильный формат значения!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refMrpItem.setUserId(userId);
                refMrpItem.setUserLocation(userLocation);
                refMrpItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refMrpItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refMrpItem, auditEvent);
                }
                context.execute("PF('wDlgRefMrpModal').hide()");
            }else if (referenceCode.equals("ref_org_type")) {

                if(refOrgTypeItem.getCode() == null || refOrgTypeItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refOrgTypeItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refOrgTypeItem.getNameRu() == null || refOrgTypeItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refOrgTypeItem.setUserId(userId);
                refOrgTypeItem.setUserLocation(userLocation);
                refOrgTypeItem.setDatlast(datlast);
                refOrgTypeItem.setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(refOrgTypeItem);
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(refOrgTypeItem);
                }
                context.execute("PF('wDlgRefOrgTypeModal').hide()");
            }else if (referenceCode.equals(RefMfoRegItem.REF_CODE)){

                if(refMfoRegItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refMfoRegItem.getNameRu() == null || refMfoRegItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                if (refMfoRegItem.getRefDepartmentId() == 0)
                    err_msg = "Выберите филиал НБРК!";
                if (refMfoRegItem.getRefLpId() == 0)
                    err_msg = "Выберите Юр. лицо";
                if (refMfoRegItem.getEndDate() != null) {
                    if(refMfoRegItem.getBase() == null || refMfoRegItem.getBase().trim().isEmpty())
                        err_msg = "Необходимо указать основание";
                }
                if (refMfoRegItem.getNumReg() == null || refMfoRegItem.getNumReg().trim().isEmpty())
                    err_msg = "Введите номер";
                if (refMfoRegItem.getFioManager() == null || refMfoRegItem.getFioManager().trim().isEmpty())
                    err_msg = "Введите ФИО руководителя";
                if (refMfoRegItem.getAddress() == null || refMfoRegItem.getAddress().trim().isEmpty())
                    err_msg = "Введите адрес";
                if (refMfoRegItem.getContactDetails() == null || refMfoRegItem.getContactDetails().trim().isEmpty())
                    err_msg = "Введите номер телефона, факс, адрес электронной почты, интернет- ресурс (при наличии)";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refMfoRegItem.setUserId(userId);
                refMfoRegItem.setUserLocation(userLocation);
                refMfoRegItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refMfoRegItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refMfoRegItem, auditEvent);
                }
                context.execute("PF('wDlgRefMfoRegModal').hide()");
            }else if (referenceCode.equals(RefDealBAItem.REF_CODE)){

                if(refDealBAItem.getCode() == null || refDealBAItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refDealBAItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(refDealBAItem.getNameRu() == null || refDealBAItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                if(refDealBAItem.getNumAcc() == null)
                    err_msg = "Введите номер счета!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refDealBAItem.setUserId(userId);
                refDealBAItem.setUserLocation(userLocation);
                refDealBAItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refDealBAItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refDealBAItem, auditEvent);
                }
                context.execute("PF('wDlgRefDealBAModal').hide()");
            } else  if (referenceCode.equals("ref_type_activity")){
                if(getRefTypeActivityItem().getNameRu()==null || getRefTypeActivityItem().getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                 if(getRefTypeActivityItem().getBeginDate()==null)
                    err_msg = "Введите дату начала действия!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                getRefTypeActivityItem().setUserId(userId);
                getRefTypeActivityItem().setUserLocation(userLocation);
                getRefTypeActivityItem().setDatlast(datlast);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                   // id = sessionBean.getReference().insertRefTypeActivityItem(refTypeActivityItem);
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, getRefTypeActivityItem(), auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    //sessionBean.getReference().updateRefTypeActivityItem(refTypeActivityItem);
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, getRefTypeActivityItem(), auditEvent);
                }
                context.execute("PF('wDlgRefTypeActivityModal').hide()");
            } else  if (referenceCode.equals("ref_npa")){
                if(getRefNpaItem().getNameRu()==null || getRefNpaItem().getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                 if(getRefNpaItem().getBeginDate()==null)
                    err_msg = "Введите дату начала действия!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                getRefNpaItem().setUserId(userId);
                getRefNpaItem().setUserLocation(userLocation);
                getRefNpaItem().setDatlast(datlast);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    //id = sessionBean.getReference().insertRefNpaItem(refNpaItem);
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, getRefNpaItem(), auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    //sessionBean.getReference().updateRefNpaItem(refNpaItem);
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, getRefNpaItem(), auditEvent);
                }
                context.execute("PF('wDlgRefNpaModal').hide()");
            } else  if (referenceCode.equals("ref_wkd_holidays")){
                if(getRefWkdHolidayItem().getNameDate()==null || getRefWkdHolidayItem().getNameDate().trim().isEmpty())
                    err_msg = "Введите наименование!";
                 if(getRefWkdHolidayItem().getBeginDate()==null)
                    err_msg = "Введите дату начала действия!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                getRefWkdHolidayItem().setUserId(userId);
                getRefWkdHolidayItem().setUserLocation(userLocation);
                getRefWkdHolidayItem().setDatlast(datlast);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    //id = sessionBean.getReference().insertRefWkdHolidayItem(refWkdHolidayItem);
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, getRefWkdHolidayItem(), auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                   //sessionBean.getReference().updateRefWkdHolidayItem(refWkdHolidayItem);
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, getRefWkdHolidayItem(), auditEvent);
                }
                context.execute("PF('wDlgRefWkdHolidayModal').hide()");
            }else if (referenceCode.equals("ref_finrep_indic")) {
               /* if(refFinRepIndicItem.getCode() == null || refFinRepIndicItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!"; */
                if(getRefFinRepIndicItem().getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(getRefFinRepIndicItem().getNameRu() == null || getRefFinRepIndicItem().getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                getRefFinRepIndicItem().setUserId(userId);
                getRefFinRepIndicItem().setUserLocation(userLocation);
                getRefFinRepIndicItem().setDatlast(datlast);
                getRefFinRepIndicItem().setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(getRefFinRepIndicItem());
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(getRefFinRepIndicItem());
                }
                context.execute("PF('wDlgRefFinRepIndicModal').hide()");
            } else if (referenceCode.equals("ref_type_deals")) {
               /* if(refFinRepIndicItem.getCode() == null || refFinRepIndicItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!"; */
                if(getRefTypeDealsItem().getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(getRefTypeDealsItem().getNameRu() == null || getRefTypeDealsItem().getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                getRefTypeDealsItem().setUserId(userId);
                getRefTypeDealsItem().setUserLocation(userLocation);
                getRefTypeDealsItem().setDatlast(datlast);
                getRefTypeDealsItem().setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(getRefTypeDealsItem());
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(getRefTypeDealsItem());
                }
                context.execute("PF('wDlgRefTypeDealsModal').hide()");
            } else if (referenceCode.equals("ref_degree_relation")) {
               /* if(refFinRepIndicItem.getCode() == null || refFinRepIndicItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!"; */
                if(getRefDegreeRelationItem().getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(getRefDegreeRelationItem().getNameRu() == null || getRefDegreeRelationItem().getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                getRefDegreeRelationItem().setUserId(userId);
                getRefDegreeRelationItem().setUserLocation(userLocation);
                getRefDegreeRelationItem().setDatlast(datlast);
                getRefDegreeRelationItem().setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(getRefDegreeRelationItem());
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(getRefDegreeRelationItem());
                }
                context.execute("PF('wDlgRefDegreeRelationModal').hide()");
            } else if (referenceCode.equals("ref_sign_related")) {
               /* if(refFinRepIndicItem.getCode() == null || refFinRepIndicItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!"; */
                if(getRefSignRelatedItem().getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(getRefSignRelatedItem().getNameRu() == null || getRefSignRelatedItem().getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                getRefSignRelatedItem().setUserId(userId);
                getRefSignRelatedItem().setUserLocation(userLocation);
                getRefSignRelatedItem().setDatlast(datlast);
                getRefSignRelatedItem().setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(getRefSignRelatedItem());
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(getRefSignRelatedItem());
                }
                context.execute("PF('wDlgRefSignRelatedModal').hide()");
            }else if (referenceCode.equals("ref_type_risk")) {
               /* if(refFinRepIndicItem.getCode() == null || refFinRepIndicItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!"; */
                if(getRefTypeRiskItem().getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                if(getRefTypeRiskItem().getNameRu() == null || getRefTypeRiskItem().getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                getRefTypeRiskItem().setUserId(userId);
                getRefTypeRiskItem().setUserLocation(userLocation);
                getRefTypeRiskItem().setDatlast(datlast);
                getRefTypeRiskItem().setRefCode(referenceCode);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefSimpleItem(getRefTypeRiskItem());
                } else if (editType.equals("update_item")) {
                    if (editKind == null) {
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefSimpleItem(getRefTypeRiskItem());
                }
                context.execute("PF('wDlgRefTypeRiskModal').hide()");
            } else  if (referenceCode.equals("ref_basisofcontrol")){
                if(getRefBasisofControlItem().getNameRu()==null || getRefBasisofControlItem().getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                if(getRefBasisofControlItem().getBasisControl()==null || getRefBasisofControlItem().getBasisControl().trim().isEmpty())
                     err_msg = "Введите основание контроля!";
                if(getRefBasisofControlItem().getBeginDate()==null)
                    err_msg = "Введите дату начала действия!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                getRefBasisofControlItem().setUserId(userId);
                getRefBasisofControlItem().setUserLocation(userLocation);
                getRefBasisofControlItem().setDatlast(datlast);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                   // id = sessionBean.getReference().insertRefTypeActivityItem(refTypeActivityItem);
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, getRefBasisofControlItem(), auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    //sessionBean.getReference().updateRefTypeActivityItem(refTypeActivityItem);
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, getRefBasisofControlItem(), auditEvent);
                }
                context.execute("PF('wDlgRefBasisofControlModal').hide()");
            } else  if (referenceCode.equals("ref_extind")){
                if(refExtIndItem.getCode()==null || refExtIndItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if(refExtIndItem.getNameRu()==null || refExtIndItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                if(refExtIndItem.getBeginDate()==null)
                    err_msg = "Введите дату начала действия!";
                if (refExtIndItem.getValueType().equals(" ") || refExtIndItem.getValueType()==null)
                    err_msg = "Выберите тип возвращаемого значения!";
                if (RefExtParamList == null)
                    err_msg = "Заполните параметры!";

                int nullCount = 0;

                if (RefExtParamList != null) {
                    for (RefExtIndicatorParam param : RefExtParamList) {
                        if (param.getValueType().equals(" ") || param.getValueType() == null)
                            nullCount++;
                    }
                    if (nullCount > 0) err_msg = "Выберите тип значения";
                }

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                getRefExtIndItem().setUserId(userId);
                getRefExtIndItem().setUserLocation(userLocation);
                getRefExtIndItem().setDatlast(datlast);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefExtIndicatorWithParam(getRefExtIndItem(), RefExtParamList, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefExtIndicatorWithParam(getRefExtIndItem(), RefExtParamList, auditEvent);
                }
                context.execute("PF('wDlgRefExtIndModal').hide()");
            } else if (referenceCode.equals(RefMajorMemberItem.REF_CODE)){
                if(refMajorMemberItem.getNameRu() == null || refMajorMemberItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                if (refMajorMemberItem.getBeginDate() == null)
                    err_msg = "Введите дату начала дейтсвия!";

                if (!refMajorMemberItem.getIsNonRezident()){
                    if(refMajorMemberItem.getIdn() == null)
                        err_msg = "Введите БИН";
                }
                if (refMajorMemberItem.getIdn() != null) {
                    if (!Validators.validateIDN(refMajorMemberItem.getIdn()))
                        err_msg = "Введен не корректный БИН";
                }
                if (refMajorMemberItem.getLegalAddress() == null || refMajorMemberItem.getLegalAddress().trim().isEmpty())
                    err_msg = "Введите Юр. адрес !";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refMajorMemberItem.setUserId(userId);
                refMajorMemberItem.setUserLocation(userLocation);
                refMajorMemberItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refMajorMemberItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refMajorMemberItem, auditEvent);
                }
                context.execute("PF('wDlgRefMajorMemberModal').hide()");
            }else if (referenceCode.equals(RefPeriodAlgItem.REF_CODE)){

                if(refPeriodAlgItem.getNameRu() == null || refPeriodAlgItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                else if(refPeriodAlgItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                else if (refPeriodAlgItem.getAlg().trim().isEmpty())
                    err_msg = "Введите алгоритм!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refPeriodAlgItem.setUserId(userId);
                refPeriodAlgItem.setUserLocation(userLocation);
                refPeriodAlgItem.setDatlast(datlast);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refPeriodAlgItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refPeriodAlgItem, auditEvent);
                }
                context.execute("PF('wDlgRefPeriodAlgModal').hide()");
            }else if (referenceCode.equals(RefPeriodItem.REF_CODE)){

                if(refPeriodItem.getNameRu() == null || refPeriodItem.getNameRu().trim().isEmpty())
                    err_msg = "Введите наименование!";
                else if(refPeriodItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";
                else if (refPeriodItem.getRefPeriodAlgId() == null || refPeriodItem.getRefPeriodAlgId() == 0)
                    err_msg = "Выберите алгоритм!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refPeriodItem.setUserId(userId);
                refPeriodItem.setUserLocation(userLocation);
                refPeriodItem.setDatlast(datlast);

                if (editType.equals("insert_item") || editType.equals("refresh_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refPeriodItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refPeriodItem, auditEvent);
                }

                if(argumentList.size() > 0) {
                    for(RefPeriodArgument item : argumentList){
                        if(item.getValueType() == null){
                            throw new OracleException("Укажите тип данных для аргумента " + item.getName() + "!");
                        }else if (item.getStrValue() == null || item.getStrValue().trim().isEmpty()){
                            throw new OracleException("Введите значение для аргумента " + item.getName() + "!");
                        }
                    }
                    sessionBean.getReference().insertArguments(argumentList, refPeriodItem.getId(), null);
                }

                context.execute("PF('wDlgRefPeriodModal').hide()");
            } else if (referenceCode.equals(RefInsurGroupsItem.REF_CODE)){

                if (refInsurGroupsItem.getCode() == null || refInsurGroupsItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if (refInsurGroupsItem.getRefLegalPerson() == null)
                    err_msg = "Выберите наименование юр.лица!";
                if (refInsurGroupsItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refInsurGroupsItem.setUserId(userId);
                refInsurGroupsItem.setUserLocation(userLocation);
                refInsurGroupsItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item") || editType.equals("insert_child_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refInsurGroupsItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refInsurGroupsItem, auditEvent);
                }
                context.execute("PF('wDlgRefInsurGroupsModal').hide()");
            }else if (referenceCode.equals(RefBankConglomeratesItem.REF_CODE)){

                if (refBankConglItem.getCode() == null || refBankConglItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if (refBankConglItem.getRefUnionPersons() == null)
                    err_msg = "Выберите наименование юр./физ.лица!";
                if (refBankConglItem.getRefLegalPerson() == null && editType.equals("insert_child_item"))
                    err_msg = "Выберите наименование родит.организации!";
                if (refBankConglItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refBankConglItem.setUserId(userId);
                refBankConglItem.setUserLocation(userLocation);
                refBankConglItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item") || editType.equals("insert_child_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refBankConglItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refBankConglItem, auditEvent);
                }
                context.execute("PF('wDlgRefBankConglModal').hide()");
            } else if (referenceCode.equals(RefShareHoldersItem.REF_CODE)){

                if (refShareHoldItem.getCode() == null || refShareHoldItem.getCode().trim().isEmpty())
                    err_msg = "Введите код!";
                if (refShareHoldItem.getRefUnionPersons() == null)
                    err_msg = "Выберите наименование юр./физ.лица!";
                if (refShareHoldItem.getRefIssuers() == null)
                    err_msg = "Выберите наименование эмитента!";
                if (refShareHoldItem.getShare_value() == null)
                    err_msg = "Введите долю участия!";
                if (refShareHoldItem.getType_holders() == null)
                    err_msg = "Введите вид держателя акций!";
                if (refShareHoldItem.getNote() == null)
                   err_msg = "Введите прмечание!";
                if (refShareHoldItem.getBeginDate() == null)
                    err_msg = "Введите дату начала действия!";

                if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

                refShareHoldItem.setUserId(userId);
                refShareHoldItem.setUserLocation(userLocation);
                refShareHoldItem.setDatlast(datlast);

                if(editType.equals("insert_item") || editType.equals("refresh_item") || editType.equals("insert_child_item")) {
                    id = sessionBean.getReference().insertRefAbstractItem(referenceCode, refShareHoldItem, auditEvent);
                }else if(editType.equals("update_item")){
                    if(editKind == null){
                        throw new OracleException("Выберите вид редактирования!");
                    }
                    sessionBean.getReference().updateRefAbstractItem(referenceCode, refShareHoldItem, auditEvent);
                }
                context.execute("PF('wDlgRefShareHoldModal').hide()");
            }
        } catch (OracleException ex) {
            FacesMessage message = new FacesMessage();
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            message.setSummary(ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }
        refreshSimpleRefList("update_ref");
    }

    public void getFillExtParam() {

       RequestContext context = RequestContext.getCurrentInstance();
       String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();SimpleReference refValueType = new SimpleReference();
       Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();
       String err_msg = "";
       String alg = "";
       Long refExtIndId = 0L;

       try{
           refValueType.setRefCode("ref_value_type");
           RefValueTypeList =  sessionBean.getReference().getRefSimpleListByParams(date,refValueType);

           if(selectedRefExtInd == null || refExtIndItem.getAlgorithm() == null || (refExtIndItem.getAlgorithm() != null && refExtIndItem.getAlgorithm().trim().isEmpty())) {
               err_msg = "Заполните поле алгоритм!";
           }
           if (!err_msg.trim().isEmpty()) {
               throw new OracleException(err_msg);
           }

           if (selectedRefExtInd == null) {
               alg = refExtIndItem.getAlgorithm();
           }else {
               alg = refExtIndItem.getAlgorithm();
               refExtIndId = selectedRefExtInd.getId();
           }

           RefExtParamList = sessionBean.getReference().getRefExtParamListByParams(Convert.getDateFromString("01.01.3333"), alg, refExtIndId);
           context.update(liferayNameSpace + ":frmRefExtParamModal");
      } catch (OracleException ex) {
         FacesMessage message = new FacesMessage();
         message.setSeverity(FacesMessage.SEVERITY_ERROR);
         message.setSummary(ex.getMessage());
         FacesContext.getCurrentInstance().addMessage(null, message);
         return;
      }
 }

    public void getChangeExtParam () {
        RequestContext context = RequestContext.getCurrentInstance();
        String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();
        SimpleReference refValueType = new SimpleReference();
        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();
        String err_msg = "";
        String alg = "";
        Long id = 0L;
        Long refExtIndId = 0L;

        try {
            refValueType.setRefCode("ref_value_type");
            RefValueTypeList = sessionBean.getReference().getRefSimpleListByParams(date, refValueType);
            if (selectedRefExtInd == null || refExtIndItem.getAlgorithm().trim().isEmpty() || refExtIndItem.getAlgorithm() == null)
                err_msg = "Заполните поле алгоритм!";

            if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

            if (selectedRefExtInd == null)
                alg = refExtIndItem.getAlgorithm();
            else {
                alg = selectedRefExtInd.getAlgorithm();
                id = selectedRefExtInd.getId();
            }

            RefExtParamList = sessionBean.getReference().getRefExtParamListByParams(Convert.getDateFromString("01.01.3333"), alg, id);
            context.update(liferayNameSpace + ":frmRefExtParamModal");
        } catch (OracleException ex) {
            FacesMessage message = new FacesMessage();
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            message.setSummary(ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

    }
     // methods for ref_majormember subreferences

    public void getAddUpdMemberOrgs(String type){
        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();
        RequestContext context = RequestContext.getCurrentInstance();
        String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();
        SimpleReference refStatus = new SimpleReference();
        refStatus.setRefCode("ref_status");
        refStatusList = sessionBean.getReference().getRefSimpleListByParams(date,refStatus);
        typeMM = type;
        if (typeMM.equals("insert_item")){
            userName = null;
            userLocation = null;
            datlast = null;

            refMajorMemberOrgItem = new RefMajorMemberOrgItem();
            refMajorMemberOrgItem.setBeginDate(date);
            setIsVisibleEditKnd(false);
        }else if ((typeMM.equals("update_item")) || (typeMM.equals("refresh_item")) || (typeMM.equals("view_item"))) {
            RefMajorMemberOrgItem filter = new RefMajorMemberOrgItem();
            filter.setId(false ? selectedRefMajorMemberOrgVer.getId() : selectedRefMajorMemberOrg.getId());
            refMajorMemberOrgItem = (RefMajorMemberOrgItem) sessionBean.getReference().getRefAbstractItem("ref_major_memorgs", filter);
            userName = refMajorMemberOrgItem.getUserName();
            userLocation = refMajorMemberOrgItem.getUserLocation();
            datlast = refMajorMemberOrgItem.getDatlast();
            setIsVisibleEditKnd(true);
        }

        context.update(liferayNameSpace + ":frmRefMajorMemOrgsModal");
        context.execute("PF('wDlgRefMajorMemOrgsModal').show()");
    }

    public void getAddUpdMemberDetails(String type){
        Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();
        RequestContext context = RequestContext.getCurrentInstance();
        String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();
        typeMM = type;
        if (typeMM.equals("insert_item")){
            userName = null;
            userLocation = null;
            datlast = null;

            refMajorMemDetailsItem = new RefMajorMemDetailsItem();
            refMajorMemDetailsItem.setBeginDate(date);
            setIsVisibleEditKnd(false);
        }else if ((typeMM.equals("update_item")) || (typeMM.equals("refresh_item")) || (typeMM.equals("view_item"))) {
            RefMajorMemDetailsItem filter = new RefMajorMemDetailsItem();
            filter.setId(false ? selectedRefMajorMemDetailsVer.getId() : selectedRefMajorMemDetails.getId());
            refMajorMemDetailsItem = (RefMajorMemDetailsItem) sessionBean.getReference().getRefAbstractItem("ref_major_memdetails", filter);
            userName = refMajorMemDetailsItem.getUserName();
            userLocation = refMajorMemDetailsItem.getUserLocation();
            datlast = refMajorMemDetailsItem.getDatlast();
            setIsVisibleEditKnd(true);
        }

        context.update(liferayNameSpace + ":frmRefMajorMemDetailsModal");
        context.execute("PF('wDlgRefMajorMemDetailsModal').show()");
    }

    public void saveRefMajorMemOrgsItem() {
        RequestContext context = RequestContext.getCurrentInstance();
        String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();
        String err_msg = "";
        Long id = 0L;
        Long kindEvent = null;
        datlast = sessionBean.getIntegration().getNewDateFromBackEndServer();

        if(typeMM.equals("insert_item"))
            kindEvent = 49L;
        else if (typeMM.equals("update_item"))
            kindEvent = 51L;
        else if (typeMM.equals("refresh_item"))
            kindEvent = 6L;

        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setCodeObject("ref_major_memorgs");
        auditEvent.setNameObject(null);
        auditEvent.setIdKindEvent(kindEvent);
        auditEvent.setDateEvent(datlast);
        auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
        auditEvent.setUserId(userId);
        auditEvent.setUserLocation(userLocation);

        try {
            if(refMajorMemberOrgItem.getNameRu()==null || refMajorMemberOrgItem.getNameRu().trim().isEmpty())
                err_msg = "Введите юр.лицо !";
             if(refMajorMemberOrgItem.getBeginDate()==null)
                err_msg = "Введите дату начала действия!";
             if(refMajorMemberOrgItem.getRefStatus()==0)
                err_msg = "Выберите статус!";

            if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

            refMajorMemberOrgItem.setUserId(userId);
            refMajorMemberOrgItem.setUserLocation(userLocation);
            refMajorMemberOrgItem.setDatlast(datlast);
            refMajorMemberOrgItem.setRefMajorMember(refMajorMemberItem.getId());

            if (typeMM.equals("insert_item") || typeMM.equals("refresh_item")) {
                id = sessionBean.getReference().insertRefAbstractItem("ref_major_memorgs",refMajorMemberOrgItem, auditEvent);
            }else if(typeMM.equals("update_item")){
                if(editKind == null){
                    throw new OracleException("Выберите вид редактирования!");
                }
                sessionBean.getReference().updateRefAbstractItem("ref_major_memorgs", refMajorMemberOrgItem, auditEvent);
             }
            context.execute("PF('wDlgRefMajorMemOrgsModal').hide()");
            filterRefMajorMemberOrgItem.setSearchAllVer(searchAllVer);
            filterRefMajorMemberOrgItem.setRefMajorMember(refMajorMemberItem.getId());
            RefMajorMemberOrgList = (List<RefMajorMemberOrgItem>) sessionBean.getReference().getRefAbstractByFilterList("ref_major_memorgs", filterRefMajorMemberOrgItem, Convert.getDateFromString("01.01.3333"));


        } catch (OracleException ex) {
            FacesMessage message = new FacesMessage();
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            message.setSummary(ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

    }

    public void saveRefMajorMemDetailsItem() {
        RequestContext context = RequestContext.getCurrentInstance();
        String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();
        String err_msg = "";
        Long id = 0L;
        Long kindEvent = null;
        datlast = sessionBean.getIntegration().getNewDateFromBackEndServer();

        if(typeMM.equals("insert_item"))
            kindEvent = 49L;
        else if (typeMM.equals("update_item"))
            kindEvent = 51L;
        else if (typeMM.equals("refresh_item"))
            kindEvent = 6L;

        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setCodeObject("ref_major_memdetails");
        auditEvent.setNameObject(null);
        auditEvent.setIdKindEvent(kindEvent);
        auditEvent.setDateEvent(datlast);
        auditEvent.setIdRefRespondent(sessionBean.respondent.getId());
        auditEvent.setUserId(userId);
        auditEvent.setUserLocation(userLocation);

        try {
             if(selectedRefMajorMemberOrg.getId()==null)
                err_msg = "Выберите организацию в таблице 1!";
             if(refMajorMemDetailsItem.getBeginDate()==null)
                err_msg = "Введите дату начала действия!";

            if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

            refMajorMemDetailsItem.setUserId(userId);
            refMajorMemDetailsItem.setUserLocation(userLocation);
            refMajorMemDetailsItem.setDatlast(datlast);
            refMajorMemDetailsItem.setRef_major_memorgs(selectedRefMajorMemberOrg.getId());

            if (typeMM.equals("insert_item") || typeMM.equals("refresh_item")) {
                id = sessionBean.getReference().insertRefAbstractItem("ref_major_memdetails",refMajorMemDetailsItem, auditEvent);
            }else if(typeMM.equals("update_item")){
                if(editKind == null){
                    throw new OracleException("Выберите вид редактирования!");
                }
                sessionBean.getReference().updateRefAbstractItem("ref_major_memdetails", refMajorMemDetailsItem, auditEvent);
             }
            context.execute("PF('wDlgRefMajorMemDetailsModal').hide()");
            filterRefMajorMemDetailsItem.setSearchAllVer(searchAllVer);
            filterRefMajorMemDetailsItem.setRef_major_memorgs(selectedRefMajorMemberOrg.getId());
            RefMajorMemDetailsList = (List<RefMajorMemDetailsItem>) sessionBean.getReference().getRefAbstractByFilterList("ref_major_memdetails", filterRefMajorMemDetailsItem, Convert.getDateFromString("01.01.3333"));



        } catch (OracleException ex) {
            FacesMessage message = new FacesMessage();
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            message.setSummary(ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }

    }

    public void refreshRefMajorMemVerList(String subreferencesName) {
        RequestContext context = RequestContext.getCurrentInstance();
        String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();
        if (subreferencesName.equals(RefMajorMemberOrgItem.REF_CODE)) {
            selectedRefMajorMemberOrgVer = null;
            RefMajorMemberOrgItem refMajorMemOrgItem = new RefMajorMemberOrgItem();
            refMajorMemOrgItem.setRecId(selectedRefMajorMemberOrg.getRecId());
            RefMajorMemberOrgVerList = (List<RefMajorMemberOrgItem>) sessionBean.getReference().getRefAbstractByFilterList(subreferencesName, refMajorMemOrgItem, null);
            context.update(liferayNameSpace + ":frmRefMajorMemOrgVer");
            context.execute("PF('wDlgRefMajorMemOrgVer').show()");
        } else if (subreferencesName.equals(RefMajorMemDetailsItem.REF_CODE)) {
            selectedRefMajorMemDetailsVer = null;
            RefMajorMemDetailsItem refMajorMemDetailItem = new RefMajorMemDetailsItem();
            refMajorMemDetailItem.setRecId(selectedRefMajorMemDetails.getRecId());
            RefMajorMemDetailsVerList = (List<RefMajorMemDetailsItem>) sessionBean.getReference().getRefAbstractByFilterList(subreferencesName, refMajorMemDetailItem, null);
            context.update(liferayNameSpace + ":frmRefMajorMemDetailsVer");
            context.execute("PF('wDlgRefMajorMemDetailsVer').show()");
        }
    }

    public void refreshRefMajorMemHstList (String subreferencesName) {
        if (subreferencesName.equals(RefMajorMemberOrgItem.REF_CODE)){
            RefMajorMemberOrgHstList = (List<RefMajorMemberOrgItem>) sessionBean.getReference().getRefAbstractHstList(subreferencesName, selectedRefMajorMemberOrgVer.getId());
        } else if (subreferencesName.equals(RefMajorMemDetailsItem.REF_CODE)){
            RefMajorMemDetailsHstList = (List<RefMajorMemDetailsItem>) sessionBean.getReference().getRefAbstractHstList(subreferencesName, selectedRefMajorMemDetailsVer.getId());
        }
    }

    public void tabVisible (){
        if (referenceCode != null) {
            if (referenceCode.equals(RefPostItem.REF_CODE)) {
                RefPostVisible = false;
                selectedRefPost = null;
            }else if (referenceCode.equals(RefPersonItem.REF_CODE)){
                RefPersonVisible = false;
                selectedRefPerson = null;
            }else if (referenceCode.equals(RefLegalPersonItem.REF_CODE)){
                RefLegalPersonVisible = false;
                selectedRefLegalPerson = null;
            }else if (referenceCode.equals(RefCountryItem.REF_CODE)){
                RefCountryVisible = false;
                selectedRefCountry = null;
            }else if (referenceCode.equals(RefManagersItem.REF_CODE)){
                RefManagersVisible = false;
                selectedRefManagers = null;
            }else if (referenceCode.equals(RefTypeBusEntityItem.REF_CODE)){
                RefTypeBusEntityVisible = false;
                selectedRefTypeBusEntity = null;
            }else if (referenceCode.equals(RefRegionItem.REF_CODE)){
                RefRegionVisible = false;
                selectedRefRegion = null;
            }else if (referenceCode.equals(RefRequirementItem.REF_CODE)){
                RefRequirementVisible = false;
                selectedRefRequirement = null;
            }else if (referenceCode.equals(RefTypeProvideItem.REF_CODE)){
                RefTypeProvideVisible = false;
                selectedRefTypeProvide = null;
            }else if (referenceCode.equals(RefTransTypeItem.REF_CODE)){
                RefTransTypeVisible = false;
                selectedRefTransType = null;
            }else if (referenceCode.equals(RefBalanceAccItem.REF_CODE)){
                RefBalanceAccVisible = false;
                selectedBalanceAcc = null;
            }else if (referenceCode.equals(RefConnOrgItem.REF_CODE)){
                RefConnOrgVisible = false;
                selectedRefConnOrg = null;
            }else if (referenceCode.equals(RefDepartmentItem.REF_CODE)){
                RefDepartmentVisible = false;
                selectedRefDepartment = null;
            }else if (referenceCode.equals(RefBankItem.REF_CODE)){
                RefBankVisible = false;
                selectedRefBank = null;
            }else if (referenceCode.equals(RefRateAgencyItem.REF_CODE)){
                RefRateAgencyVisible = false;
                selectedRefRateAgency = null;
            }else if (referenceCode.equals(RefCurrencyItem.REF_CODE)){
                RefCurrencyVisible = false;
                selectedRefCurrency = null;
            }else if (referenceCode.equals(RefCurrencyRateItem.REF_CODE)){
                RefCurrencyRateVisible = false;
                selectedRefCurrencyRate = null;
            }else if (referenceCode.equals(RefSubjectTypeItem.REF_CODE)){
                RefSubjectTypeVisible = false;
                selectedRefSubjectType = null;
            }else if (referenceCode.equals(RefRespondentItem.REF_CODE)){
                RefRespondentVisible = false;
                selectedRefRespondent = null;
            }else if (referenceCode.equals(RefDocTypeItem.REF_CODE)){
                RefDocTypeVisible = false;
                selectedRefDocType = null;
            }else if (referenceCode.equals(RefDocumentItem.REF_CODE)){
                RefDocumentVisible = false;
                selectedRefDocument = null;
            }else if (referenceCode.equals(RefIssuersItem.REF_CODE)){
                RefIssuersVisible = false;
                selectedRefIssuers = null;
            }else if (referenceCode.equals(RefSecuritiesItem.REF_CODE)){
                RefSecuritiesVisible = false;
                selectedRefSecurities = null;
            }else if (referenceCode.equals(RefVidOperItem.REF_CODE)){
                RefVidOperVisible = false;
                selectedRefVidOper = null;
            }else if (referenceCode.equals(RefBranchItem.REF_CODE)){
                RefBranchVisible = false;
                selectedRefBranch = null;
            }else if (referenceCode.equals("ref_branch_old")){
                RefBranchOldVisible = false;
                selectedRefBranchOld = null;
            }else if (referenceCode.equals(RefListingEstimationItem.REF_CODE)){
                RefListingEstimationVisible = false;
                selectedRefListingEstimation = null;
            }else if (referenceCode.equals(RefRatingEstimationItem.REF_CODE)){
                RefRatingEstimationVisible = false;
                selectedRefRatingEstimation = null;
            }else if (referenceCode.equals(RefRatingCategoryItem.REF_CODE)){
                RefRatingCategoryVisible = false;
                selectedRefRatingCategory = null;
            }/*else if (referenceCode.equals("ref_request_type")){
                RefRequestTypeVisible = false;
                selectedRefRequestType = null;
            }else if (referenceCode.equals("ref_request_way")){
                RefRequestWayVisible = false;
                selectedRefRequestWay = null;
            }else if (referenceCode.equals("ref_market_kind")) {
                RefMarketKindVisible = false;
                selectedRefMarketKind = null;
            }else if (referenceCode.equals("ref_category")){
                RefCategoryVisible = false;
                selectedRefCategory = null;
            }else if (referenceCode.equals("ref_subcategory")){
                RefSubCategoryVisible = false;
                selectedRefSubCategory = null;
            }else if (referenceCode.equals("ref_account_type")){
                RefAccountTypeVisible = false;
                selectedRefAccountType = null;
            }else if (referenceCode.equals("ref_subaccount_type")){
                RefSubAccountTypeVisible = false;
                selectedRefSubAccountType = null;
            }else if (referenceCode.equals("ref_type_holder_acc")){
                RefTypeHolderAccVisible = false;
                selectedRefTypeHolderAcc = null;
            }else if (referenceCode.equals("ref_request_feature")){
                RefRequestFeatureVisible = false;
                selectedRefRequestFeature = null;
            }else if (referenceCode.equals("ref_request_sts")){
                RefRequestStsVisible = false;
                selectedRefRequestSts = null;
            }else if (referenceCode.equals("ref_repo_kind")){
                RefRepoKindVisible = false;
                selectedRefRepoKind = null;
            }else if (referenceCode.equals("ref_market_type")){
                RefMarketTypeVisible = false;
                selectedRefMarketType = null;
            }else if (referenceCode.equals("ref_trad_method")){
                RefTradMethodVisible = false;
                selectedRefTradMethod = null;
            }else if (referenceCode.equals("ref_oper_type")){
                RefOperTypeVisible = false;
                selectedRefOperType = null;
            }else if (referenceCode.equals("ref_deal_sts")){
                RefDealStsVisible = false;
                selectedRefDealSts = null;
            }*/else if (referenceCode.equals(RefMrpItem.REF_CODE)) {
                RefMrpVisible = false;
                selectedRefMrp = null;
            }else if (referenceCode.equals("ref_org_type")){
                RefOrgTypeVisible = false;
                selectedRefOrgType = null;
            }else if (referenceCode.equals(RefMfoRegItem.REF_CODE)) {
                RefMfoRegVisible = false;
                selectedRefMfoReg = null;
            }else if (referenceCode.equals(RefDealBAItem.REF_CODE)) {
                RefDealBAVisible = false;
                selectedRefDealBA = null;
            }else if (referenceCode.equals("ref_type_activity")) {
                RefTypeActivityVisible = false;
                selectedRefTypeActivity = null;
            }else if (referenceCode.equals("ref_npa")) {
                RefNpaVisible = false;
                selectedRefNpa = null;
            }else if (referenceCode.equals("ref_wkd_holidays")) {
                RefWkdHolidayVisible = false;
                selectedRefWkdHoliday = null;
            }else if (referenceCode.equals("ref_finrep_indic")) {
                RefFinRepIndicVisible = false;
                selectedRefFinRepIndic = null;
            }else if (referenceCode.equals("ref_type_deals")) {
                RefTypeDealsVisible = false;
                selectedRefTypeDeals = null;
            }else if (referenceCode.equals("ref_degree_relation")) {
                RefDegreeRelationVisible = false;
                selectedRefDegreeRelation = null;
            }else if (referenceCode.equals("ref_sign_related")) {
                RefSignRelatedVisible = false;
                selectedRefSignRelated = null;
            }else if (referenceCode.equals("ref_type_risk")) {
                RefTypeRiskVisible = false;
                selectedRefTypeRisk = null;
            }else if (referenceCode.equals("ref_basisofcontrol")) {
                RefBasisofControlVisible = false;
                selectedRefBasisofControl = null;
            } else if (referenceCode.equals("ref_insur_org")) {
                RefInsurOrgVisible = false;
                selectedRefInsurOrg = null;
            }else if (referenceCode.equals("ref_extind")) {
                RefExtIndVisible = false;
                selectedRefExtInd = null;
            }else if (referenceCode.equals(RefMajorMemberItem.REF_CODE)) {
                RefMajorMemberVisible = false;
                selectedRefMajorMember = null;
            }else if (referenceCode.equals(RefPeriodAlgItem.REF_CODE)) {
                refPeriodAlgVisible = false;
                selectedRefPeriodAlg = null;
            }else if (referenceCode.equals(RefPeriodItem.REF_CODE)) {
                refPeriodVisible = false;
                selectedRefPeriod = null;
            }else if (referenceCode.equals(RefInsurGroupsItem.REF_CODE)) {
                RefInsurGroupsVisible = false;
                selectedRefInsurGroups = null;
            }else if (referenceCode.equals(RefBankConglomeratesItem.REF_CODE)) {
                RefBankConglVisible = false;
                selectedRefBankCongl = null;
            }else if (referenceCode.equals(RefShareHoldersItem.REF_CODE)) {
                RefShareHoldVisible = false;
                selectedRefShareHold = null;
            }
        }
    }

    public void getUnionPersonData(Long unionId) {
         Date date = sessionBean.getIntegration().getNewDateFromBackEndServer();
         setIsVisibleEditKnd(false);
         if(unionId != null && unionId != 0) {

             RefUnionPersonItem unionItem = sessionBean.getReference().getUnionPersonItemById(unionId);

             switch (unionItem.getType()){
                 case 1:
                     RefLegalPersonItem filterLP = new RefLegalPersonItem();
                     filterLP.setRecId(unionItem.getRecId());
                     Long idLP = sessionBean.getReference().getRefAbstractByFilterList(RefLegalPersonItem.REF_CODE, filterLP, date).get(0).getId();
                     getLPData(idLP, "view_item", date);
                     break;
                 case 2:
                     RefPersonItem filterPerson = new RefPersonItem();
                     filterPerson.setRecId(unionItem.getRecId());
                     Long personId = sessionBean.getReference().getRefAbstractByFilterList(RefPersonItem.REF_CODE, filterPerson, date).get(0).getId();
                     getPersonData(personId, "view_item", date);
                     break;
             }
         }else {
             refLegalPersonItem = new RefLegalPersonItem();
             refPersonItem = new RefPersonItem();
         }
     }

    public void refreshRespWarrant() {
        if(refRespondentItem.getRecId() != null && refRespondentItem.getRecId() != 0) {
            respondentWarrantList = sessionBean.getReference().getRespondentWarrantList(refRespondentItem.getRecId(), sessionBean.getIntegration().getNewDateFromBackEndServer());
            selectedRespWarrant = null;
        }
    }

    public void saveRespWarrant(){
        String err_msg = "";
        try {
            if (selectedRespWarrant.getRecId() == null || selectedRespWarrant.getRecId() == 0)
                err_msg = "Выберите респондента!";
            else if (selectedRespWarrant.getNum() == null || selectedRespWarrant.getNum().trim().isEmpty())
                err_msg = "Введите номер!";
            else if (selectedRespWarrant.getbDate() == null)
                err_msg = "Введите дату начала действия!";

            if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

            if(selectedRespWarrant.getId() == null || selectedRespWarrant.getId() == 0){
                selectedRespWarrant.setRecIdParent(selectedRefRespondent.getRecId());
                sessionBean.getReference().insertRespWarrant(selectedRespWarrant);
            }else{
                sessionBean.getReference().updateRespWarrant(selectedRespWarrant);
            }

            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('wDlgRespondentWarrantModal').hide()");

            refreshRespWarrant();
        }catch (OracleException ex){
            FacesMessage message = new FacesMessage();
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            message.setSummary(ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, message);
            return;
        }
    }

    public void prepareGalleriaWarrant(Long warrantId){
        List<Image> imageListIn = new ArrayList<Image>();
        Image curImageIn = null;
        try {
            int index = 0;

            List<AttachedFile> warrantFileList = sessionBean.getPersistence().getFileListWithDataByLinkId(warrantId,5);

            for(AttachedFile fileItem : warrantFileList){
                byte[] pdfFile = null;
                if(fileItem.getFileName().substring(fileItem.getFileName().lastIndexOf(".") + 1).equalsIgnoreCase("pdf")){
                    pdfFile = fileItem.getFile();
                }else{
                    pdfFile = fileItem.getPdfFile();
                }

                ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
                PortletContext portletContext = (PortletContext) externalContext.getContext();
                String dir = portletContext.getRealPath("/resources/reports/");
                File file = File.createTempFile("1" + String.valueOf(fileItem.getId()) + "_",
                        ".pdf", new File(dir));

                if (!file.exists()) {
                    boolean created = file.createNewFile();
                    if (!created)
                        throw new Exception("Ошибка при создании pdf-файла");
                }

                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(pdfFile);
                outputStream.flush();
                outputStream.close();

                Image image = new Image();
                image.setPath("/frsi-portlet/resources/reports/" + file.getName());
                image.setTitle(fileItem.getFileName());
                image.setIndex(index);

                imageListIn.add(image);
                index++;
            }
            if(imageListIn.size() > 0) {
                curImageIn = imageListIn.get(0);
            }
        } catch (Exception e) {
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
        }

        imageList = imageListIn;
        curImage = curImageIn;
    }

    public void onUploadWarrantFile(FileUploadEvent event) {
        Long warrantId = (Long) event.getComponent().getAttributes().get("warrantId");

        UploadedFile uploadedFile = event.getFile();
        byte[] contents = uploadedFile.getContents();
        if (contents == null) return;

        String fileNameWithFormat = uploadedFile.getFileName();
        String fileName = fileNameWithFormat.substring(0, fileNameWithFormat.lastIndexOf("."));
        String contentType = uploadedFile.getContentType();
        String format = fileNameWithFormat.substring(fileNameWithFormat.lastIndexOf(".") + 1);

        List<AttachedFile> warrantFileList = sessionBean.getPersistence().getFileListByLinkId(warrantId,5, null);

        for (AttachedFile file : warrantFileList){
            if(file.getFileName().toUpperCase().equals(fileNameWithFormat.toUpperCase())){
                RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
                RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Ошибка", "Файл \"" + fileNameWithFormat + "\" уже существует!"));
                return;
            }
        }

        if(fileName.length() > 100){
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", "Слишком длинное наименование файла! Наименование не должно превышать 100 символов."));
            return;
        }

        Date curDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
        AttachedFile attachedFile = new AttachedFile();
        attachedFile.setLinkId(warrantId);
        attachedFile.setFileDate(curDate);
        attachedFile.setFileType(contentType);
        attachedFile.setFileName(fileNameWithFormat);
        attachedFile.setFile(contents);
        attachedFile.setIdUsr(userId);
        attachedFile.setFileKind(5);
        attachedFile.updateHash();
        if(!format.equalsIgnoreCase("pdf")){
            FileWrapper pdfFile = new FileWrapper();
            pdfFile.setBytes(contents);
            pdfFile.setFileFormat(format);
            try {
                pdfFile = sessionBean.getPersistence().convertFileToPdf(pdfFile, fileName);
            } catch (Exception e) {
                RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
                RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
                return;
            }
            attachedFile.setPdfFile(pdfFile.getBytes());
        }

        sessionBean.getPersistence().uploadFile(attachedFile, null, null);

        refreshRespWarrant();
    }

    public void deleteAttachedFile(Long warrantId){
        sessionBean.getPersistence().deleteAllFilesByLinkId(warrantId, 5, null);
        refreshRespWarrant();
    }

    public void deleteWarrant(Long warrantId){
        sessionBean.getReference().deleteWarrant(warrantId);
        refreshRespWarrant();
    }

    public void reviewWarrant(RespondentWarrant item){
        item.seteDate(sessionBean.getIntegration().getNewDateFromBackEndServer());
        sessionBean.getReference().updateRespWarrant(item);
        refreshRespWarrant();
    }

    public void getCurObject(String kind, Image curImageIn, List<Image> imageListIn){
        int index = curImageIn.getIndex();
        if(kind.equals("NEXT")) {
            index ++;
            if(index > imageListIn.size() - 1)
                index = 0;
        }else if(kind.equals("PREV")){
            index--;
            if(index < 0)
                index = imageListIn.size() - 1;
        }else if(kind.equals("FIRST")){
            index = 0;
        }else if (kind.equals("LAST")){
            index = imageListIn.size() - 1;
        }

        curImage = imageListIn.get(index);
    }

    public void onDownloadWarrantFile(Long warrantId) {
        List<AttachedFile> warrantFileList = sessionBean.getPersistence().getFileListWithDataByLinkId(warrantId, 5);
        byte[] zipContent;
        String fileName;
        fileName = "warrants" + Convert.dateTimeFormatCompact_.format(sessionBean.getIntegration().getNewDateFromBackEndServer()) + ".zip";
        zipContent = applicationBean.createExcelFilesZipContentByFileList(warrantFileList);
        applicationBean.putFileContentToResponseOutputStream(zipContent, "application/zip", fileName);
    }

    public boolean isVisibleBtn(){
        boolean result = false;
        if (referenceCode.equals(RefPostItem.REF_CODE) && selectedRefPost != null) {
            result = true;
        }else if (referenceCode.equals(RefPersonItem.REF_CODE) && selectedRefPerson != null){
            result = true;
        }else if (referenceCode.equals(RefLegalPersonItem.REF_CODE) && selectedRefLegalPerson != null){
            result = true;
        }else if (referenceCode.equals(RefCountryItem.REF_CODE) && selectedRefCountry != null){
            result = true;
        }else if (referenceCode.equals(RefManagersItem.REF_CODE) && selectedRefManagers != null){
            result = true;
        }else if (referenceCode.equals(RefTypeBusEntityItem.REF_CODE) && selectedRefTypeBusEntity != null){
            result = true;
        }else if (referenceCode.equals(RefRegionItem.REF_CODE) && selectedRefRegion != null){
            result = true;
        }else if (referenceCode.equals(RefRequirementItem.REF_CODE) && selectedRefRequirement != null) {
            result = true;
        }else if (referenceCode.equals(RefTypeProvideItem.REF_CODE) && selectedRefTypeProvide != null) {
            result = true;
        }else if (referenceCode.equals(RefTransTypeItem.REF_CODE)&& selectedRefTransType != null) {
            result = true;
        }else if (referenceCode.equals(RefBalanceAccItem.REF_CODE) && selectedBalanceAcc != null) {
            result = true;
        }else if (referenceCode.equals(RefConnOrgItem.REF_CODE) && selectedRefConnOrg != null) {
            result = true;
        }else if (referenceCode.equals(RefDepartmentItem.REF_CODE) && selectedRefDepartment != null) {
            result = true;
        }else if (referenceCode.equals(RefBankItem.REF_CODE) && selectedRefBank != null) {
            result = true;
        }else if (referenceCode.equals(RefRateAgencyItem.REF_CODE) && selectedRefRateAgency != null) {
            result = true;
        }else if (referenceCode.equals(RefCurrencyItem.REF_CODE) && selectedRefCurrency != null) {
            result = true;
        }else if (referenceCode.equals(RefCurrencyRateItem.REF_CODE) && selectedRefCurrencyRate != null) {
            result = true;
        }else if (referenceCode.equals(RefSubjectTypeItem.REF_CODE)&& selectedRefSubjectType != null) {
            result = true;
        }else if (referenceCode.equals(RefRespondentItem.REF_CODE) && selectedRefRespondent != null) {
            result = true;
        }else if (referenceCode.equals(RefDocTypeItem.REF_CODE) && selectedRefDocType != null) {
            result = true;
        }else if (referenceCode.equals(RefDocumentItem.REF_CODE) && selectedRefDocument != null) {
            result = true;
        }else if (referenceCode.equals(RefIssuersItem.REF_CODE) && selectedRefIssuers != null) {
            result = true;
        }else if (referenceCode.equals(RefSecuritiesItem.REF_CODE) && selectedRefSecurities != null) {
            result = true;
        }else if (referenceCode.equals(RefVidOperItem.REF_CODE) && selectedRefVidOper != null) {
            result = true;
        }else if (referenceCode.equals(RefBranchItem.REF_CODE) && selectedRefBranch != null) {
            result = true;
        }else if (referenceCode.equals("ref_branch_old") && selectedRefBranchOld != null) {
            result = true;
        }else if (referenceCode.equals(RefListingEstimationItem.REF_CODE) && selectedRefListingEstimation != null) {
            result = true;
        }else if (referenceCode.equals(RefRatingEstimationItem.REF_CODE) && selectedRefRatingEstimation != null) {
            result = true;
        }else if (referenceCode.equals(RefRatingCategoryItem.REF_CODE) && selectedRefRatingCategory != null) {
            result = true;
        }/*else if (referenceCode.equals("ref_request_type") && selectedRefRequestType != null) {
            result = true;
        }else if (referenceCode.equals("ref_request_way") && selectedRefRequestWay != null) {
            result = true;
        }else if (referenceCode.equals("ref_market_kind") && selectedRefMarketKind != null) {
            result = true;
        }else if (referenceCode.equals("ref_category") && selectedRefCategory != null) {
            result = true;
        }else if (referenceCode.equals("ref_subcategory") && selectedRefSubCategory != null) {
            result = true;
        }else if (referenceCode.equals("ref_account_type") && selectedRefAccountType != null) {
            result = true;
        }else if (referenceCode.equals("ref_subaccount_type") && selectedRefSubAccountType != null) {
            result = true;
        }else if (referenceCode.equals("ref_type_holder_acc") && selectedRefTypeHolderAcc != null) {
            result = true;
        }else if (referenceCode.equals("ref_request_feature") && selectedRefRequestFeature != null) {
            result = true;
        }else if (referenceCode.equals("ref_request_sts") && selectedRefRequestSts != null) {
            result = true;
        }else if (referenceCode.equals("ref_repo_kind") && selectedRefRepoKind != null) {
            result = true;
        }else if (referenceCode.equals("ref_market_type") && selectedRefMarketType != null) {
            result = true;
        }else if (referenceCode.equals("ref_trad_method") && selectedRefTradMethod != null) {
            result = true;
        }else if (referenceCode.equals("ref_oper_type") && selectedRefOperType != null) {
            result = true;
        }else if (referenceCode.equals("ref_deal_sts") && selectedRefDealSts != null) {
            result = true;
        }*/else if (referenceCode.equals(RefMrpItem.REF_CODE) && selectedRefMrp != null) {
            result = true;
        }else if (referenceCode.equals("ref_org_type") && selectedRefOrgType != null) {
            result = true;
        }else if (referenceCode.equals(RefMfoRegItem.REF_CODE) && selectedRefMfoReg != null) {
            result = true;
        }else if (referenceCode.equals(RefDealBAItem.REF_CODE) && selectedRefDealBA != null) {
            result = true;
        }else if (referenceCode.equals("ref_type_activity") && selectedRefTypeActivity != null) {
            result = true;
        }else if (referenceCode.equals("ref_npa") && selectedRefNpa != null) {
            result = true;
        }else if (referenceCode.equals("ref_wkd_holidays") && selectedRefWkdHoliday != null) {
            result = true;
        }else if (referenceCode.equals("ref_finrep_indic") && selectedRefFinRepIndic != null) {
            result = true;
        }else if (referenceCode.equals("ref_type_deals") && selectedRefTypeDeals != null) {
            result = true;
        }else if (referenceCode.equals("ref_degree_relation") && selectedRefDegreeRelation != null) {
            result = true;
        }else if (referenceCode.equals("ref_sign_related") && selectedRefSignRelated != null) {
            result = true;
        }else if (referenceCode.equals("ref_type_risk") && selectedRefTypeRisk != null) {
            result = true;
        }else if (referenceCode.equals(RefBasisofControlItem.REF_CODE) && selectedRefBasisofControl != null) {
            result = true;
        } else if (referenceCode.equals("ref_insur_org") && selectedRefInsurOrg != null) {
            result = true;
        }else if (referenceCode.equals(RefExtIndicatorItem.REF_CODE) && selectedRefExtInd != null) {
            result = true;
        }else if (referenceCode.equals(RefMajorMemberItem.REF_CODE) && selectedRefMajorMember != null) {
            result = true;
        }else if (referenceCode.equals(RefPeriodAlgItem.REF_CODE) && selectedRefPeriodAlg != null) {
            result = true;
        }else if (referenceCode.equals(RefPeriodItem.REF_CODE) && selectedRefPeriod != null) {
            result = true;
        } else if (referenceCode.equals(RefInsurGroupsItem.REF_CODE) && selectedInsurGroups != null) {
            result = true;
        } else if (referenceCode.equals(RefBankConglomeratesItem.REF_CODE) && selectedBankCongl != null) {
            result = true;
        }else if (referenceCode.equals(RefShareHoldersItem.REF_CODE) && selectedShareHold != null) {
            result = true;
        }

        return result;
    }

    public boolean isDisableFillBtn(){
       boolean result = false;
       if (referenceCode.equals("ref_wkd_holidays")) {
            result = true;
       }
       return result;
    }

    /* Проверка на активноить кнопок для загружаемых справочников */
    public boolean isLoadRef(String ref_code, String type){
        boolean result = false;
        if (ref_code != null && !ref_code.trim().isEmpty()) {
            if ((ref_code.equals(RefCountryItem.REF_CODE)) ||
                    (ref_code.equals(RefRateAgencyItem.REF_CODE)) ||
                    (ref_code.equals(RefSecuritiesItem.REF_CODE)) ||
                    (ref_code.equals(RefIssuersItem.REF_CODE)) ||
                    (ref_code.equals(RefCurrencyItem.REF_CODE)) ||
                    (ref_code.equals(RefCurrencyRateItem.REF_CODE)) ||
                    //(ref_code.equals(RefBankItem.REF_CODE)) ||
                    (ref_code.equals(RefLegalPersonItem.REF_CODE)) ||
                    (ref_code.equals(RefDepartmentItem.REF_CODE)) ||
                    (ref_code.equals(RefRegionItem.REF_CODE)) ||
                    (ref_code.equals(RefListingEstimationItem.REF_CODE)) ||
                    (ref_code.equals(RefRatingEstimationItem.REF_CODE)) ||
                    (ref_code.equals(RefRatingCategoryItem.REF_CODE)) ||
                    (ref_code.equals(RefBranchItem.REF_CODE))) {
                result = true;
            } else
                result = false;

            if (type != null && type.equals("insert_ref"))
                isLoad = result;
        }
        return result;
    }

    /* Загрузка в ФРСП из внешних систем (НСИ, ПУРЦБ) */
    public void loadReference() {
        Date loadDate = sessionBean.getIntegration().getNewDateFromBackEndServer();
        String resultLoad;
        if(resultList == null) {
            resultList = new ArrayList<Result>();
        }
        if (resultList.size() > 0)
            resultList.clear();
        try {
            for (RefItem refContainer : selectedRef) {
                if (!userBean.hasPermission("SU:REF:" + refContainer.getCode().toUpperCase() + ":EDIT")) {
                    resultList.add(new Result(false, refContainer.getName(), "Нет доступа."));
                    continue;
                }

                if (!isLoadRef(refContainer.getCode(), null)) {
                    resultList.add(new Result(false, refContainer.getName(), "Является не загружаемым."));
                    continue;
                }

                resultLoad = sessionBean.getSchedule().loadReferences(refContainer.getCode(), userId, userLocation, loadDate, sessionBean.respondent.getId());

                if (!resultLoad.equals("SUCCESS")) {
                    resultList.add(new Result(false, refContainer.getName(), resultLoad));
                    continue;
                }

                resultList.add(new Result(true, refContainer.getName(), ""));
            }
            refreshRefList();
        }catch (Exception e) {
            RequestContext.getCurrentInstance().addCallbackParam("hasErrors", true);
            RequestContext.getCurrentInstance().showMessageInDialog(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ошибка", e.getMessage()));
            return;
        }

        Collections.sort(resultList, new Comparator<Result>() {
            @Override
            public int compare(Result o1, Result o2) {
                if (o1.isSuccess() != o2.isSuccess()) {
                    if (o1.isSuccess()) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
                return 0;
            }
        });
    }

    public String getRowStyleClass(boolean success) {
        return success ? "customRowGreen" : "customRowRed";
    }

    public int getSuccessOperationCount(List<Result> items) {
        if (items == null) {
            return 0;
        }
        int result = 0;
        for (Result item : items) {
            if (item.isSuccess()) {
                result++;
            }
        }
        return result;
    }

    public int getFailOperationCount(List<Result> items) {
        if (items == null) {
            return 0;
        }
        int result = 0;
        for (Result item : items) {
            if (!item.isSuccess()) {
                result++;
            }
        }
        return result;
    }

    public void onNpCheckBoxChange() {
       if (referenceCode.equals(RefLegalPersonItem.REF_CODE)) {
           disableIdn = refLegalPersonItem.getIsNonRezident();
           if (disableIdn) refLegalPersonItem.setIdn(null);
       }
       if (referenceCode.equals(RefInsurOrgItem.REF_CODE)) {
           disableIdn = refInsurOrgItem.getIsNonRezident();
           if (disableIdn) refInsurOrgItem.setIdn(null);
       }
       if (referenceCode.equals(RefMajorMemberItem.REF_CODE)) {
           disableIdn = refMajorMemberItem.getIsNonRezident();
           if (disableIdn) refMajorMemberItem.setIdn(null);
       }
       if (referenceCode.equals(RefBankConglomeratesItem.REF_CODE)) {
           disableIdn = refBankConglItem.getIsNonRezident();
          if (!disableIdn) refBankConglItem.setRefCountry(Long.parseLong("1347"));
       }
       if (referenceCode.equals(RefInsurGroupsItem.REF_CODE)) {
           disableIdn = refInsurGroupsItem.getIsNonRezident();
          if (!disableIdn) refInsurGroupsItem.setRefCountry(Long.parseLong("1347"));

       }
    }

    public void onInvFundCheckBoxChange() {
        disableInvIdn = !refLegalPersonItem.getIsInvFund();
        if(disableInvIdn) refLegalPersonItem.setInvIdn(null);
    }

    public void onSelectAll(ToggleSelectEvent event) {
        selectedRef.clear();
        if (event.isSelected()) {
            selectedRef.addAll(RefList);
        }
    }

    public void lpSelectionChanged() {
           if (refLegalPersonItem.getRefOrgType()==6) {
               isVisibleBankLp = true;
               isVisibleInsurLp = false;
           }
           else if (refLegalPersonItem.getRefOrgType()==16) {
               isVisibleBankLp = false;
               isVisibleInsurLp = true;
           }
           else {
               isVisibleBankLp = false;
               isVisibleInsurLp = false;
           }
       }


    public void onRowMajorOrgSelect(SelectEvent event){
        RequestContext context = RequestContext.getCurrentInstance();
        String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();

        disableBtnMajorMemView = false;
       if (!editType.equals("view_item"))
           disableBtnMajorMem = false;

       if (RefMajorMemDetailsList!=null && RefMajorMemDetailsList.size() != 0) RefMajorMemDetailsList.clear();

       filterRefMajorMemDetailsItem.setSearchAllVer(searchAllVer);
       filterRefMajorMemDetailsItem.setRef_major_memorgs(((RefMajorMemberOrgItem) event.getObject()).getId());
       RefMajorMemDetailsList = (List<RefMajorMemDetailsItem>) sessionBean.getReference().getRefAbstractByFilterList("ref_major_memdetails", filterRefMajorMemDetailsItem, Convert.getDateFromString("01.01.3333"));

      context.update(liferayNameSpace + ":frmRefMajorMemberModal:ref_mm_view:ref_org_det_view:t_ref_majorMember_details");
    }

    public boolean isDisableOpenBtn(){
        if(selectedRef.size() != 1)
            return true;
        else
            return false;
    }

    public boolean isDisableUnLoadBtn(){
        if(selectedRef.size() == 0)
            return true;
        else
            return false;
    }

    public boolean isDisableLoadBtn() {
        if (selectedRef.size() == 0) {
            return true;
        }
        if (selectedRef.size() == 1) {
            RefItem item = selectedRef.get(0);
            if (!isLoadRef(item.getCode(), null) || !userBean.hasPermission(getRefRightCode(item.getCode(), item.getCode().equals(RefLegalPersonItem.REF_CODE) ? "LOAD" : "EDIT"))) {
                return true;
            }
        }
        return false;
    }

    public boolean disableBtnEditRef(){
        if(!isVisibleBtn()){
            return true;
        }else if (!canEdit){
            return true;
        }else if (referenceCode.equals(RefBankItem.REF_CODE)){
            if(selectedRefBank.getIsLoad() == true) {
                return true;
            }
        }
        return false;
    }

    public boolean disableBtnInsRef(){
        boolean result = false;
        if (!canEdit){
            result = true;
        }

        return result;
    }

    public void setSearchValues(){
        RequestContext context = RequestContext.getCurrentInstance();
        String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();
        if(referenceCode.equals(RefRespondentItem.REF_CODE)) {
            refRespondentItem.setPerson(selectedSearchRefLegalPerson.getId());
            refRespondentItem.setPersonName(selectedSearchRefLegalPerson.getNameRu());
            context.update(liferayNameSpace + ":frmRefRespondentModal");
        }else if (referenceCode.equals(RefMfoRegItem.REF_CODE)){
            refMfoRegItem.setRefLpId(selectedSearchRefLegalPerson.getId());
            refMfoRegItem.setLpName(selectedSearchRefLegalPerson.getNameRu());
            context.update(liferayNameSpace + ":frmRefMfoRegModal");
        }else if (referenceCode.equals(RefMajorMemberItem.REF_CODE)){
            refMajorMemberOrgItem.setRefLegalPerson(selectedSearchRefLegalPerson.getId());
            refMajorMemberOrgItem.setNameRu(selectedSearchRefLegalPerson.getNameRu());
            context.update(liferayNameSpace + ":frmRefMajorMemOrgsModal");
        } else if (referenceCode.equals(RefInsurGroupsItem.REF_CODE)){
            refInsurGroupsItem.setRefLegalPerson(selectedSearchRefLegalPerson.getId());
            refInsurGroupsItem.setIdn(selectedSearchRefLegalPerson.getIdn());
            refInsurGroupsItem.setNameRu(selectedSearchRefLegalPerson.getNameRu());
            refInsurGroupsItem.setNameKz(selectedSearchRefLegalPerson.getNameKz());
            refInsurGroupsItem.setNameEn(selectedSearchRefLegalPerson.getNameEn());
            refInsurGroupsItem.setRefCountry(selectedSearchRefLegalPerson.getRefCountry());
            refInsurGroupsItem.setRefOrgType(selectedSearchRefLegalPerson.getRefOrgType());
            refInsurGroupsItem.setLegalAddress(selectedSearchRefLegalPerson.getLegalAddress());
            refInsurGroupsItem.setFactAddress(selectedSearchRefLegalPerson.getFactAddress());
            refInsurGroupsItem.setIsNonRezident(selectedSearchRefLegalPerson.getIsNonRezident());
            refInsurGroupsItem.setRef_branch_insur(selectedSearchRefLegalPerson.getRef_branch_insur());
            context.update(liferayNameSpace + ":frmRefInsurGroupsModal");
        } else if (referenceCode.equals(RefBankConglomeratesItem.REF_CODE)){
            refBankConglItem.setRefLegalPerson(selectedSearchRefLegalPerson.getId());
            refBankConglItem.setRefLPName(selectedSearchRefLegalPerson.getNameRu());
            context.update(liferayNameSpace + ":frmRefBankConglModal");
        } else if (referenceCode.equals(RefShareHoldersItem.REF_CODE)){
            refShareHoldItem.setRefIssuers(selectedSearchRefLegalPerson.getId());
            refShareHoldItem.setNameRu(selectedSearchRefLegalPerson.getNameRu());
            context.update(liferayNameSpace + ":frmRefShareHoldModal");
        }
    }

    public void setSearchValuesU(){
        RequestContext context = RequestContext.getCurrentInstance();
        String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();
        if(referenceCode.equals(RefRespondentItem.REF_CODE)) {
            refRespondentItem.setUnionPersonsId(selectedSearchRefUnionPerson.getId());
            refRespondentItem.setPersonName(selectedSearchRefUnionPerson.getNameRu());
            context.update(liferayNameSpace + ":frmRefRespondentModal");
        } else if (referenceCode.equals(RefMajorMemberItem.REF_CODE)){
            refMajorMemberItem.setRefUnionPersons(selectedSearchRefUnionPerson.getId());
            refMajorMemberItem.setNameRu(selectedSearchRefUnionPerson.getNameRu());
            refMajorMemberItem.setNameKz(selectedSearchRefUnionPerson.getNameKz());
            refMajorMemberItem.setNameEn(selectedSearchRefUnionPerson.getNameEn());
            refMajorMemberItem.setIdn(selectedSearchRefUnionPerson.getIdn());
            refMajorMemberItem.setIsNonRezident(selectedSearchRefUnionPerson.getIsNonRezident());
            refMajorMemberItem.setRefCountry(selectedSearchRefUnionPerson.getRefCountry());
            refMajorMemberItem.setRefCountryRecId(selectedSearchRefUnionPerson.getRefCountryRecId());
            refMajorMemberItem.setCountryName(selectedSearchRefUnionPerson.getCountryName());
            refMajorMemberItem.setLegalAddress(selectedSearchRefUnionPerson.getLegalAddress());
            context.update(liferayNameSpace + ":frmRefMajorMemberModal");
        } else if (referenceCode.equals(RefBankConglomeratesItem.REF_CODE)){
            refBankConglItem.setRefUnionPersons(selectedSearchRefUnionPerson.getId());
            refBankConglItem.setNameRu(selectedSearchRefUnionPerson.getNameRu());
            refBankConglItem.setNameKz(selectedSearchRefUnionPerson.getNameKz());
            refBankConglItem.setNameEn(selectedSearchRefUnionPerson.getNameEn());
            refBankConglItem.setIdn(selectedSearchRefUnionPerson.getIdn());
            refBankConglItem.setIsNonRezident(selectedSearchRefUnionPerson.getIsNonRezident());
            refBankConglItem.setRefCountry(selectedSearchRefUnionPerson.getRefCountry());
            refBankConglItem.setRefCountryRecId(selectedSearchRefUnionPerson.getRefCountryRecId());
            refBankConglItem.setCountryName(selectedSearchRefUnionPerson.getCountryName());
            refBankConglItem.setLegalAddress(selectedSearchRefUnionPerson.getLegalAddress());
            context.update(liferayNameSpace + ":frmRefBankConglModal");
            context.update(liferayNameSpace + ":frmRefFilter");

        } else if (referenceCode.equals(RefShareHoldersItem.REF_CODE)){
            refShareHoldItem.setRefUnionPersons(selectedSearchRefUnionPerson.getId());
            refShareHoldItem.setRefUPName(selectedSearchRefUnionPerson.getNameRu());
            refShareHoldItem.setIdn(selectedSearchRefUnionPerson.getIdn());
            context.update(liferayNameSpace + ":frmRefShareHoldModal");
        }
    }

    public void searchLegalPersonList(){
        RefLegalPersonItem item = new RefLegalPersonItem();
        item.setIdn(idnForSearch);
        item.setNameRu(nameForSearch);
        item.setCurrentRec(true);
        RefLPForSearchList = (List<RefLegalPersonItem>)sessionBean.getReference().getRefAbstractByFilterList(RefLegalPersonItem.REF_CODE, item, Convert.getDateFromString("01.01.3333"));
    }

    public void searchUnionPersonList(){
         refUnionForSearchList = sessionBean.getReference().getUnionPersonItemList(Convert.getDateFromString("01.01.3333"), idnForSearchU, nameForSearchU);
    }

    public void openHolidayChange(){
        String err_msg = "";
        RequestContext context = RequestContext.getCurrentInstance();
        String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();

       try { if (selectedRefHoliday == null)
             err_msg = "Выберите дату!";

            if (!err_msg.trim().isEmpty()) throw new OracleException(err_msg);

            HolidayItem filter = new HolidayItem();
            filter.setId(false ? selectedRefHolidayVer.getId() : selectedRefHoliday.getId());
            holidayItem = (HolidayItem) sessionBean.getReference().getRefHolidayItem(filter.getId());
            userName = holidayItem.getUserName();
            userLocation = holidayItem.getUserLocation();
            datlast = holidayItem.getDatlast();
            context.update(liferayNameSpace + ":frmHolidaySelected");
            context.execute("PF('holDialog').show()");
       } catch (OracleException ex) {
           FacesMessage message = new FacesMessage();
           message.setSeverity(FacesMessage.SEVERITY_ERROR);
           message.setSummary(ex.getMessage());
           FacesContext.getCurrentInstance().addMessage(null, message);
           return;
       }
    }

    public void searchRespondent(){
        Date maxDate =  Convert.getDateFromString("01.01.3333");
        RefRespondentItem item = new RefRespondentItem();
        item.setIdn(idnForSearch);
        item.setNameRu(nameForSearch);
        respondentResultList = (List<RefRespondentItem>)sessionBean.getReference().getRefAbstractByFilterList(RefRespondentItem.REF_CODE, item, maxDate);
    }

    public void getLPDataForView(Long idLP){
        getLPData(idLP, "view_item", sessionBean.getIntegration().getNewDateFromBackEndServer());
    }

    private void getPersonData(Long personId, String type, Date date){
        RequestContext context = RequestContext.getCurrentInstance();
        String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();

        RefCountryList = (List<RefCountryItem>) sessionBean.getReference().getRefAbstractList(RefCountryItem.REF_CODE, date);

        if (type.equals("insert_item")){
            refPersonItem = new RefPersonItem();
            refPersonItem.setBeginDate(date);
        }else if ((type.equals("update_item")) || (type.equals("refresh_item")) || (type.equals("view_item"))) {
            RefPersonItem filter = new RefPersonItem();
            filter.setId(personId);
            refPersonItem = (RefPersonItem) sessionBean.getReference().getRefAbstractItem(RefPersonItem.REF_CODE, filter);
            userName = refPersonItem.getUserName();
            userLocation = refPersonItem.getUserLocation();
            datlast = refPersonItem.getDatlast();
        }
        context.update(liferayNameSpace + ":frmRefPersonModal");
        context.execute("PF('wDlgRefPersonModal').show()");
    }

    private void getLPData(Long idLP, String type, Date date){
        RequestContext context = RequestContext.getCurrentInstance();
        String liferayNameSpace = applicationBean.getLiferayFacesResponseNamespace();

        RefTypeBusEntityList = (List<RefTypeBusEntityItem>) sessionBean.getReference().getRefAbstractList(RefTypeBusEntityItem.REF_CODE,date);
        RefCountryList = (List<RefCountryItem>) sessionBean.getReference().getRefAbstractList(RefCountryItem.REF_CODE, date);
        RefRegionList = (List<RefRegionItem>) sessionBean.getReference().getRefAbstractList(RefRegionItem.REF_CODE, date);
        refreshOrgTypes(date);
        SimpleReference refOwnershipReference = new SimpleReference();
        refOwnershipReference.setRefCode("ref_ownership");
        SimpleReference refBranchInsurReference = new SimpleReference();
        refBranchInsurReference.setRefCode("ref_branch_insur");
        RefBranchInsurList = sessionBean.getReference().getRefSimpleListByParams(date,refBranchInsurReference);
        RefOwnershipList = sessionBean.getReference().getRefSimpleListByParams(date,refOwnershipReference);

        if (type.equals("insert_item")){
             refLegalPersonItem = new RefLegalPersonItem();
             refLegalPersonItem.setBeginDate(date);
             disableIdn = false;
             disableInvIdn = false;
        }else if ((type.equals("update_item")) || (type.equals("refresh_item")) || (type.equals("view_item"))) {
             RefLegalPersonItem filter = new RefLegalPersonItem();
             filter.setId(idLP);
             refLegalPersonItem = (RefLegalPersonItem) sessionBean.getReference().getRefAbstractItem(RefLegalPersonItem.REF_CODE, filter);
             if (refLegalPersonItem.getRefOrgType() == 6 && refLegalPersonItem.getIsLoad() == true)
                 loadSysName = "из АИП Ведение НСИ";
             else if (refLegalPersonItem.getRefOrgType() == 16 && refLegalPersonItem.getIsLoad() == true)
                 loadSysName = "из АИП Страховой надзор";
             else loadSysName = "не загружаемая запись";
             userName = refLegalPersonItem.getUserName();
             userLocation = refLegalPersonItem.getUserLocation();
             datlast = refLegalPersonItem.getDatlast();
             onNpCheckBoxChange();
             onInvFundCheckBoxChange();
        }
        context.update(liferayNameSpace + ":frmRefLegalPersonModal");
        context.execute("PF('wDlgRefLegalPersonModal').show()");
    }

    public void setSearchRespValues(){
        selectedRespWarrant.setRecId(selectedResultResp.getRecId());
        selectedRespWarrant.setChildName(selectedResultResp.getPersonName());
    }

    public void prepareOpenRespWarrantModal(){
        selectedRespWarrant = new RespondentWarrant();
        selectedRespWarrant.setbDate(sessionBean.getIntegration().getNewDateFromBackEndServer());
    }

    public void searchArguments(){
        if(refPeriodItem.getRefPeriodAlgId() == null || refPeriodItem.getRefPeriodAlgId() == 0) {
            argumentList = new ArrayList<RefPeriodArgument>();
        }else{
            RefPeriodAlgItem algItem = new RefPeriodAlgItem();
            algItem.setId(refPeriodItem.getRefPeriodAlgId());
            algItem = (RefPeriodAlgItem) sessionBean.getReference().getRefAbstractItem(RefPeriodAlgItem.REF_CODE, algItem);
            argumentList = sessionBean.getReference().searchArgumentsFromAlg(refPeriodItem.getId(), argumentList, algItem.getAlg());
        }
    }

    private void refreshSTs(Date date){
        RefSubjectTypeItem filterItem = new RefSubjectTypeItem();
        filterItem.setIsAdvance(true);
        RefSubjectTypeList = (List<RefSubjectTypeItem>)sessionBean.getReference().getRefAbstractByFilterList(RefSubjectTypeItem.REF_CODE, filterItem, date);
    }

    private void refreshOrgTypes(Date date){
        SimpleReference simpleReference = new SimpleReference();
        simpleReference.setRefCode("ref_org_type");
        RefOrgTypeList = sessionBean.getReference().getRefSimpleListByParams(date,simpleReference);
    }

    private void refreshDeps(Date date){
        RefDepartmentItem refDepartmentItem = new RefDepartmentItem();
        refDepartmentItem.setRefDeptTypeId(2L);
        RefDepartmentList = (List<RefDepartmentItem>) sessionBean.getReference().getRefAbstractByFilterList(RefDepartmentItem.REF_CODE, refDepartmentItem, date);
    }

    public void prepareArgumentEditShow(){
        valueType = selectedArgument.getValueType();
        strValue = selectedArgument.getStrValue();
    }

    public void prepareArgumentEditHide(){
        selectedArgument.setValueType(valueType);
        selectedArgument.setStrValue(strValue);
    }

    // region Getters and Setters

    public void setApplicationBean(ApplicationBean applicationBean) {
        this.applicationBean = applicationBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }

    public String getRefCode() {
        return refCode;
    }

    public void setRefCode(String refCode) {
        this.refCode = refCode;
    }

    public String getResultMessage() {
        return resultMessage;
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

    public List<RefPostItem> getRefPostList() {
        return RefPostList;
    }

    public void setRefPostList(List<RefPostItem> refPostList) {
        RefPostList = refPostList;
    }

    public RefPostItem getSelectedRefPost() {
        return selectedRefPost;
    }

    public void setSelectedRefPost(RefPostItem selectedRefPost) {
        this.selectedRefPost = selectedRefPost;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public List<RefPersonItem> getRefPersonList() {
        return RefPersonList;
    }

    public void setRefPersonList(List<RefPersonItem> refPersonList) {
        RefPersonList = refPersonList;
    }

    public RefPersonItem getSelectedRefPerson() {
        return selectedRefPerson;
    }

    public void setSelectedRefPerson(RefPersonItem selectedRefPerson) {
        this.selectedRefPerson = selectedRefPerson;
    }

    public boolean isValidationVisible() {
        return validationVisible;
    }

    public void setValidationVisible(boolean validationVisible) {
        this.validationVisible = validationVisible;
    }

    public boolean isRefPostVisible() {
        return RefPostVisible;
    }

    public void setRefPostVisible(boolean refPostVisible) {
        RefPostVisible = refPostVisible;
    }

    public boolean isRefPersonVisible() {
        return RefPersonVisible;
    }

    public void setRefPersonVisible(boolean refPersonVisible) {
        RefPersonVisible = refPersonVisible;
    }

    public List<RefPostItem> getRefPostHstList() {
        return RefPostHstList;
    }

    public void setRefPostHstList(List<RefPostItem> refPostHstList) {
        RefPostHstList = refPostHstList;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public void setReferenceCode(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public String getActiveTabIndex() {
        return activeTabIndex;
    }

    public void setActiveTabIndex(String activeTabIndex) {
        this.activeTabIndex = activeTabIndex;
    }

    public TabView getTabView() {
        return tabView;
    }

    public void setTabView(TabView tabView) {
        this.tabView = tabView;
    }

    public List<RefPersonItem> getRefPersonHstList() {
        return RefPersonHstList;
    }

    public void setRefPersonHstList(List<RefPersonItem> refPersonHstList) {
        RefPersonHstList = refPersonHstList;
    }

    public RefPostItem getRefPostItem() {
        return refPostItem;
    }

    public void setRefPostItem(RefPostItem refPostItem) {
        this.refPostItem = refPostItem;
    }

    public RefPersonItem getRefPersonItem() {
        return refPersonItem;
    }

    public void setRefPersonItem(RefPersonItem refPersonItem) {
        this.refPersonItem = refPersonItem;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public Date getDatlast() {
        return datlast;
    }

    public void setDatlast(Date datlast) {
        this.datlast = datlast;
    }

    public String getEditType() {
        return editType;
    }

    public void setEditType(String editType) {
        this.editType = editType;
    }

    public List<RefCountryItem> getRefCountryList() {
        return RefCountryList;
    }

    public void setRefCountryList(List<RefCountryItem> refCountryList) {
        RefCountryList = refCountryList;
    }

    public RefCountryItem getSelectedRefCountry() {
        return selectedRefCountry;
    }

    public void setSelectedRefCountry(RefCountryItem selectedRefCountry) {
        this.selectedRefCountry = selectedRefCountry;
    }

    public boolean isRefCountryVisible() {
        return RefCountryVisible;
    }

    public void setRefCountryVisible(boolean refCountryVisible) {
        RefCountryVisible = refCountryVisible;
    }

    public RefCountryItem getRefCountryItem() {
        return refCountryItem;
    }

    public void setRefCountryItem(RefCountryItem refCountryItem) {
        this.refCountryItem = refCountryItem;
    }

    public List<RefManagersItem> getRefManagersList() {
        return RefManagersList;
    }

    public void setRefManagersList(List<RefManagersItem> refManagersList) {
        RefManagersList = refManagersList;
    }

    public List<RefManagersItem> getRefManagersHstList() {
        return RefManagersHstList;
    }

    public void setRefManagersHstList(List<RefManagersItem> refManagersHstList) {
        RefManagersHstList = refManagersHstList;
    }

    public RefManagersItem getSelectedRefManagers() {
        return selectedRefManagers;
    }

    public void setSelectedRefManagers(RefManagersItem selectedRefManagers) {
        this.selectedRefManagers = selectedRefManagers;
    }

    public boolean isRefManagersVisible() {
        return RefManagersVisible;
    }

    public void setRefManagersVisible(boolean refManagersVisible) {
        RefManagersVisible = refManagersVisible;
    }

    public RefManagersItem getRefManagersItem() {
        return refManagersItem;
    }

    public void setRefManagersItem(RefManagersItem refManagersItem) {
        this.refManagersItem = refManagersItem;
    }

    public List<RefTypeBusEntityItem> getRefTypeBusEntityList() {
        return RefTypeBusEntityList;
    }

    public void setRefTypeBusEntityList(List<RefTypeBusEntityItem> refTypeBusEntityList) {
        RefTypeBusEntityList = refTypeBusEntityList;
    }

    public List<RefTypeBusEntityItem> getRefTypeBusEntityHstList() {
        return RefTypeBusEntityHstList;
    }

    public void setRefTypeBusEntityHstList(List<RefTypeBusEntityItem> refTypeBusEntityHstList) {
        RefTypeBusEntityHstList = refTypeBusEntityHstList;
    }

    public RefTypeBusEntityItem getSelectedRefTypeBusEntity() {
        return selectedRefTypeBusEntity;
    }

    public void setSelectedRefTypeBusEntity(RefTypeBusEntityItem selectedRefTypeBusEntity) {
        this.selectedRefTypeBusEntity = selectedRefTypeBusEntity;
    }

    public boolean isRefTypeBusEntityVisible() {
        return RefTypeBusEntityVisible;
    }

    public void setRefTypeBusEntityVisible(boolean refTypeBusEntityVisible) {
        RefTypeBusEntityVisible = refTypeBusEntityVisible;
    }

    public RefTypeBusEntityItem getRefTypeBusEntityItem() {
        return refTypeBusEntityItem;
    }

    public void setRefTypeBusEntityItem(RefTypeBusEntityItem refTypeBusEntityItem) {
        this.refTypeBusEntityItem = refTypeBusEntityItem;
    }

    public List<RefRegionItem> getRefRegionList() {
        return RefRegionList;
    }

    public void setRefRegionList(List<RefRegionItem> refRegionList) {
        RefRegionList = refRegionList;
    }

    public RefRegionItem getSelectedRefRegion() {
        return selectedRefRegion;
    }

    public void setSelectedRefRegion(RefRegionItem selectedRefRegion) {
        this.selectedRefRegion = selectedRefRegion;
    }

    public boolean isRefRegionVisible() {
        return RefRegionVisible;
    }

    public void setRefRegionVisible(boolean refRegionVisible) {
        RefRegionVisible = refRegionVisible;
    }

    public RefRegionItem getRefRegionItem() {
        return refRegionItem;
    }

    public void setRefRegionItem(RefRegionItem refRegionItem) {
        this.refRegionItem = refRegionItem;
    }

    public List<RefLegalPersonItem> getRefLegalPersonList() {
        return RefLegalPersonList;
    }

    public void setRefLegalPersonList(List<RefLegalPersonItem> refLegalPersonList) {
        RefLegalPersonList = refLegalPersonList;
    }

    public List<RefLegalPersonItem> getRefLegalPersonHstList() {
        return RefLegalPersonHstList;
    }

    public void setRefLegalPersonHstList(List<RefLegalPersonItem> refLegalPersonHstList) {
        RefLegalPersonHstList = refLegalPersonHstList;
    }

    public RefLegalPersonItem getSelectedRefLegalPerson() {
        return selectedRefLegalPerson;
    }

    public void setSelectedRefLegalPerson(RefLegalPersonItem selectedRefLegalPerson) {
        this.selectedRefLegalPerson = selectedRefLegalPerson;
    }

    public boolean isRefLegalPersonVisible() {
        return RefLegalPersonVisible;
    }

    public void setRefLegalPersonVisible(boolean refLegalPersonVisible) {
        RefLegalPersonVisible = refLegalPersonVisible;
    }

    public RefLegalPersonItem getRefLegalPersonItem() {
        return refLegalPersonItem;
    }

    public void setRefLegalPersonItem(RefLegalPersonItem refLegalPersonItem) {
        this.refLegalPersonItem = refLegalPersonItem;
    }

    public List<RefRequirementItem> getRefRequirementList() {
        return RefRequirementList;
    }

    public void setRefRequirementList(List<RefRequirementItem> refRequirementList) {
        RefRequirementList = refRequirementList;
    }

    public List<RefRequirementItem> getRefRequirementHstList() {
        return RefRequirementHstList;
    }

    public void setRefRequirementHstList(List<RefRequirementItem> refRequirementHstList) {
        RefRequirementHstList = refRequirementHstList;
    }

    public RefRequirementItem getSelectedRefRequirement() {
        return selectedRefRequirement;
    }

    public void setSelectedRefRequirement(RefRequirementItem selectedRefRequirement) {
        this.selectedRefRequirement = selectedRefRequirement;
    }

    public boolean isRefRequirementVisible() {
        return RefRequirementVisible;
    }

    public void setRefRequirementVisible(boolean refRequirementVisible) {
        RefRequirementVisible = refRequirementVisible;
    }

    public RefRequirementItem getRefRequirementItem() {
        return refRequirementItem;
    }

    public void setRefRequirementItem(RefRequirementItem refRequirementItem) {
        this.refRequirementItem = refRequirementItem;
    }

    public List<RefTypeProvideItem> getRefTypeProvideList() {
        return RefTypeProvideList;
    }

    public void setRefTypeProvideList(List<RefTypeProvideItem> refTypeProvideList) {
        RefTypeProvideList = refTypeProvideList;
    }

    public List<RefTypeProvideItem> getRefTypeProvideHstList() {
        return RefTypeProvideHstList;
    }

    public void setRefTypeProvideHstList(List<RefTypeProvideItem> refTypeProvideHstList) {
        RefTypeProvideHstList = refTypeProvideHstList;
    }

    public RefTypeProvideItem getSelectedRefTypeProvide() {
        return selectedRefTypeProvide;
    }

    public void setSelectedRefTypeProvide(RefTypeProvideItem selectedRefTypeProvide) {
        this.selectedRefTypeProvide = selectedRefTypeProvide;
    }

    public boolean isRefTypeProvideVisible() {
        return RefTypeProvideVisible;
    }

    public void setRefTypeProvideVisible(boolean refTypeProvideVisible) {
        RefTypeProvideVisible = refTypeProvideVisible;
    }

    public RefTypeProvideItem getRefTypeProvideItem() {
        return refTypeProvideItem;
    }

    public void setRefTypeProvideItem(RefTypeProvideItem refTypeProvideItem) {
        this.refTypeProvideItem = refTypeProvideItem;
    }

    public List<RefTransTypeItem> getRefTransTypeList() {
        return RefTransTypeList;
    }

    public void setRefTransTypeList(List<RefTransTypeItem> refTransTypeList) {
        RefTransTypeList = refTransTypeList;
    }

    public List<RefTransTypeItem> getRefTransTypeHstList() {
        return RefTransTypeHstList;
    }

    public void setRefTransTypeHstList(List<RefTransTypeItem> refTransTypeHstList) {
        RefTransTypeHstList = refTransTypeHstList;
    }

    public RefTransTypeItem getSelectedRefTransType() {
        return selectedRefTransType;
    }

    public void setSelectedRefTransType(RefTransTypeItem selectedRefTransType) {
        this.selectedRefTransType = selectedRefTransType;
    }

    public boolean isRefTransTypeVisible() {
        return RefTransTypeVisible;
    }

    public void setRefTransTypeVisible(boolean refTransTypeVisible) {
        RefTransTypeVisible = refTransTypeVisible;
    }

    public RefTransTypeItem getRefTransTypeItem() {
        return refTransTypeItem;
    }

    public void setRefTransTypeItem(RefTransTypeItem refTransTypeItem) {
        this.refTransTypeItem = refTransTypeItem;
    }

    public List<RefBalanceAccItem> getRefBalanceAccList() {
        return RefBalanceAccList;
    }

    public void setRefBalanceAccList(List<RefBalanceAccItem> refBalanceAccList) {
        RefBalanceAccList = refBalanceAccList;
    }

    public List<RefBalanceAccItem> getRefBalanceAccHstList() {
        return RefBalanceAccHstList;
    }

    public void setRefBalanceAccHstList(List<RefBalanceAccItem> refBalanceAccHstList) {
        RefBalanceAccHstList = refBalanceAccHstList;
    }

    public RefBalanceAccItem getSelectedRefBalanceAcc() {
        return selectedRefBalanceAcc;
    }

    public void setSelectedRefBalanceAcc(RefBalanceAccItem selectedRefBalanceAcc) {
        this.selectedRefBalanceAcc = selectedRefBalanceAcc;
    }

    public boolean isRefBalanceAccVisible() {
        return RefBalanceAccVisible;
    }

    public void setRefBalanceAccVisible(boolean refBalanceAccVisible) {
        RefBalanceAccVisible = refBalanceAccVisible;
    }

    public RefBalanceAccItem getRefBalanceAccItem() {
        return refBalanceAccItem;
    }

    public void setRefBalanceAccItem(RefBalanceAccItem refBalanceAccItem) {
        this.refBalanceAccItem = refBalanceAccItem;
    }

    public List<RefConnOrgItem> getRefConnOrgList() {
        return RefConnOrgList;
    }

    public void setRefConnOrgList(List<RefConnOrgItem> refConnOrgList) {
        RefConnOrgList = refConnOrgList;
    }

    public List<RefConnOrgItem> getRefConnOrgHstList() {
        return RefConnOrgHstList;
    }

    public void setRefConnOrgHstList(List<RefConnOrgItem> refConnOrgHstList) {
        RefConnOrgHstList = refConnOrgHstList;
    }

    public RefConnOrgItem getSelectedRefConnOrg() {
        return selectedRefConnOrg;
    }

    public void setSelectedRefConnOrg(RefConnOrgItem selectedRefConnOrg) {
        this.selectedRefConnOrg = selectedRefConnOrg;
    }

    public boolean isRefConnOrgVisible() {
        return RefConnOrgVisible;
    }

    public void setRefConnOrgVisible(boolean refConnOrgVisible) {
        RefConnOrgVisible = refConnOrgVisible;
    }

    public RefConnOrgItem getRefConnOrgItem() {
        return refConnOrgItem;
    }

    public void setRefConnOrgItem(RefConnOrgItem refConnOrgItem) {
        this.refConnOrgItem = refConnOrgItem;
    }

    public List<RefDepartmentItem> getRefDepartmentList() {
        return RefDepartmentList;
    }

    public void setRefDepartmentList(List<RefDepartmentItem> refDepartmentList) {
        RefDepartmentList = refDepartmentList;
    }

    public RefDepartmentItem getSelectedRefDepartment() {
        return selectedRefDepartment;
    }

    public void setSelectedRefDepartment(RefDepartmentItem selectedRefDepartment) {
        this.selectedRefDepartment = selectedRefDepartment;
    }

    public boolean isRefDepartmentVisible() {
        return RefDepartmentVisible;
    }

    public void setRefDepartmentVisible(boolean refDepartmentVisible) {
        RefDepartmentVisible = refDepartmentVisible;
    }

    public RefDepartmentItem getRefDepartmentItem() {
        return refDepartmentItem;
    }

    public void setRefDepartmentItem(RefDepartmentItem refDepartmentItem) {
        this.refDepartmentItem = refDepartmentItem;
    }

    public List<RefBankItem> getRefBankList() {
        return RefBankList;
    }

    public void setRefBankList(List<RefBankItem> refBankList) {
        RefBankList = refBankList;
    }

    public RefBankItem getSelectedRefBank() {
        return selectedRefBank;
    }

    public void setSelectedRefBank(RefBankItem selectedRefBank) {
        this.selectedRefBank = selectedRefBank;
    }

    public boolean isRefBankVisible() {
        return RefBankVisible;
    }

    public void setRefBankVisible(boolean refBankVisible) {
        RefBankVisible = refBankVisible;
    }

    public RefBankItem getRefBankItem() {
        return refBankItem;
    }

    public void setRefBankItem(RefBankItem refBankItem) {
        this.refBankItem = refBankItem;
    }

    public List<RefCurrencyItem> getRefCurrencyList() {
        return RefCurrencyList;
    }

    public void setRefCurrencyList(List<RefCurrencyItem> refCurrencyList) {
        RefCurrencyList = refCurrencyList;
    }

    public RefCurrencyItem getSelectedRefCurrency() {
        return selectedRefCurrency;
    }

    public void setSelectedRefCurrency(RefCurrencyItem selectedRefCurrency) {
        this.selectedRefCurrency = selectedRefCurrency;
    }

    public boolean isRefCurrencyVisible() {
        return RefCurrencyVisible;
    }

    public void setRefCurrencyVisible(boolean refCurrencyVisible) {
        RefCurrencyVisible = refCurrencyVisible;
    }

    public RefCurrencyItem getRefCurrencyItem() {
        return refCurrencyItem;
    }

    public void setRefCurrencyItem(RefCurrencyItem refCurrencyItem) {
        this.refCurrencyItem = refCurrencyItem;
    }

    public List<RefCurrencyRateItem> getRefCurrencyRateList() {
        return RefCurrencyRateList;
    }

    public void setRefCurrencyRateList(List<RefCurrencyRateItem> refCurrencyRateList) {
        RefCurrencyRateList = refCurrencyRateList;
    }

    public RefCurrencyRateItem getSelectedRefCurrencyRate() {
        return selectedRefCurrencyRate;
    }

    public void setSelectedRefCurrencyRate(RefCurrencyRateItem selectedRefCurrencyRate) {
        this.selectedRefCurrencyRate = selectedRefCurrencyRate;
    }

    public boolean isRefCurrencyRateVisible() {
        return RefCurrencyRateVisible;
    }

    public void setRefCurrencyRateVisible(boolean refCurrencyRateVisible) {
        RefCurrencyRateVisible = refCurrencyRateVisible;
    }

    public RefCurrencyRateItem getRefCurrencyRateItem() {
        return refCurrencyRateItem;
    }

    public void setRefCurrencyRateItem(RefCurrencyRateItem refCurrencyRateItem) {
        this.refCurrencyRateItem = refCurrencyRateItem;
    }

    public List<RefSubjectTypeItem> getRefSubjectTypeList() {
        return RefSubjectTypeList;
    }

    public void setRefSubjectTypeList(List<RefSubjectTypeItem> refSubjectTypeList) {
        RefSubjectTypeList = refSubjectTypeList;
    }

    public List<RefSubjectTypeItem> getRefSubjectTypeHstList() {
        return RefSubjectTypeHstList;
    }

    public void setRefSubjectTypeHstList(List<RefSubjectTypeItem> refSubjectTypeHstList) {
        RefSubjectTypeHstList = refSubjectTypeHstList;
    }

    public RefSubjectTypeItem getSelectedRefSubjectType() {
        return selectedRefSubjectType;
    }

    public void setSelectedRefSubjectType(RefSubjectTypeItem selectedRefSubjectType) {
        this.selectedRefSubjectType = selectedRefSubjectType;
    }

    public boolean isRefSubjectTypeVisible() {
        return RefSubjectTypeVisible;
    }

    public void setRefSubjectTypeVisible(boolean refSubjectTypeVisible) {
        RefSubjectTypeVisible = refSubjectTypeVisible;
    }

    public RefSubjectTypeItem getRefSubjectTypeItem() {
        return refSubjectTypeItem;
    }

    public void setRefSubjectTypeItem(RefSubjectTypeItem refSubjectTypeItem) {
        this.refSubjectTypeItem = refSubjectTypeItem;
    }

    public List<RefRespondentItem> getRefRespondentList() {
        return RefRespondentList;
    }

    public void setRefRespondentList(List<RefRespondentItem> refRespondentList) {
        RefRespondentList = refRespondentList;
    }

    public List<RefRespondentItem> getRefRespondentHstList() {
        return RefRespondentHstList;
    }

    public void setRefRespondentHstList(List<RefRespondentItem> refRespondentHstList) {
        RefRespondentHstList = refRespondentHstList;
    }

    public RefRespondentItem getSelectedRefRespondent() {
        return selectedRefRespondent;
    }

    public void setSelectedRefRespondent(RefRespondentItem selectedRefRespondent) {
        this.selectedRefRespondent = selectedRefRespondent;
    }

    public boolean isRefRespondentVisible() {
        return RefRespondentVisible;
    }

    public void setRefRespondentVisible(boolean refRespondentVisible) {
        RefRespondentVisible = refRespondentVisible;
    }

    public RefRespondentItem getRefRespondentItem() {
        return refRespondentItem;
    }

    public void setRefRespondentItem(RefRespondentItem refRespondentItem) {
        this.refRespondentItem = refRespondentItem;
    }

    public List<RefDocTypeItem> getRefDocTypeList() {
        return RefDocTypeList;
    }

    public void setRefDocTypeList(List<RefDocTypeItem> refDocTypeList) {
        RefDocTypeList = refDocTypeList;
    }

    public List<RefDocTypeItem> getRefDocTypeHstList() {
        return RefDocTypeHstList;
    }

    public void setRefDocTypeHstList(List<RefDocTypeItem> refDocTypeHstList) {
        RefDocTypeHstList = refDocTypeHstList;
    }

    public RefDocTypeItem getSelectedRefDocType() {
        return selectedRefDocType;
    }

    public void setSelectedRefDocType(RefDocTypeItem selectedRefDocType) {
        this.selectedRefDocType = selectedRefDocType;
    }

    public boolean isRefDocTypeVisible() {
        return RefDocTypeVisible;
    }

    public void setRefDocTypeVisible(boolean refDocTypeVisible) {
        RefDocTypeVisible = refDocTypeVisible;
    }

    public RefDocTypeItem getRefDocTypeItem() {
        return refDocTypeItem;
    }

    public void setRefDocTypeItem(RefDocTypeItem refDocTypeItem) {
        this.refDocTypeItem = refDocTypeItem;
    }

    public List<RefDocumentItem> getRefDocumentList() {
        return RefDocumentList;
    }

    public void setRefDocumentList(List<RefDocumentItem> refDocumentList) {
        RefDocumentList = refDocumentList;
    }

    public List<RefDocumentItem> getRefDocumentHstList() {
        return RefDocumentHstList;
    }

    public void setRefDocumentHstList(List<RefDocumentItem> refDocumentHstList) {
        RefDocumentHstList = refDocumentHstList;
    }

    public RefDocumentItem getSelectedRefDocument() {
        return selectedRefDocument;
    }

    public void setSelectedRefDocument(RefDocumentItem selectedRefDocument) {
        this.selectedRefDocument = selectedRefDocument;
    }

    public boolean isRefDocumentVisible() {
        return RefDocumentVisible;
    }

    public void setRefDocumentVisible(boolean refDocumentVisible) {
        RefDocumentVisible = refDocumentVisible;
    }

    public RefDocumentItem getRefDocumentItem() {
        return refDocumentItem;
    }

    public void setRefDocumentItem(RefDocumentItem refDocumentItem) {
        this.refDocumentItem = refDocumentItem;
    }

    public List<RefIssuersItem> getRefIssuersList() {
        return RefIssuersList;
    }

    public void setRefIssuersList(List<RefIssuersItem> refIssuersList) {
        RefIssuersList = refIssuersList;
    }

    public RefIssuersItem getSelectedRefIssuers() {
        return selectedRefIssuers;
    }

    public void setSelectedRefIssuers(RefIssuersItem selectedRefIssuers) {
        this.selectedRefIssuers = selectedRefIssuers;
    }

    public boolean isRefIssuersVisible() {
        return RefIssuersVisible;
    }

    public void setRefIssuersVisible(boolean refIssuersVisible) {
        RefIssuersVisible = refIssuersVisible;
    }

    public RefIssuersItem getRefIssuersItem() {
        return refIssuersItem;
    }

    public void setRefIssuersItem(RefIssuersItem refIssuersItem) {
        this.refIssuersItem = refIssuersItem;
    }

    public List<RefSecuritiesItem> getRefSecuritiesList() {
        return RefSecuritiesList;
    }

    public void setRefSecuritiesList(List<RefSecuritiesItem> refSecuritiesList) {
        RefSecuritiesList = refSecuritiesList;
    }

    public RefSecuritiesItem getSelectedRefSecurities() {
        return selectedRefSecurities;
    }

    public void setSelectedRefSecurities(RefSecuritiesItem selectedRefSecurities) {
        this.selectedRefSecurities = selectedRefSecurities;
    }

    public boolean isRefSecuritiesVisible() {
        return RefSecuritiesVisible;
    }

    public void setRefSecuritiesVisible(boolean refSecuritiesVisible) {
        RefSecuritiesVisible = refSecuritiesVisible;
    }

    public RefSecuritiesItem getRefSecuritiesItem() {
        return refSecuritiesItem;
    }

    public void setRefSecuritiesItem(RefSecuritiesItem refSecuritiesItem) {
        this.refSecuritiesItem = refSecuritiesItem;
    }


    public List<RefVidOperItem> getRefVidOperList() {
        return RefVidOperList;
    }

    public void setRefVidOperList(List<RefVidOperItem> refVidOperList) {
        RefVidOperList = refVidOperList;
    }

    public List<RefVidOperItem> getRefVidOperHstList() {
        return RefVidOperHstList;
    }

    public void setRefVidOperHstList(List<RefVidOperItem> refVidOperHstList) {
        RefVidOperHstList = refVidOperHstList;
    }

    public RefVidOperItem getSelectedRefVidOper() {
        return selectedRefVidOper;
    }

    public void setSelectedRefVidOper(RefVidOperItem selectedRefVidOper) {
        this.selectedRefVidOper = selectedRefVidOper;
    }

    public boolean isRefVidOperVisible() {
        return RefVidOperVisible;
    }

    public void setRefVidOperVisible(boolean refVidOperVisible) {
        RefVidOperVisible = refVidOperVisible;
    }

    public RefVidOperItem getRefVidOperItem() {
        return refVidOperItem;
    }

    public void setRefVidOperItem(RefVidOperItem refVidOperItem) {
        this.refVidOperItem = refVidOperItem;
    }

    public List<RefBranchItem> getRefBranchList() {
        return RefBranchList;
    }

    public void setRefBranchList(List<RefBranchItem> refBranchList) {
        RefBranchList = refBranchList;
    }

    public List<RefBranchItem> getRefBranchHstList() {
        return RefBranchHstList;
    }

    public void setRefBranchHstList(List<RefBranchItem> refBranchHstList) {
        RefBranchHstList = refBranchHstList;
    }

    public RefBranchItem getSelectedRefBranch() {
        return selectedRefBranch;
    }

    public void setSelectedRefBranch(RefBranchItem selectedRefBranch) {
        this.selectedRefBranch = selectedRefBranch;
    }

    public boolean isRefBranchVisible() {
        return RefBranchVisible;
    }

    public void setRefBranchVisible(boolean refBranchVisible) {
        RefBranchVisible = refBranchVisible;
    }

    public RefBranchItem getRefBranchItem() {
        return refBranchItem;
    }

    public void setRefBranchItem(RefBranchItem refBranchItem) {
        this.refBranchItem = refBranchItem;
    }

    public TreeNode getBalanceAccTree() {
        return balanceAccTree;
    }

    public void setBalanceAccTree(TreeNode balanceAccTree) {
        this.balanceAccTree = balanceAccTree;
    }

    public TreeNode getSelectedBalanceAcc() {
        return selectedBalanceAcc;
    }

    public void setSelectedBalanceAcc(TreeNode selectedBalanceAcc) {
        this.selectedBalanceAcc = selectedBalanceAcc;
    }

    public RefRateAgencyItem getSelectedRefRateAgency() {
        return selectedRefRateAgency;
    }

    public void setSelectedRefRateAgency(RefRateAgencyItem selectedRefRateAgency) {
        this.selectedRefRateAgency = selectedRefRateAgency;
    }

    public boolean isRefRateAgencyVisible() {
        return RefRateAgencyVisible;
    }

    public void setRefRateAgencyVisible(boolean refRateAgencyVisible) {
        RefRateAgencyVisible = refRateAgencyVisible;
    }

    public RefRateAgencyItem getRefRateAgencyItem() {
        return refRateAgencyItem;
    }

    public void setRefRateAgencyItem(RefRateAgencyItem refRateAgencyItem) {
        this.refRateAgencyItem = refRateAgencyItem;
    }

    public List<RefRateAgencyItem> getRefRateAgencyList() {
        return RefRateAgencyList;
    }

    public void setRefRateAgencyList(List<RefRateAgencyItem> refRateAgencyList) {
        RefRateAgencyList = refRateAgencyList;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getIsLoad() {
        return isLoad;
    }

    public void setIsLoad(Boolean isLoad) {
        this.isLoad = isLoad;
    }

/*    public RefLegalPersonItem getRefSimpleLPItem() {
        return refSimpleLPItem;
    }

    public void setRefSimpleLPItem(RefLegalPersonItem refSimpleLPItem) {
        this.refSimpleLPItem = refSimpleLPItem;
    }*/

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public UserBean getUserBean() {
        return userBean;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public Boolean getCanEdit() {
        return canEdit;
    }

    public void setCanEdit(Boolean canEdit) {
        this.canEdit = canEdit;
    }

    public List<RefElements> getTypePostList() {
        return typePostList;
    }

    public void setTypePostList(List<RefElements> typePostList) {
        this.typePostList = typePostList;
    }

    public List<RefElements> getRepPerDurMonthsList() {
        return repPerDurMonthsList;
    }

    public void setRepPerDurMonthsList(List<RefElements> repPerDurMonthsList) {
        this.repPerDurMonthsList = repPerDurMonthsList;
    }

    public List<RefListingEstimationItem> getRefListingEstimationList() {
        return RefListingEstimationList;
    }

    public void setRefListingEstimationList(List<RefListingEstimationItem> refListingEstimationList) {
        RefListingEstimationList = refListingEstimationList;
    }

    public RefListingEstimationItem getSelectedRefListingEstimation() {
        return selectedRefListingEstimation;
    }

    public void setSelectedRefListingEstimation(RefListingEstimationItem selectedRefListingEstimation) {
        this.selectedRefListingEstimation = selectedRefListingEstimation;
    }

    public boolean isRefListingEstimationVisible() {
        return RefListingEstimationVisible;
    }

    public void setRefListingEstimationVisible(boolean refListingEstimationVisible) {
        RefListingEstimationVisible = refListingEstimationVisible;
    }

    public RefListingEstimationItem getRefListingEstimationItem() {
        return refListingEstimationItem;
    }

    public void setRefListingEstimationItem(RefListingEstimationItem refListingEstimationItem) {
        this.refListingEstimationItem = refListingEstimationItem;
    }

    public List<RefRatingEstimationItem> getRefRatingEstimationList() {
        return RefRatingEstimationList;
    }

    public void setRefRatingEstimationList(List<RefRatingEstimationItem> refRatingEstimationList) {
        RefRatingEstimationList = refRatingEstimationList;
    }

    public RefRatingEstimationItem getSelectedRefRatingEstimation() {
        return selectedRefRatingEstimation;
    }

    public void setSelectedRefRatingEstimation(RefRatingEstimationItem selectedRefRatingEstimation) {
        this.selectedRefRatingEstimation = selectedRefRatingEstimation;
    }

    public boolean isRefRatingEstimationVisible() {
        return RefRatingEstimationVisible;
    }

    public void setRefRatingEstimationVisible(boolean refRatingEstimationVisible) {
        RefRatingEstimationVisible = refRatingEstimationVisible;
    }

    public RefRatingEstimationItem getRefRatingEstimationItem() {
        return refRatingEstimationItem;
    }

    public void setRefRatingEstimationItem(RefRatingEstimationItem refRatingEstimationItem) {
        this.refRatingEstimationItem = refRatingEstimationItem;
    }

    public List<RefRatingCategoryItem> getRefRatingCategoryList() {
        return RefRatingCategoryList;
    }

    public void setRefRatingCategoryList(List<RefRatingCategoryItem> refRatingCategoryList) {
        RefRatingCategoryList = refRatingCategoryList;
    }

    public RefRatingCategoryItem getSelectedRefRatingCategory() {
        return selectedRefRatingCategory;
    }

    public void setSelectedRefRatingCategory(RefRatingCategoryItem selectedRefRatingCategory) {
        this.selectedRefRatingCategory = selectedRefRatingCategory;
    }

    public boolean isRefRatingCategoryVisible() {
        return RefRatingCategoryVisible;
    }

    public void setRefRatingCategoryVisible(boolean refRatingCategoryVisible) {
        RefRatingCategoryVisible = refRatingCategoryVisible;
    }

    public RefRatingCategoryItem getRefRatingCategoryItem() {
        return refRatingCategoryItem;
    }

    public void setRefRatingCategoryItem(RefRatingCategoryItem refRatingCategoryItem) {
        this.refRatingCategoryItem = refRatingCategoryItem;
    }

    public String getModalName() {
        return modalName;
    }

    public void setModalName(String modalName) {
        this.modalName = modalName;
    }

    public RefPostItem getFilterRefPostItem() {
        return filterRefPostItem;
    }

    public void setFilterRefPostItem(RefPostItem filterRefPostItem) {
        this.filterRefPostItem = filterRefPostItem;
    }

    public RefPersonItem getFilterRefPersonItem() {
        return filterRefPersonItem;
    }

    public void setFilterRefPersonItem(RefPersonItem filterRefPersonItem) {
        this.filterRefPersonItem = filterRefPersonItem;
    }

    public RefLegalPersonItem getFilterRefLegalPersonItem() {
        return filterRefLegalPersonItem;
    }

    public void setFilterRefLegalPersonItem(RefLegalPersonItem filterRefLegalPersonItem) {
        this.filterRefLegalPersonItem = filterRefLegalPersonItem;
    }

    public RefCountryItem getFilterRefCountryItem() {
        return filterRefCountryItem;
    }

    public void setFilterRefCountryItem(RefCountryItem filterRefCountryItem) {
        this.filterRefCountryItem = filterRefCountryItem;
    }

    public RefManagersItem getFilterRefManagersItem() {
        return filterRefManagersItem;
    }

    public void setFilterRefManagersItem(RefManagersItem filterRefManagersItem) {
        this.filterRefManagersItem = filterRefManagersItem;
    }

    public RefTypeBusEntityItem getFilterRefTypeBusEntityItem() {
        return filterRefTypeBusEntityItem;
    }

    public void setFilterRefTypeBusEntityItem(RefTypeBusEntityItem filterRefTypeBusEntityItem) {
        this.filterRefTypeBusEntityItem = filterRefTypeBusEntityItem;
    }

    public RefRegionItem getFilterRefRegionItem() {
        return filterRefRegionItem;
    }

    public void setFilterRefRegionItem(RefRegionItem filterRefRegionItem) {
        this.filterRefRegionItem = filterRefRegionItem;
    }

    public RefRequirementItem getFilterRefRequirementItem() {
        return filterRefRequirementItem;
    }

    public void setFilterRefRequirementItem(RefRequirementItem filterRefRequirementItem) {
        this.filterRefRequirementItem = filterRefRequirementItem;
    }

    public RefTypeProvideItem getFilterRefTypeProvideItem() {
        return filterRefTypeProvideItem;
    }

    public void setFilterRefTypeProvideItem(RefTypeProvideItem filterRefTypeProvideItem) {
        this.filterRefTypeProvideItem = filterRefTypeProvideItem;
    }

    public RefTransTypeItem getFilterRefTransTypeItem() {
        return filterRefTransTypeItem;
    }

    public void setFilterRefTransTypeItem(RefTransTypeItem filterRefTransTypeItem) {
        this.filterRefTransTypeItem = filterRefTransTypeItem;
    }

    public RefBalanceAccItem getFilterRefBalanceAccItem() {
        return filterRefBalanceAccItem;
    }

    public void setFilterRefBalanceAccItem(RefBalanceAccItem filterRefBalanceAccItem) {
        this.filterRefBalanceAccItem = filterRefBalanceAccItem;
    }

    public RefConnOrgItem getFilterRefConnOrgItem() {
        return filterRefConnOrgItem;
    }

    public void setFilterRefConnOrgItem(RefConnOrgItem filterRefConnOrgItem) {
        this.filterRefConnOrgItem = filterRefConnOrgItem;
    }

    public RefDepartmentItem getFilterRefDepartmentItem() {
        return filterRefDepartmentItem;
    }

    public void setFilterRefDepartmentItem(RefDepartmentItem filterRefDepartmentItem) {
        this.filterRefDepartmentItem = filterRefDepartmentItem;
    }

    public RefBankItem getFilterRefBankItem() {
        return filterRefBankItem;
    }

    public void setFilterRefBankItem(RefBankItem filterRefBankItem) {
        this.filterRefBankItem = filterRefBankItem;
    }

    public RefRateAgencyItem getFilterRefRateAgencyItem() {
        return filterRefRateAgencyItem;
    }

    public void setFilterRefRateAgencyItem(RefRateAgencyItem filterRefRateAgencyItem) {
        this.filterRefRateAgencyItem = filterRefRateAgencyItem;
    }

    public RefCurrencyItem getFilterRefCurrencyItem() {
        return filterRefCurrencyItem;
    }

    public void setFilterRefCurrencyItem(RefCurrencyItem filterRefCurrencyItem) {
        this.filterRefCurrencyItem = filterRefCurrencyItem;
    }

    public RefCurrencyRateItem getFilterRefCurrencyRateItem() {
        return filterRefCurrencyRateItem;
    }

    public void setFilterRefCurrencyRateItem(RefCurrencyRateItem filterRefCurrencyRateItem) {
        this.filterRefCurrencyRateItem = filterRefCurrencyRateItem;
    }

    public RefSubjectTypeItem getFilterRefSubjectTypeItem() {
        return filterRefSubjectTypeItem;
    }

    public void setFilterRefSubjectTypeItem(RefSubjectTypeItem filterRefSubjectTypeItem) {
        this.filterRefSubjectTypeItem = filterRefSubjectTypeItem;
    }

    public RefRespondentItem getFilterRefRespondentItem() {
        return filterRefRespondentItem;
    }

    public void setFilterRefRespondentItem(RefRespondentItem filterRefRespondentItem) {
        this.filterRefRespondentItem = filterRefRespondentItem;
    }

    public RefDocTypeItem getFilterRefDocTypeItem() {
        return filterRefDocTypeItem;
    }

    public void setFilterRefDocTypeItem(RefDocTypeItem filterRefDocTypeItem) {
        this.filterRefDocTypeItem = filterRefDocTypeItem;
    }

    public RefDocumentItem getFilterRefDocumentItem() {
        return filterRefDocumentItem;
    }

    public void setFilterRefDocumentItem(RefDocumentItem filterRefDocumentItem) {
        this.filterRefDocumentItem = filterRefDocumentItem;
    }

    public RefIssuersItem getFilterRefIssuersItem() {
        return filterRefIssuersItem;
    }

    public void setFilterRefIssuersItem(RefIssuersItem filterRefIssuersItem) {
        this.filterRefIssuersItem = filterRefIssuersItem;
    }

    public RefSecuritiesItem getFilterRefSecuritiesItem() {
        return filterRefSecuritiesItem;
    }

    public void setFilterRefSecuritiesItem(RefSecuritiesItem filterRefSecuritiesItem) {
        this.filterRefSecuritiesItem = filterRefSecuritiesItem;
    }

    public RefVidOperItem getFilterRefVidOperItem() {
        return filterRefVidOperItem;
    }

    public void setFilterRefVidOperItem(RefVidOperItem filterRefVidOperItem) {
        this.filterRefVidOperItem = filterRefVidOperItem;
    }

    public RefBranchItem getFilterRefBranchItem() {
        return filterRefBranchItem;
    }

    public void setFilterRefBranchItem(RefBranchItem filterRefBranchItem) {
        this.filterRefBranchItem = filterRefBranchItem;
    }

    public RefListingEstimationItem getFilterRefListingEstimationItem() {
        return filterRefListingEstimationItem;
    }

    public void setFilterRefListingEstimationItem(RefListingEstimationItem filterRefListingEstimationItem) {
        this.filterRefListingEstimationItem = filterRefListingEstimationItem;
    }

    public RefRatingEstimationItem getFilterRefRatingEstimationItem() {
        return filterRefRatingEstimationItem;
    }

    public void setFilterRefRatingEstimationItem(RefRatingEstimationItem filterRefRatingEstimationItem) {
        this.filterRefRatingEstimationItem = filterRefRatingEstimationItem;
    }

    public RefRatingCategoryItem getFilterRefRatingCategoryItem() {
        return filterRefRatingCategoryItem;
    }

    public void setFilterRefRatingCategoryItem(RefRatingCategoryItem filterRefRatingCategoryItem) {
        this.filterRefRatingCategoryItem = filterRefRatingCategoryItem;
    }

    public List<RefLegalPersonItem> getRefLegalPersonVerList() {
        return RefLegalPersonVerList;
    }

    public void setRefLegalPersonVerList(List<RefLegalPersonItem> refLegalPersonVerList) {
        RefLegalPersonVerList = refLegalPersonVerList;
    }

    public RefLegalPersonItem getSelectedRefLegalPersonVer() {
        return selectedRefLegalPersonVer;
    }

    public void setSelectedRefLegalPersonVer(RefLegalPersonItem selectedRefLegalPersonVer) {
        this.selectedRefLegalPersonVer = selectedRefLegalPersonVer;
    }

    public String getEditKind() {
        return editKind;
    }

    public void setEditKind(String editKind) {
        this.editKind = editKind;
    }

    public Boolean getIsVisibleEditKnd() {
        return isVisibleEditKnd;
    }

    public void setIsVisibleEditKnd(Boolean isVisibleEditKnd) {
        this.isVisibleEditKnd = isVisibleEditKnd;
    }

    public List<RefPostItem> getRefPostVerList() {
        return RefPostVerList;
    }

    public void setRefPostVerList(List<RefPostItem> refPostVerList) {
        RefPostVerList = refPostVerList;
    }

    public RefPostItem getSelectedRefPostVer() {
        return selectedRefPostVer;
    }

    public void setSelectedRefPostVer(RefPostItem selectedRefPostVer) {
        this.selectedRefPostVer = selectedRefPostVer;
    }

    public List<RefPersonItem> getRefPersonVerList() {
        return RefPersonVerList;
    }

    public void setRefPersonVerList(List<RefPersonItem> refPersonVerList) {
        RefPersonVerList = refPersonVerList;
    }

    public RefPersonItem getSelectedRefPersonVer() {
        return selectedRefPersonVer;
    }

    public void setSelectedRefPersonVer(RefPersonItem selectedRefPersonVer) {
        this.selectedRefPersonVer = selectedRefPersonVer;
    }

    public List<RefCountryItem> getRefCountryVerList() {
        return RefCountryVerList;
    }

    public void setRefCountryVerList(List<RefCountryItem> refCountryVerList) {
        RefCountryVerList = refCountryVerList;
    }

    public RefCountryItem getSelectedRefCountryVer() {
        return selectedRefCountryVer;
    }

    public void setSelectedRefCountryVer(RefCountryItem selectedRefCountryVer) {
        this.selectedRefCountryVer = selectedRefCountryVer;
    }

    public List<RefManagersItem> getRefManagersVerList() {
        return RefManagersVerList;
    }

    public void setRefManagersVerList(List<RefManagersItem> refManagersVerList) {
        RefManagersVerList = refManagersVerList;
    }

    public RefManagersItem getSelectedRefManagersVer() {
        return selectedRefManagersVer;
    }

    public void setSelectedRefManagersVer(RefManagersItem selectedRefManagersVer) {
        this.selectedRefManagersVer = selectedRefManagersVer;
    }

    public List<RefTypeBusEntityItem> getRefTypeBusEntityVerList() {
        return RefTypeBusEntityVerList;
    }

    public void setRefTypeBusEntityVerList(List<RefTypeBusEntityItem> refTypeBusEntityVerList) {
        RefTypeBusEntityVerList = refTypeBusEntityVerList;
    }

    public RefTypeBusEntityItem getSelectedRefTypeBusEntityVer() {
        return selectedRefTypeBusEntityVer;
    }

    public void setSelectedRefTypeBusEntityVer(RefTypeBusEntityItem selectedRefTypeBusEntityVer) {
        this.selectedRefTypeBusEntityVer = selectedRefTypeBusEntityVer;
    }

    public List<RefRegionItem> getRefRegionVerList() {
        return RefRegionVerList;
    }

    public void setRefRegionVerList(List<RefRegionItem> refRegionVerList) {
        RefRegionVerList = refRegionVerList;
    }

    public RefRegionItem getSelectedRefRegionVer() {
        return selectedRefRegionVer;
    }

    public void setSelectedRefRegionVer(RefRegionItem selectedRefRegionVer) {
        this.selectedRefRegionVer = selectedRefRegionVer;
    }

    public List<RefRequirementItem> getRefRequirementVerList() {
        return RefRequirementVerList;
    }

    public void setRefRequirementVerList(List<RefRequirementItem> refRequirementVerList) {
        RefRequirementVerList = refRequirementVerList;
    }

    public RefRequirementItem getSelectedRefRequirementVer() {
        return selectedRefRequirementVer;
    }

    public void setSelectedRefRequirementVer(RefRequirementItem selectedRefRequirementVer) {
        this.selectedRefRequirementVer = selectedRefRequirementVer;
    }

    public List<RefTypeProvideItem> getRefTypeProvideVerList() {
        return RefTypeProvideVerList;
    }

    public void setRefTypeProvideVerList(List<RefTypeProvideItem> refTypeProvideVerList) {
        RefTypeProvideVerList = refTypeProvideVerList;
    }

    public RefTypeProvideItem getSelectedRefTypeProvideVer() {
        return selectedRefTypeProvideVer;
    }

    public void setSelectedRefTypeProvideVer(RefTypeProvideItem selectedRefTypeProvideVer) {
        this.selectedRefTypeProvideVer = selectedRefTypeProvideVer;
    }

    public List<RefTransTypeItem> getRefTransTypeVerList() {
        return RefTransTypeVerList;
    }

    public void setRefTransTypeVerList(List<RefTransTypeItem> refTransTypeVerList) {
        RefTransTypeVerList = refTransTypeVerList;
    }

    public RefTransTypeItem getSelectedRefTransTypeVer() {
        return selectedRefTransTypeVer;
    }

    public void setSelectedRefTransTypeVer(RefTransTypeItem selectedRefTransTypeVer) {
        this.selectedRefTransTypeVer = selectedRefTransTypeVer;
    }

    public List<RefBalanceAccItem> getRefBalanceAccVerList() {
        return RefBalanceAccVerList;
    }

    public void setRefBalanceAccVerList(List<RefBalanceAccItem> refBalanceAccVerList) {
        RefBalanceAccVerList = refBalanceAccVerList;
    }

    public RefBalanceAccItem getSelectedRefBalanceAccVer() {
        return selectedRefBalanceAccVer;
    }

    public void setSelectedRefBalanceAccVer(RefBalanceAccItem selectedRefBalanceAccVer) {
        this.selectedRefBalanceAccVer = selectedRefBalanceAccVer;
    }

    public List<RefConnOrgItem> getRefConnOrgVerList() {
        return RefConnOrgVerList;
    }

    public void setRefConnOrgVerList(List<RefConnOrgItem> refConnOrgVerList) {
        RefConnOrgVerList = refConnOrgVerList;
    }

    public RefConnOrgItem getSelectedRefConnOrgVer() {
        return selectedRefConnOrgVer;
    }

    public void setSelectedRefConnOrgVer(RefConnOrgItem selectedRefConnOrgVer) {
        this.selectedRefConnOrgVer = selectedRefConnOrgVer;
    }

    public List<RefDepartmentItem> getRefDepartmentVerList() {
        return RefDepartmentVerList;
    }

    public void setRefDepartmentVerList(List<RefDepartmentItem> refDepartmentVerList) {
        RefDepartmentVerList = refDepartmentVerList;
    }

    public RefDepartmentItem getSelectedRefDepartmentVer() {
        return selectedRefDepartmentVer;
    }

    public void setSelectedRefDepartmentVer(RefDepartmentItem selectedRefDepartmentVer) {
        this.selectedRefDepartmentVer = selectedRefDepartmentVer;
    }

    public List<RefBankItem> getRefBankVerList() {
        return RefBankVerList;
    }

    public void setRefBankVerList(List<RefBankItem> refBankVerList) {
        RefBankVerList = refBankVerList;
    }

    public RefBankItem getSelectedRefBankVer() {
        return selectedRefBankVer;
    }

    public void setSelectedRefBankVer(RefBankItem selectedRefBankVer) {
        this.selectedRefBankVer = selectedRefBankVer;
    }

    public List<RefRateAgencyItem> getRefRateAgencyVerList() {
        return RefRateAgencyVerList;
    }

    public void setRefRateAgencyVerList(List<RefRateAgencyItem> refRateAgencyVerList) {
        RefRateAgencyVerList = refRateAgencyVerList;
    }

    public RefRateAgencyItem getSelectedRefRateAgencyVer() {
        return selectedRefRateAgencyVer;
    }

    public void setSelectedRefRateAgencyVer(RefRateAgencyItem selectedRefRateAgencyVer) {
        this.selectedRefRateAgencyVer = selectedRefRateAgencyVer;
    }

    public List<RefCurrencyItem> getRefCurrencyVerList() {
        return RefCurrencyVerList;
    }

    public void setRefCurrencyVerList(List<RefCurrencyItem> refCurrencyVerList) {
        RefCurrencyVerList = refCurrencyVerList;
    }

    public RefCurrencyItem getSelectedRefCurrencyVer() {
        return selectedRefCurrencyVer;
    }

    public void setSelectedRefCurrencyVer(RefCurrencyItem selectedRefCurrencyVer) {
        this.selectedRefCurrencyVer = selectedRefCurrencyVer;
    }

    public List<RefCurrencyRateItem> getRefCurrencyRateVerList() {
        return RefCurrencyRateVerList;
    }

    public void setRefCurrencyRateVerList(List<RefCurrencyRateItem> refCurrencyRateVerList) {
        RefCurrencyRateVerList = refCurrencyRateVerList;
    }

    public RefCurrencyRateItem getSelectedRefCurrencyRateVer() {
        return selectedRefCurrencyRateVer;
    }

    public void setSelectedRefCurrencyRateVer(RefCurrencyRateItem selectedRefCurrencyRateVer) {
        this.selectedRefCurrencyRateVer = selectedRefCurrencyRateVer;
    }

    public List<RefSubjectTypeItem> getRefSubjectTypeVerList() {
        return RefSubjectTypeVerList;
    }

    public void setRefSubjectTypeVerList(List<RefSubjectTypeItem> refSubjectTypeVerList) {
        RefSubjectTypeVerList = refSubjectTypeVerList;
    }

    public RefSubjectTypeItem getSelectedRefSubjectTypeVer() {
        return selectedRefSubjectTypeVer;
    }

    public void setSelectedRefSubjectTypeVer(RefSubjectTypeItem selectedRefSubjectTypeVer) {
        this.selectedRefSubjectTypeVer = selectedRefSubjectTypeVer;
    }

    public List<RefRespondentItem> getRefRespondentVerList() {
        return RefRespondentVerList;
    }

    public void setRefRespondentVerList(List<RefRespondentItem> refRespondentVerList) {
        RefRespondentVerList = refRespondentVerList;
    }

    public RefRespondentItem getSelectedRefRespondentVer() {
        return selectedRefRespondentVer;
    }

    public void setSelectedRefRespondentVer(RefRespondentItem selectedRefRespondentVer) {
        this.selectedRefRespondentVer = selectedRefRespondentVer;
    }

    public List<RefDocTypeItem> getRefDocTypeVerList() {
        return RefDocTypeVerList;
    }

    public void setRefDocTypeVerList(List<RefDocTypeItem> refDocTypeVerList) {
        RefDocTypeVerList = refDocTypeVerList;
    }

    public RefDocTypeItem getSelectedRefDocTypeVer() {
        return selectedRefDocTypeVer;
    }

    public void setSelectedRefDocTypeVer(RefDocTypeItem selectedRefDocTypeVer) {
        this.selectedRefDocTypeVer = selectedRefDocTypeVer;
    }

    public List<RefDocumentItem> getRefDocumentVerList() {
        return RefDocumentVerList;
    }

    public void setRefDocumentVerList(List<RefDocumentItem> refDocumentVerList) {
        RefDocumentVerList = refDocumentVerList;
    }

    public RefDocumentItem getSelectedRefDocumentVer() {
        return selectedRefDocumentVer;
    }

    public void setSelectedRefDocumentVer(RefDocumentItem selectedRefDocumentVer) {
        this.selectedRefDocumentVer = selectedRefDocumentVer;
    }

    public List<RefIssuersItem> getRefIssuersVerList() {
        return RefIssuersVerList;
    }

    public void setRefIssuersVerList(List<RefIssuersItem> refIssuersVerList) {
        RefIssuersVerList = refIssuersVerList;
    }

    public RefIssuersItem getSelectedRefIssuersVer() {
        return selectedRefIssuersVer;
    }

    public void setSelectedRefIssuersVer(RefIssuersItem selectedRefIssuersVer) {
        this.selectedRefIssuersVer = selectedRefIssuersVer;
    }

    public List<RefSecuritiesItem> getRefSecuritiesVerList() {
        return RefSecuritiesVerList;
    }

    public void setRefSecuritiesVerList(List<RefSecuritiesItem> refSecuritiesVerList) {
        RefSecuritiesVerList = refSecuritiesVerList;
    }

    public RefSecuritiesItem getSelectedRefSecuritiesVer() {
        return selectedRefSecuritiesVer;
    }

    public void setSelectedRefSecuritiesVer(RefSecuritiesItem selectedRefSecuritiesVer) {
        this.selectedRefSecuritiesVer = selectedRefSecuritiesVer;
    }

    public List<RefVidOperItem> getRefVidOperVerList() {
        return RefVidOperVerList;
    }

    public void setRefVidOperVerList(List<RefVidOperItem> refVidOperVerList) {
        RefVidOperVerList = refVidOperVerList;
    }

    public RefVidOperItem getSelectedRefVidOperVer() {
        return selectedRefVidOperVer;
    }

    public void setSelectedRefVidOperVer(RefVidOperItem selectedRefVidOperVer) {
        this.selectedRefVidOperVer = selectedRefVidOperVer;
    }

    public List<RefBranchItem> getRefBranchVerList() {
        return RefBranchVerList;
    }

    public void setRefBranchVerList(List<RefBranchItem> refBranchVerList) {
        RefBranchVerList = refBranchVerList;
    }

    public RefBranchItem getSelectedRefBranchVer() {
        return selectedRefBranchVer;
    }

    public void setSelectedRefBranchVer(RefBranchItem selectedRefBranchVer) {
        this.selectedRefBranchVer = selectedRefBranchVer;
    }

    public List<RefListingEstimationItem> getRefListingEstimationVerList() {
        return RefListingEstimationVerList;
    }

    public void setRefListingEstimationVerList(List<RefListingEstimationItem> refListingEstimationVerList) {
        RefListingEstimationVerList = refListingEstimationVerList;
    }

    public RefListingEstimationItem getSelectedRefListingEstimationVer() {
        return selectedRefListingEstimationVer;
    }

    public void setSelectedRefListingEstimationVer(RefListingEstimationItem selectedRefListingEstimationVer) {
        this.selectedRefListingEstimationVer = selectedRefListingEstimationVer;
    }

    public List<RefRatingEstimationItem> getRefRatingEstimationVerList() {
        return RefRatingEstimationVerList;
    }

    public void setRefRatingEstimationVerList(List<RefRatingEstimationItem> refRatingEstimationVerList) {
        RefRatingEstimationVerList = refRatingEstimationVerList;
    }

    public RefRatingEstimationItem getSelectedRefRatingEstimationVer() {
        return selectedRefRatingEstimationVer;
    }

    public void setSelectedRefRatingEstimationVer(RefRatingEstimationItem selectedRefRatingEstimationVer) {
        this.selectedRefRatingEstimationVer = selectedRefRatingEstimationVer;
    }

    public List<RefRatingCategoryItem> getRefRatingCategoryVerList() {
        return RefRatingCategoryVerList;
    }

    public void setRefRatingCategoryVerList(List<RefRatingCategoryItem> refRatingCategoryVerList) {
        RefRatingCategoryVerList = refRatingCategoryVerList;
    }

    public RefRatingCategoryItem getSelectedRefRatingCategoryVer() {
        return selectedRefRatingCategoryVer;
    }

    public void setSelectedRefRatingCategoryVer(RefRatingCategoryItem selectedRefRatingCategoryVer) {
        this.selectedRefRatingCategoryVer = selectedRefRatingCategoryVer;
    }

    public boolean isDisableIdn() {
        return disableIdn;
    }

    public void setDisableIdn(boolean disableIdn) {
        this.disableIdn = disableIdn;
    }

    public boolean isDisableInvIdn() {
        return disableInvIdn;
    }

    public void setDisableInvIdn(boolean disableInvIdn) {
        this.disableInvIdn = disableInvIdn;
    }

    /*public List<SimpleReference> getRefRequestTypeList() {
        return RefRequestTypeList;
    }

    public void setRefRequestTypeList(List<SimpleReference> refRequestTypeList) {
        RefRequestTypeList = refRequestTypeList;
    }

    public List<SimpleReference> getRefRequestTypeHstList() {
        return RefRequestTypeHstList;
    }

    public void setRefRequestTypeHstList(List<SimpleReference> refRequestTypeHstList) {
        RefRequestTypeHstList = refRequestTypeHstList;
    }

    public List<SimpleReference> getRefRequestTypeVerList() {
        return RefRequestTypeVerList;
    }

    public void setRefRequestTypeVerList(List<SimpleReference> refRequestTypeVerList) {
        RefRequestTypeVerList = refRequestTypeVerList;
    }

    public SimpleReference getSelectedRefRequestType() {
        return selectedRefRequestType;
    }

    public void setSelectedRefRequestType(SimpleReference selectedRefRequestType) {
        this.selectedRefRequestType = selectedRefRequestType;
    }

    public SimpleReference getSelectedRefRequestTypeVer() {
        return selectedRefRequestTypeVer;
    }

    public void setSelectedRefRequestTypeVer(SimpleReference selectedRefRequestTypeVer) {
        this.selectedRefRequestTypeVer = selectedRefRequestTypeVer;
    }

    public boolean isRefRequestTypeVisible() {
        return RefRequestTypeVisible;
    }

    public void setRefRequestTypeVisible(boolean refRequestTypeVisible) {
        RefRequestTypeVisible = refRequestTypeVisible;
    }

    public SimpleReference getRefRequestTypeItem() {
        return refRequestTypeItem;
    }

    public void setRefRequestTypeItem(SimpleReference refRequestTypeItem) {
        this.refRequestTypeItem = refRequestTypeItem;
    }

    public SimpleReference getFilterRefRequestTypeItem() {
        return filterRefRequestTypeItem;
    }

    public void setFilterRefRequestTypeItem(SimpleReference filterRefRequestTypeItem) {
        this.filterRefRequestTypeItem = filterRefRequestTypeItem;
    }

    public List<SimpleReference> getRefRequestWayList() {
        return RefRequestWayList;
    }

    public void setRefRequestWayList(List<SimpleReference> refRequestWayList) {
        RefRequestWayList = refRequestWayList;
    }

    public List<SimpleReference> getRefRequestWayHstList() {
        return RefRequestWayHstList;
    }

    public void setRefRequestWayHstList(List<SimpleReference> refRequestWayHstList) {
        RefRequestWayHstList = refRequestWayHstList;
    }

    public List<SimpleReference> getRefRequestWayVerList() {
        return RefRequestWayVerList;
    }

    public void setRefRequestWayVerList(List<SimpleReference> refRequestWayVerList) {
        RefRequestWayVerList = refRequestWayVerList;
    }

    public SimpleReference getSelectedRefRequestWay() {
        return selectedRefRequestWay;
    }

    public void setSelectedRefRequestWay(SimpleReference selectedRefRequestWay) {
        this.selectedRefRequestWay = selectedRefRequestWay;
    }

    public SimpleReference getSelectedRefRequestWayVer() {
        return selectedRefRequestWayVer;
    }

    public void setSelectedRefRequestWayVer(SimpleReference selectedRefRequestWayVer) {
        this.selectedRefRequestWayVer = selectedRefRequestWayVer;
    }

    public boolean isRefRequestWayVisible() {
        return RefRequestWayVisible;
    }

    public void setRefRequestWayVisible(boolean refRequestWayVisible) {
        RefRequestWayVisible = refRequestWayVisible;
    }

    public SimpleReference getRefRequestWayItem() {
        return refRequestWayItem;
    }

    public void setRefRequestWayItem(SimpleReference refRequestWayItem) {
        this.refRequestWayItem = refRequestWayItem;
    }

    public SimpleReference getFilterRefRequestWayItem() {
        return filterRefRequestWayItem;
    }

    public void setFilterRefRequestWayItem(SimpleReference filterRefRequestWayItem) {
        this.filterRefRequestWayItem = filterRefRequestWayItem;
    }

    public List<SimpleReference> getRefMarketKindList() {
        return RefMarketKindList;
    }

    public void setRefMarketKindList(List<SimpleReference> refMarketKindList) {
        RefMarketKindList = refMarketKindList;
    }

    public List<SimpleReference> getRefMarketKindHstList() {
        return RefMarketKindHstList;
    }

    public void setRefMarketKindHstList(List<SimpleReference> refMarketKindHstList) {
        RefMarketKindHstList = refMarketKindHstList;
    }

    public List<SimpleReference> getRefMarketKindVerList() {
        return RefMarketKindVerList;
    }

    public void setRefMarketKindVerList(List<SimpleReference> refMarketKindVerList) {
        RefMarketKindVerList = refMarketKindVerList;
    }

    public SimpleReference getSelectedRefMarketKind() {
        return selectedRefMarketKind;
    }

    public void setSelectedRefMarketKind(SimpleReference selectedRefMarketKind) {
        this.selectedRefMarketKind = selectedRefMarketKind;
    }

    public SimpleReference getSelectedRefMarketKindVer() {
        return selectedRefMarketKindVer;
    }

    public void setSelectedRefMarketKindVer(SimpleReference selectedRefMarketKindVer) {
        this.selectedRefMarketKindVer = selectedRefMarketKindVer;
    }

    public boolean isRefMarketKindVisible() {
        return RefMarketKindVisible;
    }

    public void setRefMarketKindVisible(boolean refMarketKindVisible) {
        RefMarketKindVisible = refMarketKindVisible;
    }

    public SimpleReference getRefMarketKindItem() {
        return refMarketKindItem;
    }

    public void setRefMarketKindItem(SimpleReference refMarketKindItem) {
        this.refMarketKindItem = refMarketKindItem;
    }

    public SimpleReference getFilterRefMarketKindItem() {
        return filterRefMarketKindItem;
    }

    public void setFilterRefMarketKindItem(SimpleReference filterRefMarketKindItem) {
        this.filterRefMarketKindItem = filterRefMarketKindItem;
    }

    public List<SimpleReference> getRefCategoryList() {
        return RefCategoryList;
    }

    public void setRefCategoryList(List<SimpleReference> refCategoryList) {
        RefCategoryList = refCategoryList;
    }

    public List<SimpleReference> getRefCategoryHstList() {
        return RefCategoryHstList;
    }

    public void setRefCategoryHstList(List<SimpleReference> refCategoryHstList) {
        RefCategoryHstList = refCategoryHstList;
    }

    public List<SimpleReference> getRefCategoryVerList() {
        return RefCategoryVerList;
    }

    public void setRefCategoryVerList(List<SimpleReference> refCategoryVerList) {
        RefCategoryVerList = refCategoryVerList;
    }

    public SimpleReference getSelectedRefCategory() {
        return selectedRefCategory;
    }

    public void setSelectedRefCategory(SimpleReference selectedRefCategory) {
        this.selectedRefCategory = selectedRefCategory;
    }

    public SimpleReference getSelectedRefCategoryVer() {
        return selectedRefCategoryVer;
    }

    public void setSelectedRefCategoryVer(SimpleReference selectedRefCategoryVer) {
        this.selectedRefCategoryVer = selectedRefCategoryVer;
    }

    public boolean isRefCategoryVisible() {
        return RefCategoryVisible;
    }

    public void setRefCategoryVisible(boolean refCategoryVisible) {
        RefCategoryVisible = refCategoryVisible;
    }

    public SimpleReference getRefCategoryItem() {
        return refCategoryItem;
    }

    public void setRefCategoryItem(SimpleReference refCategoryItem) {
        this.refCategoryItem = refCategoryItem;
    }

    public SimpleReference getFilterRefCategoryItem() {
        return filterRefCategoryItem;
    }

    public void setFilterRefCategoryItem(SimpleReference filterRefCategoryItem) {
        this.filterRefCategoryItem = filterRefCategoryItem;
    }

    public List<SimpleReference> getRefSubCategoryList() {
        return RefSubCategoryList;
    }

    public void setRefSubCategoryList(List<SimpleReference> refSubCategoryList) {
        RefSubCategoryList = refSubCategoryList;
    }

    public List<SimpleReference> getRefSubCategoryHstList() {
        return RefSubCategoryHstList;
    }

    public void setRefSubCategoryHstList(List<SimpleReference> refSubCategoryHstList) {
        RefSubCategoryHstList = refSubCategoryHstList;
    }

    public List<SimpleReference> getRefSubCategoryVerList() {
        return RefSubCategoryVerList;
    }

    public void setRefSubCategoryVerList(List<SimpleReference> refSubCategoryVerList) {
        RefSubCategoryVerList = refSubCategoryVerList;
    }

    public SimpleReference getSelectedRefSubCategory() {
        return selectedRefSubCategory;
    }

    public void setSelectedRefSubCategory(SimpleReference selectedRefSubCategory) {
        this.selectedRefSubCategory = selectedRefSubCategory;
    }

    public SimpleReference getSelectedRefSubCategoryVer() {
        return selectedRefSubCategoryVer;
    }

    public void setSelectedRefSubCategoryVer(SimpleReference selectedRefSubCategoryVer) {
        this.selectedRefSubCategoryVer = selectedRefSubCategoryVer;
    }

    public boolean isRefSubCategoryVisible() {
        return RefSubCategoryVisible;
    }

    public void setRefSubCategoryVisible(boolean refSubCategoryVisible) {
        RefSubCategoryVisible = refSubCategoryVisible;
    }

    public SimpleReference getRefSubCategoryItem() {
        return refSubCategoryItem;
    }

    public void setRefSubCategoryItem(SimpleReference refSubCategoryItem) {
        this.refSubCategoryItem = refSubCategoryItem;
    }

    public SimpleReference getFilterRefSubCategoryItem() {
        return filterRefSubCategoryItem;
    }

    public void setFilterRefSubCategoryItem(SimpleReference filterRefSubCategoryItem) {
        this.filterRefSubCategoryItem = filterRefSubCategoryItem;
    }

    public List<SimpleReference> getRefAccountTypeList() {
        return RefAccountTypeList;
    }

    public void setRefAccountTypeList(List<SimpleReference> refAccountTypeList) {
        RefAccountTypeList = refAccountTypeList;
    }

    public List<SimpleReference> getRefAccountTypeHstList() {
        return RefAccountTypeHstList;
    }

    public void setRefAccountTypeHstList(List<SimpleReference> refAccountTypeHstList) {
        RefAccountTypeHstList = refAccountTypeHstList;
    }

    public List<SimpleReference> getRefAccountTypeVerList() {
        return RefAccountTypeVerList;
    }

    public void setRefAccountTypeVerList(List<SimpleReference> refAccountTypeVerList) {
        RefAccountTypeVerList = refAccountTypeVerList;
    }

    public SimpleReference getSelectedRefAccountType() {
        return selectedRefAccountType;
    }

    public void setSelectedRefAccountType(SimpleReference selectedRefAccountType) {
        this.selectedRefAccountType = selectedRefAccountType;
    }

    public SimpleReference getSelectedRefAccountTypeVer() {
        return selectedRefAccountTypeVer;
    }

    public void setSelectedRefAccountTypeVer(SimpleReference selectedRefAccountTypeVer) {
        this.selectedRefAccountTypeVer = selectedRefAccountTypeVer;
    }

    public boolean isRefAccountTypeVisible() {
        return RefAccountTypeVisible;
    }

    public void setRefAccountTypeVisible(boolean refAccountTypeVisible) {
        RefAccountTypeVisible = refAccountTypeVisible;
    }

    public SimpleReference getRefAccountTypeItem() {
        return refAccountTypeItem;
    }

    public void setRefAccountTypeItem(SimpleReference refAccountTypeItem) {
        this.refAccountTypeItem = refAccountTypeItem;
    }

    public SimpleReference getFilterRefAccountTypeItem() {
        return filterRefAccountTypeItem;
    }

    public void setFilterRefAccountTypeItem(SimpleReference filterRefAccountTypeItem) {
        this.filterRefAccountTypeItem = filterRefAccountTypeItem;
    }

    public List<SimpleReference> getRefSubAccountTypeList() {
        return RefSubAccountTypeList;
    }

    public void setRefSubAccountTypeList(List<SimpleReference> refSubAccountTypeList) {
        RefSubAccountTypeList = refSubAccountTypeList;
    }

    public List<SimpleReference> getRefSubAccountTypeHstList() {
        return RefSubAccountTypeHstList;
    }

    public void setRefSubAccountTypeHstList(List<SimpleReference> refSubAccountTypeHstList) {
        RefSubAccountTypeHstList = refSubAccountTypeHstList;
    }

    public List<SimpleReference> getRefSubAccountTypeVerList() {
        return RefSubAccountTypeVerList;
    }

    public void setRefSubAccountTypeVerList(List<SimpleReference> refSubAccountTypeVerList) {
        RefSubAccountTypeVerList = refSubAccountTypeVerList;
    }

    public SimpleReference getSelectedRefSubAccountType() {
        return selectedRefSubAccountType;
    }

    public void setSelectedRefSubAccountType(SimpleReference selectedRefSubAccountType) {
        this.selectedRefSubAccountType = selectedRefSubAccountType;
    }

    public SimpleReference getSelectedRefSubAccountTypeVer() {
        return selectedRefSubAccountTypeVer;
    }

    public void setSelectedRefSubAccountTypeVer(SimpleReference selectedRefSubAccountTypeVer) {
        this.selectedRefSubAccountTypeVer = selectedRefSubAccountTypeVer;
    }

    public boolean isRefSubAccountTypeVisible() {
        return RefSubAccountTypeVisible;
    }

    public void setRefSubAccountTypeVisible(boolean refSubAccountTypeVisible) {
        RefSubAccountTypeVisible = refSubAccountTypeVisible;
    }

    public SimpleReference getRefSubAccountTypeItem() {
        return refSubAccountTypeItem;
    }

    public void setRefSubAccountTypeItem(SimpleReference refSubAccountTypeItem) {
        this.refSubAccountTypeItem = refSubAccountTypeItem;
    }

    public SimpleReference getFilterRefSubAccountTypeItem() {
        return filterRefSubAccountTypeItem;
    }

    public void setFilterRefSubAccountTypeItem(SimpleReference filterRefSubAccountTypeItem) {
        this.filterRefSubAccountTypeItem = filterRefSubAccountTypeItem;
    }

    public List<SimpleReference> getRefTypeHolderAccList() {
        return RefTypeHolderAccList;
    }

    public void setRefTypeHolderAccList(List<SimpleReference> refTypeHolderAccList) {
        RefTypeHolderAccList = refTypeHolderAccList;
    }

    public List<SimpleReference> getRefTypeHolderAccHstList() {
        return RefTypeHolderAccHstList;
    }

    public void setRefTypeHolderAccHstList(List<SimpleReference> refTypeHolderAccHstList) {
        RefTypeHolderAccHstList = refTypeHolderAccHstList;
    }

    public List<SimpleReference> getRefTypeHolderAccVerList() {
        return RefTypeHolderAccVerList;
    }

    public void setRefTypeHolderAccVerList(List<SimpleReference> refTypeHolderAccVerList) {
        RefTypeHolderAccVerList = refTypeHolderAccVerList;
    }

    public SimpleReference getSelectedRefTypeHolderAcc() {
        return selectedRefTypeHolderAcc;
    }

    public void setSelectedRefTypeHolderAcc(SimpleReference selectedRefTypeHolderAcc) {
        this.selectedRefTypeHolderAcc = selectedRefTypeHolderAcc;
    }

    public SimpleReference getSelectedRefTypeHolderAccVer() {
        return selectedRefTypeHolderAccVer;
    }

    public void setSelectedRefTypeHolderAccVer(SimpleReference selectedRefTypeHolderAccVer) {
        this.selectedRefTypeHolderAccVer = selectedRefTypeHolderAccVer;
    }

    public boolean isRefTypeHolderAccVisible() {
        return RefTypeHolderAccVisible;
    }

    public void setRefTypeHolderAccVisible(boolean refTypeHolderAccVisible) {
        RefTypeHolderAccVisible = refTypeHolderAccVisible;
    }

    public SimpleReference getRefTypeHolderAccItem() {
        return refTypeHolderAccItem;
    }

    public void setRefTypeHolderAccItem(SimpleReference refTypeHolderAccItem) {
        this.refTypeHolderAccItem = refTypeHolderAccItem;
    }

    public SimpleReference getFilterRefTypeHolderAccItem() {
        return filterRefTypeHolderAccItem;
    }

    public void setFilterRefTypeHolderAccItem(SimpleReference filterRefTypeHolderAccItem) {
        this.filterRefTypeHolderAccItem = filterRefTypeHolderAccItem;
    }

    public List<SimpleReference> getRefRequestFeatureList() {
        return RefRequestFeatureList;
    }

    public void setRefRequestFeatureList(List<SimpleReference> refRequestFeatureList) {
        RefRequestFeatureList = refRequestFeatureList;
    }

    public List<SimpleReference> getRefRequestFeatureHstList() {
        return RefRequestFeatureHstList;
    }

    public void setRefRequestFeatureHstList(List<SimpleReference> refRequestFeatureHstList) {
        RefRequestFeatureHstList = refRequestFeatureHstList;
    }

    public List<SimpleReference> getRefRequestFeatureVerList() {
        return RefRequestFeatureVerList;
    }

    public void setRefRequestFeatureVerList(List<SimpleReference> refRequestFeatureVerList) {
        RefRequestFeatureVerList = refRequestFeatureVerList;
    }

    public SimpleReference getSelectedRefRequestFeature() {
        return selectedRefRequestFeature;
    }

    public void setSelectedRefRequestFeature(SimpleReference selectedRefRequestFeature) {
        this.selectedRefRequestFeature = selectedRefRequestFeature;
    }

    public SimpleReference getSelectedRefRequestFeatureVer() {
        return selectedRefRequestFeatureVer;
    }

    public void setSelectedRefRequestFeatureVer(SimpleReference selectedRefRequestFeatureVer) {
        this.selectedRefRequestFeatureVer = selectedRefRequestFeatureVer;
    }

    public boolean isRefRequestFeatureVisible() {
        return RefRequestFeatureVisible;
    }

    public void setRefRequestFeatureVisible(boolean refRequestFeatureVisible) {
        RefRequestFeatureVisible = refRequestFeatureVisible;
    }

    public SimpleReference getRefRequestFeatureItem() {
        return refRequestFeatureItem;
    }

    public void setRefRequestFeatureItem(SimpleReference refRequestFeatureItem) {
        this.refRequestFeatureItem = refRequestFeatureItem;
    }

    public SimpleReference getFilterRefRequestFeatureItem() {
        return filterRefRequestFeatureItem;
    }

    public void setFilterRefRequestFeatureItem(SimpleReference filterRefRequestFeatureItem) {
        this.filterRefRequestFeatureItem = filterRefRequestFeatureItem;
    }

    public List<SimpleReference> getRefRequestStsList() {
        return RefRequestStsList;
    }

    public void setRefRequestStsList(List<SimpleReference> refRequestStsList) {
        RefRequestStsList = refRequestStsList;
    }

    public List<SimpleReference> getRefRequestStsHstList() {
        return RefRequestStsHstList;
    }

    public void setRefRequestStsHstList(List<SimpleReference> refRequestStsHstList) {
        RefRequestStsHstList = refRequestStsHstList;
    }

    public List<SimpleReference> getRefRequestStsVerList() {
        return RefRequestStsVerList;
    }

    public void setRefRequestStsVerList(List<SimpleReference> refRequestStsVerList) {
        RefRequestStsVerList = refRequestStsVerList;
    }

    public SimpleReference getSelectedRefRequestSts() {
        return selectedRefRequestSts;
    }

    public void setSelectedRefRequestSts(SimpleReference selectedRefRequestSts) {
        this.selectedRefRequestSts = selectedRefRequestSts;
    }

    public SimpleReference getSelectedRefRequestStsVer() {
        return selectedRefRequestStsVer;
    }

    public void setSelectedRefRequestStsVer(SimpleReference selectedRefRequestStsVer) {
        this.selectedRefRequestStsVer = selectedRefRequestStsVer;
    }

    public boolean isRefRequestStsVisible() {
        return RefRequestStsVisible;
    }

    public void setRefRequestStsVisible(boolean refRequestStsVisible) {
        RefRequestStsVisible = refRequestStsVisible;
    }

    public SimpleReference getRefRequestStsItem() {
        return refRequestStsItem;
    }

    public void setRefRequestStsItem(SimpleReference refRequestStsItem) {
        this.refRequestStsItem = refRequestStsItem;
    }

    public SimpleReference getFilterRefRequestStsItem() {
        return filterRefRequestStsItem;
    }

    public void setFilterRefRequestStsItem(SimpleReference filterRefRequestStsItem) {
        this.filterRefRequestStsItem = filterRefRequestStsItem;
    }

    public List<SimpleReference> getRefRepoKindList() {
        return RefRepoKindList;
    }

    public void setRefRepoKindList(List<SimpleReference> refRepoKindList) {
        RefRepoKindList = refRepoKindList;
    }

    public List<SimpleReference> getRefRepoKindHstList() {
        return RefRepoKindHstList;
    }

    public void setRefRepoKindHstList(List<SimpleReference> refRepoKindHstList) {
        RefRepoKindHstList = refRepoKindHstList;
    }

    public List<SimpleReference> getRefRepoKindVerList() {
        return RefRepoKindVerList;
    }

    public void setRefRepoKindVerList(List<SimpleReference> refRepoKindVerList) {
        RefRepoKindVerList = refRepoKindVerList;
    }

    public SimpleReference getSelectedRefRepoKind() {
        return selectedRefRepoKind;
    }

    public void setSelectedRefRepoKind(SimpleReference selectedRefRepoKind) {
        this.selectedRefRepoKind = selectedRefRepoKind;
    }

    public SimpleReference getSelectedRefRepoKindVer() {
        return selectedRefRepoKindVer;
    }

    public void setSelectedRefRepoKindVer(SimpleReference selectedRefRepoKindVer) {
        this.selectedRefRepoKindVer = selectedRefRepoKindVer;
    }

    public boolean isRefRepoKindVisible() {
        return RefRepoKindVisible;
    }

    public void setRefRepoKindVisible(boolean refRepoKindVisible) {
        RefRepoKindVisible = refRepoKindVisible;
    }

    public SimpleReference getRefRepoKindItem() {
        return refRepoKindItem;
    }

    public void setRefRepoKindItem(SimpleReference refRepoKindItem) {
        this.refRepoKindItem = refRepoKindItem;
    }

    public SimpleReference getFilterRefRepoKindItem() {
        return filterRefRepoKindItem;
    }

    public void setFilterRefRepoKindItem(SimpleReference filterRefRepoKindItem) {
        this.filterRefRepoKindItem = filterRefRepoKindItem;
    }

    public List<SimpleReference> getRefMarketTypeList() {
        return RefMarketTypeList;
    }

    public void setRefMarketTypeList(List<SimpleReference> refMarketTypeList) {
        RefMarketTypeList = refMarketTypeList;
    }

    public List<SimpleReference> getRefMarketTypeHstList() {
        return RefMarketTypeHstList;
    }

    public void setRefMarketTypeHstList(List<SimpleReference> refMarketTypeHstList) {
        RefMarketTypeHstList = refMarketTypeHstList;
    }

    public List<SimpleReference> getRefMarketTypeVerList() {
        return RefMarketTypeVerList;
    }

    public void setRefMarketTypeVerList(List<SimpleReference> refMarketTypeVerList) {
        RefMarketTypeVerList = refMarketTypeVerList;
    }

    public SimpleReference getSelectedRefMarketType() {
        return selectedRefMarketType;
    }

    public void setSelectedRefMarketType(SimpleReference selectedRefMarketType) {
        this.selectedRefMarketType = selectedRefMarketType;
    }

    public SimpleReference getSelectedRefMarketTypeVer() {
        return selectedRefMarketTypeVer;
    }

    public void setSelectedRefMarketTypeVer(SimpleReference selectedRefMarketTypeVer) {
        this.selectedRefMarketTypeVer = selectedRefMarketTypeVer;
    }

    public boolean isRefMarketTypeVisible() {
        return RefMarketTypeVisible;
    }

    public void setRefMarketTypeVisible(boolean refMarketTypeVisible) {
        RefMarketTypeVisible = refMarketTypeVisible;
    }

    public SimpleReference getRefMarketTypeItem() {
        return refMarketTypeItem;
    }

    public void setRefMarketTypeItem(SimpleReference refMarketTypeItem) {
        this.refMarketTypeItem = refMarketTypeItem;
    }

    public SimpleReference getFilterRefMarketTypeItem() {
        return filterRefMarketTypeItem;
    }

    public void setFilterRefMarketTypeItem(SimpleReference filterRefMarketTypeItem) {
        this.filterRefMarketTypeItem = filterRefMarketTypeItem;
    }

    public List<SimpleReference> getRefTradMethodList() {
        return RefTradMethodList;
    }

    public void setRefTradMethodList(List<SimpleReference> refTradMethodList) {
        RefTradMethodList = refTradMethodList;
    }

    public List<SimpleReference> getRefTradMethodHstList() {
        return RefTradMethodHstList;
    }

    public void setRefTradMethodHstList(List<SimpleReference> refTradMethodHstList) {
        RefTradMethodHstList = refTradMethodHstList;
    }

    public List<SimpleReference> getRefTradMethodVerList() {
        return RefTradMethodVerList;
    }

    public void setRefTradMethodVerList(List<SimpleReference> refTradMethodVerList) {
        RefTradMethodVerList = refTradMethodVerList;
    }

    public SimpleReference getSelectedRefTradMethod() {
        return selectedRefTradMethod;
    }

    public void setSelectedRefTradMethod(SimpleReference selectedRefTradMethod) {
        this.selectedRefTradMethod = selectedRefTradMethod;
    }

    public SimpleReference getSelectedRefTradMethodVer() {
        return selectedRefTradMethodVer;
    }

    public void setSelectedRefTradMethodVer(SimpleReference selectedRefTradMethodVer) {
        this.selectedRefTradMethodVer = selectedRefTradMethodVer;
    }

    public boolean isRefTradMethodVisible() {
        return RefTradMethodVisible;
    }

    public void setRefTradMethodVisible(boolean refTradMethodVisible) {
        RefTradMethodVisible = refTradMethodVisible;
    }

    public SimpleReference getRefTradMethodItem() {
        return refTradMethodItem;
    }

    public void setRefTradMethodItem(SimpleReference refTradMethodItem) {
        this.refTradMethodItem = refTradMethodItem;
    }

    public SimpleReference getFilterRefTradMethodItem() {
        return filterRefTradMethodItem;
    }

    public void setFilterRefTradMethodItem(SimpleReference filterRefTradMethodItem) {
        this.filterRefTradMethodItem = filterRefTradMethodItem;
    }

    public SimpleReference getFilterRefOperTypeItem() {
        return filterRefOperTypeItem;
    }

    public void setFilterRefOperTypeItem(SimpleReference filterRefOperTypeItem) {
        this.filterRefOperTypeItem = filterRefOperTypeItem;
    }

    public SimpleReference getRefOperTypeItem() {
        return refOperTypeItem;
    }

    public void setRefOperTypeItem(SimpleReference refOperTypeItem) {
        this.refOperTypeItem = refOperTypeItem;
    }

    public boolean isRefOperTypeVisible() {
        return RefOperTypeVisible;
    }

    public void setRefOperTypeVisible(boolean refOperTypeVisible) {
        RefOperTypeVisible = refOperTypeVisible;
    }

    public SimpleReference getSelectedRefOperTypeVer() {
        return selectedRefOperTypeVer;
    }

    public void setSelectedRefOperTypeVer(SimpleReference selectedRefOperTypeVer) {
        this.selectedRefOperTypeVer = selectedRefOperTypeVer;
    }

    public SimpleReference getSelectedRefOperType() {
        return selectedRefOperType;
    }

    public void setSelectedRefOperType(SimpleReference selectedRefOperType) {
        this.selectedRefOperType = selectedRefOperType;
    }

    public List<SimpleReference> getRefOperTypeVerList() {
        return RefOperTypeVerList;
    }

    public void setRefOperTypeVerList(List<SimpleReference> refOperTypeVerList) {
        RefOperTypeVerList = refOperTypeVerList;
    }

    public List<SimpleReference> getRefOperTypeHstList() {
        return RefOperTypeHstList;
    }

    public void setRefOperTypeHstList(List<SimpleReference> refOperTypeHstList) {
        RefOperTypeHstList = refOperTypeHstList;
    }

    public List<SimpleReference> getRefOperTypeList() {
        return RefOperTypeList;
    }

    public void setRefOperTypeList(List<SimpleReference> refOperTypeList) {
        RefOperTypeList = refOperTypeList;
    }

    public List<SimpleReference> getRefDealStsList() {
        return RefDealStsList;
    }

    public void setRefDealStsList(List<SimpleReference> refDealStsList) {
        RefDealStsList = refDealStsList;
    }

    public List<SimpleReference> getRefDealStsHstList() {
        return RefDealStsHstList;
    }

    public void setRefDealStsHstList(List<SimpleReference> refDealStsHstList) {
        RefDealStsHstList = refDealStsHstList;
    }

    public List<SimpleReference> getRefDealStsVerList() {
        return RefDealStsVerList;
    }

    public void setRefDealStsVerList(List<SimpleReference> refDealStsVerList) {
        RefDealStsVerList = refDealStsVerList;
    }

    public SimpleReference getSelectedRefDealSts() {
        return selectedRefDealSts;
    }

    public void setSelectedRefDealSts(SimpleReference selectedRefDealSts) {
        this.selectedRefDealSts = selectedRefDealSts;
    }

    public SimpleReference getSelectedRefDealStsVer() {
        return selectedRefDealStsVer;
    }

    public void setSelectedRefDealStsVer(SimpleReference selectedRefDealStsVer) {
        this.selectedRefDealStsVer = selectedRefDealStsVer;
    }

    public boolean isRefDealStsVisible() {
        return RefDealStsVisible;
    }

    public void setRefDealStsVisible(boolean refDealStsVisible) {
        RefDealStsVisible = refDealStsVisible;
    }

    public SimpleReference getRefDealStsItem() {
        return refDealStsItem;
    }

    public void setRefDealStsItem(SimpleReference refDealStsItem) {
        this.refDealStsItem = refDealStsItem;
    }

    public SimpleReference getFilterRefDealStsItem() {
        return filterRefDealStsItem;
    }

    public void setFilterRefDealStsItem(SimpleReference filterRefDealStsItem) {
        this.filterRefDealStsItem = filterRefDealStsItem;
    }*/

    public RefItem getFilterRefMainItem() {
        return filterRefMainItem;
    }

    public void setFilterRefMainItem(RefItem filterRefMainItem) {
        this.filterRefMainItem = filterRefMainItem;
    }

    /*public Set<RefItem> getRefContainers() {
        return refContainers;
    }

    public void setRefContainers(Set<RefItem> refContainers) {
        this.refContainers = refContainers;
    }*/

    public List<Result> getResultList() {
        return resultList;
    }

    public List<RefMrpItem> getRefMrpList() {
        return RefMrpList;
    }

    public void setRefMrpList(List<RefMrpItem> refMrpList) {
        RefMrpList = refMrpList;
    }

    public List<RefMrpItem> getRefMrpHstList() {
        return RefMrpHstList;
    }

    public void setRefMrpHstList(List<RefMrpItem> refMrpHstList) {
        RefMrpHstList = refMrpHstList;
    }

    public List<RefMrpItem> getRefMrpVerList() {
        return RefMrpVerList;
    }

    public void setRefMrpVerList(List<RefMrpItem> refMrpVerList) {
        RefMrpVerList = refMrpVerList;
    }

    public RefMrpItem getSelectedRefMrp() {
        return selectedRefMrp;
    }

    public void setSelectedRefMrp(RefMrpItem selectedRefMrp) {
        this.selectedRefMrp = selectedRefMrp;
    }

    public RefMrpItem getSelectedRefMrpVer() {
        return selectedRefMrpVer;
    }

    public void setSelectedRefMrpVer(RefMrpItem selectedRefMrpVer) {
        this.selectedRefMrpVer = selectedRefMrpVer;
    }

    public boolean isRefMrpVisible() {
        return RefMrpVisible;
    }

    public void setRefMrpVisible(boolean refMrpVisible) {
        RefMrpVisible = refMrpVisible;
    }

    public RefMrpItem getRefMrpItem() {
        return refMrpItem;
    }

    public void setRefMrpItem(RefMrpItem refMrpItem) {
        this.refMrpItem = refMrpItem;
    }

    public List<RefElements> getTypeMrpList() {
        return typeMrpList;
    }

    public void setTypeMrpList(List<RefElements> typeMrpList) {
        this.typeMrpList = typeMrpList;
    }

    public RefMrpItem getFilterRefMrpItem() {
        return filterRefMrpItem;
    }

    public void setFilterRefMrpItem(RefMrpItem filterRefMrpItem) {
        this.filterRefMrpItem = filterRefMrpItem;
    }


    public String getIdnForSearch() {
        return idnForSearch;
    }

    public String getNameForSearch() {
        return nameForSearch;
    }

    public RefLegalPersonItem getSelectedSearchRefLegalPerson() {
        return selectedSearchRefLegalPerson;
    }

    public void setSelectedSearchRefLegalPerson(RefLegalPersonItem selectedSearchRefLegalPerson) {
        this.selectedSearchRefLegalPerson = selectedSearchRefLegalPerson;
    }

    public void setIdnForSearch(String idnForSearch) {
        this.idnForSearch = idnForSearch;
    }

    public void setNameForSearch(String nameForSearch) {
        this.nameForSearch = nameForSearch;
    }

    public List<RefLegalPersonItem> getRefLPForSearchList() {
        return RefLPForSearchList;
    }

    public void setRefLPForSearchList(List<RefLegalPersonItem> refLPForSearchList) {
        RefLPForSearchList = refLPForSearchList;
    }

    public List<RefBankItem> getRefBankHstList() {
        return RefBankHstList;
    }

    public void setRefBankHstList(List<RefBankItem> refBankHstList) {
        RefBankHstList = refBankHstList;
    }

    public List<SimpleReference> getDeptTypeList() {
        return deptTypeList;
    }

    public List<SimpleReference> getRefOrgTypeList() {
        return RefOrgTypeList;
    }

    public void setRefOrgTypeList(List<SimpleReference> refOrgTypeList) {
        RefOrgTypeList = refOrgTypeList;
    }

    public List<SimpleReference> getRefOrgTypeHstList() {
        return RefOrgTypeHstList;
    }

    public void setRefOrgTypeHstList(List<SimpleReference> refOrgTypeHstList) {
        RefOrgTypeHstList = refOrgTypeHstList;
    }

    public List<SimpleReference> getRefOrgTypeVerList() {
        return RefOrgTypeVerList;
    }

    public void setRefOrgTypeVerList(List<SimpleReference> refOrgTypeVerList) {
        RefOrgTypeVerList = refOrgTypeVerList;
    }

    public SimpleReference getSelectedRefOrgType() {
        return selectedRefOrgType;
    }

    public void setSelectedRefOrgType(SimpleReference selectedRefOrgType) {
        this.selectedRefOrgType = selectedRefOrgType;
    }

    public SimpleReference getSelectedRefOrgTypeVer() {
        return selectedRefOrgTypeVer;
    }

    public void setSelectedRefOrgTypeVer(SimpleReference selectedRefOrgTypeVer) {
        this.selectedRefOrgTypeVer = selectedRefOrgTypeVer;
    }

    public boolean isRefOrgTypeVisible() {
        return RefOrgTypeVisible;
    }

    public void setRefOrgTypeVisible(boolean refOrgTypeVisible) {
        RefOrgTypeVisible = refOrgTypeVisible;
    }

    public SimpleReference getRefOrgTypeItem() {
        return refOrgTypeItem;
    }

    public void setRefOrgTypeItem(SimpleReference refOrgTypeItem) {
        this.refOrgTypeItem = refOrgTypeItem;
    }

    public SimpleReference getFilterRefOrgTypeItem() {
        return filterRefOrgTypeItem;
    }

    public void setFilterRefOrgTypeItem(SimpleReference filterRefOrgTypeItem) {
        this.filterRefOrgTypeItem = filterRefOrgTypeItem;
    }

    public boolean isSearchAllVer() {
        return searchAllVer;
    }

    public void setSearchAllVer(boolean searchAllVer) {
        this.searchAllVer = searchAllVer;
    }

    public List<RefMfoRegItem> getRefMfoRegList() {
        return RefMfoRegList;
    }

    public void setRefMfoRegList(List<RefMfoRegItem> refMfoRegList) {
        RefMfoRegList = refMfoRegList;
    }

    public List<RefMfoRegItem> getRefMfoRegHstList() {
        return RefMfoRegHstList;
    }

    public void setRefMfoRegHstList(List<RefMfoRegItem> refMfoRegHstList) {
        RefMfoRegHstList = refMfoRegHstList;
    }

    public List<RefMfoRegItem> getRefMfoRegVerList() {
        return RefMfoRegVerList;
    }

    public void setRefMfoRegVerList(List<RefMfoRegItem> refMfoRegVerList) {
        RefMfoRegVerList = refMfoRegVerList;
    }

    public RefMfoRegItem getSelectedRefMfoReg() {
        return selectedRefMfoReg;
    }

    public void setSelectedRefMfoReg(RefMfoRegItem selectedRefMfoReg) {
        this.selectedRefMfoReg = selectedRefMfoReg;
    }

    public RefMfoRegItem getSelectedRefMfoRegVer() {
        return selectedRefMfoRegVer;
    }

    public void setSelectedRefMfoRegVer(RefMfoRegItem selectedRefMfoRegVer) {
        this.selectedRefMfoRegVer = selectedRefMfoRegVer;
    }

    public boolean isRefMfoRegVisible() {
        return RefMfoRegVisible;
    }

    public void setRefMfoRegVisible(boolean refMfoRegVisible) {
        RefMfoRegVisible = refMfoRegVisible;
    }

    public RefMfoRegItem getRefMfoRegItem() {
        return refMfoRegItem;
    }

    public void setRefMfoRegItem(RefMfoRegItem refMfoRegItem) {
        this.refMfoRegItem = refMfoRegItem;
    }

    public RefMfoRegItem getFilterRefMfoRegItem() {
        return filterRefMfoRegItem;
    }

    public void setFilterRefMfoRegItem(RefMfoRegItem filterRefMfoRegItem) {
        this.filterRefMfoRegItem = filterRefMfoRegItem;
    }

    public List<RefElements> getRefKndList() {
        return refKndList;
    }

    public void setRefKndList(List<RefElements> refKndList) {
        this.refKndList = refKndList;
    }

    public List<RefDealBAItem> getRefDealBAList() {
        return RefDealBAList;
    }

    public void setRefDealBAList(List<RefDealBAItem> refDealBAList) {
        RefDealBAList = refDealBAList;
    }

    public List<RefDealBAItem> getRefDealBAHstList() {
        return RefDealBAHstList;
    }

    public void setRefDealBAHstList(List<RefDealBAItem> refDealBAHstList) {
        RefDealBAHstList = refDealBAHstList;
    }

    public List<RefDealBAItem> getRefDealBAVerList() {
        return RefDealBAVerList;
    }

    public void setRefDealBAVerList(List<RefDealBAItem> refDealBAVerList) {
        RefDealBAVerList = refDealBAVerList;
    }

    public RefDealBAItem getSelectedRefDealBA() {
        return selectedRefDealBA;
    }

    public void setSelectedRefDealBA(RefDealBAItem selectedRefDealBA) {
        this.selectedRefDealBA = selectedRefDealBA;
    }

    public RefDealBAItem getSelectedRefDealBAVer() {
        return selectedRefDealBAVer;
    }

    public void setSelectedRefDealBAVer(RefDealBAItem selectedRefDealBAVer) {
        this.selectedRefDealBAVer = selectedRefDealBAVer;
    }

    public boolean isRefDealBAVisible() {
        return RefDealBAVisible;
    }

    public void setRefDealBAVisible(boolean refDealBAVisible) {
        RefDealBAVisible = refDealBAVisible;
    }

    public RefDealBAItem getRefDealBAItem() {
        return refDealBAItem;
    }

    public void setRefDealBAItem(RefDealBAItem refDealBAItem) {
        this.refDealBAItem = refDealBAItem;
    }

    public RefDealBAItem getFilterRefDealBAItem() {
        return filterRefDealBAItem;
    }

    public void setFilterRefDealBAItem(RefDealBAItem filterRefDealBAItem) {
        this.filterRefDealBAItem = filterRefDealBAItem;
    }

    public LazyDataModel<RefSecuritiesItem> getLdmRefSecListItem() {
        return ldmRefSecListItem;
    }

    public void setLdmRefSecListItem(LazyDataModel<RefSecuritiesItem> ldmRefSecListItem) {
        this.ldmRefSecListItem = ldmRefSecListItem;
    }

    public LazyDataModel<RefIssuersItem> getLdmRefIssuerListItem() {
        return ldmRefIssuerListItem;
    }

    public void setLdmRefIssuerListItem(LazyDataModel<RefIssuersItem> ldmRefIssuerListItem) {
        this.ldmRefIssuerListItem = ldmRefIssuerListItem;
    }

    public List<RefTypeActivityItem> getRefTypeActivityList() {
        return RefTypeActivityList;
    }

    public void setRefTypeActivityList(List<RefTypeActivityItem> refTypeActivityList) {
        RefTypeActivityList = refTypeActivityList;
    }

    public List<RefTypeActivityItem> getRefTypeActivityHstList() {
        return RefTypeActivityHstList;
    }

    public void setRefTypeActivityHstList(List<RefTypeActivityItem> refTypeActivityHstList) {
        RefTypeActivityHstList = refTypeActivityHstList;
    }

    public List<RefTypeActivityItem> getRefTypeActivityVerList() {
        return RefTypeActivityVerList;
    }

    public void setRefTypeActivityVerList(List<RefTypeActivityItem> refTypeActivityVerList) {
        RefTypeActivityVerList = refTypeActivityVerList;
    }

    public RefTypeActivityItem getSelectedRefTypeActivity() {
        return selectedRefTypeActivity;
    }

    public void setSelectedRefTypeActivity(RefTypeActivityItem selectedRefTypeActivity) {
        this.selectedRefTypeActivity = selectedRefTypeActivity;
    }

    public RefTypeActivityItem getSelectedRefTypeActivityVer() {
        return selectedRefTypeActivityVer;
    }

    public void setSelectedRefTypeActivityVer(RefTypeActivityItem selectedRefTypeActivityVer) {
        this.selectedRefTypeActivityVer = selectedRefTypeActivityVer;
    }

    public boolean isRefTypeActivityVisible() {
        return RefTypeActivityVisible;
    }

    public void setRefTypeActivityVisible(boolean refTypeActivityVisible) {
        RefTypeActivityVisible = refTypeActivityVisible;
    }

    public RefTypeActivityItem getRefTypeActivityItem() {
        return refTypeActivityItem;
    }

    public void setRefTypeActivityItem(RefTypeActivityItem refTypeActivityItem) {
        this.refTypeActivityItem = refTypeActivityItem;
    }

    public RefTypeActivityItem getFilterRefTypeActivityItem() {
        return filterRefTypeActivityItem;
    }

    public void setFilterRefTypeActivityItem(RefTypeActivityItem filterRefTypeActivityItem) {
        this.filterRefTypeActivityItem = filterRefTypeActivityItem;
    }

    public List<RefNpaItem> getRefNpaList() {
        return RefNpaList;
    }

    public void setRefNpaList(List<RefNpaItem> refNpaList) {
        RefNpaList = refNpaList;
    }

    public List<RefNpaItem> getRefNpaHstList() {
        return RefNpaHstList;
    }

    public void setRefNpaHstList(List<RefNpaItem> refNpaHstList) {
        RefNpaHstList = refNpaHstList;
    }

    public List<RefNpaItem> getRefNpaVerList() {
        return RefNpaVerList;
    }

    public void setRefNpaVerList(List<RefNpaItem> refNpaVerList) {
        RefNpaVerList = refNpaVerList;
    }

    public RefNpaItem getSelectedRefNpa() {
        return selectedRefNpa;
    }

    public void setSelectedRefNpa(RefNpaItem selectedRefNpa) {
        this.selectedRefNpa = selectedRefNpa;
    }

    public RefNpaItem getSelectedRefNpaVer() {
        return selectedRefNpaVer;
    }

    public void setSelectedRefNpaVer(RefNpaItem selectedRefNpaVer) {
        this.selectedRefNpaVer = selectedRefNpaVer;
    }

    public boolean isRefNpaVisible() {
        return RefNpaVisible;
    }

    public void setRefNpaVisible(boolean refNpaVisible) {
        RefNpaVisible = refNpaVisible;
    }

    public RefNpaItem getRefNpaItem() {
        return refNpaItem;
    }

    public void setRefNpaItem(RefNpaItem refNpaItem) {
        this.refNpaItem = refNpaItem;
    }

    public RefNpaItem getFilterRefNpaItem() {
        return filterRefNpaItem;
    }

    public void setFilterRefNpaItem(RefNpaItem filterRefNpaItem) {
        this.filterRefNpaItem = filterRefNpaItem;
    }

    public List<RefWkdHolidayItem> getRefWkdHolidayList() {
        return RefWkdHolidayList;
    }

    public void setRefWkdHolidayList(List<RefWkdHolidayItem> refWkdHolidayList) {
        RefWkdHolidayList = refWkdHolidayList;
    }

    public List<RefWkdHolidayItem> getRefWkdHolidayHstList() {
        return RefWkdHolidayHstList;
    }

    public void setRefWkdHolidayHstList(List<RefWkdHolidayItem> refWkdHolidayHstList) {
        RefWkdHolidayHstList = refWkdHolidayHstList;
    }

    public RefWkdHolidayItem getSelectedRefWkdHoliday() {
        return selectedRefWkdHoliday;
    }

    public void setSelectedRefWkdHoliday(RefWkdHolidayItem selectedRefWkdHoliday) {
        this.selectedRefWkdHoliday = selectedRefWkdHoliday;
    }

    public List<RefWkdHolidayItem> getRefWkdHolidayVerList() {
        return RefWkdHolidayVerList;
    }

    public void setRefWkdHolidayVerList(List<RefWkdHolidayItem> refWkdHolidayVerList) {
        RefWkdHolidayVerList = refWkdHolidayVerList;
    }

    public RefWkdHolidayItem getSelectedRefWkdHolidayVer() {
        return selectedRefWkdHolidayVer;
    }

    public void setSelectedRefWkdHolidayVer(RefWkdHolidayItem selectedRefWkdHolidayVer) {
        this.selectedRefWkdHolidayVer = selectedRefWkdHolidayVer;
    }

    public boolean isRefWkdHolidayVisible() {
        return RefWkdHolidayVisible;
    }

    public void setRefWkdHolidayVisible(boolean refWkdHolidayVisible) {
        RefWkdHolidayVisible = refWkdHolidayVisible;
    }

    public RefWkdHolidayItem getRefWkdHolidayItem() {
        return refWkdHolidayItem;
    }

    public void setRefWkdHolidayItem(RefWkdHolidayItem refWkdHolidayItem) {
        this.refWkdHolidayItem = refWkdHolidayItem;
    }

    public RefWkdHolidayItem getFilterRefWkdHolidayItem() {
        return filterRefWkdHolidayItem;
    }

    public void setFilterRefWkdHolidayItem(RefWkdHolidayItem filterRefWkdHolidayItem) {
        this.filterRefWkdHolidayItem = filterRefWkdHolidayItem;
    }

    public List<HolidayItem> getRefHolidayList() {
        return RefHolidayList;
    }

    public void setRefHolidayList(List<HolidayItem> refHolidayList) {
        RefHolidayList = refHolidayList;
    }

    public HolidayItem getSelectedRefHoliday() {
        return selectedRefHoliday;
    }

    public void setSelectedRefHoliday(HolidayItem selectedRefHoliday) {
        this.selectedRefHoliday = selectedRefHoliday;
    }

    public List<HolidayItem> getRefHolidayVerList() {
        return RefHolidayVerList;
    }

    public void setRefHolidayVerList(List<HolidayItem> refHolidayVerList) {
        RefHolidayVerList = refHolidayVerList;
    }

    public HolidayItem getSelectedRefHolidayVer() {
        return selectedRefHolidayVer;
    }

    public void setSelectedRefHolidayVer(HolidayItem selectedRefHolidayVer) {
        this.selectedRefHolidayVer = selectedRefHolidayVer;
    }

    public HolidayItem getHolidayItem() {
        return holidayItem;
    }

    public void setHolidayItem(HolidayItem holidayItem) {
        this.holidayItem = holidayItem;
    }

    public HolidayItem getFilterRefHolidayItem() {
        return filterRefHolidayItem;
    }

    public void setFilterRefHolidayItem(HolidayItem filterRefHolidayItem) {
        this.filterRefHolidayItem = filterRefHolidayItem;
    }

    public Date getFilterDateForFillRef() {
        return filterDateForFillRef;
    }

    public void setFilterDateForFillRef(Date filterDateForFillRef) {
        this.filterDateForFillRef = filterDateForFillRef;
    }

    public List<SimpleReference> getRefFinRepIndicList() {
        return RefFinRepIndicList;
    }

    public void setRefFinRepIndicList(List<SimpleReference> refFinRepIndicList) {
        RefFinRepIndicList = refFinRepIndicList;
    }

    public List<SimpleReference> getRefFinRepIndicHstList() {
        return RefFinRepIndicHstList;
    }

    public void setRefFinRepIndicHstList(List<SimpleReference> refFinRepIndicHstList) {
        RefFinRepIndicHstList = refFinRepIndicHstList;
    }

    public List<SimpleReference> getRefFinRepIndicVerList() {
        return RefFinRepIndicVerList;
    }

    public void setRefFinRepIndicVerList(List<SimpleReference> refFinRepIndicVerList) {
        RefFinRepIndicVerList = refFinRepIndicVerList;
    }

    public SimpleReference getSelectedRefFinRepIndic() {
        return selectedRefFinRepIndic;
    }

    public void setSelectedRefFinRepIndic(SimpleReference selectedRefFinRepIndic) {
        this.selectedRefFinRepIndic = selectedRefFinRepIndic;
    }

    public SimpleReference getSelectedRefFinRepIndicVer() {
        return selectedRefFinRepIndicVer;
    }

    public void setSelectedRefFinRepIndicVer(SimpleReference selectedRefFinRepIndicVer) {
        this.selectedRefFinRepIndicVer = selectedRefFinRepIndicVer;
    }

    public boolean isRefFinRepIndicVisible() {
        return RefFinRepIndicVisible;
    }

    public void setRefFinRepIndicVisible(boolean refFinRepIndicVisible) {
        RefFinRepIndicVisible = refFinRepIndicVisible;
    }

    public SimpleReference getRefFinRepIndicItem() {
        return refFinRepIndicItem;
    }

    public void setRefFinRepIndicItem(SimpleReference refFinRepIndicItem) {
        this.refFinRepIndicItem = refFinRepIndicItem;
    }

    public SimpleReference getFilterRefFinRepIndicItem() {
        return filterRefFinRepIndicItem;
    }

    public void setFilterRefFinRepIndicItem(SimpleReference filterRefFinRepIndicItem) {
        this.filterRefFinRepIndicItem = filterRefFinRepIndicItem;
    }

    public List<SimpleReference> getRefTypeDealsList() {
        return RefTypeDealsList;
    }

    public void setRefTypeDealsList(List<SimpleReference> refTypeDealsList) {
        RefTypeDealsList = refTypeDealsList;
    }

    public List<SimpleReference> getRefTypeDealsHstList() {
        return RefTypeDealsHstList;
    }

    public void setRefTypeDealsHstList(List<SimpleReference> refTypeDealsHstList) {
        RefTypeDealsHstList = refTypeDealsHstList;
    }

    public List<SimpleReference> getRefTypeDealsVerList() {
        return RefTypeDealsVerList;
    }

    public void setRefTypeDealsVerList(List<SimpleReference> refTypeDealsVerList) {
        RefTypeDealsVerList = refTypeDealsVerList;
    }

    public SimpleReference getSelectedRefTypeDeals() {
        return selectedRefTypeDeals;
    }

    public void setSelectedRefTypeDeals(SimpleReference selectedRefTypeDeals) {
        this.selectedRefTypeDeals = selectedRefTypeDeals;
    }

    public SimpleReference getSelectedRefTypeDealsVer() {
        return selectedRefTypeDealsVer;
    }

    public void setSelectedRefTypeDealsVer(SimpleReference selectedRefTypeDealsVer) {
        this.selectedRefTypeDealsVer = selectedRefTypeDealsVer;
    }

    public boolean isRefTypeDealsVisible() {
        return RefTypeDealsVisible;
    }

    public void setRefTypeDealsVisible(boolean refTypeDealsVisible) {
        RefTypeDealsVisible = refTypeDealsVisible;
    }

    public SimpleReference getRefTypeDealsItem() {
        return refTypeDealsItem;
    }

    public void setRefTypeDealsItem(SimpleReference refTypeDealsItem) {
        this.refTypeDealsItem = refTypeDealsItem;
    }

    public SimpleReference getFilterRefTypeDealsItem() {
        return filterRefTypeDealsItem;
    }

    public void setFilterRefTypeDealsItem(SimpleReference filterRefTypeDealsItem) {
        this.filterRefTypeDealsItem = filterRefTypeDealsItem;
    }

    public List<SimpleReference> getRefDegreeRelationList() {
        return RefDegreeRelationList;
    }

    public void setRefDegreeRelationList(List<SimpleReference> refDegreeRelationList) {
        RefDegreeRelationList = refDegreeRelationList;
    }

    public List<SimpleReference> getRefDegreeRelationHstList() {
        return RefDegreeRelationHstList;
    }

    public void setRefDegreeRelationHstList(List<SimpleReference> refDegreeRelationHstList) {
        RefDegreeRelationHstList = refDegreeRelationHstList;
    }

    public List<SimpleReference> getRefDegreeRelationVerList() {
        return RefDegreeRelationVerList;
    }

    public void setRefDegreeRelationVerList(List<SimpleReference> refDegreeRelationVerList) {
        RefDegreeRelationVerList = refDegreeRelationVerList;
    }

    public SimpleReference getSelectedRefDegreeRelation() {
        return selectedRefDegreeRelation;
    }

    public void setSelectedRefDegreeRelation(SimpleReference selectedRefDegreeRelation) {
        this.selectedRefDegreeRelation = selectedRefDegreeRelation;
    }

    public SimpleReference getSelectedRefDegreeRelationVer() {
        return selectedRefDegreeRelationVer;
    }

    public void setSelectedRefDegreeRelationVer(SimpleReference selectedRefDegreeRelationVer) {
        this.selectedRefDegreeRelationVer = selectedRefDegreeRelationVer;
    }

    public boolean isRefDegreeRelationVisible() {
        return RefDegreeRelationVisible;
    }

    public void setRefDegreeRelationVisible(boolean refDegreeRelationVisible) {
        RefDegreeRelationVisible = refDegreeRelationVisible;
    }

    public SimpleReference getRefDegreeRelationItem() {
        return refDegreeRelationItem;
    }

    public void setRefDegreeRelationItem(SimpleReference refDegreeRelationItem) {
        this.refDegreeRelationItem = refDegreeRelationItem;
    }

    public SimpleReference getFilterRefDegreeRelationItem() {
        return filterRefDegreeRelationItem;
    }

    public void setFilterRefDegreeRelationItem(SimpleReference filterRefDegreeRelationItem) {
        this.filterRefDegreeRelationItem = filterRefDegreeRelationItem;
    }

    public List<SimpleReference> getRefSignRelatedList() {
        return RefSignRelatedList;
    }

    public void setRefSignRelatedList(List<SimpleReference> refSignRelatedList) {
        RefSignRelatedList = refSignRelatedList;
    }

    public List<SimpleReference> getRefSignRelatedHstList() {
        return RefSignRelatedHstList;
    }

    public void setRefSignRelatedHstList(List<SimpleReference> refSignRelatedHstList) {
        RefSignRelatedHstList = refSignRelatedHstList;
    }

    public List<SimpleReference> getRefSignRelatedVerList() {
        return RefSignRelatedVerList;
    }

    public void setRefSignRelatedVerList(List<SimpleReference> refSignRelatedVerList) {
        RefSignRelatedVerList = refSignRelatedVerList;
    }

    public SimpleReference getSelectedRefSignRelated() {
        return selectedRefSignRelated;
    }

    public void setSelectedRefSignRelated(SimpleReference selectedRefSignRelated) {
        this.selectedRefSignRelated = selectedRefSignRelated;
    }

    public SimpleReference getSelectedRefSignRelatedVer() {
        return selectedRefSignRelatedVer;
    }

    public void setSelectedRefSignRelatedVer(SimpleReference selectedRefSignRelatedVer) {
        this.selectedRefSignRelatedVer = selectedRefSignRelatedVer;
    }

    public boolean isRefSignRelatedVisible() {
        return RefSignRelatedVisible;
    }

    public void setRefSignRelatedVisible(boolean refSignRelatedVisible) {
        RefSignRelatedVisible = refSignRelatedVisible;
    }

    public SimpleReference getRefSignRelatedItem() {
        return refSignRelatedItem;
    }

    public void setRefSignRelatedItem(SimpleReference refSignRelatedItem) {
        this.refSignRelatedItem = refSignRelatedItem;
    }

    public SimpleReference getFilterRefSignRelatedItem() {
        return filterRefSignRelatedItem;
    }

    public void setFilterRefSignRelatedItem(SimpleReference filterRefSignRelatedItem) {
        this.filterRefSignRelatedItem = filterRefSignRelatedItem;
    }

    public List<SimpleReference> getRefTypeRiskList() {
        return RefTypeRiskList;
    }

    public void setRefTypeRiskList(List<SimpleReference> refTypeRiskList) {
        RefTypeRiskList = refTypeRiskList;
    }

    public List<SimpleReference> getRefTypeRiskHstList() {
        return RefTypeRiskHstList;
    }

    public void setRefTypeRiskHstList(List<SimpleReference> refTypeRiskHstList) {
        RefTypeRiskHstList = refTypeRiskHstList;
    }

    public List<SimpleReference> getRefTypeRiskVerList() {
        return RefTypeRiskVerList;
    }

    public void setRefTypeRiskVerList(List<SimpleReference> refTypeRiskVerList) {
        RefTypeRiskVerList = refTypeRiskVerList;
    }

    public SimpleReference getSelectedRefTypeRisk() {
        return selectedRefTypeRisk;
    }

    public void setSelectedRefTypeRisk(SimpleReference selectedRefTypeRisk) {
        this.selectedRefTypeRisk = selectedRefTypeRisk;
    }

    public SimpleReference getSelectedRefTypeRiskVer() {
        return selectedRefTypeRiskVer;
    }

    public void setSelectedRefTypeRiskVer(SimpleReference selectedRefTypeRiskVer) {
        this.selectedRefTypeRiskVer = selectedRefTypeRiskVer;
    }

    public boolean isRefTypeRiskVisible() {
        return RefTypeRiskVisible;
    }

    public void setRefTypeRiskVisible(boolean refTypeRiskVisible) {
        RefTypeRiskVisible = refTypeRiskVisible;
    }

    public SimpleReference getRefTypeRiskItem() {
        return refTypeRiskItem;
    }

    public void setRefTypeRiskItem(SimpleReference refTypeRiskItem) {
        this.refTypeRiskItem = refTypeRiskItem;
    }

    public SimpleReference getFilterRefTypeRiskItem() {
        return filterRefTypeRiskItem;
    }

    public void setFilterRefTypeRiskItem(SimpleReference filterRefTypeRiskItem) {
        this.filterRefTypeRiskItem = filterRefTypeRiskItem;
    }

    public List<RefBasisofControlItem> getRefBasisofControlList() {
        return RefBasisofControlList;
    }

    public void setRefBasisofControlList(List<RefBasisofControlItem> refBasisofControlList) {
        RefBasisofControlList = refBasisofControlList;
    }

    public List<RefBasisofControlItem> getRefBasisofControlHstList() {
        return RefBasisofControlHstList;
    }

    public void setRefBasisofControlHstList(List<RefBasisofControlItem> refBasisofControlHstList) {
        RefBasisofControlHstList = refBasisofControlHstList;
    }

    public List<RefBasisofControlItem> getRefBasisofControlVerList() {
        return RefBasisofControlVerList;
    }

    public void setRefBasisofControlVerList(List<RefBasisofControlItem> refBasisofControlVerList) {
        RefBasisofControlVerList = refBasisofControlVerList;
    }

    public RefBasisofControlItem getSelectedRefBasisofControl() {
        return selectedRefBasisofControl;
    }

    public void setSelectedRefBasisofControl(RefBasisofControlItem selectedRefBasisofControl) {
        this.selectedRefBasisofControl = selectedRefBasisofControl;
    }

    public RefBasisofControlItem getSelectedRefBasisofControlVer() {
        return selectedRefBasisofControlVer;
    }

    public void setSelectedRefBasisofControlVer(RefBasisofControlItem selectedRefBasisofControlVer) {
        this.selectedRefBasisofControlVer = selectedRefBasisofControlVer;
    }

    public boolean isRefBasisofControlVisible() {
        return RefBasisofControlVisible;
    }

    public void setRefBasisofControlVisible(boolean refBasisofControlVisible) {
        RefBasisofControlVisible = refBasisofControlVisible;
    }

    public RefBasisofControlItem getRefBasisofControlItem() {
        return refBasisofControlItem;
    }

    public void setRefBasisofControlItem(RefBasisofControlItem refBasisofControlItem) {
        this.refBasisofControlItem = refBasisofControlItem;
    }

    public RefBasisofControlItem getFilterRefBasisofControlItem() {
        return filterRefBasisofControlItem;
    }

    public void setFilterRefBasisofControlItem(RefBasisofControlItem filterRefBasisofControlItem) {
        this.filterRefBasisofControlItem = filterRefBasisofControlItem;
    }

    public SimpleReference getFilterRefBranchOldItem() {
        return filterRefBranchOldItem;
    }

    public void setFilterRefBranchOldItem(SimpleReference filterRefBranchOldItem) {
        this.filterRefBranchOldItem = filterRefBranchOldItem;
    }

    public List<SimpleReference> getRefBranchOldList() {
        return RefBranchOldList;
    }

    public void setRefBranchOldList(List<SimpleReference> refBranchOldList) {
        RefBranchOldList = refBranchOldList;
    }

    public List<SimpleReference> getRefBranchOldVerList() {
        return RefBranchOldVerList;
    }

    public void setRefBranchOldVerList(List<SimpleReference> refBranchOldVerList) {
        RefBranchOldVerList = refBranchOldVerList;
    }

    public SimpleReference getSelectedRefBranchOld() {
        return selectedRefBranchOld;
    }

    public void setSelectedRefBranchOld(SimpleReference selectedRefBranchOld) {
        this.selectedRefBranchOld = selectedRefBranchOld;
    }

    public SimpleReference getSelectedRefBranchOldVer() {
        return selectedRefBranchOldVer;
    }

    public void setSelectedRefBranchOldVer(SimpleReference selectedRefBranchOldVer) {
        this.selectedRefBranchOldVer = selectedRefBranchOldVer;
    }

    public boolean isRefBranchOldVisible() {
        return RefBranchOldVisible;
    }

    public void setRefBranchOldVisible(boolean refBranchOldVisible) {
        RefBranchOldVisible = refBranchOldVisible;
    }

    public SimpleReference getRefBranchOldItem() {
        return refBranchOldItem;
    }

    public void setRefBranchOldItem(SimpleReference refBranchOldItem) {
        this.refBranchOldItem = refBranchOldItem;
    }

    public Boolean getIsVisibleBankLp() { return isVisibleBankLp;  }

    public List<SubjectTypePost> getSubjectTypePostList() {
        return subjectTypePostList;
    }

    public List<RespondentWarrant> getRespondentWarrantList() {
        return respondentWarrantList;
    }

    public void setIsVisibleBankLp(Boolean isVisibleBankLp) { this.isVisibleBankLp = isVisibleBankLp;}

    public RespondentWarrant getSelectedRespWarrant() {
        return selectedRespWarrant;
    }

    public void setSelectedRespWarrant(RespondentWarrant selectedRespWarrant) {
        this.selectedRespWarrant = selectedRespWarrant;
    }

    public List<RefRespondentItem> getRespondentResultList() {
        return respondentResultList;
    }

    public RefRespondentItem getSelectedResultResp() {
        return selectedResultResp;
    }

    public void setSelectedResultResp(RefRespondentItem selectedResultResp) {
        this.selectedResultResp = selectedResultResp;
    }

    public List<Image> getImageList() {
        return imageList;
    }

    public void setImageList(List<Image> imageList) {
        this.imageList = imageList;
    }

    public Image getCurImage() {
        return curImage;
    }

    public void setCurImage(Image curImage) {
        this.curImage = curImage;
    }

    public List<RefInsurOrgItem> getRefInsurOrgList() {
        return RefInsurOrgList;
    }

    public void setRefInsurOrgList(List<RefInsurOrgItem> refInsurOrgList) {
        RefInsurOrgList = refInsurOrgList;
    }

    public RefInsurOrgItem getSelectedRefInsurOrg() {
        return selectedRefInsurOrg;
    }

    public void setSelectedRefInsurOrg(RefInsurOrgItem selectedRefInsurOrg) {
        this.selectedRefInsurOrg = selectedRefInsurOrg;
    }

    public RefInsurOrgItem getSelectedRefInsurOrgVer() {
        return selectedRefInsurOrgVer;
    }

    public void setSelectedRefInsurOrgVer(RefInsurOrgItem selectedRefInsurOrgVer) {
        this.selectedRefInsurOrgVer = selectedRefInsurOrgVer;
    }

    public boolean isRefInsurOrgVisible() {
        return RefInsurOrgVisible;
    }

    public void setRefInsurOrgVisible(boolean refInsurOrgVisible) {
        RefInsurOrgVisible = refInsurOrgVisible;
    }

    public RefInsurOrgItem getRefInsurOrgItem() {
        return refInsurOrgItem;
    }

    public void setRefInsurOrgItem(RefInsurOrgItem refInsurOrgItem) {
        this.refInsurOrgItem = refInsurOrgItem;
    }

    public RefInsurOrgItem getFilterRefInsurOrgItem() {
        return filterRefInsurOrgItem;
    }

    public void setFilterRefInsurOrgItem(RefInsurOrgItem filterRefInsurOrgItem) {
        this.filterRefInsurOrgItem = filterRefInsurOrgItem;
    }

    public List<RefExtIndicatorItem> getRefExtIndList() {
        return RefExtIndList;
    }

    public void setRefExtIndList(List<RefExtIndicatorItem> refExtIndList) {
        RefExtIndList = refExtIndList;
    }

    public List<RefExtIndicatorItem> getRefExtIndHstList() {
        return RefExtIndHstList;
    }

    public void setRefExtIndHstList(List<RefExtIndicatorItem> refExtIndHstList) {
        RefExtIndHstList = refExtIndHstList;
    }

    public List<RefExtIndicatorItem> getRefExtIndVerList() {
        return RefExtIndVerList;
    }

    public void setRefExtIndVerList(List<RefExtIndicatorItem> refExtIndVerList) {
        RefExtIndVerList = refExtIndVerList;
    }

    public RefExtIndicatorItem getSelectedRefExtInd() {
        return selectedRefExtInd;
    }

    public void setSelectedRefExtInd(RefExtIndicatorItem selectedRefExtInd) {
        this.selectedRefExtInd = selectedRefExtInd;
    }

    public RefExtIndicatorItem getSelectedRefExtIndVer() {
        return selectedRefExtIndVer;
    }

    public void setSelectedRefExtIndVer(RefExtIndicatorItem selectedRefExtIndVer) {
        this.selectedRefExtIndVer = selectedRefExtIndVer;
    }

    public boolean isRefExtIndVisible() {
        return RefExtIndVisible;
    }

    public void setRefExtIndVisible(boolean refExtIndVisible) {
        RefExtIndVisible = refExtIndVisible;
    }

    public RefExtIndicatorItem getRefExtIndItem() {
        return refExtIndItem;
    }

    public void setRefExtIndItem(RefExtIndicatorItem refExtIndItem) {
        this.refExtIndItem = refExtIndItem;
    }

    public RefExtIndicatorItem getFilterRefExtIndItem() {
        return filterRefExtIndItem;
    }

    public void setFilterRefExtIndItem(RefExtIndicatorItem filterRefExtIndItem) {
        this.filterRefExtIndItem = filterRefExtIndItem;
    }

    public List<RefExtIndicatorParam> getRefExtParamList() {
        return RefExtParamList;
    }

    public void setRefExtParamList(List<RefExtIndicatorParam> refExtParamList) {
        RefExtParamList = refExtParamList;
    }

    public RefExtIndicatorParam getSelectedRefExtParam() {
        return selectedRefExtParam;
    }

    public void setSelectedRefExtParam(RefExtIndicatorParam selectedRefExtParam) {
        this.selectedRefExtParam = selectedRefExtParam;
    }

    public boolean isRefExtParamVisible() {
        return RefExtParamVisible;
    }

    public void setRefExtParamVisible(boolean refExtParamVisible) {
        RefExtParamVisible = refExtParamVisible;
    }

    public RefExtIndicatorParam getRefExtParamItem() {
        return refExtParamItem;
    }

    public void setRefExtParamItem(RefExtIndicatorParam refExtParamItem) {
        this.refExtParamItem = refExtParamItem;
    }

    public RefExtIndicatorParam getFilterRefExtParamItem() {
        return filterRefExtParamItem;
    }

    public void setFilterRefExtParamItem(RefExtIndicatorParam filterRefExtParamItem) {
        this.filterRefExtParamItem = filterRefExtParamItem;
    }

    public List<RefUnionPersonItem> getRefUnionPersonsList() {
        return RefUnionPersonsList;
    }

    public void setRefUnionPersonsList(List<RefUnionPersonItem> refUnionPersonsList) {
        RefUnionPersonsList = refUnionPersonsList;
    }

    public RefUnionPersonItem getFilterRefUnionPersonsItem() {
        return filterRefUnionPersonsItem;
    }

    public void setFilterRefUnionPersonsItem(RefUnionPersonItem filterRefUnionPersonsItem) {
        this.filterRefUnionPersonsItem = filterRefUnionPersonsItem;
    }

    public List<RefMajorMemberItem> getRefMajorMemberList() {
        return RefMajorMemberList;
    }

    public void setRefMajorMemberList(List<RefMajorMemberItem> refMajorMemberList) {
        RefMajorMemberList = refMajorMemberList;
    }

    public List<RefMajorMemberItem> getRefMajorMemberHstList() {
        return RefMajorMemberHstList;
    }

    public void setRefMajorMemberHstList(List<RefMajorMemberItem> refMajorMemberHstList) {
        RefMajorMemberHstList = refMajorMemberHstList;
    }

    public List<RefMajorMemberItem> getRefMajorMemberVerList() {
        return RefMajorMemberVerList;
    }

    public void setRefMajorMemberVerList(List<RefMajorMemberItem> refMajorMemberVerList) {
        RefMajorMemberVerList = refMajorMemberVerList;
    }

    public RefMajorMemberItem getSelectedRefMajorMember() {
        return selectedRefMajorMember;
    }

    public void setSelectedRefMajorMember(RefMajorMemberItem selectedRefMajorMember) {
        this.selectedRefMajorMember = selectedRefMajorMember;
    }

    public RefMajorMemberItem getSelectedRefMajorMemberVer() {
        return selectedRefMajorMemberVer;
    }

    public void setSelectedRefMajorMemberVer(RefMajorMemberItem selectedRefMajorMemberVer) {
        this.selectedRefMajorMemberVer = selectedRefMajorMemberVer;
    }

    public boolean isRefMajorMemberVisible() {
        return RefMajorMemberVisible;
    }

    public void setRefMajorMemberVisible(boolean refMajorMemberVisible) {
        RefMajorMemberVisible = refMajorMemberVisible;
    }

    public RefMajorMemberItem getRefMajorMemberItem() {
        return refMajorMemberItem;
    }

    public void setRefMajorMemberItem(RefMajorMemberItem refMajorMemberItem) {
        this.refMajorMemberItem = refMajorMemberItem;
    }

    public RefMajorMemberItem getFilterRefMajorMemberItem() {
        return filterRefMajorMemberItem;
    }

    public void setFilterRefMajorMemberItem(RefMajorMemberItem filterRefMajorMemberItem) {
        this.filterRefMajorMemberItem = filterRefMajorMemberItem;
    }

    public Boolean getIsVisibleInsurLp() {
        return isVisibleInsurLp;
    }

    public void setIsVisibleInsurLp(Boolean isVisibleInsurLp) {
        this.isVisibleInsurLp = isVisibleInsurLp;
    }

    public String getTypeUnionPersons() {
        return typeUnionPersons;
    }

    public void setTypeUnionPersons(String typeUnionPersons) {
        this.typeUnionPersons = typeUnionPersons;
    }

    public List<SimpleReference> getRefOwnershipList() {
        return RefOwnershipList;
    }

    public void setRefOwnershipList(List<SimpleReference> refOwnershipList) {
        RefOwnershipList = refOwnershipList;
    }

    public List<SimpleReference> getRefBranchInsurList() {
        return RefBranchInsurList;
    }

    public void setRefBranchInsurList(List<SimpleReference> refBranchInsurList) {
        RefBranchInsurList = refBranchInsurList;
    }

    public List<SimpleReference> getExtSysList() {
        return extSysList;
    }

    public void setExtSysList(List<SimpleReference> extSysList) {
        this.extSysList = extSysList;
    }

     public String getIdnForSearchU() {
         return idnForSearchU;
     }

     public void setIdnForSearchU(String idnForSearchU) {
         this.idnForSearchU = idnForSearchU;
     }

     public String getNameForSearchU() {
         return nameForSearchU;
     }

     public void setNameForSearchU(String nameForSearchU) {
         this.nameForSearchU = nameForSearchU;
     }

     public List<RefUnionPersonItem> getRefUnionForSearchList() {
         return refUnionForSearchList;
     }

     public void setRefUnionForSearchList(List<RefUnionPersonItem> refUnionForSearchList) {
         this.refUnionForSearchList = refUnionForSearchList;
     }

     public RefUnionPersonItem getSelectedSearchRefUnionPerson() {
         return selectedSearchRefUnionPerson;
     }

     public void setSelectedSearchRefUnionPerson(RefUnionPersonItem selectedSearchRefUnionPerson) {
         this.selectedSearchRefUnionPerson = selectedSearchRefUnionPerson;
     }

    public List<RefPeriodAlgItem> getRefPeriodAlgList() {
        return refPeriodAlgList;
    }

    public void setRefPeriodAlgList(List<RefPeriodAlgItem> refPeriodAlgList) {
        this.refPeriodAlgList = refPeriodAlgList;
    }

    public List<RefPeriodAlgItem> getRefPeriodAlgHstList() {
        return refPeriodAlgHstList;
    }

    public void setRefPeriodAlgHstList(List<RefPeriodAlgItem> refPeriodAlgHstList) {
        this.refPeriodAlgHstList = refPeriodAlgHstList;
    }

    public List<RefPeriodAlgItem> getRefPeriodAlgVerList() {
        return refPeriodAlgVerList;
    }

    public void setRefPeriodAlgVerList(List<RefPeriodAlgItem> refPeriodAlgVerList) {
        this.refPeriodAlgVerList = refPeriodAlgVerList;
    }

    public RefPeriodAlgItem getSelectedRefPeriodAlg() {
        return selectedRefPeriodAlg;
    }

    public void setSelectedRefPeriodAlg(RefPeriodAlgItem selectedRefPeriodAlg) {
        this.selectedRefPeriodAlg = selectedRefPeriodAlg;
    }

    public RefPeriodAlgItem getSelectedRefPeriodAlgVer() {
        return selectedRefPeriodAlgVer;
    }

    public void setSelectedRefPeriodAlgVer(RefPeriodAlgItem selectedRefPeriodAlgVer) {
        this.selectedRefPeriodAlgVer = selectedRefPeriodAlgVer;
    }

    public boolean isRefPeriodAlgVisible() {
        return refPeriodAlgVisible;
    }

    public void setRefPeriodAlgVisible(boolean refPeriodAlgVisible) {
        this.refPeriodAlgVisible = refPeriodAlgVisible;
    }

    public RefPeriodAlgItem getRefPeriodAlgItem() {
        return refPeriodAlgItem;
    }

    public void setRefPeriodAlgItem(RefPeriodAlgItem refPeriodAlgItem) {
        this.refPeriodAlgItem = refPeriodAlgItem;
    }

    public RefPeriodAlgItem getFilterRefPeriodAlgItem() {
        return filterRefPeriodAlgItem;
    }

    public void setFilterRefPeriodAlgItem(RefPeriodAlgItem filterRefPeriodAlgItem) {
        this.filterRefPeriodAlgItem = filterRefPeriodAlgItem;
    }

    public List<RefPeriodItem> getRefPeriodList() {
        return refPeriodList;
    }

    public void setRefPeriodList(List<RefPeriodItem> refPeriodList) {
        this.refPeriodList = refPeriodList;
    }

    public List<RefPeriodItem> getRefPeriodHstList() {
        return refPeriodHstList;
    }

    public void setRefPeriodHstList(List<RefPeriodItem> refPeriodHstList) {
        this.refPeriodHstList = refPeriodHstList;
    }

    public List<RefPeriodItem> getRefPeriodVerList() {
        return refPeriodVerList;
    }

    public void setRefPeriodVerList(List<RefPeriodItem> refPeriodVerList) {
        this.refPeriodVerList = refPeriodVerList;
    }

    public RefPeriodItem getSelectedRefPeriod() {
        return selectedRefPeriod;
    }

    public void setSelectedRefPeriod(RefPeriodItem selectedRefPeriod) {
        this.selectedRefPeriod = selectedRefPeriod;
    }

    public RefPeriodItem getSelectedRefPeriodVer() {
        return selectedRefPeriodVer;
    }

    public void setSelectedRefPeriodVer(RefPeriodItem selectedRefPeriodVer) {
        this.selectedRefPeriodVer = selectedRefPeriodVer;
    }

    public boolean isRefPeriodVisible() {
        return refPeriodVisible;
    }

    public void setRefPeriodVisible(boolean refPeriodVisible) {
        this.refPeriodVisible = refPeriodVisible;
    }

    public RefPeriodItem getRefPeriodItem() {
        return refPeriodItem;
    }

    public void setRefPeriodItem(RefPeriodItem refPeriodItem) {
        this.refPeriodItem = refPeriodItem;
    }

    public RefPeriodItem getFilterRefPeriodItem() {
        return filterRefPeriodItem;
    }

    public void setFilterRefPeriodItem(RefPeriodItem filterRefPeriodItem) {
        this.filterRefPeriodItem = filterRefPeriodItem;
    }

    public List<RefPeriodArgument> getArgumentList() {
        return argumentList;
    }

    public void setArgumentList(List<RefPeriodArgument> argumentList) {
        this.argumentList = argumentList;
    }

    public RefPeriodArgument getSelectedArgument() {
        return selectedArgument;
    }

    public void setSelectedArgument(RefPeriodArgument selectedArgument) {
        this.selectedArgument = selectedArgument;
    }

    public ValueType[] getValueTypes() {
        return valueTypes;
    }

    public void setValueTypes(ValueType[] valueTypes) {
        this.valueTypes = valueTypes;
    }
    public List<RefMajorMemberOrgItem> getRefMajorMemberOrgList() {
        return RefMajorMemberOrgList;
    }

    public void setRefMajorMemberOrgList(List<RefMajorMemberOrgItem> refMajorMemberOrgList) {
        RefMajorMemberOrgList = refMajorMemberOrgList;
    }

    public List<RefMajorMemberOrgItem> getRefMajorMemberOrgHstList() {
        return RefMajorMemberOrgHstList;
    }

    public void setRefMajorMemberOrgHstList(List<RefMajorMemberOrgItem> refMajorMemberOrgHstList) {
        RefMajorMemberOrgHstList = refMajorMemberOrgHstList;
    }

    public List<RefMajorMemberOrgItem> getRefMajorMemberOrgVerList() {
        return RefMajorMemberOrgVerList;
    }

    public void setRefMajorMemberOrgVerList(List<RefMajorMemberOrgItem> refMajorMemberOrgVerList) {
        RefMajorMemberOrgVerList = refMajorMemberOrgVerList;
    }

    public RefMajorMemberOrgItem getSelectedRefMajorMemberOrg() {
        return selectedRefMajorMemberOrg;
    }

    public void setSelectedRefMajorMemberOrg(RefMajorMemberOrgItem selectedRefMajorMemberOrg) {
        this.selectedRefMajorMemberOrg = selectedRefMajorMemberOrg;
    }

    public RefMajorMemberOrgItem getSelectedRefMajorMemberOrgVer() {
        return selectedRefMajorMemberOrgVer;
    }

    public void setSelectedRefMajorMemberOrgVer(RefMajorMemberOrgItem selectedRefMajorMemberOrgVer) {
        this.selectedRefMajorMemberOrgVer = selectedRefMajorMemberOrgVer;
    }

    public boolean isRefMajorMemberOrgVisible() {
        return RefMajorMemberOrgVisible;
    }

    public void setRefMajorMemberOrgVisible(boolean refMajorMemberOrgVisible) {
        RefMajorMemberOrgVisible = refMajorMemberOrgVisible;
    }

    public RefMajorMemberOrgItem getRefMajorMemberOrgItem() {
        return refMajorMemberOrgItem;
    }

    public void setRefMajorMemberOrgItem(RefMajorMemberOrgItem refMajorMemberOrgItem) {
        this.refMajorMemberOrgItem = refMajorMemberOrgItem;
    }

    public RefMajorMemberOrgItem getFilterRefMajorMemberOrgItem() {
        return filterRefMajorMemberOrgItem;
    }

    public void setFilterRefMajorMemberOrgItem(RefMajorMemberOrgItem filterRefMajorMemberOrgItem) {
        this.filterRefMajorMemberOrgItem = filterRefMajorMemberOrgItem;
    }

    public List<RefMajorMemDetailsItem> getRefMajorMemDetailsList() {
        return RefMajorMemDetailsList;
    }

    public void setRefMajorMemDetailsList(List<RefMajorMemDetailsItem> refMajorMemDetailsList) {
        RefMajorMemDetailsList = refMajorMemDetailsList;
    }

    public List<RefMajorMemDetailsItem> getRefMajorMemDetailsHstList() {
        return RefMajorMemDetailsHstList;
    }

    public void setRefMajorMemDetailsHstList(List<RefMajorMemDetailsItem> refMajorMemDetailsHstList) {
        RefMajorMemDetailsHstList = refMajorMemDetailsHstList;
    }

    public List<RefMajorMemDetailsItem> getRefMajorMemDetailsVerList() {
        return RefMajorMemDetailsVerList;
    }

    public void setRefMajorMemDetailsVerList(List<RefMajorMemDetailsItem> refMajorMemDetailsVerList) {
        RefMajorMemDetailsVerList = refMajorMemDetailsVerList;
    }

    public RefMajorMemDetailsItem getSelectedRefMajorMemDetails() {
        return selectedRefMajorMemDetails;
    }

    public void setSelectedRefMajorMemDetails(RefMajorMemDetailsItem selectedRefMajorMemDetails) {
        this.selectedRefMajorMemDetails = selectedRefMajorMemDetails;
    }

    public RefMajorMemDetailsItem getSelectedRefMajorMemDetailsVer() {
        return selectedRefMajorMemDetailsVer;
    }

    public void setSelectedRefMajorMemDetailsVer(RefMajorMemDetailsItem selectedRefMajorMemDetailsVer) {
        this.selectedRefMajorMemDetailsVer = selectedRefMajorMemDetailsVer;
    }

    public boolean isRefMajorMemDetailsVisible() {
        return RefMajorMemDetailsVisible;
    }

    public void setRefMajorMemDetailsVisible(boolean refMajorMemDetailsVisible) {
        RefMajorMemDetailsVisible = refMajorMemDetailsVisible;
    }

    public RefMajorMemDetailsItem getRefMajorMemDetailsItem() {
        return refMajorMemDetailsItem;
    }

    public void setRefMajorMemDetailsItem(RefMajorMemDetailsItem refMajorMemDetailsItem) {
        this.refMajorMemDetailsItem = refMajorMemDetailsItem;
    }

    public RefMajorMemDetailsItem getFilterRefMajorMemDetailsItem() {
        return filterRefMajorMemDetailsItem;
    }

    public void setFilterRefMajorMemDetailsItem(RefMajorMemDetailsItem filterRefMajorMemDetailsItem) {
        this.filterRefMajorMemDetailsItem = filterRefMajorMemDetailsItem;
    }

    public List<SimpleReference> getRefValueTypeList() {
        return RefValueTypeList;
    }

    public void setRefValueTypeList(List<SimpleReference> refValueTypeList) {
        RefValueTypeList = refValueTypeList;
    }

    public List<SimpleReference> getRefStatusList() {
        return refStatusList;
    }

    public void setRefStatusList(List<SimpleReference> refStatusList) {
        this.refStatusList = refStatusList;
    }

    public TreeNode getInsurGroupsTree() {
        return insurGroupsTree;
    }

    public void setInsurGroupsTree(TreeNode insurGroupsTree) {
        this.insurGroupsTree = insurGroupsTree;
    }

    public TreeNode getSelectedInsurGroups() {
        return selectedInsurGroups;
    }

    public void setSelectedInsurGroups(TreeNode selectedInsurGroups) {
        this.selectedInsurGroups = selectedInsurGroups;
    }

    public List<RefInsurGroupsItem> getRefInsurGroupsList() {
        return RefInsurGroupsList;
    }

    public void setRefInsurGroupsList(List<RefInsurGroupsItem> refInsurGroupsList) {
        RefInsurGroupsList = refInsurGroupsList;
    }

    public List<RefInsurGroupsItem> getRefInsurGroupsHstList() {
        return RefInsurGroupsHstList;
    }

    public void setRefInsurGroupsHstList(List<RefInsurGroupsItem> refInsurGroupsHstList) {
        RefInsurGroupsHstList = refInsurGroupsHstList;
    }

    public List<RefInsurGroupsItem> getRefInsurGroupsVerList() {
        return RefInsurGroupsVerList;
    }

    public void setRefInsurGroupsVerList(List<RefInsurGroupsItem> refInsurGroupsVerList) {
        RefInsurGroupsVerList = refInsurGroupsVerList;
    }

    public RefInsurGroupsItem getSelectedRefInsurGroups() {
        return selectedRefInsurGroups;
    }

    public void setSelectedRefInsurGroups(RefInsurGroupsItem selectedRefInsurGroups) {
        this.selectedRefInsurGroups = selectedRefInsurGroups;
    }

    public RefInsurGroupsItem getSelectedRefInsurGroupsVer() {
        return selectedRefInsurGroupsVer;
    }

    public void setSelectedRefInsurGroupsVer(RefInsurGroupsItem selectedRefInsurGroupsVer) {
        this.selectedRefInsurGroupsVer = selectedRefInsurGroupsVer;
    }

    public boolean isRefInsurGroupsVisible() {
        return RefInsurGroupsVisible;
    }

    public void setRefInsurGroupsVisible(boolean refInsurGroupsVisible) {
        RefInsurGroupsVisible = refInsurGroupsVisible;
    }

    public RefInsurGroupsItem getRefInsurGroupsItem() {
        return refInsurGroupsItem;
    }

    public void setRefInsurGroupsItem(RefInsurGroupsItem refInsurGroupsItem) {
        this.refInsurGroupsItem = refInsurGroupsItem;
    }

    public RefInsurGroupsItem getFilterRefInsurGroupsItem() {
        return filterRefInsurGroupsItem;
    }

    public void setFilterRefInsurGroupsItem(RefInsurGroupsItem filterRefInsurGroupsItem) {
        this.filterRefInsurGroupsItem = filterRefInsurGroupsItem;
    }

    public TreeNode getBankConglTree() {
        return bankConglTree;
    }

    public void setBankConglTree(TreeNode bankConglTree) {
        this.bankConglTree = bankConglTree;
    }

    public TreeNode getSelectedBankCongl() {
        return selectedBankCongl;
    }

    public void setSelectedBankCongl(TreeNode selectedBankCongl) {
        this.selectedBankCongl = selectedBankCongl;
    }

    public List<RefBankConglomeratesItem> getRefBankConglList() {
        return RefBankConglList;
    }

    public void setRefBankConglList(List<RefBankConglomeratesItem> refBankConglList) {
        RefBankConglList = refBankConglList;
    }

    public List<RefBankConglomeratesItem> getRefBankConglHstList() {
        return RefBankConglHstList;
    }

    public void setRefBankConglHstList(List<RefBankConglomeratesItem> refBankConglHstList) {
        RefBankConglHstList = refBankConglHstList;
    }

    public List<RefBankConglomeratesItem> getRefBankConglVerList() {
        return RefBankConglVerList;
    }

    public void setRefBankConglVerList(List<RefBankConglomeratesItem> refBankConglVerList) {
        RefBankConglVerList = refBankConglVerList;
    }

    public RefBankConglomeratesItem getSelectedRefBankCongl() {
        return selectedRefBankCongl;
    }

    public void setSelectedRefBankCongl(RefBankConglomeratesItem selectedRefBankCongl) {
        this.selectedRefBankCongl = selectedRefBankCongl;
    }

    public RefBankConglomeratesItem getSelectedRefBankConglVer() {
        return selectedRefBankConglVer;
    }

    public void setSelectedRefBankConglVer(RefBankConglomeratesItem selectedRefBankConglVer) {
        this.selectedRefBankConglVer = selectedRefBankConglVer;
    }

    public boolean isRefBankConglVisible() {
        return RefBankConglVisible;
    }

    public void setRefBankConglVisible(boolean refBankConglVisible) {
        RefBankConglVisible = refBankConglVisible;
    }

    public RefBankConglomeratesItem getRefBankConglItem() {
        return refBankConglItem;
    }

    public void setRefBankConglItem(RefBankConglomeratesItem refBankConglItem) {
        this.refBankConglItem = refBankConglItem;
    }

    public RefBankConglomeratesItem getFilterRefBankConglItem() {
        return filterRefBankConglItem;
    }

    public void setFilterRefBankConglItem(RefBankConglomeratesItem filterRefBankConglItem) {
        this.filterRefBankConglItem = filterRefBankConglItem;
    }

    public TreeNode getShareHoldTree() {
        return shareHoldTree;
    }

    public void setShareHoldTree(TreeNode shareHoldTree) {
        this.shareHoldTree = shareHoldTree;
    }

    public TreeNode getSelectedShareHold() {
        return selectedShareHold;
    }

    public void setSelectedShareHold(TreeNode selectedShareHold) {
        this.selectedShareHold = selectedShareHold;
    }

    public List<RefShareHoldersItem> getRefShareHoldList() {
        return RefShareHoldList;
    }

    public void setRefShareHoldList(List<RefShareHoldersItem> refShareHoldList) {
        RefShareHoldList = refShareHoldList;
    }

    public List<RefShareHoldersItem> getRefShareHoldHstList() {
        return RefShareHoldHstList;
    }

    public void setRefShareHoldHstList(List<RefShareHoldersItem> refShareHoldHstList) {
        RefShareHoldHstList = refShareHoldHstList;
    }

    public List<RefShareHoldersItem> getRefShareHoldVerList() {
        return RefShareHoldVerList;
    }

    public void setRefShareHoldVerList(List<RefShareHoldersItem> refShareHoldVerList) {
        RefShareHoldVerList = refShareHoldVerList;
    }

    public RefShareHoldersItem getSelectedRefShareHold() {
        return selectedRefShareHold;
    }

    public void setSelectedRefShareHold(RefShareHoldersItem selectedRefShareHold) {
        this.selectedRefShareHold = selectedRefShareHold;
    }

    public RefShareHoldersItem getSelectedRefShareHoldVer() {
        return selectedRefShareHoldVer;
    }

    public void setSelectedRefShareHoldVer(RefShareHoldersItem selectedRefShareHoldVer) {
        this.selectedRefShareHoldVer = selectedRefShareHoldVer;
    }

    public boolean isRefShareHoldVisible() {
        return RefShareHoldVisible;
    }

    public void setRefShareHoldVisible(boolean refShareHoldVisible) {
        RefShareHoldVisible = refShareHoldVisible;
    }

    public RefShareHoldersItem getRefShareHoldItem() {
        return refShareHoldItem;
    }

    public void setRefShareHoldItem(RefShareHoldersItem refShareHoldItem) {
        this.refShareHoldItem = refShareHoldItem;
    }

    public RefShareHoldersItem getFilterRefShareHoldItem() {
        return filterRefShareHoldItem;
    }

    public void setFilterRefShareHoldItem(RefShareHoldersItem filterRefShareHoldItem) {
        this.filterRefShareHoldItem = filterRefShareHoldItem;
    }

    public String getLoadSysName() {
        return loadSysName;
    }

    public void setLoadSysName(String loadSysName) {
        this.loadSysName = loadSysName;
    }

    public boolean isDisableBtnMajorMem() {
        return disableBtnMajorMem;
    }

    public void setDisableBtnMajorMem(boolean disableBtnMajorMem) {
        this.disableBtnMajorMem = disableBtnMajorMem;
    }

    public boolean isDisableBtnMajorMemView() {
        return disableBtnMajorMemView;
    }

    public void setDisableBtnMajorMemView(boolean disableBtnMajorMemView) {
        this.disableBtnMajorMemView = disableBtnMajorMemView;
    }

    public String getTypeMM() {
        return typeMM;
    }

    public void setTypeMM(String typeMM) {
        this.typeMM = typeMM;
    }

    public RefPeriodArgument getPrepareArgument() {
        return prepareArgument;
    }

    public void setPrepareArgument(RefPeriodArgument prepareArgument) {
        this.prepareArgument = prepareArgument;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    // endregion
}