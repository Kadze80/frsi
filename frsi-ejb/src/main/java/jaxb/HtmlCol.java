package jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "col")
@XmlAccessorType(XmlAccessType.FIELD)
public class HtmlCol extends HtmlTag {
    @XmlAttribute(name = "data-templ")
    public String data_templ;

    @XmlAttribute(name = "data-cellId")
    public String data_cellId;

    @XmlAttribute(name = "data-dynamicGroupLabel")
    public String data_dynamicGroupLabel;

    @XmlAttribute(name = "data-cellText")
    public String data_cellText;

    public HtmlCol() {
    }

    public HtmlCol(HtmlCol s) {
        super(s);
        this.data_templ = s.data_templ;
        this.data_cellId = s.data_cellId;
        this.data_dynamicGroupLabel = s.data_dynamicGroupLabel;
        this.data_cellText = s.data_cellText;
    }
}
