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
public class ConnectionErrorBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger("fileLogger");

    private boolean pollEnabled = false;

    @PostConstruct
    public void init() { // Not optimized. Called each time.
        pollEnabled = true;
    }

    public boolean isPollDisabled() {
        return !pollEnabled;
    }
}
