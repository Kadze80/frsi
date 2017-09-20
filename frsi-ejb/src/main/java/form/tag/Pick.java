package form.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Pick")
@XmlAccessorType(XmlAccessType.FIELD)
public class Pick extends Component {

	@XmlAttribute
	public String ref;

	@XmlAttribute
	public String receiver;

	@XmlAttribute
	public String key;

	@XmlAttribute
	public String keyValue;
}
