package dk.kb.util;

import org.hamcrest.CoreMatchers;
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

    @Test
    void resolveDottedGlob() throws IOException {
        URL url1 = Resolver.resolveURL("yaml/subfolder/somefile.yaml");
        assertThat("The test file should be available", notNullValue().matches(url1));

        Path known = Path.of(url1.getPath()); // /$HOME/$USER/UNKNOWN/kb-util/target/test-classes/yaml/subfolder/somefile.yaml
        String parent = known.getParent().getParent().getParent().getFileName().toString(); // test-classes

        assertThat("The file '" + known + "' should be resolved with /./ in the path",
                   is(Resolver.resolveGlob("yaml/subfolder/./somefile.yaml").size()).matches(1));

        assertThat("The file '" + known + "' should be resolved with /../ in the path",
                   is(Resolver.resolveGlob("yaml/subfolder/../subfolder/somefile.yaml").size()).matches(1));

        String glob = "../" + parent + "/yaml/subfolder/somefile.yaml";

        // During mvn release:perform, a check git checkout is done under "target", resulting in 2 matches from
        // the resolver instead of the usual 1. The asserts below accepts both 1+ matches

        assertThat("The file '" + known + "' should be resolved with ../ at the start of the path for glob '" +
                   glob + "' derived from absolute path '" + known + "' but returned matches " +
                   Resolver.resolveGlob(glob),
                is(Resolver.resolveGlob(glob).isEmpty()).matches(false));

        assertThat("The file '" + known + "' should be resolved with /../../ in the path",
                   is(Resolver.resolveGlob("yaml/subfolder/../../yaml/subfolder/somefile.yaml").isEmpty()).matches(false));

        assertThat("The file '" + known + "' should be resolved with both /./ and /../ in the path",
                   is(Resolver.resolveGlob("yaml/subfolder/./../subfolder/somefile.yaml").isEmpty()).matches(false));
    }

    @Test
    void extractSegments() {
        assertThat("Glob segments that dots higher than root at the start should not be seen as viable",
                   is(Resolver.extractSegments("/../foo/bar.txt").size()).matches(0));

        assertThat("Sanity check of dotting up to the edge should pass",
                   is(Resolver.extractSegments("/foo/../foo/bar.txt").size()).matches(2));  // ["foo", "bar.txt"]

        assertThat("Glob segments that dots higher than root at some point not be seen as viable",
                   is(Resolver.extractSegments("/foo/../../boom/bar.txt").size()).matches(0));

    }


    @Test
    void resolveGlobRelative() {
        assertThat("Globbing relative to the class path should work",
                   Resolver.resolveGlob("yaml/overwrite*.yaml").size() == 2);
    }

}