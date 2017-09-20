package entities;

import java.io.Serializable;

public enum ValueType implements Serializable {
    BOOLEAN("Логикалық", "Логический", "Boolean"),
    STRING("Жол", "Строка", "String"),
    DATE("Дата", "Дата", "Date"),
    NUMBER_0("Бүтін сан", "Целое число", "Integer"),
    NUMBER_1("Нақты сан (.1)", "Вещественное число (.1)", "Number (.1)"),
    NUMBER_2("Нақты сан (.2)", "Вещественное число (.2)", "Number (.2)"),
    NUMBER_3("Нақты сан (.3)", "Вещественное число (.3)", "Number (.3)"),
    NUMBER_4("Нақты сан (.4)", "Вещественное число (.4)", "Number (.4)"),
    NUMBER_5("Нақты сан (.5)", "Вещественное число (.5)", "Number (.5)"),
    NUMBER_6("Нақты сан (.6)", "Вещественное число (.6)", "Number (.6)"),
    NUMBER_7("Нақты сан (.7)", "Вещественное число (.7)", "Number (.7)"),
    NUMBER_8("Нақты сан (.8)", "Вещественное число (.8)", "Number (.8)");

    private String nameKz;
    private String nameRu;
    private String nameEn;

    ValueType(String nameKz, String nameRu, String nameEn) {
        this.nameKz = nameKz;
        this.nameRu = nameRu;
        this.nameEn = nameEn;
    }

    public String getNameKz() {
        return nameKz;
    }

    public String getNameRu() {
        return nameRu;
    }

    public String getNameEn() {
        return nameEn;
    }

    public String getName(String languageCode) {
        if (languageCode.equals("kz"))
            return nameKz;
        else if (languageCode.equals("en"))
            return nameEn;
        else
            return nameRu;
    }
}
