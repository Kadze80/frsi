package util;

import com.google.gson.Gson;

public class SettingsValueConverter {
    public static <T> T fromRaw(String rawValue, T defaultValue, Class<T> clazz) {
        if (rawValue == null) {
            return defaultValue;
        } else {
            Gson gson = new Gson();
            return gson.fromJson(rawValue, clazz);
        }
    }

    public static <T> String toRaw(T value, T defaultValue, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.toJson(value == null ? defaultValue : value, clazz);

    }
}
