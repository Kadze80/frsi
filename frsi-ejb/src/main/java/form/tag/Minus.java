package form.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Minus")
@XmlAccessorType(XmlAccessType.FIELD)
public class Minus extends Component {

    public String tableId;
}
