package dk.kb.util.xml;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple LRUcache based on the LinkedHashMap
 *
 * @see LinkedHashMap
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private int initialCapacity;

    public LRUCache(int initialCapacity) {
        super(initialCapacity + 1, 0.75f, true);
        this.initialCapacity = initialCapacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        if (size() > initialCapacity) {
            return true;
        }
        return false;
    }
}


