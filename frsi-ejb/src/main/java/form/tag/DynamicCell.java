package form.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by nuriddin on 4/4/16.
 */
@XmlRootElement(name = "DynamicCell")
@XmlAccessorType(XmlAccessType.FIELD)
public class DynamicCell extends Container {
    @XmlAttribute
    public boolean header;
}
