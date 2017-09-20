package entities;

import oracle.jdbc.OracleCallableStatement;

/**
 * Created by Ayupov.Bakhtiyar on 08.02.2017.
 */
public class RefHelperOut {

    private OcsNumMap ocsNumMap;
    private AbstractReference item;


    // region Getter and Setter

    public OcsNumMap getOcsNumMap() {
        return ocsNumMap;
    }

    public void setOcsNumMap(OcsNumMap ocsNumMap) {
        this.ocsNumMap = ocsNumMap;
    }

    public AbstractReference getItem() {
        return item;
    }

    public void setItem(AbstractReference item) {
        this.item = item;
    }

    // endregion

    // region equals and hashcode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RefHelperOut that = (RefHelperOut) o;

        if (ocsNumMap != null ? !ocsNumMap.equals(that.ocsNumMap) : that.ocsNumMap != null) return false;
        return !(item != null ? !item.equals(that.item) : that.item != null);

    }

    @Override
    public int hashCode() {
        int result = ocsNumMap != null ? ocsNumMap.hashCode() : 0;
        result = 31 * result + (item != null ? item.hashCode() : 0);
        return result;
    }


    // endregion
}
