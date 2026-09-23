package dk.kb.util.xml;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class LRUCacheTest {
    
    @Test
    public void testLRUCache1() {
        LRUCache<String, String> cache = new LRUCache<>(3);
        cache.put("1", "a");
        cache.put("2", "b");
        cache.put("3", "c");
        cache.put("4", "d");
        assertThat(cache, is(Map.of("2", "b", "3", "c", "4", "d")));
    }

    @Test
    public void testLRUCache2() {
        LRUCache<String, String> cache = new LRUCache<>(3);
        cache.put("1", "a");
        cache.put("2", "b");
        cache.put("3", "c");
        cache.get("1");
        cache.put("4", "d");
        assertThat(cache, is(Map.of("1", "a", "3", "c", "4", "d")));
    }

    @Test
    void holdsCapacityElements() {
        int capacity = 7;
        LRUCache<String, String> cache = new LRUCache<>(capacity);
        cache.put("1", "a");
        cache.put("2", "b");
        cache.put("3", "c");
        cache.put("4", "d");
        cache.put("5", "e");
        cache.put("6", "f");
        cache.put("7", "g");
        cache.put("8", "h");
        cache.put("9", "i");
        assertThat(cache.size(), is(capacity));
    }

}