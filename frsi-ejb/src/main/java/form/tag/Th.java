package form.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "th")
@XmlAccessorType(XmlAccessType.FIELD)
public class Th extends Container {

	@XmlAttribute
	public String colspan;
	@XmlAttribute
	public String rowspan;
	@XmlAttribute
	public String fixleftcnt;
}
