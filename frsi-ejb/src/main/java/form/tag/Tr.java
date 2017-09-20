package form.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "tr")
@XmlAccessorType(XmlAccessType.FIELD)
public class Tr extends Container {
    @XmlAttribute
    public String fixed;
    @XmlAttribute(name = "data_dynamicRowLabel")
    public String data_dynamicRowLabel;
}
