package form.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Component {

    @XmlAttribute
    public String id;

    @XmlAttribute
    public String style;

    @XmlAttribute(name = "class")
    public String styleClass;

    public Container container; // parent
}
