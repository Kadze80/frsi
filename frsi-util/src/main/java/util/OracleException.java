package util;

/**
 * Created by Ayupov.Bakhtiyar on 27.04.2015.
 */
public class OracleException extends Exception {

    public OracleException( String err_msg) {
        super(err_msg);
    }

    public OracleException() {}

}
