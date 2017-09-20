package form.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DynamicRow")
@XmlAccessorType(XmlAccessType.FIELD)
public class DynamicRow extends Container {

    @XmlAttribute
    public String groupId;
    @XmlAttribute
    public String label;
}
