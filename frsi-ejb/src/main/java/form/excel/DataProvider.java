package form.excel;

import java.util.*;

/**
 * Created by Nuriddin.Baideuov on 23.06.2015.
 */
public class DataProvider implements IDataProvider {
    Map<String, String> data;
    Map<String, List<String>> contKeys = new HashMap<String, List<String>>();
    String currentContainer;
    SortedMap<String, List<String>> rowKeys;
    Set<String> processedRows;


    public DataProvider(Map<String, String> data) {
        this.data = data;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String containerName = entry.getKey().substring(0, entry.getKey().indexOf("*"));
            if (!contKeys.containsKey(containerName))
                contKeys.put(containerName, new ArrayList<String>());
            contKeys.get(containerName).add(entry.getKey());
        }

    }


    @Override
    public void setCurrentContainer(String containerName) {
        this.currentContainer = containerName;
        rowKeys = new TreeMap<String, List<String>>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (!o1.contains(".") || !o2.contains("."))
                    return o1.compareTo(o2);

                String group1 = o1.substring(0, o1.lastIndexOf("."));
                String group2 = o2.substring(0, o2.lastIndexOf("."));

                if (!group1.equals(group2))
                    return group1.compareTo(group2);

                String lastIdPartByDot1 = o1.substring(o1.lastIndexOf(".") + 1);
                String lastIdPartByDot2 = o2.substring(o2.lastIndexOf(".") + 1);
                Integer minorId1;
                Integer minorId2;
                try {
                    minorId1 = Integer.valueOf(lastIdPartByDot1);
                } catch (NumberFormatException e) {
                    minorId1 = 0;
                }
                try {
                    minorId2 = Integer.valueOf(lastIdPartByDot2);
                } catch (NumberFormatException e) {
                    minorId2 = 0;
                }

                return minorId1 - minorId2;
            }
        });
        List<String> keys = contKeys.get(currentContainer);
        if (keys != null) {
            for (String key : keys) {
                String code = key.substring(key.lastIndexOf(":") + 1);
                if (!rowKeys.containsKey(code))
                    rowKeys.put(code, new ArrayList<String>());
                rowKeys.get(code).add(key);
            }
        }
        processedRows = new HashSet<String>();
    }

    @Override
    public Map<String, String> nextRecData(String groupPrefix) {
        Iterator<String> it = rowKeys.keySet().iterator();
        Map<String, String> recData = new HashMap<String, String>();
        while (it.hasNext() && recData.isEmpty()) {
            String code = it.next();
            if (groupPrefix == null || code.startsWith(groupPrefix)) {
                for (String key : rowKeys.get(code))
                    recData.put(key, data.get(key));
                it.remove();
            }
        }
        return recData;
    }

    @Override
    public boolean hasNextRec(String groupPrefix) {
        for (String code : rowKeys.keySet()) {
            if (groupPrefix == null || code.startsWith(groupPrefix))
                return true;
        }
        return false;
    }

    @Override
    public Map<String, String> getAllData() {
        return data;
    }
}
