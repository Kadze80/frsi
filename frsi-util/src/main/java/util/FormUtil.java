package util;

/**
 * Form utility methods
 *
 * @author Ardak Saduakassov
 */
public class FormUtil {

    public static String getContainerNameFromParamName(String paramName) {
        String[] parts = paramName.split("\\*");
        return parts.length > 0 ? parts[0] : null;
    }

    public static String getParamNameWithoutContainer(String paramName) {
        String[] parts = paramName.split("\\*");
        return parts.length > 1 ? parts[1] : null;
    }

    public static String getParamNameWithoutContainerAndKeyValue(String paramName) {
        String result = null;
        String[] parts = paramName.split("\\*");
        if (parts.length > 1) {
            String[] part1parts = parts[1].split(":");
            if (part1parts.length > 0) result = part1parts[0];
        }
        return result;
    }

    public static String getKeyAndValueFromParamName(String paramName) {
        String paramNameWithoutContainer = getParamNameWithoutContainer(paramName);
        String[] parts = paramNameWithoutContainer.split(":");
        return  parts.length > 2 ? parts[1] + ":" + parts[2] : null;
    }



}
