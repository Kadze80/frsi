package jaxb;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "select")
@XmlAccessorType(XmlAccessType.FIELD)
public class HtmlSelect extends HtmlTag {

    @XmlAttribute
    public String name;

    @XmlAttribute
    public String multiple;

    @XmlAttribute
    public String disabled;

    @XmlAttribute
    public String size;

    @XmlAttribute
    public String autofocus; // html5

    @XmlAttribute
    public String form; // html5

    @XmlAttribute
    public String required; // html5

    @XmlElement(name = "option")
    public List<HtmlOption> options;
}
