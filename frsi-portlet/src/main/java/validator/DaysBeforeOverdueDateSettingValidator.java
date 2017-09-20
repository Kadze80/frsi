package validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

@FacesValidator("daysBeforeOverdueDateSettingValidator")
public class DaysBeforeOverdueDateSettingValidator implements Validator {

    private boolean noticeOverdueDateSettingValue;

    @Override
    public void validate(FacesContext facesContext, UIComponent uiComponent, Object o) throws ValidatorException {
        if (o == null)
            return;
        int value = (Integer) o;
        if (noticeOverdueDateSettingValue && value < 1)
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Значение должно быть больше нуля",
                    "Значение должно быть больше нуля"));
    }

    public boolean isNoticeOverdueDateSettingValue() {
        return noticeOverdueDateSettingValue;
    }

    public void setNoticeOverdueDateSettingValue(boolean noticeOverdueDateSettingValue) {
        this.noticeOverdueDateSettingValue = noticeOverdueDateSettingValue;
    }
}
