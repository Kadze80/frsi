package entities;

import java.io.Serializable;

/**
 * @author Ardak Saduakassov
 */
public class Error implements Serializable {
    private static final long serialVersionUID = 1L;

    public static enum Level { ERROR, WARNING, INFO, DEBUG }
    public static enum Stage { UNKNOWN, WEB_FORM_VALIDATION }

    private Level level;
    private Stage stage;

    private String location;
    private String descriptionEn;
    private String descriptionKz;
    private String descriptionRu;

    public Error() {
    }

    public Error(Level level, Stage stage, String location, String descriptionEn, String descriptionKz, String descriptionRu) {
        this.level = level;
        this.stage = stage;
        this.location = location;
        this.descriptionEn = descriptionEn;
        this.descriptionKz = descriptionKz;
        this.descriptionRu = descriptionRu;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getDescriptionKz() {
        return descriptionKz;
    }

    public void setDescriptionKz(String descriptionKz) {
        this.descriptionKz = descriptionKz;
    }

    public String getDescriptionRu() {
        return descriptionRu;
    }

    public void setDescriptionRu(String descriptionRu) {
        this.descriptionRu = descriptionRu;
    }

    public String getDescription(String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) return descriptionEn;
        String lang = languageCode.trim().toLowerCase();
        if (lang.equals("kz") || lang.equals("kk") || lang.equals("kaz")) return descriptionKz;
        else if (lang.equals("ru") || lang.equals("rus")) return descriptionRu;
        else return descriptionEn;
    }
}
