package jaxb;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "option")
@XmlAccessorType(XmlAccessType.FIELD)
public class HtmlOption extends HtmlTag {

    @XmlAttribute
    public String label;

    @XmlAttribute
    public String disabled;

    @XmlAttribute
    public String selected;

    @XmlAttribute
    public String value;

    @XmlValue
    public String content;
}
