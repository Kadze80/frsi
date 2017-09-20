package jaxb;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "span")
@XmlAccessorType(XmlAccessType.FIELD)
public class HtmlSpan extends HtmlTag {

	@XmlValue
	public String content;
}
