package form.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "InputText")
@XmlAccessorType(XmlAccessType.FIELD)
public class InputText extends Input {
    @XmlAttribute
    public Boolean multiLine;
    @XmlAttribute
    public String ref;
    @XmlAttribute
    public String refCode;
    @XmlAttribute
    public String refCaption;
}
