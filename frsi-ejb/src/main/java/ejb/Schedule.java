package ejb;

import entities.*;
import util.OracleException;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by Ayupov.Bakhtiyar on 25.06.2015.
 */
public interface Schedule {

    void init();
    void setTimerService(AuditEvent auditEvent);

    // Загрузка справочников из НСИ и ПУРЦБ
    String loadReferences(String ref_code_, Long id_usr_, String User_Location_, Date loadDate, Long respondentId);

    // Получить список загружаемых Справочников
    List<String> getLoadReference();

    // Уведомления
    void searchNotice();

    Boolean isNoticeUser(String tableName, Long userId, Long kindEvent, Connection connection);

    Role getRoleByUserId(Long userId);

    PortalUser getUserByUserId(long userId, Connection connection);

    void updateAEMainSts(Long id, int idSts, Connection connection);

    // Сохранение данных входных форм в виде списка
    void saveToReportHistoryListAll();

    void overdueReports();

    void updateBalanceAccTemplate(Long respId, Long userId, String userLocation);

}
