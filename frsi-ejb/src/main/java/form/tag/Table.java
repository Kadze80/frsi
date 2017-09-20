package form.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "table")
@XmlAccessorType(XmlAccessType.FIELD)
public class Table extends Container {
    @XmlAttribute
    public String valueCheckFunc;
}
