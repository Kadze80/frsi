package form.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "script")
@XmlAccessorType(XmlAccessType.FIELD)
public class Script extends Content {
    @XmlAttribute
    public String type;
}
