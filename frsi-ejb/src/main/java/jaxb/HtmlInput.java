package jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "input")
@XmlAccessorType(XmlAccessType.FIELD)
public class HtmlInput extends HtmlTag {

	@XmlAttribute
	public String type;

	@XmlAttribute
	public String name;

	@XmlAttribute
	public String readonly;

	@XmlAttribute
	public String disabled;

	@XmlAttribute
	public String onchange;

	@XmlAttribute
	public String onclick;

	@XmlAttribute
	public String onkeydown;

	@XmlAttribute
	public String onkeypress;

	@XmlAttribute
	public String onfocus;

	@XmlAttribute
	public String value;

	@XmlAttribute
	public String ref;

	@XmlAttribute
	public String refCode;

	@XmlAttribute
	public String refCaption;

	@XmlAttribute
	public String multiValue;

	@XmlAttribute
	public String viewModel;

	@XmlAttribute(name = "data-receive")
	public String dataReceive;

	@XmlAttribute(name = "data-filter-field")
	public String dataFilterField;

}
