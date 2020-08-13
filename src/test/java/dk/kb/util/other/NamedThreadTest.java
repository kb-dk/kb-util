package dk.kb.util.other;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class NamedThreadTest {
    
    @Test
    void lineNumber() {
        String oldName = Thread.currentThread().getName();
        assertThat(oldName, is(not("testName")));
        
        try (NamedThread named = new NamedThread()) {
            //We test that the thread was named for this line, so do not change the line number or the test will fail
            assertThat(Thread.currentThread().getName(), is(("dk.kb.util.other.NamedThreadTest(NamedThreadTest.java:23) ")));
        }
        assertThat(Thread.currentThread().getName(), is(oldName));
        
    }
    
    @Test
    void as() {
        
        String oldName = Thread.currentThread().getName();
        assertThat(oldName, is(not("testName")));
        
        try (NamedThread named = NamedThread.as("testName")) {
            assertThat(Thread.currentThread().getName(), is(("testName")));
        }
        assertThat(Thread.currentThread().getName(), is(oldName));
        
    }
    
    @Test
    void postfix() {
        String oldName = Thread.currentThread().getName();
        assertThat(oldName, is(not("testName")));
        
        try (NamedThread named = NamedThread.postfix("testName")) {
            assertThat(Thread.currentThread().getName(), is((oldName + "->testName")));
        }
        assertThat(Thread.currentThread().getName(), is(oldName));
        
    }
    
    
    @Test
    void namedThreadFunction() {
        String oldName = Thread.currentThread().getName();
        assertThat(oldName, is(not("testName")));
        
        IntStream.range(0, 10).mapToObj(i -> i + "")
                 .parallel()
                 .map(NamedThread.namedThread((String i) -> Thread.currentThread().getName() + i,
                                              i -> "testName"))
                 .forEach(element -> assertThat(element, startsWith("testName")));
        
        IntStream.range(0, 10).mapToObj(i -> i + "")
                 .parallel()
                 .map((String i) -> Thread.currentThread().getName() + i)
                 .forEach(element -> assertThat(element, not(startsWith("testName"))));
        
    }
    
    @Test
    void testNamedThreadConsumer() {
        String oldName = Thread.currentThread().getName();
        assertThat(oldName, is(not("testName")));
        
        IntStream.range(0, 10).mapToObj(i -> i + "")
                 .parallel()
                 .forEach(NamedThread.namedThread((String i) -> {
                                                      String threadname = Thread.currentThread().getName();
                                                      assertThat(threadname, startsWith("testName"));
                                                  },
                                                  i -> "testName"));
        
        IntStream.range(0, 10).mapToObj(i -> i + "")
                 .parallel()
                 .forEach((String i) -> {
                     String threadname = Thread.currentThread().getName();
                     assertThat(threadname, not(startsWith("testName")));
                 });
        
    }
    
    @Test
    void testNamedThreadPredicate() {
        
        String oldName = Thread.currentThread().getName();
        assertThat(oldName, is(not("testName")));
        
        long renamed = IntStream.range(0, 10).mapToObj(i -> i + "")
                                .parallel()
                                .filter(NamedThread.namedThread((String i) -> Thread.currentThread()
                                                                                    .getName()
                                                                                    .startsWith("testName"),
                                                                i -> "testName"))
                                .count();
        assertThat(renamed,is(10));
        long notRenamed = IntStream.range(0, 10).mapToObj(i -> i + "")
                                   .parallel()
                                   .filter((String i) -> Thread.currentThread()
                                                               .getName()
                                                               .startsWith("testName"))
                                   .count();
        assertThat(notRenamed,is(0));
    }
}