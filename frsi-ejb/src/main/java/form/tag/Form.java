package form.tag;

import entities.FormTag;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "Form")
@XmlAccessorType(XmlAccessType.FIELD)
public class Form extends Container {

	@XmlAttribute
    public String languageCode;
    @XmlAttribute
    public String title;
    @XmlAttribute
    public String shortName;
    @XmlAttribute
    public String beginDate;
    @XmlAttribute
    public String endDate;
	@XmlAttribute
	public String tag;
    @XmlAttribute
    public String typeCode;
    @XmlAttribute
    public int periodCount;
    @XmlAttribute
    public Integer xmlVersion;

	public Date dateBeginDate;
	public Date dateEndDate;
    public FormTag formTag;
}
