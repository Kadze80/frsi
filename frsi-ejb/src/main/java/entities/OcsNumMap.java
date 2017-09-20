package entities;

import oracle.jdbc.OracleCallableStatement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ayupov.Bakhtiyar on 09.02.2017.
 */
public class OcsNumMap {
    private OracleCallableStatement ocs;
    private Map<String, Integer> numMap = new HashMap<String, Integer>();


    // region Getter and Setter
    public OracleCallableStatement getOcs() {
        return ocs;
    }

    public void setOcs(OracleCallableStatement ocs) {
        this.ocs = ocs;
    }

    public Map<String, Integer> getNumMap() {
        return numMap;
    }

    public void setNumMap(Map<String, Integer> numMap) {
        this.numMap = numMap;
    }

    // endregion
}
