package dk.kb.util.yaml;

import dk.kb.util.Resolver;
import org.junit.jupiter.api.Disabled;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class YAMLTest {
    @SuppressWarnings("ConstantConditions")
    @Test
    public void testToString() throws IOException {
        try (Stream<String> lines = Files.lines(Resolver.getPathFromClasspath("test.yml"))) {
            String contents = lines.map(line -> line.replaceAll("#.*$", "").stripTrailing())
                    .filter(line -> !line.isBlank())
                    .collect(Collectors.joining("\n")) + "\n";
            assertEquals(contents, YAML.resolveLayeredConfigs("test.yml").toString());
        }
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
        assertEquals(List.of("a", "b", "c"), yaml.getList("arrayofstrings"),
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
        assertThrows(NotFoundException.class, () -> yaml.getSubMap("nonexisting"),
                "Requesting a non-existing sub map should result in a NotFoundException");
    }

    @Test
    public void testMissingString() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("test.yml").getSubMap("test");
        assertThrows(NotFoundException.class, () -> yaml.getString("nonexisting"),
                "Requesting a non-existing String should result in a NotFoundException");
    }

    @Test
    public void testDefault() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("test.yml").getSubMap("test");
        assertEquals(87, yaml.getInteger("nonexisting", 87),
                "Requesting a non-existing integer with a default value should work");
    }


    @Test
    public void testNestedExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml")
                .extrapolate(true);
        assertEquals(System.getProperties().getProperty("user.home") + " Hello World",
                yaml.getString("test.somestring"),
                "Nested request for string should be supported");
    }

    @Test
    public void testRootExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml")
                .getSubMap("test").extrapolate(true);
        assertEquals(System.getProperties().getProperty("user.home") + " Hello World",
                yaml.getString("somestring"),
                "Direct request for string from resolved root should be supported");
    }

    @Test
    public void testArrayExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml")
                .getSubMap("test").extrapolate(true);
        assertEquals(List.of("a", System.getProperty("user.name"), "c"),
                yaml.getList("arrayofstrings"),
                "Arrays of strings should be supported");
    }

    @Test
    public void testKeptPathExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml")
                .getSubMap("test").extrapolate(true);

        assertEquals("nested.sublevel2string: " + System.getProperties().getProperty("user.name") + "\n",
                yaml.getSubMap("nested", true).toString(),
                "When we get map with subkeys preserved, we should see the nested prefix");
    }

    @Test
    public void testMissingSubMapExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml")
                .getSubMap("test").extrapolate(true);
        assertThrows(NotFoundException.class, () -> yaml.getSubMap("nonexisting"),
                "Requesting a non-existing sub map should result in a NotFoundException");
    }

    @Test
    public void testMissingStringExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml")
                .extrapolate(true).getSubMap("test");
        assertThrows(NotFoundException.class, () -> yaml.getString("nonexisting"),
                "Requesting a non-existing String should result in a NotFoundException");
    }

    @Test
    @Disabled("Test not relevant after 20240312 as extrapolation is done up front instead of per request")
    public void testNonResolvableExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolatedNonresolvable.yml")
                .extrapolate(true);
        // "test.somestring" Contains ${cannot.be.resolved}
        assertThrows(Exception.class, () -> yaml.getString("test.somestring"),
                "Requesting a non-resolvable system property should fail");
    }

    @Test
    public void testDefaultExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml")
                .extrapolate(true).getSubMap("test");
        assertEquals(87, yaml.getInteger("nonexisting", 87),
                "Requesting a non-existing integer with a default value should work");
    }


    @Test
    public void testIntExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test")
                .extrapolate(true);
        Integer i = yaml.getInteger("someint");
        assertTrue(i >= 17, "Extracted integer should be at least 17 (the lowest Java version " +
                "supported by kb-util), but was " + i);
    }

    @Test
    public void testIntArrayExtrapolatedType() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test")
                .extrapolate(true);
        List<Integer> ints = yaml.getList("arrayofints");
        assertInstanceOf(Integer.class, ints.get(0),
                "First element in the extracted Integer list should be an Integer but was " +
                        ints.get(0).getClass());
    }

    @Test
    public void testIntArrayExtrapolatedContent() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test").extrapolate(true);
        List<Integer> ints = yaml.getList("arrayofints");
        assertTrue(ints.get(0) >= 17,
                "Arrays of integers should be supported. Expected first element to be >= 17, but got array " + ints);
    }

    @Test
    public void testTypesExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test")
                .extrapolate(true);
        final double EXPECTED = 61.0; // 61 = Java 17. Anything greater is also OK
        double actual = yaml.getDouble("somedouble");

        assertTrue(actual >= EXPECTED * 0.99,
                "Double should be supported and as expected. Expected>=" + EXPECTED + ", resolved=" + actual);
        assertEquals(true, yaml.getBoolean("somebool"),
                "Boolean should be supported and as expected");
    }

    @Test
    public void testExtrapolatedExceptionImplicit() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test")
                .extrapolate(true);
        assertThrows(Exception.class, () -> yaml.getString("exceptionimplicit"),
                "Requesting a property with expansion of implicit sys: where the expansion " +
                        "could not be fulfilled, should throw an Exception");
    }

    @Test
    public void testExtrapolatedExceptionExplicitEnv() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test")
                .extrapolate(true);
        assertThrows(Exception.class, () -> yaml.getString("exceptionenv"),
                "Requesting a property with expansion of explicit env: where the expansion could not be " +
                        "fulfilled, should throw an Exception");
    }

    @Test
    public void testExtrapolatedFallback() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test")
                .extrapolate(true);
        String actual = yaml.getString("fallback");
        assertEquals("mydefault", actual,
                "Looking up a non-existing property with fallback should return the fallback");
    }

    @Test
    public void testExtrapolatedEnvFallback() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test")
                .extrapolate(true);
        String actual = yaml.getString("envfallback");
        assertEquals("envdefault", actual,
                "Looking up a non-existing environment variable with fallback should return the fallback");
    }

    @Test
    public void testExtrapolatedNestedFallbackHit() throws IOException {
        final String home = System.getProperties().getProperty("user.home");
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test")
                .extrapolate(true);
        String actual = yaml.getString("nestedfallback");
        assertEquals(home, actual,
                "Looking up a non-existing environment variable with fallback to an existing environment variable should return the value of the secondary environment variable");
    }

    @Test
    public void testExtrapolatedNestedFallbackFail() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test")
                .extrapolate(true);
        String actual = yaml.getString("nestedfallbackfail");
        assertEquals("reach", actual,
                "Looking up a non-existing environment variable with nonexisting fallback resolve " +
                        "should return the fallback's fallback");
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
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test")
                .extrapolate(true);
        assertEquals(System.getProperty("user.name"), yaml.getString("arrayofstrings[1]"),
                "Index-specified entry in arrays should be gettable");
        assertEquals("c", yaml.getString("arrayofstrings[last]"),
                "Last entry in arrays should be gettable");
    }

    @Test
    public void testFailingListEntryExtrapolated() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test");
        assertThrows(Exception.class,
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
    public void testLayeredConfigsFail() {
        assertThrows(IllegalArgumentException.class,
                () -> YAML.resolveLayeredConfigs(YAML.MERGE_ACTION.fail, YAML.MERGE_ACTION.fail,
                        "yaml/overwrite-1.yaml", "yaml/overwrite-2.yaml"),
                "Merging with duplicate keys and MERGE_ACTION.fail should throw an Exception");
    }

    @Test
    public void testLayeredConfigsListOverwrite() throws IOException {
        YAML multi = YAML.resolveLayeredConfigs(YAML.MERGE_ACTION.union, YAML.MERGE_ACTION.keep_extra,
                "yaml/overwrite-1.yaml", "yaml/overwrite-2.yaml");

        List<String> aList = multi.getList("upperA.subList");
        List<String> eList = List.of("three", "four");
        assertEquals(eList, aList,
                "Merged YAML should a list of only the elements from file #2");
    }

    @Test
    public void testFailingMultiConfig() {
        assertThrows(FileNotFoundException.class,
                () -> YAML.resolveMultiConfig("Not_there.yml", "Not_there_2.yml"),
                "Attempting to resolve non-existing multi-config should throw an Exception");
    }

    @Test
    public void testFailingParse() {
        File nonExisting = new File("Non-existing");
        assertThrows(FileNotFoundException.class,
                () -> YAML.parse(nonExisting),
                "Attempting to resolve non-existing config File should throw an Exception");

        assertThrows(FileNotFoundException.class,
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
        assertThrows(AccessDeniedException.class, () -> Files.readString(nonRead),
                "Reading test file '" + nonRead + "' should throw an Exception");

        // Test that YAML throws appropriate exceptions when the resource cannot be read
        assertThrows(AccessDeniedException.class, () -> YAML.parse(nonRead),
                "Parsing  test file '" + nonRead + "' directly should throw an Exception");

        assertThrows(AccessDeniedException.class, () -> new YAML(nonRead.toString()),
                "Parsing  test file '" + nonRead + "' using constructor should throw an Exception");

        assertThrows(AccessDeniedException.class, () -> YAML.resolveLayeredConfigs(nonRead.toString()),
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
        assertEquals(List. of(1, 2), ints, "Arrays of integers should be supported");
    }

    @Test
    public void testTypes() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("test.yml").getSubMap("test");
        final double EXPECTED = 87.13;
        double actual = yaml.getDouble("somedouble");

        assertEquals(EXPECTED, actual, EXPECTED * 0.01, "Double should be supported and as expected");
        assertEquals(true, yaml.getBoolean("somebool"),
                "Boolean should be supported and as expected");
    }

    @Test
    public void testListEntry() throws IOException {
        YAML yamlTest = YAML.resolveLayeredConfigs("test.yml");

        assertEquals("b", yamlTest.getString("test.arrayofstrings[1]"),
                "Index-specified entry in arrays should be gettable");
        assertEquals("c", yamlTest.getString("test.arrayofstrings[last]"),
                "Last entry in arrays should be gettable");
    }

    @Test
    public void testFailingListEntry() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("test.yml").getSubMap("test");
        assertThrows(Exception.class,
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
        assertThrows(Exception.class, () -> yaml.getInteger("test.\"zoo.baz\".inner.sub"),
                "Missing quote sub-key test.\"zoo.baz\".inner.sub should fail but did not");
        assertEquals(1, yaml.getInteger("test.'sub.list'.[0]"),
                "Dotted list-key with index test.'sub.list'.[0]");
    }


    /* Helpers below */

    private void assertExtrapolated(String path, String expected, String message) throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("testExtrapolated.yml").getSubMap("test")
                .extrapolate(true);
        String actual = yaml.getString(path);
        assertEquals(expected, actual, message);
    }

    @Test
    public void testPathSubstitute() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/path_substitution.yaml");
        yaml.setExtrapolate(true);

        assertEquals("foo", yaml.get(".lower.bar"), "Basic path substitution should work");
        assertEquals("boom", yaml.get(".fallback.bar"),
                "Path substitution with default value should work");
        assertEquals("foo", yaml.get(".mixing.bar"),
                "Path substitution with default value should work");
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
        assertEquals(List.of("bar", "zoo", "baz"), yaml.getMultiple("conditionalpropermap.[default!=true].foo"));
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

        assertEquals("kaboom", yaml.get(".sams.bar"),
                "Path substitution with conditional path should work");
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
        assertEquals("boom", yaml.getSubMap("nested").get("inner.foosubst"),
                "Getting from submap should work");
    }

    @Test
    public void testGetMultiple() throws IOException {
        // This visits everything called name.
        // Therefore, it currently returns 16 elements where 15 of these are scalars and a single one is a map itself.
        MultipleValuesVisitor visitor = new MultipleValuesVisitor();
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<String> testValues = Arrays.asList("fooz", "foo", "bar", "baz", "qux", "john",
                "Thyra", "Gunhild", "Margrethe");

        yaml.visit("**.name", yaml, visitor);
        assertEquals(16, visitor.extractedValues.size());
        assertTrue(visitor.extractedValues.containsAll(testValues));
    }

    @Test
    public void testSubsetFromGetMultiple() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        MultipleValuesVisitor visitor = new MultipleValuesVisitor();
        List<String> testValues = Arrays.asList("foo", "bar", "baz");

        yaml.visit("test.tuplesequence[*].*.name", yaml, visitor);
        assertEquals(3, visitor.extractedValues.size());
        assertTrue(visitor.extractedValues.containsAll(testValues));
        assertFalse(visitor.extractedValues.contains("fooz"));

    }

    @Test
    public void testSubsetFromGetMultipleEmptyBrackets() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<String> testValues = Arrays.asList("foo", "bar", "baz");
        MultipleValuesVisitor visitor = new MultipleValuesVisitor();
        yaml.visit("test.tuplesequence[].*.name", yaml, visitor);
        assertEquals(3, visitor.extractedValues.size());
        assertTrue(visitor.extractedValues.containsAll(testValues));
        assertFalse(visitor.extractedValues.contains("fooz"));

    }

    @Test
    public void testConditionalArrayLookupInVisit() throws IOException {
        MultipleValuesVisitor visitor = new MultipleValuesVisitor();
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        yaml.visit("test.arrayofqueens[name=Thyra].name", yaml, visitor);
        assertEquals(1, visitor.extractedValues.size());
        assertTrue(visitor.extractedValues.contains("Thyra"));
    }

    @Test
    public void testNegativeConditionalArrayLookupInVisit() throws IOException {
        MultipleValuesVisitor visitor = new MultipleValuesVisitor();
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        yaml.visit("test.arrayofqueens[name!=Thyra].name", yaml, visitor);
        assertEquals(2, visitor.extractedValues.size());
        assertFalse(visitor.extractedValues.contains("Thyra"));
    }

    @Test
    public void testIndexBasedLookupInVisit() throws IOException {
        MultipleValuesVisitor visitor = new MultipleValuesVisitor();
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        yaml.visit("test.arrayofqueens[0].name", yaml, visitor);
        assertEquals(1, visitor.extractedValues.size());
        assertTrue(visitor.extractedValues.contains("Thyra"));
    }

    @Test
    public void testLastLookupInVisit() throws IOException {
        MultipleValuesVisitor visitor = new MultipleValuesVisitor();
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        yaml.visit("test.arrayofqueens[last].name", yaml, visitor);
        assertEquals(1, visitor.extractedValues.size());
        assertTrue(visitor.extractedValues.contains("Margrethe"));

    }

    @Test
    public void testGetMultipleFromSubYaml() throws IOException {
        MultipleValuesVisitor visitor = new MultipleValuesVisitor();
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<String> testValues = Arrays.asList("foo", "bar", "baz");
        yaml.visit("test.tuplesequence[*].*.name", yaml, visitor);
        assertEquals(3, visitor.extractedValues.size());
        assertTrue(visitor.extractedValues.containsAll(testValues));
    }

    // The YAML structure has subtrees under the sequence.
    // The task here is to get all 'name's under 'primary', but not those under 'secondary'
    @Test
    public void testSequenceWithSubtrees() throws IOException {
        MultipleValuesVisitor visitor = new MultipleValuesVisitor();
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<String> expectedNames = Arrays.asList("foo", "bar", "baz");
        yaml.visit("subtrees[*].*.primary.name", yaml, visitor);

        assertEquals(expectedNames, visitor.extractedValues);
    }


    @Test
    public void testGetMultipleFromSubYamlOnScalar() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        MultipleValuesVisitor visitor = new MultipleValuesVisitor();

        yaml.visit("anothertest.name", yaml, visitor);
        assertEquals(1, visitor.extractedValues.size());
        assertTrue(visitor.extractedValues.contains("qux"));
    }

    @Test
    public void testNewTraverserSingleAsterix() throws IOException {
        YAML test = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        MultipleValuesVisitor visitor = new MultipleValuesVisitor();

        String yPath = "*.somestring";

        test.visit(yPath, test, visitor);
        assertEquals(1, visitor.extractedValues.size());
    }

    @Test
    public void testNonexplicitListGet() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        Object listO = yaml.get("test.arrayofstrings");
        assertInstanceOf(List.class, listO, "The extracted type should be a list");
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
        assertEquals(List.of(1, 2, 3, "john"), yaml.getMultiple("test.anotherlevel.name.**"),
                "Double wildcard at the end of the path");
    }

    @Test
    public void testIllegalImplicitListSkipping() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        assertThrows(NotFoundException.class, () -> yaml.get("test.tuplesequence.item2.name"),
                "Requesting a submap from a list without explicit list handling should fail");
    }

    @Test
    public void testWildcardList() throws IOException {
        MultipleValuesVisitor visitor = new MultipleValuesVisitor();
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        yaml.visit("lasting.**", yaml, visitor);
        assertEquals(List.of("foo", "bar", "boom"), visitor.extractedValues,
                "Using double wildcard with lists should return all elements");
    }

    @Test
    public void testWildcardLast() throws IOException {
        MultipleValuesVisitor visitor = new MultipleValuesVisitor();
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        yaml.visit("lasting.*.sub[last]", yaml, visitor);
        assertEquals(List.of("foo", "boom"), visitor.extractedValues,
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
        assertEquals("foo", actual,
                "Path substitution should work for get with wildcard extraction of lists");
    }

    @Test
    public void testVisitPathSubstitution() throws IOException {
        MultipleValuesVisitor visitor = new MultipleValuesVisitor();
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml")
                .extrapolate(true);
        yaml.visit("ypath.sublist.*", yaml, visitor);
        assertEquals(List.of("foo"), visitor.extractedValues,
                "Path substitution should work for visit with wildcard extraction of lists");
    }

    @Test
    public void testVisitPathSubstitutionSubmap() throws IOException {
        MultipleValuesVisitor visitor = new MultipleValuesVisitor();
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml")
                .extrapolate(true);
        yaml.visit("ypath.sublist2[last].submap.ref", yaml, visitor);
        assertEquals(List.of("foo"), visitor.extractedValues,
                "Path substitution should work for visit through list down to map");
    }

    @Test
    public void testVisitPathSubstitutionConditional() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml")
                .extrapolate(true);
        MultipleValuesVisitor visitor = new MultipleValuesVisitor();
        yaml.visit("ypath.maps[order=first].value", yaml, visitor);
        assertEquals(List.of("foo"), visitor.extractedValues,
                "Path substitution should work for visit using conditionals");
    }

    @Test
    @Disabled("Disabled 20240312 due to change of the behaviour of getMultiple")
    public void testGetMultipleOld() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");

        List<Object> extractedNames = yaml.getMultiple("name");
        assertEquals(16, extractedNames.size());
    }

    @Test
    @Disabled("Disabled 20240312 due to change of the behaviour of getMultiple")
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

    @Test
    public void testExtrapolatingDefaultEnabled() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/extrapolation.yaml");
        assertEquals("foo", yaml.getString("test.somestring"),
                "Default should be extrapolation for test.somestring");
    }

    @Test
    public void testDisableInterpolation() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs(false, "yaml/extrapolation.yaml");
        assertEquals("${path:ypath.major}", yaml.getString("test.somestring"),
                "Disabling interpolation should work for test.somestring");
    }

    @Test
    public void testExtrapolatingTypes() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/extrapolation.yaml");
        yaml.setExtrapolate(true);

        assertEquals("foo", yaml.get("test.somestring"),
                "Extrapolating to string should work for test.somestring");
        assertEquals("foo", yaml.getString("test.somestring"),
                "Extrapolating to int should work for explicit string request for test.somestring");

        assertEquals(123, yaml.get("test.someint"),
                "Extrapolating to int should work for test.someint");
        assertEquals(123, yaml.getInteger("test.someint"),
                "Extrapolating to int should work for explicit int request for test.someint");
        assertEquals(123, yaml.getLong("test.someint"),
                "Extrapolating to long should work for explicit long request for test.someint");

        assertEquals(12.34, yaml.get("test.somedouble"),
                "Extrapolating to double should work for test.somedouble");
        assertEquals(12.34, yaml.getDouble("test.somedouble"),
                "Extrapolating to double should work for explicit double request for test.somedouble");

        assertEquals(true, yaml.get("test.somebool"),
                "Extrapolating to boolean should work for test.somebool");
        assertEquals(true, yaml.getBoolean("test.somebool"),
                "Extrapolating to boolean should work for explicit boolean request for test.somebool");
    }

    @Test
    public void testExtrapolatingIndirection() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/extrapolation.yaml");
        yaml.setExtrapolate(true);

        assertEquals(System.getProperty("user.name"), yaml.get("test.indirectstart"),
                "Multi-level extrapolating should work for test.indirectstart");
    }

    @Test
    public void testExtrapolatingList() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/extrapolation.yaml");
        yaml.setExtrapolate(true);

        List<String> strList = yaml.getList("test.indirectstringlist");
        assertEquals(List.of("foo", "oof"), strList,
                "Requesting test.indirectstringlist should yield a string list");

        List<Integer> intList = yaml.getList("test.indirectnumberlist");
        assertEquals(List.of(123, 234), intList, "Requesting test.indirectnumberlist should yield an int list");

        // Calling <Long>getList() does not give Long objects in the list.
        List<? extends Number> longList = yaml.<Long>getList("test.indirectnumberlist");
        assertEquals(List.of(123, 234),
                longList.stream().map(Number::intValue).toList(),
                "Requesting test.indirectnumberlist should yield a list of numbers");
    }

    @Test
    public void testExtrapolatingMap() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/extrapolation.yaml");
        yaml.setExtrapolate(true);

        YAML sub = yaml.getSubMap("test.indirectnumbermap");
        assertEquals(123, sub.get("foo"), "Map entry for 'foo' should be as expected");
        assertEquals(234, sub.get("bar"), "Map entry for 'bar' should be as expected");
        assertEquals(123, sub.get("sub.subfoo"), "Map entry for 'sub.foo' should be as expected");
    }

    @Test
    @Disabled("Paths to structures are not implemented yet")
    public void testExtrapolatingEmptyList() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/extrapolation.yaml");
        yaml.setExtrapolate(true);

        List<String> strList = yaml.getList("test.emptylist");
        assertTrue(strList.isEmpty(),
                "Requesting test.emptylist as string list should yield an empty string list");

        List<Boolean> boolList = yaml.getList("test.emptylist");
        assertTrue(boolList.isEmpty(),
                "Requesting test.emptylist as boolean list should yield an empty boolean list");
    }

    @Test
    public void testExtrapolatingFail() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs(false, "yaml/extrapolation_fail.yaml");
        assertThrowsExactly(IllegalArgumentException.class, () -> yaml.setExtrapolate(true),
                "Enabling extrapolation with a failing substitution should fail with an exception");
    }

    // List<YAML> yamls1 = getYAMLList("foo")
    // and
    // List<YAML> yamls1 = getList("foo")
    // should both work
    @Test
    public void testGetImplicitYAMLList() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/visitor.yaml");
        List<YAML> explicit = yaml.getYAMLList("test.tuplesequence");
        List<YAML> implicit = yaml.getList("test.tuplesequence");
        assertEquals(explicit, implicit,
                "Explicit and implicit List<YAML> retrieval should be the same");
    }

    @Test
    public void testContainsKeyNullValues() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/null.yaml");

        boolean containsKey = yaml.containsKey("emptyvalue");
        assertTrue(containsKey);

        containsKey = yaml.containsKey("nullvalue");
        assertTrue(containsKey);

        containsKey = yaml.containsKey("tildevalue");
        assertTrue(containsKey);
    }

    @Test
    public void testGetNullValues() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/null.yaml");
        YAML empty = new YAML();

        Object nullValue = yaml.get("nullvalue");
        assertEquals(empty, nullValue);

        Object emptyValue = yaml.get("emptyvalue");
        assertEquals(empty, emptyValue);

        Object tildeValue = yaml.get("tildevalue");
        assertEquals(empty, tildeValue);
    }

    @Test
    public void testGetMultipleNullValues() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/null.yaml");

        List<Object> nullValues = yaml.getMultiple("multipleNull.**");

        assertEquals(Collections.nCopies(3, null), nullValues);
    }

    @Test
    public void testGetSubMapNull() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/null.yaml");
        YAML emptyYaml = new YAML();

        YAML nullYaml = yaml.getSubMap("emptyvalue");
        assertEquals(emptyYaml, nullYaml);

        nullYaml = yaml.getSubMap("nullvalue");
        assertEquals(emptyYaml, nullYaml);

        nullYaml = yaml.getSubMap("tildevalue");
        assertEquals(emptyYaml, nullYaml);
    }

    // TODO: all these methods: YAML.getList, YAML.getYAMLList should be able to return an empty structure, if the value for the key
    //  is null

    @Test
    public void testGetYAMLListNull() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/null.yaml");
        YAML empty = new YAML();

        List<YAML> nullYaml = yaml.getYAMLList("listWithSomeYamlValuesAndNull");

        assertEquals("bar", nullYaml.get(0).getString("foo"));
        assertEquals(empty, nullYaml.get(2).get("moo"));
    }

    @Test
    public void testGetListNull() throws IOException {
        YAML yaml = YAML.resolveLayeredConfigs("yaml/null.yaml");
        YAML empty = new YAML();

        List<Object> yamlList = yaml.getList("listWithMixedValuesAndNull");
        List<Object> emptyList = yaml.getList("listWithEmptyValues");

        // empty structures
        assertEquals(empty, yamlList.get(0));
        assertEquals(empty, yamlList.get(4));
        // YAML Structures
        Object entry1 = yamlList.get(1);
        assertNotNull(entry1);
        if (entry1 instanceof Map<?, ?> wildcardMap) {
            Map<String, Object> typedMap = wildcardMap.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            e -> (String) e.getKey(), Map.Entry::getValue));
            YAML entry = new YAML(typedMap);
            assertEquals("bar", entry.getString("foo"));
            // Object
            assertEquals("stringValue", yamlList.get(5));
            // All empty values
            emptyList.forEach(value -> assertEquals(empty, value));
        } else {
            fail("Expected Map<String, Object>, got " + entry1.getClass().getName());
        }
    }

}
