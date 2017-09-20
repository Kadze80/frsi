package form.tag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "InputSelect")
@XmlAccessorType(XmlAccessType.FIELD)
public class InputSelect extends Input {

    @XmlAttribute
    public String ref;
    @XmlAttribute
    public String refCode;
    @XmlAttribute
    public String refCaption;
    @XmlAttribute
    public Boolean multiValue;
    @XmlAttribute
    public String viewModel;
}
