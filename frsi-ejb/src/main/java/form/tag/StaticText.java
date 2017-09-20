package form.tag;

import form.process.FormProcessor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "StaticText")
@XmlAccessorType(XmlAccessType.FIELD)
public class StaticText extends Component {

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

	public String errorCode = FormProcessor.ERR_UNCHECKED;
}
