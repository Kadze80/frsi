package form.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DynamicFunction")
@XmlAccessorType(XmlAccessType.FIELD)
public class DynamicFunction {

    @XmlAttribute
    public String name;
}
