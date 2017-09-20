package ejb;

import entities.*;
import util.OracleException;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Ayupov.Bakhtiyar on 14.04.2016.
 */

public interface Reference {
    void init();

    // Excel шаблоны для системы(МФ контроль, результаты проверок, справочники и т.п.)
    Template getTemplateData(Template template);

    // REF_MAIN
    List<RefItem> getRefList(Long userId, RefItem filterRefMainItem);
    RefItem getRefByCode(String code);

    // Получить элементы справочника
    List<RefElements> getRefElements(String name, Boolean withCode);

    // Общие методы для справочников
    boolean refItemExists(String refName, Long rec_id, Date date);
    Long getRefRecId(String refName, String refColumn, String refColumnValue, Date date);
    Long getRefRecIdById(String refName, Long id);
    Long getRefIdByRecIdBeginDate(String refName, Date date, Long recId);
    String getRefOriginalValue(String refName, String refColumn, String refColumnValue, Date date);
    String getRefItemNameByRecId(String refName, String refColumn, Long recId, Date date);
    List<? extends AbstractReference> getReferenceItemsByName(String refName, String refColumn, Date date);
    List<? extends AbstractReference> getReferenceItemByName(String refName, String refColumn, Date date);
    List<InputSelectRecord> getReferenceItemsByNameViewModel(String refName, Date date, String refColumn, InputSelectViewModel vm);
    List<InputSelectRecord> getReferenceItemsByNameViewModelPage(String refName, Date date, InputSelectViewModel vm, Filters filters, int offset, int limit);
    int getReferenceItemsCount(String refName, Date date, Filters filters);

    List<RefElements> getRefKndList();

    // Универсальные методы для справочника
    List<? extends AbstractReference> getRefAbstractList(String refCode, Date date);
    List<? extends AbstractReference> getRefAbstractByFilterList(String refCode, AbstractReference filter, Date date);
    List<? extends AbstractReference> getRefAbstractHstList(String refCode, Long id);
    AbstractReference getRefAbstractItem(String refCode, AbstractReference filter);
    Long insertRefAbstractItem(String refCode, AbstractReference item, AuditEvent auditEvent) throws OracleException;
    void updateRefAbstractItem(String refCode, AbstractReference item, AuditEvent auditEvent) throws OracleException;
    void deleteRefAbstractItem(String refCode, Long id, AuditEvent auditEvent) throws OracleException;

    // Справочник должностей
    PostType getPostType(Long id);
    List<RefPostItem> getRefPosts(Long typePost, Date date);

    // Справочник юридических лиц
    List<RefLegalPersonItem> getLegalPersons(Date date);
    List<RefLegalPersonItem> getLegalPersonsByFilters(Date date, String bin, String name);
    List<RefLegalPersonItem> getLegalPersonsByBin(Date date, String bin);
    List<RefLegalPersonItem> getRefLegalPersonsByFilterPage(Date date_, RefLegalPersonItem filter, int offset, int limit);
    int getRefLegalPersonsCount(Date date_, RefLegalPersonItem filter);
    List<RefLegalPersonItem> getRefLPTaxByFilterPage(Date date_, RefUnionPersonItem filter, int offset, int limit);

    // Справочник балансовых счетов для отчетов о сделках
    List<RefBalanceAccItem> getRefBalanceAccLastRecord(Date date_);
    List<BalanceAccountRec> getSortedBalanceAccounts(Date reportDate);
    List<RefBalanceAccItem> getRefBalanceAccountsByCode(Date date, String code);
    List<RefBalanceAccItem> getRefBalanceAccountsByParentCode(Date date, String parentCode);
    List<RefBalanceAccItem> getRefBalanceAccountsByLevelCode(Date date, String levelCode);

    // Справочник подразделений НБ РК
    Long getRefDepartmentId(Long RecId, Date date_);
    List<SimpleReference> getRefDeptType(Date date_);

    // Справочник банков второго уровня
    List<RefBankItem> getBanks(Date date);
    List<RefBankItem> getBanksByFilters(Date date, String name);

    // Справочник валют
    List<CurrencyWrapper> getCurrencyWrappers(Date date);
    List<String> getRateACurrencyRecIds(Date date);

    // Справочник типов субъектов
    List<RefSubjectTypeItem> getRefSubjectTypeListAdvanced(Date date_, boolean includeFirstLevelBank);
    List<RefSubjectTypeItem> getSTListByUser(Long userId);
    Long getRefSubjectTypeId(Long RecId, Date date_);
    List<SubjectTypePost> getSubjectTypePostList(long stRecId, Date date);
    void updateSubjectTypePost(List<SubjectTypePost> subjectTypePostList, long stRecId);
    List<Long> getActiveSubjectTypePosts(Long refSubjectTypeRecId);
    List<RefSubjectTypeItem> getRefSubjectTypeByWarrant(Long refRespondentRecId);

    // Справочник респондентов(подотчетных организаций)
    RefRespondentItem getRespondentByRecId(Long recId, Date reportDate);
    RefRespondentItem getRespondentByIdn(String idn, Date reportDate);
    Map<String, Long> getRespondentBinEntityIdMap();
    List<RefRespondentItem> getUserRespsBySTRDepRecIds(Long userId, Date reportDate, List<Long> subjectTypeRecIds, List<RefDepartmentItem> depList);
    List<RefRespondentItem> getUserRespondentsBySubjectType(long userId, Long stRecId, Date reportDate, List<RefDepartmentItem> deps);
    List<RefRespondentItem> getRespondentsByUserSTRecIdList(Long userId, List<Long> stRecIdList);
    List<RefRespondentItem> getRespondentsWithWarrants(Long parentRespRecId, Date reportDate, Boolean withParent);
    List<RespondentWarrant> getRespondentWarrantList(Long respRecId, Date date);
    RespondentWarrant getRespondentWarrant(Long respRecId, String idnChild, Date date);
    Boolean respondentHaveWarrant(Long recId, Date date);
    Boolean respondentHaveWarrantByIdn(Long recId, Date date, String idnChild);
    void insertRespWarrant(RespondentWarrant respondentWarrant);
    void updateRespWarrant(RespondentWarrant respondentWarrant);
    void deleteWarrant(Long warrantId);
    Boolean submitReportsByWarrant(Long recIdParent, Long recIdChild);

    // Справочник Эмитентов
    List<RefIssuersItem> getIssuers(Date date);
    List<RefIssuersItem> getIssuersByFilters(Date date, String nameFilter);

    // Справочник Ценных бумаг
    List<RefSecuritiesItem> getSecurities(Date date);
    List<RefSecuritiesItem> getSecuritiesByIssuerId(Date date, Long issuerId);
    List<RefSecuritiesItem> getSecuritiesByNin(Date date, String nin);
    List<RefSecuritiesItem> getSecuritiesByRecId(Date date, Long recId);
    List<RefSecuritiesItem> getPagingSecuritiesByNin(Date date, String nin, int offset, int limit);
    int getSecuritiesCount(Date date, String nin);

    // Справочник межформенных контролей
    List<RefCrosscheckItem> getRefCrosscheckListByFormCodeDateSubjectType(String formCode, Date date_, boolean forSuperUser, Long subjectTypeRecId, boolean extSysControls);
    List<RefCrosscheckItem> getRefCrosscheckListByFormCodeDate(String formCode, Date date_, boolean forSuperUser, boolean extSysControls);
    List<RefCrosscheckForm> getRefCrosscheckForms(Long refCrosscheckId);

    // Справочник правил выходных форм
    List<RefReportsRulesItem> getRefReportsRulesListByFormCodeDate(String formCode, Date date_);

    // Справочник показателей внешних систем
    List<RefExtIndicatorItem> getExtIndicatorItemsByFilters(Date date_, RefExtIndicatorItem filter);

    // Справочник аргументов сроков предоставления
    List<RefPeriodArgument> getRefPeriodArguments(Long refPeriodId);
    void insertArguments(List<RefPeriodArgument> itemList, Long refPeriodId, Connection connection);
    void updateArgument(RefPeriodArgument item);
    List<RefPeriodArgument> searchArgumentsFromAlg(Long refPeriodId, List<RefPeriodArgument> argumentList, String alg);


    // Справочник физ./юр. лиц
    List<RefUnionPersonItem> getRefUnionPersonItemsByFilterPage(Date date_, RefUnionPersonItem filter, int offset, int limit);
    int getRefUnionPersonItemsCount(Date date_, RefUnionPersonItem filter);

    List<RefUnionPersonItem> getRefLPTaxItemsByFilterPage(Date date_, RefUnionPersonItem filter, int offset, int limit);
    int getRefLPTaxItemsCount(Date date_, RefUnionPersonItem filter);

    Long createNewPersonFromTax(Long recId, Long userId, String userLocation);

    // Таблица связей юр и физ лиц - ref_unionpersons
    RefUnionPersonItem getUnionPersonItemById(Long unionId);
    List<RefUnionPersonItem> getUnionPersonItemList(Date date, String idn, String nameRu);

    //для КН
    // для Справочника выходных и праздничных дней
    List<HolidayItem> getRefHolidayList(Date date_);
    List<HolidayItem> getRefHolidayListByParams(Date date_, HolidayItem refHolidayItem);
    HolidayItem getRefHolidayItem(Long id);
    /*Long insertRefNpaItem(RefWkdHolidayItem refWkdHolidayItem) throws OracleException;*/
    void updateRefHolidayItem(HolidayItem refHolidayItemList) throws OracleException;
    void fillRefWkdHolidays(HolidayItem refHolidayItem, Date dateYear) throws OracleException;

    List<RefExtIndicatorParam> getRefExtParamListByParams(Date date_, String algorithm, Long idRefExtInd)  throws OracleException;
    Long insertRefExtIndicatorWithParam(RefExtIndicatorItem item, List<RefExtIndicatorParam> refExtIndList, AuditEvent auditEvent) throws OracleException;
    void updateRefExtIndicatorWithParam(RefExtIndicatorItem item, List<RefExtIndicatorParam> refExtIndList, AuditEvent auditEvent) throws OracleException;

    //Подсправочник наименований организаций крупных участников
    List<RefMajorMemberOrgItem> getRefMajorMemOrgListByParam(RefMajorMemberOrgItem item);

    // Методы для страндартных справочников
    List<SimpleReference> getRefSimpleListByParams(Date date, SimpleReference refSimpleItem);
    List<SimpleReference> getRefSimpleHstList(Long id, String refCode);
    SimpleReference getRefSimpleItem(Long id, String refCode);
    Long insertRefSimpleItem(SimpleReference refSimpleItem) throws OracleException;
    void updateRefSimpleItem(SimpleReference refSimpleItem) throws OracleException;
    void deleteRefSimpleItem(Long id, String refCode) throws OracleException;

    // region Выгрузка в Excel
    // region новый метод, выгрузки в Excel - не выгружает больше справочники по несколько раз !
    FileWrapper referenceToExcelFile(final RefItem refItem, boolean allRecords, Date date, Boolean loadBank) throws Exception;
    FileWrapper simpleReferenceToExcelFile(final RefItem refItem, List<SimpleReference> refList) throws Exception;
    FileWrapper refPostToExcelFile(final RefItem refItem, List<RefPostItem> refList) throws Exception;
    FileWrapper refPersonToExcelFile(final RefItem refItem, List<RefPersonItem> refList) throws Exception;
    FileWrapper refLegalPersonToExcelFile(final RefItem refItem, List<RefLegalPersonItem> refList) throws Exception;
    FileWrapper refCountryToExcelFile(final RefItem refItem, List<RefCountryItem> refList) throws Exception;
    FileWrapper refManagersToExcelFile(final RefItem refItem, List<RefManagersItem> refList) throws Exception;
    FileWrapper refTypeBusEntityToExcelFile(final RefItem refItem, List<RefTypeBusEntityItem> refList) throws Exception;
    FileWrapper refRegionToExcelFile(final RefItem refItem, List<RefRegionItem> refList) throws Exception;
    FileWrapper refRequirementToExcelFile(final RefItem refItem, List<RefRequirementItem> refList) throws Exception;
    FileWrapper refTypeProvideToExcelFile(final RefItem refItem, List<RefTypeProvideItem> refList) throws Exception;
    FileWrapper refTransTypesToExcelFile(final RefItem refItem, List<RefTransTypeItem> refList) throws Exception;
    FileWrapper refBalanceAccountToExcelFile(final RefItem refItem, List<RefBalanceAccItem> refList) throws Exception;
    FileWrapper refConnOrgToExcelFile(final RefItem refItem, List<RefConnOrgItem> refList) throws Exception;
    FileWrapper refDepartmentToExcelFile(final RefItem refItem, List<RefDepartmentItem> refList) throws Exception;
    FileWrapper refBankToExcelFile(final RefItem refItem, List<RefBankItem> refList) throws Exception;
    FileWrapper refRateAgencyToExcelFile(final RefItem refItem, List<RefRateAgencyItem> refList) throws Exception;
    FileWrapper refCurrencyToExcelFile(final RefItem refItem, List<RefCurrencyItem> refList) throws Exception;
    FileWrapper refCurrencyRateToExcelFile(final RefItem refItem, List<RefCurrencyRateItem> refList) throws Exception;
    FileWrapper refSubjectTypeToExcelFile(final RefItem refItem, List<RefSubjectTypeItem> refList) throws Exception;
    FileWrapper refRespondentToExcelFile(final RefItem refItem, List<RefRespondentItem> refList) throws Exception;
    FileWrapper refDocTypeToExcelFile(final RefItem refItem, List<RefDocTypeItem> refList) throws Exception;
    FileWrapper refDocumentToExcelFile(final RefItem refItem, List<RefDocumentItem> refList) throws Exception;
    FileWrapper refIssuersToExcelFile(final RefItem refItem, List<RefIssuersItem> refList) throws Exception;
    FileWrapper refSecuritiesToExcelFile(final RefItem refItem, List<RefSecuritiesItem> refList) throws Exception;
    FileWrapper refVidOperToExcelFile(final RefItem refItem, List<RefVidOperItem> refList) throws Exception;
    FileWrapper refBranchToExcelFile(final RefItem refItem, List<RefBranchItem> refList) throws Exception;
    FileWrapper refCrosscheckToExcelFile(final RefItem refItem, List<RefCrosscheckItem> refList) throws Exception;
    FileWrapper refReportsRulesToExcelFile(final RefItem refItem, List<RefReportsRulesItem> refList) throws Exception;
    FileWrapper refListingEstimationToExcelFile(final RefItem refItem, List<RefListingEstimationItem> refList) throws Exception;
    FileWrapper refRatingEstimationToExcelFile(final RefItem refItem, List<RefRatingEstimationItem> refList) throws Exception;
    FileWrapper refRatingCategoryToExcelFile(final RefItem refItem, List<RefRatingCategoryItem> refList) throws Exception;
    FileWrapper refMrpToExcelFile(final RefItem refItem, List<RefMrpItem> refList) throws Exception;

    // endregion

    String getOracleDate(Date date);

    long getFirstLevelBankRecId();

}
