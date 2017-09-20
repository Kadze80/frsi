package form.process;

import javax.xml.bind.Unmarshaller;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamReader;
import java.util.HashMap;
import java.util.Map;

public class LocationListener extends Unmarshaller.Listener {

	private XMLStreamReader xsr;
	private Map<Object, Location> locations;

	public LocationListener(XMLStreamReader xsr) {
		this.xsr = xsr;
		this.locations = new HashMap<Object, Location>();
	}

	@Override
	public void afterUnmarshal(Object target, Object parent) {
		locations.put(target, xsr.getLocation());
	}

	public Location getLocation(Object o) {
		return locations.get(o);
	}

}
