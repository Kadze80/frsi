package ejb;

import dataform.FormulaSyntaxError;
import entities.*;
import entities.Error;
import util.OracleException;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Persistence {

	String getTestMessage();
	void init();

	// Form CRUD
	List<Form> getReportFormsByUserIdNoDate(Long userId, List<RefNpaItem> npaList);
	// List<Form> getFormsNoLob(Date date);
	List<Form> getFormsNoLob();
	// List<Form> getInputForms();
	List<Form> getFormsByReportDate(Date reportDate);
	// List<Form> getFormsByReportDateSubjectType(Date reportDate, String subject_type_code);
	List<Form> getFormsByCodeReportDate(String code, Date reportDate);
	List<Form> getFormsByCodeLanguageReportDate(String code, String languageCode, Date reportDate, Connection connection);
	List<Form> getFormsByUserSubjectTypeRecIds(long userId, List<Long> stRecIds, List<RefNpaItem> npaList);
	List<Form> getFormsByUserIdDate(long userId, Date reportDate, RefRespondentItem respondentItem, boolean editForms);
	List<Form> getFormsByUserIdDateSTList(long userId, Date reportDate, List<Long> subjectTypeRecIdList);
	List<Form> getOutFormsByUserSubjectTypeRecIds(long userId, Date reportDate, List<Long> stRecIds, String languageCode);
	List<Form> getAllOutFormsByUserSubjectTypeRecIds(long userId, List<Long> stRecIds, List<RefNpaItem> npaList);
	List<Form> getFormsNotInSubjForm(Long subjectTypeRecId);
	// List<Form> getReportsByReportDate(Date reportDate, String subject_type_code);
	// List<Form> getOutFormsByReportDate(Date reportDate);
	// List<Form> getInputFormsByReportDate(Date reportDate);
	/*List<Form> getFormsBySubjectType(Date reportDate, long resp);*/
	// Forms of user
	// List<Form> getReportsByUserReportDate(long userId, Date reportDate);
	// List<Form> getOutFormsByUserReportDate(long userId, Date reportDate);
	// List<Form> getFormsNoLobByUser(long userId, Date date);
	List<FormListItem> getFormListItemsNoLobNoDate();
	List<FormListItem> getFormListItemsNoLob(Date dateBegin, Date dateEnd, List<String> formCodes, String formCode, String languageCode);
	List<SubjectType_Form> getSubjTypeForms(Long subjectTypeRecId);
	void addSubjTypeForms(Long subjectTypeRecId, String formCode, AuditEvent auditEvent);
	void updateSubjTypeForm(SubjectType_Form subjectType_form, AuditEvent auditEvent);
	void delSubjTypeForms(Long id, AuditEvent auditEvent);
	Long getFormId(String code, Date date);
	Long getFormIdByCode(String code);
	Long getCountHistoryIdByFormId(Long id);
	Form getForm(Long id, Date date);
	Form getFormNoLob(Long id, Date date);
	Form getFormNoHistoryByCode(String code);
	Form getFormWithXls(Long id, Date date);
	Form getFormWithXlsOut(Long id, Date date);
	Form getFormWithActualXls(Long id, Date reportDate, boolean updateRef);
	Form newForm(String xml, String languageCode);
	Long insertForm(Form form, AuditEvent auditEvent);
	void updateForm(Form form, AuditEvent auditEvent);
	void updateXmlForm(String xml, Long id);
	void deleteForm(Long id, AuditEvent auditEvent);
	void deleteFormHistory(Long id, AuditEvent auditEvent);
    void deleteFormHistoryByCode(String formCode, Connection connection);
	Long insertFormHistory(FormHistory formHistory, boolean isFillList, AuditEvent auditEvent, Connection connection);
	void updateFormHistoryWithXls(FormListItem formListItem, AuditEvent auditEvent);
	void updateFormHistoryWithXlsOut(FormListItem formListItem, AuditEvent auditEvent);
	void updateFormHistoryIsFillList(long formHisoryId, boolean isFillList);
	FormHistory getFormHistoryWithInputValueChecks(Long id);
	Long getFormHistoryId(String formCode, Date beginDate, Connection connection);

	FormHistory getFormHistoryWithJsCode(Long id);
	String getFormNameByFormCodeLanguageCodeReportDate(String formCode, String languageCode, Date reportDate);
	List<Period> getPeriods();
    Period getPeriod(Long id);

	List<Form> getNpaForms(Long npaRecId, Boolean exist);
	void addNpaForms(Long npaRecId, Long formHistoryId, AuditEvent auditEvent);
	void delNpaForms(Long npaRecId, Long formHistoryId, AuditEvent auditEvent);

	// Reports
	// List<ReportListItem> getReportListByIdn(String idn, String languageCode);

	/**
	 * Возвращает список отчетов. ОСТОРОЖНО!!! Возвращает только по последней истории
	 * @param idn
	 * @param reportDate
	 * @param formCode
	 * @param languageCode
     * @return
     */
	List<ReportListItem> getReportListByIdnReportDateFormCode(String idn, boolean child, Date reportDate, String formCode, String languageCode);
	List<ReportHistory> getReportHistoryByReportNoLobs(Report report, Connection connection);
	List<ReportHistory> getReportHistoryByReportIdNoLobs(Long reportId, Connection connection);
	List<ReportHistoryListItem> getReportHistoryListByReportId(long reportId, boolean forSuperUser);
	ReportHistoryListItem getReportHistoryListItemById(long reportHistoryId, Connection connection);
	ReportHistory getLastReportHistoryByReportId(Long reportId, boolean withData, boolean withAttachment, boolean forSuperUser, Connection connection);
	ReportHistory getLastReportHistoryByReportIdNoLobs(Long reportId, boolean forSuperUser, Connection connection);
	Long getLastReportHistoryIdByReportId(Long reportId, boolean forSuperUser, Connection connection);
	List<ReportStatus> getReportStatusHistoryByReportHistoryId(Long reportHistoryId, Connection connection);
	ReportStatus getLastReportStatusByReportId(long reportId, boolean forSuperUser, Connection connection);
	ReportStatus getLastReportStatusByReportHistoryId(long reportHistoryId, Connection connection);
	List<ReportStatus> getReportStatusHistoryByReportId(long reportId, boolean forSuperUser, Connection connection);
	Long getReportId(String idn, Date reportDate, String formCode);
	Report getReport(Long id, Connection connection);
	Report getReportByIdnDateForm(String idn, Date reportDate, String formCode);
	Long createReportOutputReport(Date reportDate, Form form, Map<String, String> inputValues, Set<Long> inputReportIds, PortalUser user, String idn, String initStatus, Date curDate);
	void updateReportOutputReport(Long reportId, Map<String, String> inputValues, Set<Long> inputReportIds, PortalUser user, String initStatus, Date curDate);
	void deleteReport(Long id, Connection connection);
	void deleteReportTransactional(Long id, ReportStatus status, AuditEvent auditEvent);
	ReportHistory getReportHistory(Long id, boolean withData, boolean withAttachment);
	long getReportIdByReportHistoryId(long reportHistoryId, Connection connection);
	Long insertReportHistory(ReportHistory reportHistory);
	void updateReportHistory(ReportHistory reportHistory);
	void updateReportHistory(ReportHistory reportHistory, Connection connection) throws SQLException;
	void updateReportHistoryData(Long reportHistoryId, String data, AuditEvent auditEvent);
	void deleteReportHistory(Long id);
	ReportStatus getReportStatus(Long id);
	Long insertReportStatusHistory(ReportStatus reportStatus, AuditEvent auditEvent);
	void updateReportStatusHistory(ReportStatus reportStatus);
	void deleteReportStatusHistory(Long id);
	/*Report getReportByApprovalId(long approvalId);*/
	List<Report> getReportByRepDateFormRespondents(Date reportDate, String formName, List<RefRespondentItem> respondents);
	List<Report> getReportByRepDateFormRespondentsStatusCode(Date reportDate, String formName, List<RefRespondentItem> respondents, String statusCode);
	List<Report> getAllReports();
	String getReportValueByKeyReportHistoryId(String key, Long reportHistoryId);
	Report getReportFromXml(String xml, String languageCode, Date reportDate) throws Exception;
	Report getReportFromXml(byte[] array, String languageCode, Date reportDate) throws Exception;
	boolean checkPeriod(Date reportDate, String formCode, Long subjectTypeRecId);
	String getSignerInfoByReportIdRefPostRecId(long reportHistoryId, long refPostRecId);
	void updateReportControlResultCode(long reportHistoryId, String controlResultCode);
	void updateReportControlResultCode2(long reportHistoryId, String controlResultCode);
	boolean isStatusCompatible(String oldStatus, String newStatus);
	void setReportApproved(Long reportId, boolean approved, AbstractUser user, Date approvalDate, AuditEvent auditEvent);

	void insertReportRefLink(Connection connection, Long reportHistoryId, String refCode, Long recId);
	void deleteReportRefLink(Connection connection, Long reportHistoryId, Long reportId);

	void editReportNote(Long reportHistoryId, String note, boolean append, boolean newLine);
	String getReportNote(Long reportHistoryId, Connection connection);

	// Attached Files
	List<AttachedFile> getFileListByLinkId(Long linkId, int fileKind, Connection connection);
	List<AttachedFile> getFileListWithDataByLinkId(Long linkId, int fileKind);
	void uploadFile(AttachedFile attachedFile, AuditEvent auditEvent, Connection connection);
	void updatePdfFile(AttachedFile attachedFile);
	String getDataFileFormat(Long id, Connection connection);
	AttachedFile getDataFile(Long id, boolean forPrint);
	void deleteFile(Long id, AuditEvent auditEvent, Connection connection);
	void deleteAllFilesByLinkId(Long linkId, int fileKind, Connection connection);
	Boolean haveAttachedFiles(Long linkId, int fileKind);

	Map<String, String> updateCalculatedFields(String formCode, Date reportDate, Map<String, String> kvMap, String languageCode, boolean fillExpectedData);
	Map<String, String> validateAndNormalizeDataFormat(Map<String, String> kvMap, List<InputValueCheck> inputValueChecks, boolean returnInvalidData);
	Map<String, String> validateAndNormalizeDataFormatBA(Map<String, String> kvMap, List<InputValueCheck> inputValueChecks);

	// Approval
	List<ApprovalItem> getApprovalItems();
	List<ApprovalItem> getApprovalItemsByRespondent(Long respondentId, String language_code);
	List<ApprovalItem> getApprovalItemsByRepDateRespondent(Long respondentId, String language_code, Date reportDate);
	List<ApprovalItem> getUnApprovalItemsByRepDateRespondent(Long respondentId, String language_code, Date reportDate);
	List<ApprovalItem> getApprovalItemsByUser(Long userId);
	List<ApprovalItem> getApprovalItemsByUserEntity(Long userId, Long entityId);
	List<ApprovalItem> getApprovalItemsByRepDateForm(Date reportDate, String formName);
	List<ApprovalItem> getApprovalItemsByRepDateFormResp(Date reportDate, String formName, Long respondentId);
	String getFormTitleByRepDateFormResp(Date reportDate, String formName, Long respondentId, String language_code);
	List<ApprovalItem> getApprovalItemsByRepDateFormResps(Date reportDate, String formName, String resps);
	List<ApprovalItem> getApprovalDistinctItemsByRepDate(Date reportDate);
	ApprovalItem getApprovalItem(Long id);
	void updateApprovalItem(ApprovalItem approvalItem);
	void deleteApprovalItem(Long id);

	// Report data
	Map<String, String> getKvMap(Long reportId);
	String getHtmlWithInitialData(Form form, RefRespondentItem respondent, Date reportDate, boolean forView);
	String getHtmlWithoutDynamicRowTemplates(String html);
	//String getHtmlWithLogData(String logId);
	String getHtmlWithReportData(Long reportId, boolean forView, boolean forSuperUser);
	String getHtmlWithReportHistoryData(long reportHistoryId, boolean forView);
	byte[] downloadTemplateExcel(Long formHistoryId, Boolean in);

	void putReportProp(Long reportHistoryId, String propKey, String propValue);
	void putReportProps(Long reportHistoryId, Map<String, String> props);
	String getReportPropValue(Long reportHistoryId, String propKey);

	// Out_Data
	void insertOutData(OutDataItem outDataItem);
	//OutDataItem getOutData(Long id);
	List<OutDataItem> getOutDataItemsByRepDate(Date reportDate, String language_code, int is_approved);
	List<RefRespondentItem> getOutDataRespItemsByOutDataId(Long outDataId);
	void updateOutDataItem(Long id, boolean approved);
	int updateOutDataItemNote(Long id, String note);
	void deleteOutDataItem(Long id);
	List<ReportListItem> getFilteredOutputReportList(long userId, List<String> formCodes, List<String> idnList,
													 List<RefNpaItem> filterNpa, Date reportDateBegin, Date reportDateEnd, String languageCode);
	void updateOutputReport(Long id, boolean approved, AbstractUser user, Date approvalDate, AuditEvent auditEvent) throws Exception;
	String getHtmlWithOutputReportData(Long reportId, boolean forView);
	List<InputReportListItem> getInputReportsByOutputReportId(Long outputReportId, String languageCode);
	List<RefRespondentItem> getInputReportRespondentsByOutputReportId(Long outputReportId);
	List<ReportListItem> getOutputReportListByInputReportId(Long inputReportId, String languageCode);

	// Export
	byte[] getExcelFileContent(ReportListItem reportListItem, boolean forView, boolean forSuperUser);
	byte[] getExcelFileContentForReport(String html);
	FileWrapper generateExcelFile(ReportListItem reportListItem, boolean forSuperUser) throws Exception;
	FileWrapper generateExcelFileFromReportHistory(long reportHistoryId) throws Exception;
	List<FileWrapper> generateExcelFiles(List<ReportListItem> reportListItems, boolean forSuperUser, Long respondentId, Long userId, String userLocation) throws Exception;
	FileWrapper unionExcel(List<FileWrapper> fileWrappers, String fileName) throws Exception;
	FileWrapper generatePdfFile(ReportListItem reportListItem, boolean forSuperUser) throws Exception;
	FileWrapper generatePdfFileFromReportHistory(long reportHistoryId) throws Exception;
	ExcelData extractExcelData(byte[] xlsFile, Date reportDate) throws Exception;
	byte[] replaceExcelData(byte[] xlsFile) throws Exception;
	FileWrapper controlResultsToExcelFile(List<ControlResultItem> controlResults, ReportListItem reportListItem) throws Exception;
	FileWrapper controlResultsAllToExcelFile(List<ControlResultItem> controlResults, RefRespondentItem refRespondentItem) throws Exception;
	FileWrapper extControlResultsAllToExcelFile(List<ControlResultItem> controlResults, RefRespondentItem refRespondentItem) throws Exception;
	FileWrapper controlResultGroupsToExcelFile(List<ControlResultItemGroup> controlResults) throws Exception;
	FileWrapper extControlResultGroupsToExcelFile(List<ControlResultItemGroup> controlResults) throws Exception;
	FileWrapper generateControlResultsPdfFile(List<ControlResultItem> controlResults, ReportListItem reportListItem) throws Exception;
	FileWrapper generateControlResultsAllPdfFile(List<ControlResultItem> controlResults, RefRespondentItem refRespondentItem) throws Exception;
	FileWrapper generateControlResultGroupsPdfFile(List<ControlResultItemGroup> controlResultGroups) throws Exception;
	FileWrapper validationResultsToExcelFile(String validationMessage, List<Error> errors, ReportListItem reportListItem) throws Exception;
	FileWrapper generateValidationResultsPdfFile(String validationMessage, List<Error> errors, ReportListItem reportListItem) throws Exception;
	FileWrapper generateSuInfoPdfFile(String typeInfo, String infoName, List<ReportValueNameListItem> itemList, List<ColumnModel> columns) throws Exception;
	FileWrapper suInfoToExcelFile(String typeInfo, String infoName, List<ReportValueNameListItem> itemList, List<ColumnModel> columns) throws Exception;
	FileWrapper suInfoReportsToExcelFile(String typeInfo, String infoName, List<ReportValueNameListItem> itemList) throws Exception;
	FileWrapper suInfoSummaryToExcelFile(String infoName, List<ReportValueNameListItem> itemList) throws Exception;
	FileWrapper convertFileToPdf(FileWrapper fileWrapper, String fileBaseName) throws Exception;

	// SubjectTypes
	Map<String, String> getGuide(Date reportDate, String className, String fieldName);

	// List<ApprovalItem> infoSubmittedReports(Long respondentId, String language_code, Date reportDate);
	// List<ApprovalItem> infoNotSubmittedReports(Long respondentId, String language_code, Date reportDate);
	// List<ApprovalItem> infoSubmittedRespsByReport(String form_code, Date reportDate);
	// List<ApprovalItem> infoNotSubmittedRespsByReport(String form_code, Date reportDate);


	void savePermissions(Set<Permission> permissions, Set<PermissionFormContainer> pfContainers, Set<PermissionDepartment> departments, Set<PermissionSubjectType> subjectTypes, Set<PermissionRespondent> respondents,
						 long selectedUserGroupId, long selectedUserId, Long userId, String userLocation, AuditEvent auditEvent, Date date);
	// Permissions for UserGroup
	void saveGroupCommonPermissions(Set<Permission> permissions, long selectedUserGroupId, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException;
	String getGroupPrefix();
	List<Permission> getAllPermissionByUserGroup(long userGroupId, long roleId);
	void insertUserGroupPermission(long userGroupId, Permission permission, Connection connection, AuditEvent auditEvent);
	void updateUserGroupPermission(Permission permission);
	void deleteUserGroupPermission(long id, Connection connection, AuditEvent auditEvent);
	void deleteUserGroupPermissions(long userGroupId, boolean isDeleteUserPermissions, Connection connection);

	List<PermissionFormContainer> getAllPfContainerByUserGroupNSubjectTypeFormTypeCode(long userGroupId, List<Long> stRecIds, String formTypeCode, List<RefNpaItem> npaList, String languageCode, Map<Long, String> refRespondents);
	void deleteUserGroupPermissionForms(long userGroupId, boolean isDeleteUserPermissions, Connection connection);

	void saveUserGroupPermissionDepartments(Set<PermissionDepartment> departments, long selectedUserGroupId, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException;
	List<PermissionDepartment> getAllDepartmentByUserGroup(long userGroupId);
	void insertUserGroupPermissionDepartment(long userGroupId, PermissionDepartment department, Connection connection, AuditEvent auditEvent);
	void deleteUserGroupPermissionDepartment(long id, Connection connection, AuditEvent auditEvent);
	void deleteUserGroupPermissionDepartments(long userGroupId, boolean isDeleteUserPermissions, Connection connection);

	void saveUserGroupPermissionSubjectTypes(Set<PermissionSubjectType> subjectTypes, long selectedUserGroupId, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException;
	List<PermissionSubjectType> getAllPermissionSubjectTypesByUserGroup(long userGroupId);
	void insertUserGroupPermissionSubjectType(long userGroupId, PermissionSubjectType subjectType, Connection connection, AuditEvent auditEvent);
	void deleteUserGroupPermissionSubjectType(long id, Connection connection, AuditEvent auditEvent);
	void deleteUserGroupPermissionSubjectType(long userGroupId, boolean isDeleteUserPermissions, Connection connection);

	void saveUserGroupRespondents(Set<PermissionRespondent> respondents, long selectedUserGroupId, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException;
	List<PermissionRespondent> getAllRespondentByUserGroup(long userGroupId, Long refDepartmentRecId, Long refSubjectTypeRecId, Long refRespondentRecId);
	List<Long> getUserGroupPermissionRespondentsRecIds(long userGroupId);
	void insertUserGroupPermissionRespondent(long userGroupId, PermissionRespondent respondent, Connection connection, AuditEvent auditEvent);
	void updateUserGroupPermissionRespondent(PermissionRespondent respondent);
	void deleteUserGroupPermissionRespondent(long id, Connection connection, AuditEvent auditEvent);
	void deleteUserGroupPermissionRespondents(long userGroupId, boolean isDeleteUserPermissions, Connection connection);

	boolean hasPermission(Long userId, String permissionName, Connection connection);

	// Permissions for User
	void saveUserCommonPermissions(Set<Permission> permissions, long selectedUserId, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException;
	List<Permission> getAllPermissionByUser(long userId, long roleId);
	void insertUserPermission(long userId, Permission permission, Connection connection, AuditEvent auditEvent);
	void updateUserPermission(Permission permission, Connection connection, AuditEvent auditEvent);
	void deleteUserPermission(long id, Connection connection, AuditEvent auditEvent);

	List<PermissionFormContainer> getAllPfContainerByUserNSubjectTypeFormTypeCode(long userId, List<Long> stRecIds, String formTypeCode, List<RefNpaItem> npaList, String languageCode, Map<Long, String> refRespondents);

	void saveUserPermissionDepartments(Set<PermissionDepartment> departments, long selectedUserId, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException;
	List<PermissionDepartment> getAllPermissionDepartmentByUser(long userId);
	void insertUserPermissionDepartment(long userId, PermissionDepartment department, Connection connection, AuditEvent auditEvent);
	void updateUserPermissionDepartment(PermissionDepartment department, Connection connection, AuditEvent auditEvent);
	void deleteUserPermissionDepartment(long id, Connection connection, AuditEvent auditEvent);
	void deleteUserPermissionDepartments(long userId, Connection connection);

	void saveUserPermissionSubjectTypes(Set<PermissionSubjectType> subjectTypes, long selectedUserId, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException;
	List<PermissionSubjectType> getAllPermissionSubjectTypesByUser(long userId);
	void insertUserPermissionSubjectType(long userId, PermissionSubjectType subjectType, Connection connection, AuditEvent auditEvent);
	void updateUserPermissionSubjectType(PermissionSubjectType subjectType, Connection connection, AuditEvent auditEvent);
	void deleteUserPermissionSubjectType(long id, Connection connection, AuditEvent auditEvent);
	void deleteUserPermissionSubjectTypes(long userId, Connection connection);

	void saveUserRespondents(Set<PermissionRespondent> respondents, long selectedUserId, Long userId, String userLocation, Date date, Long parentAEId, Connection connection) throws SQLException;
	List<PermissionRespondent> getAllRespondentByUser(long userId, Long refDepartmentRecId, Long refSubjectTypeRecId, Long refRespondentRecId);
	List<PermissionRespondent> getRespondentsByWarrant(long userId, long refRespondentRecId);
	void insertUserPermissionRespondent(long userId, PermissionRespondent respondent, Connection connection, AuditEvent auditEvent);
	void updateUserPermissionRespondent(PermissionRespondent respondent, Connection connection, AuditEvent auditEvent);
	void deleteUserPermissionRespondent(long id, Connection connection, AuditEvent auditEvent);
	void deleteUserPermissionRespondents(long userId, Connection connection);

	boolean hasPermissionRespForm(long userId, String formCode, String permissionName, String idn);
	boolean hasOutputPermissionForm(long userId, String formCode, String permissionName);

	Role getRole(long roleId, Connection connection);
	List<Role> getRoles(Connection connection);
	List<Role> getRolesByDepRecId(List<Long> depRecIdList);
	Role getRoleByUserId(Long userId);

	void synchronizeUserGroups(List<PortalUserGroup> groups);
	void synchronizeUsers(List<PortalUser> users);
	List<PortalUserGroup> getUserGroupsByFilter(long userId, Long[] roleIds, Long[] subjectTypeRecIds, Long[] departmentRecIds, String filterGroupName, String filterUser);
	List<PortalUser> getUsers(String screenName, String lastName, String firstName, String middleName);
	List<PortalUser> getUsersByGroupId(long groupId);
	List<PortalUser> getUsersByGroupIdRespondentId(long groupId, long respondentId);
	List<PortalUser> getUsersByFilter(long groupId, Long[] respondentIds, String filterUser);
	PortalUserGroup getUserGroupByGroupId(long groupId, Connection connection);
	PortalUser getUserByUserId(long userId, Connection connection);
	void insertPortalUserGroup(PortalUserGroup userGroup, boolean isFull, Connection connection, AuditEvent auditEvent);
	void updatePortalUserGroup(PortalUserGroup userGroup, boolean isFull, Map<String, Boolean> deletePermissions, Connection connection, AuditEvent auditEvent);
	void deletePortalUserGroup(long userGroupId, Connection connection, AuditEvent auditEvent);
	void insertPortalUser(PortalUser user, boolean isFull, Connection connection);
	void updatePortalUser(PortalUser user, boolean isFull, Connection connection, AuditEvent auditEvent);
	void updatePortalUser(PortalUser user, boolean isFull, List<UserWarrant> warrants, Connection connection, AuditEvent auditEvent, boolean fillUserPermissions);
	void blockPortalUser(long userId, boolean value, Connection connection);
	void addUserToUserGroup(long userGroupId, long userId, Connection connection, AuditEvent auditEvent);
	void deleteUserFromUserGroup(long userGroupId, long userId, Connection connection, AuditEvent auditEvent);
	void deleteUserFromUserGroup(long userGroupId, long userId, Connection connection, AuditEvent auditEvent, boolean fillUserPermission);
	Set<Long> getDuplicatedUserInUserGroups(String groupPrefix, Connection connection);
	Set<Long> getUserGroupsByUser(long userId, Connection connection);
	PortalUserGroup getUserGroupByUser(long userId, Connection connection);
	boolean isExistUserInUserGroup(long groupId, long userId, Connection connection);
	List<PortalUser> getUsersByRespondentId(long respondentId);
	List<PortalUser> getRespondentUsersByRespondentId(long respondentId);
	List<PortalUserGroup> getUserGroupByRoleIdList(List<Long> roleIdList, List<Long> depRecIdList);
	List<PortalUser> getUsers(List<Long> roleIdList, List<Long> groupIdList, List<Long> subjectTypeRecIdList, List<Long> respondentRecIdList);

	void fillUserPermissions(long userId, boolean fillRightItems, boolean fillDepartments, boolean fillSubjectTypes, boolean fillCreditors, boolean fillForms, Connection connection) throws SQLException;

	List<String> getUserPermissionNames(long userId);

	Set<Long> filterUsersByFormPermission(Set<Long> users, long reportId, String formCode, String permissionName);

	List<Long> getUserRespondents(long userId);

	// boolean hasAnyUserPermission(String permissionName);

	// ReportHistorySignature
	void updateSignature(long reportId, String signature, long userId, String userLocation, Date signDate, long userWarrantId, AuditEvent auditEvent);
	void clearSignatures(long reportId);
	List<ReportHistorySignature> getSignaturesByReportHistory(long reportHistoryId);

	// Reports of user
	ReportListItem getReportListByReportId(long reportId, String languageCode, boolean forSuperUser);
	ReportListItem getReportListByReportIdAdvanced(long reportId, String languageCode, boolean forSuperUser);
	List<ReportListItem> getReportListByUserIdnFormCodesReportDateRange(long userId, String idn, List<String> idnList, boolean haveWarrant, List<String> formCodes,
																		List<RefNpaItem> filterNpa, Date reportDateBegin, Date reportDateEnd, String languageCode);
	List<ReportListItem> getReportListByUserIdnListFormCodesReportDateRange(long userId, List<String> idnList, List<String> formCodes,
																			List<RefNpaItem> filterNpa, Date reportDateBegin, Date reportDateEnd, String languageCode);
	PortalUser getRespondentCeo(long respondentId, Date date);
	PortalUser getRespondentChiefAccountant(long respondentId, Date date);
	PortalUser getRespondentDraftedBy(long respondentId, Date date);

	// test
	List<ReportListItem> getReportListByIdnNoLobsV2(String idn, Date date, String code, String formType);

	Long insertTmpReportV2(Long idReport, Long idReportHistory);

	void deleteTmpReportV2(Long idReport);

	Long insertTmpReportV2Dtl(Long idReport, Long idKnd, String keyName, String keyValue);

	Long insertGrpTmpReportV2Dtl(Long idReport, Long idKnd, Map<String, String> kvMap);

	Map<String, String> transformRefCaptionToRecId(String formCode, Date reportDate, String languageCode, Map<String, String> kvMap, String idn);

	// Аудит
	Long insertAuditEvent(AuditEvent auditEvent) throws OracleException;
	Long insertAuditEventWithParams(AuditEvent auditEvent, AuditEventParam[] params) throws OracleException;
	AuditEventParam getAuditEventParam(long auditEventId, String code);

	AuditEvent getAuditEvent(Long aeId, Connection connection);

	List<AuditEvent> getAuditEventList(Date dateBegin, Date dateEnd, List<Long> respondentList, String userCode,
											  List<Long> eventNameList, List<Long> eventKindList, String codeObject,
											  String nameObject, Boolean isArchive, Long userId);
	void moveToFromArchive(List<Long> eventList, Boolean isArchive) throws OracleException;

	// region Уведомление
	List<Notice> getNoticeList(List<Long> eventNameList, String nameNotice);

	void updateNoticeMessage(Long id, String subjectMsg, String message, AuditEvent auditEvent);

	List<NoticeSettings> getNoticeSettings(String tableName, Long idNotice);

	void updateNoticeSettings(Long noticeId, List<NoticeSettings> stList, List<NoticeSettings> roleList, List<NoticeSettings> respList, List<NoticeSettings> userList, List<NoticeSettings> groupList, AuditEvent auditEvent);

	List<NoticeMail> getNoticeMailList(Long userId);

	void setNoticeMailHowRead(Long mailId, int isRead, AuditEvent auditEvent);

	void deleteNoticeMail(Long id);

	void deleteNoticeMail(List<NoticeMail> noticeMailList, Long userId, String userLocation);

	void insertNoticeMail(List<PortalUser> userToList, PortalUser userFrom, String userLocation, String subjectMsg, String message);

	String getNoticeMessageById(Long id);

	String getNoticeSubjectMessageById(Long id);

	List<NoticeSettings> getNoticeForUser(Long userId);

	Boolean checkUserNoticeOff(Long userId, Long kindEvent, int typeAddress);

	Boolean isNoticeUser(String tableName, Long userId, Long kindEvent);

	void insertNoticeUserOff(List<NoticeSettings> settingsList, Long userId, String userLocation, Connection connection);

	boolean sendNotice(Long aeId, Long userIdFrom, List<PortalUser> portalUserList, String subjectMsg, String textMessage, Connection connection);

	String changeOfVariables(String message, Long aeId, Long userIdFrom, Long userIdTo, Connection connection);

	// endregion

	void testInsert(int n);

	List<Integer> getTestNumbers();

	int getFormMaxXlsVersion(String code, Date date);

	Long saveAndGetId(String source, Report report, Long respondentId, Long formId, AbstractUser user, Date curDate, Map<String, String> keyValueMap, Date prevReportDate, boolean isAutoSave);

	String getHashFiles(Long reportHistoryId);

	boolean fillDefaultValueByFormWithValues(Connection connection, Long reportHistoryId, String inputValueCheck, String reportHistoryData, boolean insRefValue);

	Date getSignDate(Long reportHistoryId, String valueFunc, Date date, Connection connection);

	void updateSignDate(Long reportHistoryId, Date date, String inputValueCheck, Map<String, String> kvMap, Connection connection);

	Map<String, String> getUpdatedCurrentDate(String inputValueCheck, Map<String, String> kvMap, Date defaultDate);

	boolean updateCurrentDate(long reportHistoryId, long respondentId, AbstractUser user);

	List<ReportListItem> getAllReportsForInfo(long userId, List<Long> subjectTypeRecIdList, List<Long> respRecIdList, List<String> formCodesList, Date date, String languageCode, Boolean stateSender, AuditEvent auditEvent);

	List<Template> getTemplateList(Long typeTemplate);

	void updateTemplateXlsOut(Template template);

	List<Error> validateKvMap(Map<String, String> kvMap, Long id, Date reportDate);

	List<Error> validateRefValues(Long reportId, Date reportDate);

	Map<String, String> executeValueFunctions(Map<String, String> kvMap, String formCode, Date reportDate);

	void updateReportHistoryComment(Long reportHistoryId, String currentHistoryComment);

	List<String> getAllOracleUsers();

	boolean checkUniqueDesignUserName(long id,String designUserName);

	List<OutReportRuleItem> getOutReportRuleListByFormCodeDate(String code, Date reportDate);

	void insertOutReportRuleItem(OutReportRuleItem item, long formHistoryId);

	void clearOutReportRuleItems(long formHistoryId);

	void makeConsolidatedReport(String formCode, Date reportDate);

	DataSet execQuery(String query, QueryType qt);

    int copyReportHistory();

	void insertIntoReportRefLink();

	Variant getExternalSystemIndicatorValue(String code, String formCode, Date reportDate, String idn, Param[] params) throws SQLException;

	Variant getRefItemValueByRecId(String refName, String refColumn, Long recId, Date date, ValueType vt);

    List<RefExtIndicatorItem> getExtIndicatorItemsByCode(String code);

	List<Long> getDepRecIdListByUser(Long userId);

	Long getDepRecIdByUser(Long userId);

	PeriodAlgResult executePeriodAlg(long refPeriodRecId, String formCode, Date reportDate, String idn);

	List<ReportPeriod> getRespondentOverdueReports(Long userId, String idn, int periodCount);

	List<ReportPeriod> getAllOverdueReports(int periodCount);

	SettingsItem getSettingsItemByTypeUserId(SettingsItem.Type settingType, Long userId);

	void saveSettingsItems(SettingsItem[] settingsItem, String userLocation);

	int getDaysBeforeOverdueDateSettingDefaultValue();

	// region Доверенность
	List<UserWarrant> getUserWarrantsByPrincipal(Long userId);
	List<UserWarrant> getUserWarrantsByAttorney(Long userId);
	List<UserWarrant> getActiveWarrantsByAttorney(Long userId, Date date);
	// endregion


	void updateBalanceAccountTemplate(String xml, String code, Long kindEvent, Long respondentId, Long userId, String userLocation, Date beginDate, Connection connection);


}
