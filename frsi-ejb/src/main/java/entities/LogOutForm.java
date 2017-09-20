package entities;

import java.io.Serializable;
import java.util.Map;

public class LogOutForm implements Serializable {
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

	public Map<String,String> inputValues;
}
