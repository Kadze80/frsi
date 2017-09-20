package jaxb;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "report")
@XmlAccessorType(XmlAccessType.FIELD)
public class Report {

    @XmlAttribute
    public String form;

    @XmlElement(name = "data")
    public Data data;
}
