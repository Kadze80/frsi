package form.html;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CssParser {

    private String css;
    private Map<String, String> propertyValues;

    public CssParser(String css) {
        this.css = css;
        createPropertyValues();
    }

    private void createPropertyValues() {
        propertyValues = new HashMap<String, String>();
        String[] pvPairs = css.split(";");
        for (String pvPair : pvPairs)
            if (!pvPair.trim().isEmpty()) {
                String[] pvPairParts = pvPair.split(":");
                if (pvPairParts.length > 1) {
                    String property = pvPairParts[0].trim();
                    String value = pvPairParts[1].trim();
                    if (!property.isEmpty()) propertyValues.put(property, value);
                }
            }
    }

    public String getPropertyValue(String property) {
        return propertyValues.get(property);
    }

    public Set<String> getProperties(){
        return propertyValues.keySet();
    }

    // Getters and setters

    public String getCss() {
        return css;
    }

    public void setCss(String css) {
        this.css = css;
    }
}
