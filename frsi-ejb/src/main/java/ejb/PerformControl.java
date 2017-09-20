package ejb;

import entities.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.StreamHandler;

/**
 * Created by Marat.Madybayev on 10.10.2014.
 */
public interface PerformControl {
	void init();
	List<ControlResultItem> runTask(Date reportDate, String name, String idn, boolean forSuperUser, Long refSubjectTypeRecId, boolean cascade, boolean extSysControls);
	List<ControlResultItem> runTask(Date reportDate, String name, String idn, boolean forSuperUser, Long refSubjectTypeRecId, boolean extSysControls);
    List<ControlResultItem> runTaskAll(List<Report> reports, boolean forSuperUser, boolean cascade, long userId, String userLocation, long respondentId, Long idKindEvent, boolean extSysControls);
    List<ControlResultItemGroup> runTaskNGetGrouped(List<Report> reports, boolean forSuperUser, long userId, String userLocation, long respondentId, boolean extSysControls);
	List<ControlResultItem> runTaskUnsaved(Date reportDate, String name, String idn, Long refSubjectTypeRecId, Map<String, String> mapData, boolean extSysControls);
}
