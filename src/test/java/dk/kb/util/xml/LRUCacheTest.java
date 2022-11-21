package dk.kb.util.xml;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class LRUCacheTest {
    
    @Test
    public void testLRUCache1(){
        LRUCache<String,String> cache = new LRUCache<>(3);
        cache.put("1","a");
        cache.put("2","b");
        cache.put("3","c");
        cache.put("4","d");
        assertThat(cache.get("1"), nullValue());
        assertThat(cache.get("2"), is("b"));
    }
    @Test
    public void testLRUCache2(){
        LRUCache<String,String> cache = new LRUCache<>(3);
        cache.put("1","a");
        cache.put("2","b");
        cache.put("3","c");
        cache.get("1");
        cache.put("4","d");
        assertThat(cache.get("1"), is("a"));
        assertThat(cache.get("2"), nullValue());
    }
}