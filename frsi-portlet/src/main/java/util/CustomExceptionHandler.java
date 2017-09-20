package util;

import com.liferay.faces.bridge.context.BridgeContext;
import org.primefaces.context.RequestContext;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

/**
 * Custom JSF exception handler
 *
 * @author Ardak Saduakassov
 */
public class CustomExceptionHandler extends ExceptionHandlerWrapper {

    private ExceptionHandler wrapped;

    public CustomExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return wrapped;
    }

    @Override
    public void handle() throws FacesException {
        String ns = BridgeContext.getCurrentInstance().getResponseNamespace();
        Iterator iterator = getUnhandledExceptionQueuedEvents().iterator();
        while (iterator.hasNext()) {
            ExceptionQueuedEvent event = (ExceptionQueuedEvent) iterator.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            Throwable throwable = context.getException();
            Throwable cause = throwable.getCause();
            Throwable rootCause = getRootCauseRecursive(cause);
            FacesContext facesContext = FacesContext.getCurrentInstance();

            try {
                String messageSummary = throwable.getMessage();

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                String messageDetails = sw.toString();

                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, messageSummary, messageDetails);

                Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
                sessionMap.put("errorMessage", message);

                if (rootCause != null && rootCause instanceof ConnectionLostException) {
                    RequestContext.getCurrentInstance().update(ns + ":ccDialogConnError:dlgConnError");
                    RequestContext.getCurrentInstance().execute("PF('wDlgConnError').show()");
                    RequestContext.getCurrentInstance().execute("PF('wConnPoll').start()");
                } else {
                    RequestContext.getCurrentInstance().update(ns + ":ccDialogError:dlgError");
                    RequestContext.getCurrentInstance().execute("PF('wDlgError').show()");
                }
            } finally {
                iterator.remove();
            }
        }
        getWrapped().handle();
    }

    private Throwable getRootCauseRecursive(Throwable cause) {
        if (cause == null || cause == cause.getCause() || cause.getCause() == null)
            return cause;
        else
            return getRootCauseRecursive(cause.getCause());
    }
}
