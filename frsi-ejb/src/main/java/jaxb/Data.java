package jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "data")
@XmlAccessorType(XmlAccessType.FIELD)
public class Data {

    @XmlElement(name = "item")
    public List<Item> items;
}
