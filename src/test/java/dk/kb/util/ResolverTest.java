package dk.kb.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

class ResolverTest {
    
    @Test
    void getPathFromClasspath() {
        Path path = Resolver.getPathFromClasspath("resolver/testfile.txt");
        assertThat(Files.exists(path),is(true));
        assertThat(path.toString(),startsWith(System.getProperty("user.home")));
    
    }
    
    @Test
    void readFileFromClasspath() throws IOException {
        assertThat(Resolver.readFileFromClasspath("resolver/testfile.txt"),is("contents\n"));
    }
}