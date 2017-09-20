package util;

public class AccessDeniedException extends RuntimeException {
    private String target;

    public AccessDeniedException(String target) {
        super("Нет доступа: "+target);
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
}
