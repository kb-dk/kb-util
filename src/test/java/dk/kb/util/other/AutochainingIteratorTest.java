package dk.kb.util.other;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class AutochainingIteratorTest {
    
    @Test
    void testAutochainingIterator(){
    
        List<List<String>> lists = List.of(List.of("a", "b", "c"), List.of("d", "e", "f"));
        
        Iterator<String> it = new AutochainingIterator<>(
                (Integer offset) -> {
                    if (offset == null) {
                        offset = 0;
                    }
                    final List<String> strings;
                    if (offset < lists.size()) {
                        strings = lists.get(offset);
                    } else {
                        strings  = Collections.emptyList();
                    }
                    return AutochainingIterator.IteratorOffset.of(offset + 1, strings.iterator());
                }
        );
        String result = StringListUtils.asStream(it).collect(Collectors.joining(" "));
        assertThat(result,is("a b c d e f"));
    }

}