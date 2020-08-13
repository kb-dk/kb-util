package dk.kb.util.other;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringListUtilsTest {
    
    @Test
    void cutMiddle() {
    
        String string
                = "SU1:SU1_DESK:HOLD,MOVE:HFBOT,HFGEO,HFPAN,HFZM,HFFAR,HFJTS,HFJSP,HFKVT,HAUDL,HATSS,HFFJ,HFTSS,LFZM,LFPAN,LFBOT,LFGEO,LFJTS,LFKVT,LFZM,LFFJ,HASEM,BAGEO,BAIVT,BALSA,BAPEN,BAHBS,BAINF,BAINH,BASEM";
    
        String result = StringListUtils.cutMiddle(string, 100);
        assertThat(result.length(),is(100));
        assertThat(result,is("SU1:SU1_DESK:HOLD,MOVE:HFBOT,HFGEO,HFPAN,HFZM,HF...M,BAGEO,BAIVT,BALSA,BAPEN,BAHBS,BAINF,BAINH,BASEM"));
    }
    
    @Test
    void cutEnd() {
        
        String string
                = "SU1:SU1_DESK:HOLD,MOVE:HFBOT,HFGEO,HFPAN,HFZM,HFFAR,HFJTS,HFJSP,HFKVT,HAUDL,HATSS,HFFJ,HFTSS,LFZM,LFPAN,LFBOT,LFGEO,LFJTS,LFKVT,LFZM,LFFJ,HASEM,BAGEO,BAIVT,BALSA,BAPEN,BAHBS,BAINF,BAINH,BASEM";
        
        String result = StringListUtils.cutEnd(string, 100);
        assertThat(result.length(),is(100));
        assertThat(result,is("SU1:SU1_DESK:HOLD,MOVE:HFBOT,HFGEO,HFPAN,HFZM,HFFAR,HFJTS,HFJSP,HFKVT,HAUDL,HATSS,HFFJ,HFTSS,LFZM..."));

    }
    
    @Test
    void testNotNull(){
        assertThat(StringListUtils.notNull(null), is(""));
        assertThat(StringListUtils.notNull("a"), is("a"));
        
    }
    
    @Test
    void testFirstOf(){
        assertThat(StringListUtils.firstOf(Arrays.asList("1","2"),"3"),is("1"));
        assertThat(StringListUtils.firstOf(Collections.emptyList(),"3"),is("3"));
        assertThat(StringListUtils.firstOf(null,"3"),is("3"));
    }
    @Test
    void getParamOrProps(){
        Properties props
                = new Properties();
        props.put("foo","bar");
        assertThat(StringListUtils.getParamOrProps(props,"test","foo"),is("test"));
        assertThat(StringListUtils.getParamOrProps(props,null,"foo"),is("bar"));
        assertThat(StringListUtils.getParamOrProps(props,"","foo"),is("bar"));
        assertThat(StringListUtils.getParamOrProps(props,null,"bar"), nullValue(String.class));
    }
    @Test
    void removeEmpties(){
        assertThat(StringListUtils.removeEmpties(Arrays.asList("a","2","",null)),is(Arrays.asList("a","2")));
        assertThat(StringListUtils.removeEmpties(Arrays.asList("",null)),is(Arrays.asList()));
    
        assertThat(StringListUtils.removeEmpties("a","2","", null),is(Arrays.asList("a","2")));
        assertThat(StringListUtils.removeEmpties("",null),is(Arrays.asList()));
    
    }
  
    @Test
    void setOf(){
        List<String> actual = new ArrayList<>(StringListUtils.setOf("aaabbb", "aaa", "aaa"))
                                      .stream().sorted().collect(Collectors.toList());
        List<String> expected = List.of("aaabbb", "aaa")
                                    .stream().sorted().collect(Collectors.toList());
        assertThat(actual, CoreMatchers.is(expected));
    }
    
    @Test
    void orDefault(){
        assertThat(StringListUtils.orDefault("foo","bar"),is("foo"));
        assertThat(StringListUtils.orDefault("","bar"),is("bar"));
        assertThat(StringListUtils.orDefault(null,"bar"),is("bar"));
        Object value = new Object();
        assertThat(StringListUtils.orDefault(null,value),is(value));
    }
    
    @Test
    void asList(){
        List<Object> ts = StringListUtils.asList("foo", "bar");
        assertThat(ts, CoreMatchers.instanceOf(ArrayList.class));
    }
    

    @Test
    void subString(){
        String string = "abcdefg";
        assertThat(StringListUtils.substring(string,0,3),is("abc"));
        assertThat(StringListUtils.substring(string,-3,3),is("efgabc"));
        assertThat(StringListUtils.substring(string,-3,20),is("efg"));
        assertThat(StringListUtils.substring(string,-5,-3),is("cd"));
    
    }
}