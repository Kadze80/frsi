package entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Entity
 *
 * @author Ardak Saduakassov
 */
public class FormTag implements Serializable {
    private static final long serialVersionUID = 1L;

    public boolean updateCalculatedFieldsManually;
    public List<SortField> sortFields;
    public boolean hasDynamicRows;
    public boolean canAttachedFile;
    public String periodType; //для выходных форм с динамическими полями
    public Map<String, String> keyFields;
}
