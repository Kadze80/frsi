package dataform;

import java.io.Serializable;
import java.util.Map;
import org.apache.log4j.Logger;

public class DataForm implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger("fileLogger");

	protected Map<String,String> attributeValueMap;

	public DataForm(Map<String, String> attributeValueMap) {
		this.attributeValueMap = attributeValueMap;
	}

	public DataForm() {
	}

	public Map<String, String> getAttributeValueMap() {
		return attributeValueMap;
	}
}
