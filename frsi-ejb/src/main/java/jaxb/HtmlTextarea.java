package jaxb;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "textarea")
@XmlAccessorType(XmlAccessType.FIELD)
public class HtmlTextarea extends HtmlTag {

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

	@XmlValue
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

}
