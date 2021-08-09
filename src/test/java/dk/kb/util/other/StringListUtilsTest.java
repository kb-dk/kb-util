package dk.kb.util.other;

import org.apache.commons.collections4.list.SetUniqueList;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
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
    
    
    @Test
    public void testCollectionsEmptyListImmutable() {
        List<String> immutable = Collections.emptyList();
        try {
            immutable.add("test");
            Assertions.fail("empty list is not immutable, what magic is this?");
        } catch (UnsupportedOperationException e){
            //expected
        }
        List<String> list = StringListUtils.toModifiableList(immutable);
        Assertions.assertEquals(0,list.size());
        list.add("Test");
        Assertions.assertEquals(list.get(0),("Test"));
    }
    
    @Test
    public void testListsOfImmutable() {
        List<String> immutable = List.of();
        try {
            immutable.add("test");
            Assertions.fail("empty list is not immutable, what magic is this?");
        } catch (UnsupportedOperationException e){
            //expected
        }
        List<String> list = StringListUtils.toModifiableList(immutable);
        Assertions.assertEquals(0,list.size());
        list.add("Test");
        Assertions.assertEquals(list.get(0),("Test"));
    }
    
    @Test
    public void testArraysAsListImmutable() {
        List<String> immutable = Arrays.asList();
        try {
            immutable.add("test");
            Assertions.fail("empty list is not immutable, what magic is this?");
        } catch (UnsupportedOperationException e){
            //expected
        }
        List<String> list = StringListUtils.toModifiableList(immutable);
        Assertions.assertEquals(0,list.size());
        list.add("Test");
        Assertions.assertEquals(list.get(0),("Test"));
    }
    
    
    @Test
    public void testApacheCollections() {
        List<String> immutable = new UnmodifiableList<>(new ArrayList<>());
        try {
            immutable.add("test");
            Assertions.fail("empty list is not immutable, what magic is this?");
        } catch (UnsupportedOperationException e){
            //expected
        }
        testToModifiableList(immutable);
    }
    
    @Test
    public void testApacheSetList() {
        List<String> sList = SetUniqueList.setUniqueList(StringListUtils.toModifiableList(Arrays.asList("foo", "bar")));

        testToModifiableList(sList);
    }

    private void testToModifiableList(List<String> immutableList) {
        final int startSize = immutableList.size();
        List<String> list = StringListUtils.toModifiableList(immutableList);
        Assertions.assertEquals(startSize, list.size());
        list.add("Test");
        Assertions.assertEquals(list.get(startSize),("Test"));
    }

    // Checking for immutability on an ArrayList (an extremely common scenario) causes an expansion of the ArrayList
    // when the internal capacity of the ArrayList is equal to the number of elements (a fairly common scenario).
    @Test
    public void testArrayListExpansionOnToMutableList() throws NoSuchFieldException, IllegalAccessException {
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList("foo", "bar"));

        Field elementData = arrayList.getClass().getDeclaredField("elementData");
        elementData.setAccessible(true);
        final int startCapacity = ((Object[])elementData.get(arrayList)).length;
        Assertions.assertEquals(2, startCapacity,
                                "Start capacity should equal the number of initial elements");

        testToModifiableList(arrayList);

        final int afterCheckCapacity = ((Object[])elementData.get(arrayList)).length;
        Assertions.assertEquals(2, afterCheckCapacity,
                                "After check capacity should equal the number of initial elements");
    }

    @Test
    public void testSameList() {
        List<String> testList = new LinkedList<>();
        List<String> modifiableTestList = StringListUtils.toModifiableList(testList);
        //Same object, not same contents in new wrapping
        Assertions.assertEquals(testList, modifiableTestList);
        Assertions.assertEquals(0,testList.size());
    }
    
    
    @Test
    public void testAppendOnlyList() {
        List<String> evergrowing = new ArrayList<String>(List.of("Test")) {
            @Override
            public String remove(int index) {
                throw new UnsupportedOperationException("Remove is not allowed");
            }
    
            @Override
            public boolean remove(Object o) {
                throw new UnsupportedOperationException("Remove is not allowed");
            }
    
            @Override
            protected void removeRange(int fromIndex, int toIndex) {
                throw new UnsupportedOperationException("Remove is not allowed");
            }
    
            @Override
            public boolean removeAll(Collection<?> c) {
                throw new UnsupportedOperationException("Remove is not allowed");
            }
    
            @Override
            public boolean retainAll(Collection<?> c) {
                throw new UnsupportedOperationException("Remove is not allowed");
            }
    
            @Override
            public boolean removeIf(Predicate<? super String> filter) {
                throw new UnsupportedOperationException("Remove is not allowed");
            }
        };
        try {
            evergrowing.remove("test");
            Assertions.fail("appendOnlyList allows removes..., what magic is this?");
        } catch (UnsupportedOperationException e){
            //expected
        }
        List<String> list = StringListUtils.toModifiableList(evergrowing);
        Assertions.assertEquals(1,list.size());
        list.add("Test2");
        Assertions.assertEquals(list.get(0),("Test"));
        Assertions.assertEquals(list.get(1),("Test2"));
    
    }
}
