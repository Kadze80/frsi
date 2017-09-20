package form.tag;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Container extends Component {

	@XmlAttribute
	public String name; // Copied to localName, then all parents names are prepended to it

    @XmlMixed
    @XmlAnyElement(lax = true)
    public List<Object> elements = new ArrayList<Object>();

    public Container container; // parent
    public String localName; // Initial name without parent names prefix
}
