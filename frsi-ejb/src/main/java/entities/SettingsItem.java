package entities;

import com.google.gson.Gson;

import java.io.Serializable;

public class SettingsItem implements Serializable {

    public enum Type {
        SHOW_OVERDUE_REPORTS("Показать просроченные отчеты при первом входе", "Показать просроченные отчеты при первом входе", "Показать просроченные отчеты при первом входе"),
        NOTICE_MAIL_OVERDUE_DATE("Уведомлять на почту о наступлении срока предоставления", "Уведомлять о наступлении срока предоставления", "Уведомлять о наступлении срока предоставления"),
        NOTICE_SYS_OVERDUE_DATE("Уведомлять в системе о наступлении срока предоставления", "Уведомлять о наступлении срока предоставления", "Уведомлять о наступлении срока предоставления"),
        DAYS_BEFORE_OVERDUE_DATE("Количество дней до срока предоставления отчета", "Количество дней до срока предоставления отчета", "Количество дней до срока предоставления отчета");

        private String nameRu;
        private String nameKz;
        private String nameEn;

        Type(String nameRu, String nameKz, String nameEn) {
            this.nameRu = nameRu;
            this.nameKz = nameKz;
            this.nameEn = nameEn;
        }

        public String getNameRu() {
            return nameRu;
        }

        public String getNameKz() {
            return nameKz;
        }

        public String getNameEn() {
            return nameEn;
        }

        public String getName(String languageCode) {
            if (languageCode.equals("kz")) return nameKz;
            else if (languageCode.equals("ru")) return nameRu;
            else return nameEn;
        }
    }

    private Type type;
    private Long userId;
    private String rawValue;

    public SettingsItem() {
        this(null, null, null);
    }

    public SettingsItem(Type type, Long userId) {
        this(type, userId, null);
    }

    public SettingsItem(Type type, Long userId, String rawValue) {
        this.type = type;
        this.userId = userId;
        this.rawValue = rawValue;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }
}
