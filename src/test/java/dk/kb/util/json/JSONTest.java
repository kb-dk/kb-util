package dk.kb.util.json;

import dk.kb.util.Resolver;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JSONTest {
    
    @Test
    void toJsonTest1() {
        assertThat(JSON.toJson(List.of("foo", Map.of("bar","baz")),false), is("[\"foo\",{\"bar\":\"baz\"}]"));
    
        assertThat(JSON.toJson(List.of("foo", "bar", new Date(0)), false), is("[\"foo\",\"bar\",\"1970-01-01T00:00:00.000+00:00\"]"));
    }
    
    
    @Test
    void toJsonTestJava8Dates() {
        
        assertThat(JSON.toJson(List.of("foo", "bar",
                                       LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)), false), is("[\"foo\",\"bar\",\"1970-01-01T00:00:00\"]"));
    }
    
    @Test
    void ToJsonTest2() {
        assertThat(JSON.toJson(List.of("foo", Map.of("bar","baz"))), is("[ \"foo\", {\n  \"bar\" : \"baz\"\n} ]"));
    }
    
    @Test
    void fromJsonString() {
        ArrayList<?> result = JSON.fromJson("[ \"foo\", \"bar\" ]", ArrayList.class);
        assertThat(result, is(List.of("foo", "bar")));
    }
    
    @Test
    void FromJsonFile() {
        Path path = Resolver.getPathFromClasspath("json/test.json");
        if (path == null){
            fail("Path json/test.json not found on classpath");
        }
        ArrayList<?> result = JSON.fromJson(path.toFile(), ArrayList.class);
        assertThat(result, is(List.of("foo", "bar")));
    }
}
