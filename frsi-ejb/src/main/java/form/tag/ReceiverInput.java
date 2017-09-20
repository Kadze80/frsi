package form.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Nuriddin.Baideuov on 27.11.2015.
 */
@XmlRootElement(name = "ReceiverInput")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReceiverInput extends Input {
    @XmlAttribute
    public String ref;
    @XmlAttribute
    public String refCode;
    @XmlAttribute
    public String refCaption;
    @XmlAttribute
    public Boolean multiLine;
    @XmlAttribute
    public Boolean multiValue;
}
