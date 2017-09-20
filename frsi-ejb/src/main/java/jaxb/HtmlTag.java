package jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public abstract class HtmlTag {

    @XmlAttribute
    public String id;

    @XmlAttribute
    public String style;

    @XmlAttribute(name = "class")
    public String clazz;

    public HtmlTag() {
    }

    public HtmlTag(HtmlTag s) {
        this.id = s.id;
        this.style = s.style;
        this.clazz = s.clazz;
    }
}
