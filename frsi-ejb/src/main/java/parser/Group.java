package parser;

import java.util.*;

/**
 * Created by nuriddin on 9/1/16.
 */
public class Group {

    private GroupKey key;
    private Map<String, Set<String>> rows;
    private int index;

    public Group(GroupKey key) {
        this.key = key;
        rows = new HashMap<String, Set<String>>();
    }

    public void addRowNum(String rowNum, String bin) {
        if (!rows.containsKey(bin)) rows.put(bin, new HashSet<String>());
        Set<String> r = rows.get(bin);
        r.add(rowNum);
    }

    public void addRowNums(Collection<String> rownums, String bin) {
        if (!rows.containsKey(bin)) rows.put(bin, new HashSet<String>());
        Set<String> r = rows.get(bin);
        r.addAll(rownums);
    }

    public Map<String, Set<String>> getRows() {
        return rows;
    }

    public GroupKey getKey() {
        return key;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        return key.equals(group.key);

    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
