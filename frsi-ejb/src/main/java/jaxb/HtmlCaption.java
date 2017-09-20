package jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement(name = "span")
@XmlAccessorType(XmlAccessType.FIELD)
public class HtmlCaption extends HtmlTag {

	@XmlValue
	public String content;
}
