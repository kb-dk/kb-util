package dk.kb.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class ResolverTest {
    
    @Test
    void getPathFromClasspath() {
        Path path = Resolver.getPathFromClasspath("resolver/testfile.txt");
        assertThat(Files.exists(path),is(true));
        assertThat(path.toString(),startsWith(System.getProperty("user.home")));
    
    }
    
    @Test
    void getNonExistingPathFromClasspath() {
        Path path = Resolver.getPathFromClasspath("nonexistingfolder/nofilehere.txt");
        assertThat(path, is(nullValue()));
    }

    @Test
    void readFileFromClasspath() throws IOException {
        assertThat(Resolver.readFileFromClasspath("resolver/testfile.txt"),is("contents\n"));
    }

}