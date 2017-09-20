package form.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "td")
@XmlAccessorType(XmlAccessType.FIELD)
public class Td extends Container {

	@XmlAttribute
	public String colspan;
	@XmlAttribute
	public String rowspan;
	@XmlAttribute
	public String fixed;
}
