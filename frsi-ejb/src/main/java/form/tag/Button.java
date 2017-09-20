package form.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by nuriddin on 8/10/16.
 */
@XmlRootElement(name = "Button")
@XmlAccessorType(XmlAccessType.FIELD)
public class Button extends Component {
    @XmlAttribute
    public String text;
}
