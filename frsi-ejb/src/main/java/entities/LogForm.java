package entities;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Entity
 *
 * @author Ardak Saduakassov
 * @deprecated Use Report and ReportHistory instead.
 */
public class LogForm implements Serializable {
	private static final long serialVersionUID = 1L;

	public long id;
	public String type;
	public int version;

	public long userId;
	public String userScreenName;
	public String userFullName;
	public String ibin;				// Respondent's IIN|BIN
	public String formLanguageCode;
	public String formName;
	public String formTitle;
	public String reportDate;

	public String saveDate;
	public String deliveryWay; 	// XML|EXCEL|WEB_FORM
	public long dataSize;		// Pure data size in bytes (inputValues size)

    public String hash;
    public String signature;

	public Map<String,String> inputValues;
}
