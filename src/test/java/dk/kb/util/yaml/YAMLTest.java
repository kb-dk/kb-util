package dk.kb.util.yaml;

import dk.kb.util.Resolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class YAMLTest {
    @SuppressWarnings("ConstantConditions")
    @Test
    public void testToString() throws IOException {
        String contents = Files.readAllLines(Resolver.getPathFromClasspath("test.yml"))
                .stream()
                .map(line -> line.replaceAll("#.*$", "").stripTrailing())
                .filter(line -> !line.isBlank())
                .collect(Collectors.joining("\n")) + "\n";
        assertEquals(contents, YAML.resolveLayeredConfigs("test.yml").toString());
    }

    @Test
    public void testLoad() throws IOException {
        YAML.resolveLayeredConfigs("test.yml").getSubMap("test");
        //YAML.resolveLayeredConfigs("test.yml");
    }

    @Test
    public void testEmptyList() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("test.yml");
        List<YAML> result = yaml.getYAMLList("test.emptyList");
        assertEquals(0, result.size());
    }

    @Test
    public void testNested() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("test.yml");
        assertEquals("Hello World", yaml.getString("test.somestring"),
                "Nested request for string should be supported");
    }

    @Test
    public void testRoot() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("test.yml").getSubMap("test");
        assertEquals("Hello World", yaml.getString("somestring"),
                "Direct request for string from resolved root should be supported");
    }

    @Test
    public void testArray() throws IOException {
        YAML yaml = new YAML("test.yml").getSubMap("test");
        assertEquals("[a, b, c]", yaml.getList("arrayofstrings").toString(),
                "Arrays of strings should be supported");
    }

    @Test
    public void testKeptPath() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("test.yml").getSubMap("test");
        assertEquals("nested.sublevel2string: sub1\n", yaml.getSubMap("nested", true).toString(),
                "When we get map with subkeys preserved, we should see the nested previs");
    }

    @Test
    public void testMissingSubMap() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("test.yml").getSubMap("test");
        try {
            yaml.getSubMap("nonexisting");
        } catch (NotFoundException e) {
            return;
        }
        fail("Requesting a non-existing sub map should result in a NotFoundException");
    }

    @Test
    public void testMissingString() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("test.yml").getSubMap("test");
        try {
            yaml.getString("nonexisting");
        } catch (NotFoundException e) {
            return;
        }
        fail("Requesting a non-existing String should result in a NotFoundException");
    }

    @Test
    public void testDefault() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("test.yml").getSubMap("test");
        assertEquals(87, yaml.getInteger("nonexisting", 87),
                "Requesting a non-existing integer with a default value should work");
    }


    @Test
    public void testNestedExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").extrapolate(true);
        assertEquals(System.getProperties().getProperty("user.home") + " Hello World", yaml.getString("test.somestring"),
                "Nested request for string should be supported");
    }

    @Test
    public void testRootExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test").extrapolate(true);
        assertEquals(System.getProperties().getProperty("user.home") + " Hello World", yaml.getString("somestring"),
                "Direct request for string from resolved root should be supported");
    }

    @Test
    public void testArrayExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test").extrapolate(true);
        assertEquals("[a, " + System.getProperties().getProperty("user.name") + ", c]", yaml.getList("arrayofstrings").toString(),
                "Arrays of strings should be supported");
    }

    @Test
    public void testKeptPathExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test").extrapolate(true);

        assertEquals("nested.sublevel2string: ${user.name}\n", yaml.getSubMap("nested", true).toString(),
                "When we get map with subkeys preserved, we should see the nested previs. Extrapolation does NOT happen on toString");
    }

    @Test
    public void testMissingSubMapExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test").extrapolate(true);
        try {
            yaml.getSubMap("nonexisting");
        } catch (NotFoundException e) {
            return;
        }
        fail("Requesting a non-existing sub map should result in a NotFoundException");
    }

    @Test
    public void testMissingStringExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").extrapolate(true).getSubMap("test");
        try {
            yaml.getString("nonexisting");
        } catch (NotFoundException e) {
            return;
        }
        fail("Requesting a non-existing String should result in a NotFoundException");
    }

    @Test
    public void testNonResolvableExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolatedNonresolvable.yml").extrapolate(true);
        try {
            yaml.getString("test.somestring"); // Contains ${cannot.be.resolved}
            fail("Requesting a non-resolvable system property should fail");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testDefaultExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").extrapolate(true).getSubMap("test");
        assertEquals(87, yaml.getInteger("nonexisting", 87),
                "Requesting a non-existing integer with a default value should work");
    }


    @Test
    public void testIntExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test").extrapolate(true);
        Integer i = yaml.getInteger("someint");
        assertTrue(i instanceof Integer, "Extracted object should be an Integer");
        assertTrue(i >= 11, "Extracted integer should be at least 11 (the lowest Java version " +
                "supported by kb-util), but was " + i);
    }

    @Test
    public void testIntArrayExtrapolatedType() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test").extrapolate(true);
        List<Integer> ints = yaml.getList("arrayofints");
        assertTrue(ints.get(0) instanceof Integer,
                "First element in the extracted Integer list should be an Integer but was " + ints.get(0).getClass());
    }

    @Test
    public void testIntArrayExtrapolatedContent() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test").extrapolate(true);
        List<Integer> ints = yaml.getList("arrayofints");
        assertTrue(ints.get(0) >= 11,
                "Arrays of integers should be supported. Expected first element to be >= 11, but got array " + ints);
    }

    @Test
    public void testTypesExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test").extrapolate(true);
        final double EXPECTED = 55.00;
        double actual = yaml.getDouble("somedouble");

        assertTrue(actual >= EXPECTED * 0.99, // 55 = Java 11. Anything highter is also OK
                "Double should be supported and as expected. Expected=" + EXPECTED + ", resolved=" + actual);
        assertEquals(true, yaml.getBoolean("somebool"),
                "Boolean should be supported and as expected");
    }

    @Test
    public void testExtrapolatedExceptionImplicit() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test").extrapolate(true);
        try {
            yaml.getString("exceptionimplicit");
            fail("Requesting a property with expansion of implicit sys:, where the expansion could not be fulfilled, should throw an Exception");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testExtrapolatedExceptionExplicitEnv() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test").extrapolate(true);
        try {
            yaml.getString("exceptionenv");
            fail("Requesting a property with expansion of explicit env:, where the expansion could not be fulfilled, should throw an Exception");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testExtrapolatedFallback() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test").extrapolate(true);
        String actual = yaml.getString("fallback");
        assertEquals("mydefault", actual,
                "Looking up a non-existing property with fallback should return the fallback");
    }

    @Test
    public void testExtrapolatedEnvFallback() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test").extrapolate(true);
        String actual = yaml.getString("envfallback");
        assertEquals("envdefault", actual,
                "Looking up a non-existing environment variable with fallback should return the fallback");
    }

    @Test
    public void testExtrapolatedNestedFallbackHit() throws IOException {
        final String home = System.getProperties().getProperty("user.home");
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test").extrapolate(true);
        String actual = yaml.getString("nestedfallback");
        assertEquals(home, actual,
                "Looking up a non-existing environment variable with fallback to an existing environment variable should return the value of the secondary environment variable");
    }

    @Test
    public void testExtrapolatedNestedFallbackFail() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test").extrapolate(true);
        String actual = yaml.getString("nestedfallbackfail");
        assertEquals("reach", actual,
                "Looking up a non-existing environment variable with nonexisting fallback resolve  should return the fallback's fallback");
    }

    @Test
    public void testExtrapolatedExplicit() throws IOException {
        assertExtrapolated("sysuser", System.getProperty("user.name"),
                "Looking up a sys:-prefixed system property should work");
    }

    @Test
    public void testExtrapolatedEnv() throws IOException {
        assertExtrapolated("envuser", System.getProperty("user.name"),
                "Looking up an environment variable should work (assuming env:USERNAME == sys:user.name)");
    }

    @Test
    public void testExtrapolatedMixedFallbackEnvSys() throws IOException {
        assertExtrapolated("mixedfallbackenvsys", System.getProperty("user.name"),
                "Nested fallback should work across env/sys");
    }

    @Test
    public void testListEntryExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test").extrapolate(true);
        assertEquals(System.getProperty("user.name"), yaml.getString("arrayofstrings[1]"),
                "Index-specified entry in arrays should be gettable");
        assertEquals("c", yaml.getString("arrayofstrings[last]"),
                "Last entry in arrays should be gettable");
    }

    @Test
    public void testFailingListEntryExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test");
        Assertions.assertThrows(Exception.class,
                () -> yaml.getString("arrayofstrings[3]"),
                "Requesting an index in a collection greater than or equal to list length should fail");
    }


    @Test
    public void testAlias() throws IOException {
        YAML yaml = YAML.resolveMultiConfig("alias.yml");
        assertEquals("FooServer", yaml.getString("serviceSetup.theServer"),
                "The alias YAML should have the expected value for alias-using 'theServer'");
    }

    @Test
    public void testMultiConfig() throws IOException {
        YAML yaml = YAML.resolveMultiConfig("config_pair_part_1.yml", "config_pair_part_2.yml");
        assertEquals("bar", yaml.getString("serviceSetup.someString"),
                "The merged YAML should have the expected value for plain key 'somestring'");
        assertEquals("FooServer", yaml.getString("serviceSetup.theServer"),
                "The merged YAML should have the expected value for alias-using 'theServer'");
    }

    @Test
    public void testLayeredConfigsDefault() throws IOException {
        YAML first = YAML.resolveLayeredConfigs("yaml/overwrite-1.yaml");
        YAML multi = YAML.resolveLayeredConfigs("yaml/overwrite-1.yaml", "yaml/overwrite-2.yaml");

        final String FIRST_ONLY = "upperA.subYAML.sub2Element";
        assertTrue(first.containsKey(FIRST_ONLY),
                "Non-merged first file should contain element '" + FIRST_ONLY + "'");
        assertEquals("bar", multi.getString(FIRST_ONLY),
                "Merged YAML should contain element '" + FIRST_ONLY + "' from file #1");

        assertEquals("baz", multi.getString("upperA.subElement"),
                "Merged YAML should contain overwritten element 'upperA.subElement'");

        assertEquals(12, multi.getInteger("upperC.subCElement"),
                "Merged YAML should contain element 'upperC.subClement' from file #2");

        List<String> aList = multi.getList("upperA.subList");
        List<String> eList = Arrays.asList("three", "four");
        assertEquals(eList, aList,
                "Merged YAML should contain the last list");
    }

    @Test
    public void testLayeredConfigsFail() throws IOException {
        try {
            YAML.resolveLayeredConfigs(YAML.MERGE_ACTION.fail, YAML.MERGE_ACTION.fail,
                    "yaml/overwrite-1.yaml", "yaml/overwrite-2.yaml");
            fail("Merging with duplicate keys and MERGE_ACTION.fail should throw an Exception");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testLayeredConfigsListOverwrite() throws IOException {
        YAML multi = YAML.resolveLayeredConfigs(YAML.MERGE_ACTION.union, YAML.MERGE_ACTION.keep_extra,
                "yaml/overwrite-1.yaml", "yaml/overwrite-2.yaml");

        List<String> aList = multi.getList("upperA.subList");
        List<String> eList = Arrays.asList("three", "four");
        assertEquals(eList, aList,
                "Merged YAML should a list of only the elements from file #2");
    }

    @Test
    public void testFailingMultiConfig() {
        Assertions.assertThrows(FileNotFoundException.class,
                () -> YAML.resolveMultiConfig("Not_there.yml", "Not_there_2.yml"),
                "Attempting to resolve non-existing multi-config should throw an Exception");
    }

    @Test
    public void testFailingParse() {
        File nonExisting = new File("Non-existing");
        Assertions.assertThrows(FileNotFoundException.class,
                () -> YAML.parse(nonExisting),
                "Attempting to resolve non-existing config File should throw an Exception");

        Assertions.assertThrows(FileNotFoundException.class,
                () -> YAML.parse(nonExisting.toPath()),
                "Attempting to resolve non-existing config Path should throw an Exception");
    }

    @Test
    void nonReadableException() throws IOException {
        Set<PosixFilePermission> ownerWritable = PosixFilePermissions.fromString("-w-------"); // Only writable
        FileAttribute<?> permissions = PosixFilePermissions.asFileAttribute(ownerWritable);
        Path nonRead = Files.createTempFile("kbutil_", "tmp", permissions);
        Files.writeString(nonRead, "Hello World");

        // Check that the temp file was created correctly
        assertThat("Test file '" + nonRead + "' exists", Files.exists(nonRead));
        Assertions.assertThrows(AccessDeniedException.class, () -> Files.readString(nonRead),
                "Reading test file '" + nonRead + "' should throw an Exception");


        // Test that YAML throws appropriate exceptions when the resource cannot be read
        Assertions.assertThrows(AccessDeniedException.class, () -> YAML.parse(nonRead),
                "Parsing  test file '" + nonRead + "' directly should throw an Exception");

        Assertions.assertThrows(AccessDeniedException.class, () -> new YAML(nonRead.toString()),
                "Parsing  test file '" + nonRead + "' using constructor should throw an Exception");

        Assertions.assertThrows(AccessDeniedException.class, () -> YAML.resolveLayeredConfigs(nonRead.toString()),
                "Parsing  test file '" + nonRead + "' using resolve should throw an Exception");

        Files.delete(nonRead);
    }


    @Test
    public void testMultiGlob() throws IOException {
        YAML yaml = YAML.resolveMultiConfig("config_pair_part_[1-2].yml");
        assertEquals("bar", yaml.getString("serviceSetup.someString"),
                "The merged YAML should have the expected value for plain key 'somestring'");
        assertEquals("FooServer", yaml.getString("serviceSetup.theServer"),
                "The merged YAML should have the expected value for alias-using 'theServer'");
    }

    @Test
    public void testMergeCollision() throws IOException {
        YAML yaml = YAML.resolveMultiConfig("config_pair_part_1.yml", "config_pair_part_2.yml");
        assertEquals("number_2", yaml.getString("collision"),
                "When merging, the latest definition should win");
    }

    @Test
    public void testIntArray() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("test.yml").getSubMap("test");
        List<Integer> ints = yaml.getList("arrayofints");
        assertEquals("[1, 2]", ints.toString(),
                "Arrays of integers should be supported");
    }

    @Test
    public void testTypes() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("test.yml").getSubMap("test");
        final double EXPECTED = 87.13;
        double actual = yaml.getDouble("somedouble");

        assertTrue(actual >= EXPECTED * 0.99 && actual <= EXPECTED * 1.01,
                "Double should be supported and as expected");
        assertEquals(true, yaml.getBoolean("somebool"),
                "Boolean should be supported and as expected");
    }

    @Test
    public void testListEntry() throws IOException {
        YAML yamlTest = YAML.resolveLayeredConfigs("test.yml");
        YAML yaml = YAML.resolveLayeredConfigs("test.yml").getSubMap("test");

        assertEquals("b", yamlTest.getString("test.arrayofstrings[1]"),
                "Index-specified entry in arrays should be gettable");
        assertEquals("c", yamlTest.getString("test.arrayofstrings[last]"),
                "Last entry in arrays should be gettable");
    }

    @Test
    public void testFailingListEntry() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("test.yml").getSubMap("test");
        Assertions.assertThrows(Exception.class,
                () -> yaml.getString("arrayofstrings[3]"),
                "Requesting an index in a collection greater than or equal to list length should fail");
    }

    @Test
    public void testListMapEntry() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_maps.yml");
        try {
            yaml.getSubMap("test.listofmaps[1]");
        } catch (NotFoundException e) {
            fail("listofmaps[1] should return a map but failed", e);
        }

        assertEquals("barA2", yaml.getString("test.listofmaps[0].fooA2"),
                "Index-specified sub-map sub-entry should be gettable");
    }

    @Test
    public void testNestedLists() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_lists.yml");
        try {
            yaml.getList("test.listoflists[1]");
        } catch (NotFoundException e) {
            fail(e.getMessage(), e);
        }

        assertEquals("llItemB", yaml.getString("test.listoflists[1].[0]"),
                "Index-specified sub-map sub-entry should be gettable");
    }

    @Test
    public void testDotEscape() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("dots.yml");
        // test:
        //  plain: 'Hello'
        //  foo.bar: 87
        //  zoo.baz:
        //    inner.sub: 'World'
        assertEquals("Hello", yaml.getString("test.plain"),
                "Basic non-dotted key test.plain");
        assertEquals(87, yaml.getInteger("test.'foo.bar'"),
                "Dotted sub-key test.'foo.bar'");
        assertEquals(87, yaml.getInteger("test.\"foo.bar\""),
                "Dotted sub-key test.\"foo.bar\"");
        assertEquals("World", yaml.getString("test.\"zoo.baz\".'inner.sub'"),
                "Double-dotted sub-key test.\"zoo.baz\".'inner.sub'");
        try {
            yaml.getInteger("test.\"zoo.baz\".inner.sub");
            fail("Missing quote sub-key test.\"zoo.baz\".inner.sub should fail but did not");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(1, yaml.getInteger("test.'sub.list'.[0]"),
                "Dotted list-key with index test.'sub.list'.[0]");
    }


    /* Helpers below */

    private void assertExtrapolated(String path, String expected, String message) throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test").extrapolate(true);
        String actual = yaml.getString(path);
        assertEquals(expected, actual, message);
    }

    @Test
    public void testPathSubstitute() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/path_substitution.yaml");
        yaml.setExtrapolate(true);

        assertEquals("foo", yaml.get(".lower.bar"), "Basic path substitution should work");
        assertEquals("boom", yaml.get(".fallback.bar"), "Path substitution with default value should work");
        assertEquals("foo", yaml.get(".mixing.bar"), "Path substitution with default value should work");
    }

    @Test
    public void testConditionalIndex() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_maps.yml");
        assertEquals("boom", yaml.get("conditionalpropermap.[default=true].foo"));
    }

    @Test
    public void testNonConditionalMap() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_maps.yml");
        assertEquals("zoo", yaml.get("conditionalpropermap.bucket2.foo"));
    }

    @Test
    public void testNonConditionalMapList() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_maps.yml");
        assertEquals("zoo", yaml.get("conditionalmaplist[1].bucket2.foo"));
    }

    @Test
    public void testReverseConditionalIndex() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_maps.yml");
        assertEquals("bar", yaml.get("conditionalpropermap.[default!=true].foo"));
    }

    @Test
    public void testConditionalIndexProper() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_maps.yml");
        assertEquals("boom", yaml.get("conditionalpropermap.[default=true].foo"));
    }

    @Test
    public void testConditionalMapList() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_maps.yml");
        assertEquals("boom", yaml.get("conditionalmaplist.[default=true].foo"));
    }

    @Test
    public void testConditionalFlatMapList() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_maps.yml");
        assertEquals("boom", yaml.get("conditionalflatmaplist.[default=true].foo"));
    }

    @Test
    public void testConditionalAnonymousMapList() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_maps.yml");
        assertEquals("boom", yaml.get("conditionalanonymousmaplist.[default=true].foo"));
    }

    @Test
    public void testConditionalPathSubstitute() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/path_substitution.yaml");
        yaml.setExtrapolate(true);

        assertEquals("kaboom", yaml.get(".sams.bar"), "Path substitution with conditional path should work");
    }

    @Test
    public void testDirectPath() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_maps.yml");
        yaml.setExtrapolate(true);

        assertEquals("boom", yaml.get("nested.inner.foosubst"), "Getting by full path should work");
    }

    @Test
    public void testSubMapPath() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("nested_maps.yml");
        yaml.setExtrapolate(true);

        // It is important to test this WITHOUT performing the direct get request from testDirectPath first as
        // that creates the substitutors
        assertEquals("boom", yaml.getSubMap("nested").get("inner.foosubst"), "Getting from submap should work");
    }

    @Test
    public void testGetMultiple() throws IOException {
        // This visits everything called name. Therefore, it currently returns 16 elements where 15 of these are scalars and a single one is a map itself.
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<String> testValues = Arrays.asList("fooz", "foo", "bar", "baz", "qux", "john",
                "Thyra", "Gunhild", "Margrethe");

        List<Object> extractedNames = yaml.visit("**.name", yaml);
        assertEquals(16, extractedNames.size());
        assertTrue(extractedNames.containsAll(testValues));
    }

    @Test
    public void testSubsetFromGetMultiple() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<String> testValues = Arrays.asList("foo", "bar", "baz");

        List<Object> extractedNames = yaml.visit("test.tuplesequence[*].*.name", yaml);
        assertEquals(3, extractedNames.size());
        assertTrue(extractedNames.containsAll(testValues));
        assertFalse(extractedNames.contains("fooz"));

    }

    @Test
    public void testSubsetFromGetMultipleEmptyBrackets() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<String> testValues = Arrays.asList("foo", "bar", "baz");

        List<Object> extractedNames = yaml.visit("test.tuplesequence[].*.name", yaml);
        assertEquals(3, extractedNames.size());
        assertTrue(extractedNames.containsAll(testValues));
        assertFalse(extractedNames.contains("fooz"));

    }

    @Test
    public void testConditionalArrayLookupInVisit() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<Object> extractedNames = yaml.visit("test.arrayofqueens[name=Thyra].name", yaml);
        assertEquals(1, extractedNames.size());
        assertTrue(extractedNames.contains("Thyra"));
    }

    @Test
    public void testNegativeConditionalArrayLookupInVisit() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<Object> extractedNames = yaml.visit("test.arrayofqueens[name!=Thyra].name", yaml);
        assertEquals(2, extractedNames.size());
        assertFalse(extractedNames.contains("Thyra"));
    }

    @Test
    public void testIndexBasedLookupInVisit() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<Object> extractedNames = yaml.visit("test.arrayofqueens[0].name", yaml);
        assertEquals(1, extractedNames.size());
        assertTrue(extractedNames.contains("Thyra"));
    }

    @Test
    public void testLastLookupInVisit() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<Object> extractedNames = yaml.visit("test.arrayofqueens[last].name", yaml);
        assertEquals(1, extractedNames.size());
        assertTrue(extractedNames.contains("Margrethe"));

    }

    @Test
    public void testGetMultipleFromSubYaml() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<String> testValues = Arrays.asList("foo", "bar", "baz");
        List<Object> extractedNames = yaml.visit("test.tuplesequence[*].*.name", yaml);
        assertEquals(3, extractedNames.size());
        assertTrue(extractedNames.containsAll(testValues));
    }

    // The YAML structure has subtrees under the sequence.
    // The task here is to get all 'name's under 'primary', but not those under 'secondary'
    @Test
    public void testSequenceWithSubtrees() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<String> expectedNames = Arrays.asList("foo", "bar", "baz");
        List<Object> extractedNames = yaml.visit("subtrees[*].*.primary.name", yaml);

        assertEquals(expectedNames, extractedNames);
    }


    @Test
    public void testGetMultipleFromSubYamlOnScalar() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");

        List<Object> extractedNames = yaml.visit("anothertest.name", yaml);
        assertEquals(1, extractedNames.size());
        assertTrue(extractedNames.contains("qux"));
    }

    @Test
    public void testNewTraverserSingleAsterix() throws IOException {
        YAML test = YAML.resolveLayeredConfigs("yaml/visitor.yaml");

        String yPath = "*.somestring";

        List<Object> result = test.visit(yPath, test);
        assertEquals(1, result.size());
    }

    @Test
    public void testNonexplicitListGet() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        Object listO = yaml.get("test.arrayofstrings");
        assertTrue(listO instanceof List, "The extracted type should be a list");
    }

    @Test
    public void testExplicitListSkipping() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        Object listO = yaml.get("test.tuplesequence.[].item2.name");
        assertEquals("bar", listO, "");
    }

    @Test
    public void testDoubleWildcardLast() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        assertEquals(1, yaml.get("test.anotherlevel.name.**"),
                "Double wildcard at the end of the path");
    }

    @Test
    public void testIllegalImplicitListSkipping() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        assertThrows(NotFoundException.class, () -> yaml.get("test.tuplesequence.item2.name"),
                "Requesting a submap from a list withput explicit list handling should fail");
    }

    @Test
    public void testWildcardList() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<Object> actual = yaml.visit("lasting.**", yaml);
        assertEquals("[foo, baz, boom]", actual.toString(),
                "Using double wildcard with lists should return all elements");
    }

    @Test
    public void testWildcardLast() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<Object> actual = yaml.visit("lasting.*.one[last]", yaml);
        assertEquals("[foo, boom]", actual.toString(),
                "Using [last] with multiple should not mix indexes");
    }

    @Test
    public void testGetDirectPathSubstitution() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml")
                .extrapolate(true);
        Object actual = yaml.get("ypath.direct");
        assertEquals("foo", actual.toString(),
                "Path substitution should work for get direct path");
    }

    @Test
    public void testGetPathSubstitution() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml")
                .extrapolate(true);
        Object actual = yaml.get("ypath.sublist.*");
        assertEquals("foo", actual.toString(),
                "Path substitution should work for get with wildcard extraction of lists");
    }

    @Test
    public void testVisitPathSubstitution() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml")
                .extrapolate(true);
        List<Object> actual = yaml.visit("ypath.sublist.*", yaml);
        assertEquals("[foo]", actual.toString(),
                "Path substitution should work for visit with wildcard extraction of lists");
    }

    @Test
    public void testVisitPathSubstitutionSubmap() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml")
                .extrapolate(true);
        List<Object> actual = yaml.visit("ypath.sublist2[last].submap.ref", yaml);
        assertEquals("[foo]", actual.toString(),
                "Path substitution should work for visit trhough list down to map");
    }

    @Test
    public void testVisitPathSubstitutionConditional() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml")
                .extrapolate(true);
        List<Object> actual = yaml.visit("ypath.maps[order=first].value", yaml);
        assertEquals("[foo]", actual.toString(),
                "Path substitution should work for visit using conditionals");
    }

    @Test
    public void testGetMultipleOld() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<String> testValues = Arrays.asList("fooz", "foo", "bar", "baz", "qux", "john", "doe",
                "Thyra", "Gunhild", "Margrethe");

        List<Object> extractedNames = yaml.getMultiple("name");
        assertEquals(16, extractedNames.size());
    }

    @Test
    public void testSubsetFromGetMultipleOld() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<YAML> subsetYaml = yaml.getYAMLList("test.tuplesequence");
        List<String> testValues = Arrays.asList("foo", "bar", "baz");

        List<Object> extractedNames = new ArrayList<>();

        for (YAML yamlEntry : subsetYaml) {
            extractedNames.addAll(yamlEntry.getMultiple("name"));
        }

        assertEquals(3, extractedNames.size());
        assertTrue(extractedNames.containsAll(testValues));
        assertFalse(extractedNames.contains("fooz"));

    }

    @Test
    public void testGetMultipleFromSubYamlOld() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<String> testValues = Arrays.asList("foo", "bar", "baz");

        List<Object> extractedNames = yaml.getMultipleFromSubYaml("test.tuplesequence", "name");
        assertEquals(3, extractedNames.size());
        assertTrue(extractedNames.containsAll(testValues));
    }
}
