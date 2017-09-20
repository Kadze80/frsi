package form.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "PlusMinusBaDetails")
@XmlAccessorType(XmlAccessType.FIELD)
public class PlusMinusBaDetails extends Component {

    public String tableId;
    public String rowId;
}
