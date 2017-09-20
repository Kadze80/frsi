package entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nuriddin.Baideuov on 16.06.2015.
 */
public enum ControlResultType implements Serializable {
    NONE, FAIL, SUCCESS, NO_DATA;

    public static Map<String, String> resMap = new HashMap<String, String>();

    static {
        resMap.put("en_NONE", "");
        resMap.put("en_FAIL", "Fail");
        resMap.put("en_NO_DATA", "No data");
        resMap.put("en_SUCCESS", "Success");

        resMap.put("kz_NONE", "");
        resMap.put("kz_FAIL", "Орындалмады");
        resMap.put("kz_NO_DATA", "Мәлімет жоқ");
        resMap.put("kz_SUCCESS", "Орындалды");

        resMap.put("ru_NONE", "");
        resMap.put("ru_FAIL", "Не выполнено");
        resMap.put("en_NO_DATA", "Нет данных");
        resMap.put("ru_SUCCESS", "Выполнено");
    }
}
