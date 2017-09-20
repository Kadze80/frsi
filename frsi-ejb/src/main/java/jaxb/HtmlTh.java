package jaxb;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "th")
@XmlAccessorType(XmlAccessType.FIELD)
public class HtmlTh extends HtmlTag {

	@XmlAttribute
	public String abbr;

	@XmlAttribute
	public String headers;

	@XmlAttribute
	public String colspan;

	@XmlAttribute
	public String rowspan;

	@XmlAttribute
	public String scope;

	@XmlAttribute
	public String sorted;

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

	@XmlAttribute(name = "data-colspan")
	public String data_colspan;

	public HtmlTh() {
	}

	public HtmlTh(HtmlTh s) {
		super(s);
		this.abbr = s.abbr;
		this.headers = s.abbr;
		this.colspan = s.colspan;
		this.rowspan = s.rowspan;
		this.scope = s.scope;
		this.sorted = s.sorted;
		this.elements = s.elements;
		this.data_dynamicGroupLabel = s.data_dynamicGroupLabel;
		this.data_templ = s.data_templ;
		this.data_cellId = s.data_cellId;
		this.data_cellText = s.data_cellText;
		this.data_colspan=s.data_colspan;
	}
}
