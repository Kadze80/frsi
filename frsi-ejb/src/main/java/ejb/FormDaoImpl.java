package ejb;

import org.apache.log4j.Logger;

import javax.ejb.Stateless;

/**
 * Form DAO EJB implementation
 *
 * @author Ardak Saduakassov
 */
@Stateless
public class FormDaoImpl implements FormDaoLocal, FormDaoRemote {
    private static final Logger logger = Logger.getLogger("fileLogger");

}
