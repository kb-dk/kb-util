package dk.kb.util.reader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CircularIntBufferTest {

    private CircularIntBuffer buffer;

    @BeforeEach
    void setup() {
        buffer = new CircularIntBuffer(4, 4);
        buffer.add(3);
        buffer.add(5);
        buffer.add(12);
    }

    @Test
    void containsAddedValues() {
        assertTrue(buffer.contains(3));
        assertTrue(buffer.contains(12));
    }

    @Test
    void  containsEqualIntegerObject() {
        int probablyOutsideCachedValues = 214_748_364;
        buffer.add(probablyOutsideCachedValues);
        assertTrue(buffer.contains(probablyOutsideCachedValues));
    }

    @Test
    void  doesNotContainValuesNotAdded() {
        assertFalse(buffer.contains(2));
        assertFalse(buffer.contains(4));
        assertFalse(buffer.contains(11));
        assertFalse(buffer.contains(13));

        assertFalse(buffer.contains(3.0));
        assertFalse(buffer.contains((short) 12));
        assertFalse(buffer.contains(5L));

        assertFalse(buffer.contains("3"));
        assertFalse(buffer.contains(null));
    }

}
