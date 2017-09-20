package mb;

import ejb.PersistenceLocal;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.io.Serializable;

@ManagedBean
@RequestScoped
public class TestBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @EJB public PersistenceLocal persistence;

    public String getTestMessage() {
        return "Test message from Web module";
    }

    public String getTestMessageFromEjb() {
        return persistence == null ? "Null" : persistence.getTestMessage();
    }
}
