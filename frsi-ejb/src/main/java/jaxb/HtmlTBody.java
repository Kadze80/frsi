package jaxb;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "tbody")
@XmlAccessorType(XmlAccessType.FIELD)
public class HtmlTBody extends HtmlTag {
    @XmlAttribute(name = "data-dynamicGroupLabel")
    public String data_dynamicGroupLabel;

    @XmlElement(name = "tr")
    public List<HtmlTr> trs;
}
