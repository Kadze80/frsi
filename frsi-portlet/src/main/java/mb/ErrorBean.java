package mb;

import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Map;

/**
 * Managed bean
 *
 * @author Ardak Saduakassov
 */
@ManagedBean
@RequestScoped
public class ErrorBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    private FacesMessage message;
    private String messageSummary;
    private String messageDetail;
    private String messageType;
    private String messageColor;

    @PostConstruct
    public void init() { // Not optimized. Called each time.
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
        message = (FacesMessage) sessionMap.get("errorMessage");

        messageType = "Ошибка";
        messageColor = "black";

        if (message == null) {
            messageSummary = "Ошибка неизвестного типа";
            messageDetail = null;
        } else {
            messageSummary = message.getSummary();
            messageDetail = message.getDetail();
            if (message.getSeverity().equals(FacesMessage.SEVERITY_INFO)) {
                messageType = "Информация";
                messageColor = "green";
            } else if (message.getSeverity().equals(FacesMessage.SEVERITY_WARN)) {
                messageType = "Предупреждение";
                messageColor = "blue";
            } else if (message.getSeverity().equals(FacesMessage.SEVERITY_ERROR)) {
                messageType = "Ошибка";
                messageColor = "red";
            } else if (message.getSeverity().equals(FacesMessage.SEVERITY_FATAL)) {
                messageType = "Неустранимая ошибка";
                messageColor = "red";
            }
        }
    }

    public String getMessageSummary() {
        return messageSummary;
    }

    public String getMessageDetail() {
        return messageDetail;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getMessageColor() {
        return messageColor;
    }
}
