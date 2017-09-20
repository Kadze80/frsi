package form.excel;

import java.util.Map;

/**
 * Created by Nuriddin.Baideuov on 23.06.2015.
 */
public interface IDataProvider {
    void setCurrentContainer(String containerName);
    Map<String, String> nextRecData(String groupPrefix);
    boolean hasNextRec(String groupPrefix);
    Map<String, String> getAllData();
}
