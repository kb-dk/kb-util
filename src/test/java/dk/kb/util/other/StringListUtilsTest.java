package dk.kb.util.other;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class StringListUtilsTest {
    
    @Test
    void truncateMiddle() {
        
        String string
                = "SU1:SU1_DESK:HOLD,MOVE:HFBOT,HFGEO,HFPAN,HFZM,HFFAR,HFJTS,HFJSP,HFKVT,HAUDL,HATSS,HFFJ,HFTSS,LFZM,LFPAN,LFBOT,LFGEO,LFJTS,LFKVT,LFZM,LFFJ,HASEM,BAGEO,BAIVT,BALSA,BAPEN,BAHBS,BAINF,BAINH,BASEM";
        
        String result = StringListUtils.truncateMiddle(string, 100);
        assertThat(result.length(), is(100));
        assertThat(result,
                   is("SU1:SU1_DESK:HOLD,MOVE:HFBOT,HFGEO,HFPAN,HFZM,HF...M,BAGEO,BAIVT,BALSA,BAPEN,BAHBS,BAINF,BAINH,BASEM"));
    }

    @Test
    void truncateMiddleNoOp() {
        String input = "abcdefghij";
        int maxLength = 10;

        assertThat(input.length(), is(maxLength));
        String result = StringListUtils.truncateMiddle(input, maxLength);
        assertThat(result, is(equalTo(input)));
    }

    @Test
    void truncateEnd() {
        
        String string
                = "SU1:SU1_DESK:HOLD,MOVE:HFBOT,HFGEO,HFPAN,HFZM,HFFAR,HFJTS,HFJSP,HFKVT,HAUDL,HATSS,HFFJ,HFTSS,LFZM,LFPAN,LFBOT,LFGEO,LFJTS,LFKVT,LFZM,LFFJ,HASEM,BAGEO,BAIVT,BALSA,BAPEN,BAHBS,BAINF,BAINH,BASEM";
        
        String result = StringListUtils.truncateEnd(string, 100);
        assertThat(result.length(), is(100));
        assertThat(result,
                   is("SU1:SU1_DESK:HOLD,MOVE:HFBOT,HFGEO,HFPAN,HFZM,HFFAR,HFJTS,HFJSP,HFKVT,HAUDL,HATSS,HFFJ,HFTSS,LFZM..."));
        
    }
    
    @Test
    void truncateEndNoOp() {
        String input = "abcdefghij";
        int maxLength = 10;

        assertThat(input.length(), is(maxLength));
        String result = StringListUtils.truncateEnd(input, maxLength);
        assertThat(result, is(equalTo(input)));

    }

    @Test
    void testNotNull() {
        assertThat(StringListUtils.notNull(null), is(""));
        assertThat(StringListUtils.notNull("a"), is("a"));
        
    }
    
    @Test
    void testGetFirst() {
        assertThat(StringListUtils.getFirst(Arrays.asList("1", "2"), "3"), is("1"));
        assertThat(StringListUtils.getFirst(Collections.emptyList(), "3"), is("3"));
        assertThat(StringListUtils.getFirst(null, "3"), is("3"));
    }
    
    @Test
    void getParamOrProps() {
        Properties props
                = new Properties();
        props.put("foo", "bar");
        assertThat(StringListUtils.getParamOrProps("test", props, "foo"), is("test"));
        assertThat(StringListUtils.getParamOrProps(null, props, "foo"), is("bar"));
        assertThat(StringListUtils.getParamOrProps("", props, "foo"), is("bar"));
        assertThat(StringListUtils.getParamOrProps(null, props, "bar"), nullValue(String.class));
    }
    
    @Test
    void removeEmpties() {
        assertThat(StringListUtils.removeEmpties(Arrays.asList("a", "2", "", null)), is(Arrays.asList("a", "2")));
        assertThat(StringListUtils.removeEmpties(Arrays.asList("a", null, "2")), is(Arrays.asList("a", "2")));
        assertThat(StringListUtils.removeEmpties(Arrays.asList(null, "a", null, "2")), is(Arrays.asList("a", "2")));
        assertThat(StringListUtils.removeEmpties(Arrays.asList("", null)), is(Arrays.asList()));
        
        assertThat(StringListUtils.removeEmpties("a", "2", "", null), is(Arrays.asList("a", "2")));
        assertThat(StringListUtils.removeEmpties(null, "a", "2", ""), is(Arrays.asList("a", "2")));
        assertThat(StringListUtils.removeEmpties("", null), is(Arrays.asList()));
        
    }
    
    @Test
    void orDefault() {
        assertThat(StringListUtils.useDefaultIfNullOrEmpty("foo", "bar"), is("foo"));
        assertThat(StringListUtils.useDefaultIfNullOrEmpty("", "bar"), is("bar"));
        assertThat(StringListUtils.useDefaultIfNullOrEmpty(null, "bar"), is("bar"));
        Object value = new Object();
        assertThat(StringListUtils.useDefaultIfNull(null, value), is(value));
    }
    
    @Test
    void asList() {
        List<Object> ts = StringListUtils.toModifiableList("foo", "bar");
        assertThat(ts, CoreMatchers.instanceOf(ArrayList.class));
    }
    
    
    @Test
    void subString() {
        String string = "abcdefg";
        assertThat(StringListUtils.substring(string, 0, 3), is("abc"));
        assertThat(StringListUtils.substring(string, string.length()+1, string.length()+3), is(""));
        assertThat(StringListUtils.substring(string, string.length()+1, 2), is("ab"));
        assertThat(StringListUtils.substring(string, -3, 3), is("efgabc"));
        assertThat(StringListUtils.substring(string, -3, -4), is("efgabc"));
        assertThat(StringListUtils.substring(string, -5, -3), is("cd"));
        assertThat(StringListUtils.substring(string, -3, 20), is("efg"));
        assertThat(StringListUtils.substring(string, 0, string.length()+3), is("abcdefg"));
        assertThat(StringListUtils.substring(string, -2, 0), is("fg"));
        assertThat(StringListUtils.substring(string, 2, -1), is("cdef"));
    }
    
    @Test
    void removeSubstrings() {
        List<String> actual = StringListUtils.removeSubstrings(List.of("aaabbb", "aaa", "b"))
                                      .stream().sorted().collect(Collectors.toList());
        List<String> expected = List.of("aaabbb")
                                    .stream().sorted().collect(Collectors.toList());
        assertThat(actual, is(expected));
    }

    @Test
    void removeSubstringsReverseOrder() {
        List<String> actual = StringListUtils.removeSubstrings(List.of("b", "aaa", "aaabbb"))
                                      .stream().sorted().collect(Collectors.toList());
        List<String> expected = List.of("aaabbb")
                                    .stream().sorted().collect(Collectors.toList());
        assertThat(actual, is(expected));
    }

}