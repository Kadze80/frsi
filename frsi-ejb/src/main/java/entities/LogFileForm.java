package entities;

import java.io.Serializable;
import java.util.Map;

public class LogFileForm implements Serializable {
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
    public String file;
}
