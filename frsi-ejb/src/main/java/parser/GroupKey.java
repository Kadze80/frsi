package parser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nuriddin on 9/1/16.
 */
public class GroupKey {
    private Map<Long, Object> map;

    public GroupKey() {
        map = new HashMap<Long, Object>();
    }

    public void add(Long item) {
        add(item, null);
    }

    public void add(Long item, Object value) {
        map.put(item, value);
    }

    public Map<Long, Object> getData() {
        return map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupKey groupKey = (GroupKey) o;

        return map.equals(groupKey.map);

    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return "GroupKey{" +
                "map=" + map +
                '}';
    }
}
