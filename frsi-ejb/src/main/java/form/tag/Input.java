package form.tag;

import form.process.FormProcessor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Input extends Component {

	@XmlAttribute
	public String name;
	@XmlAttribute
	public String key;
	@XmlAttribute
	public String keyValue;
	@XmlAttribute
	public String value;
	@XmlAttribute
	public String valueType;
	@XmlAttribute
	public String mask;
	@XmlAttribute
	public Boolean auto;
	@XmlAttribute
	public Boolean readonly;
	@XmlAttribute
	public Boolean required;
	@XmlAttribute
	public String valueCheckFunc;
	@XmlAttribute
	public Boolean unique;
	@XmlAttribute
	public String uniqueArea;
	@XmlAttribute
	public String valueFunc;
	@XmlAttribute
	public Boolean disabled;

	public String errorCode = FormProcessor.ERR_UNCHECKED;
}
