package jaxb;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "tr")
@XmlAccessorType(XmlAccessType.FIELD)
public class HtmlTr extends HtmlTag {

	@XmlElement(name = "th")
	public List<HtmlTh> ths;

	@XmlElement(name = "td")
	public List<HtmlTd> tds;

	@XmlAttribute(name = "data_dynamicRowLabel")
	public String data_dynamicRowLabel;

	@XmlAttribute(name = "data-dynamicGroupLabel")
	public String data_dynamicGroupLabel;

	@XmlAttribute(name = "data-rowId")
	public String data_rowId;
}
