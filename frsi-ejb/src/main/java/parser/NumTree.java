package parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by nuriddin on 9/8/16.
 */
public class NumTree {
    private Map<String, Set<String>> numberTree;

    public NumTree(String keyColumn, String container, Set<String> keySet) {
        append(keyColumn, container, keySet);
    }

    private Map<String, Set<String>> build(String keyColumn, String container, Set<String> keySet) {
        Map<String, Set<String>> t = new HashMap<String, Set<String>>();
        for (String key : keySet) {
            String con = key.substring(0, key.indexOf("*"));
            if (!con.equalsIgnoreCase(container)) continue;

            String kCol = key.substring(key.indexOf(":") + 1, key.lastIndexOf(":"));
            if (kCol.isEmpty() || !kCol.equalsIgnoreCase(keyColumn)) continue;

            String r = key.substring(key.lastIndexOf(":") + 1);
            if (r.isEmpty() || !r.toLowerCase().startsWith("$d.")) continue;

            String parent = r.toLowerCase().substring("$d.".length());
            if (parent.contains(".")) parent = parent.substring(0, parent.lastIndexOf("."));
            if (!t.containsKey(parent)) t.put(parent, new HashSet<String>());
            t.get(parent).add(r);
        }
        return t;
    }

    public void append(String keyColumn, String container, Set<String> keySet) {
        if (numberTree == null) numberTree = new HashMap<String, Set<String>>();
        Map<String, Set<String>> b = build(keyColumn, container, keySet);
        for (Map.Entry<String, Set<String>> e : b.entrySet()) {
            if (numberTree.containsKey(e.getKey())) {
                numberTree.get(e.getKey()).addAll(e.getValue());
            } else {
                numberTree.put(e.getKey(), e.getValue());
            }
        }
    }

    public Set<String> getRowIds(String templ) {
        if (templ.toLowerCase().endsWith(".n")) {
            templ = templ.substring(0, templ.lastIndexOf(".n"));
        }
        if (templ.toLowerCase().startsWith("$d.")) {
            templ = templ.substring("$d.".length());
        }
        if (numberTree.containsKey(templ))
            return new HashSet<String>(numberTree.get(templ));
        else
            return new HashSet<String>();
    }
}
