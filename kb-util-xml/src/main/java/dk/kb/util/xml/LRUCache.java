package dk.kb.util.xml;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple LRU cache based on the LinkedHashMap.
 * Least recently used entries are removed to avoid exceeding capacity.
 *
 * @see LinkedHashMap
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 8750905580314069364L;
    private final int capacity;

    public LRUCache(int capacity) {
        super(capacity + 1, 0.75f, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > capacity;
    }
}


