package util;

/**
 * Created by nuriddin on 8/22/16.
 */
public class ReportHelper {
    public static String getControlRowStyleClass(Long controlResultType) {
        if (controlResultType == null) return "";
        String result = "";
        switch (controlResultType.intValue()) {
            case 1:
                result = "controlSuccess";
                break;
            case 2:
            case 3:
                result = "controlFail";
                break;
        }
        return result;
    }
}
