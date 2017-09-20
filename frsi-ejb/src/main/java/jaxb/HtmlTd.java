package jaxb;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "td")
@XmlAccessorType(XmlAccessType.FIELD)
public class HtmlTd extends HtmlTag {

	@XmlAttribute
	public String headers;

	@XmlAttribute
	public String colspan;

	@XmlAttribute
	public String rowspan;

	@XmlMixed
	@XmlAnyElement(lax = true)
	public List<Object> elements = new ArrayList<Object>();

	@XmlAttribute(name = "data-dynamicGroupLabel")
	public String data_dynamicGroupLabel;

	@XmlAttribute(name = "data-templ")
	public String data_templ;

	@XmlAttribute(name = "data-cellId")
	public String data_cellId;

	@XmlAttribute(name = "data-cellText")
	public String data_cellText;

	public HtmlTd() {
	}

	public HtmlTd(HtmlTd s) {
		super(s);
		this.headers = s.headers;
		this.colspan = s.colspan;
		this.rowspan = s.rowspan;
		this.elements = s.elements;
		this.data_dynamicGroupLabel = s.data_dynamicGroupLabel;
		this.data_templ = s.data_templ;
		this.data_cellId = s.data_cellId;
		this.data_cellText = s.data_cellText;
	}
}
