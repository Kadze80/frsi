package mb;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@ManagedBean(eager = true)
@ApplicationScoped
public class SortBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateFormat dateFormatDMYT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public int sortDateStringFormatDMYT(Object o1, Object o2) {
        try {
            Date date1 = dateFormatDMYT.parse((String) o1);
            Date date2 = dateFormatDMYT.parse((String) o2);
            return date1.compareTo(date2);
        } catch (Exception e) {
            return 0;
        }
    }
}