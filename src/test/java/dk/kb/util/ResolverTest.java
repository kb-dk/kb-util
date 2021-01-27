package dk.kb.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class ResolverTest {
    
    @Test
    void getPathFromClasspath() {
        Path path = Resolver.getPathFromClasspath("resolver/testfile.txt");
        assertThat(Files.exists(path),is(true));
        assertThat(path.toString(), startsWith(System.getProperty("user.dir")));
    
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

    @Test
    void resolveGlob() throws IOException {
        URL url1 = Resolver.resolveURL("yaml/overwrite-1.yaml");
        assertThat("The test files should be available", notNullValue().matches(url1));

        String parent2 = new File(url1.getPath()).getParentFile().getParent(); // 2 steps up so we can check path *
        assertThat("Sanity check path should work", Files.exists(Path.of(parent2, "yaml/overwrite-1.yaml")));

        assertThat("The right number of files should be resolved for file wildcard",
                   Resolver.resolveGlob(parent2 + "/yaml/overwrite*.yaml").size() == 2);

        assertThat("The right number of files should be resolved for single character file wildcard",
                   Resolver.resolveGlob(parent2 + "/yaml/overwrite-?.yaml").size() == 2);

        assertThat("The right number of files should be resolved for path wildcard",
                   Resolver.resolveGlob(parent2 + "/*/overwrite*.yaml").size() == 2);

        assertThat("The right number of files should be resolved for bracket wildcard",
                   Resolver.resolveGlob(parent2 + "/yaml/overwrite-[2-3].yaml").size() == 1);

        assertThat("The right number of files should be resolved combining globs",
                   Resolver.resolveGlob(parent2 + "/ya[klm]l/over*-[1-2].yam?").size() == 2);
    }

}