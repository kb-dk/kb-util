package dk.kb.util.reader;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class VerbatimMatcherTest {

    @Test
    public void testSimple() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRule("World");
        assertMatches(matcher, "Hello World!",
                      "World");
    }

    @Test
    public void testMulti() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("World", "Cruel");
        assertMatches(matcher, "Hello Cruel Worlds",
                      "Cruel", "World");
    }

    @Test
    public void testOverlap() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("London", "East London");
        assertMatches(matcher, "Welcome to East London",
                      "East London", "London");
    }

    @Test
    public void testNone() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("London", "East London");
        assertMatches(matcher, "This is somewhere else");
    }

    @Test
    public void testPerfect() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("London");
        assertMatches(matcher, "London",
                      "London");
    }

    @Test
    public void testDelimiter1() {
        CollectingMatcher matcher = new CollectingMatcher();
        Pattern delimiter = Pattern.compile(" +");
        matcher.addRules("London", "East London");
        assertMatches(matcher, delimiter, "East London is burning",
                      "East London", "London");
    }

    @Test
    public void testDelimiter2() {
        CollectingMatcher matcher = new CollectingMatcher();
        Pattern delimiter = Pattern.compile(" +");
        matcher.addRules("London", "East-London");
        assertMatches(matcher, delimiter, "East-London is burning",
                      "East-London");
    }

    @Test
    public void testDelimiter3() {
        CollectingMatcher matcher = new CollectingMatcher();
        Pattern delimiter = Pattern.compile("-");
        matcher.addRules("London", "East-London");
        assertMatches(matcher, delimiter, "Look, East-London is burning",
                      "London");
    }

    @Test
    public void testNoRules() {
        CollectingMatcher matcher = new CollectingMatcher();
        assertMatches(matcher, "London");
    }

    @Test
    public void testNoSource() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("London");
        assertMatches(matcher, "");
    }

    @Test
    public void testGetExistingNode() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("London");
        assertNotNull(matcher.getNode("London", false),
                      "There should be a Node for 'London'");
    }

    @Test
    public void testGetNonExistingNode() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("London");
        assertNull(matcher.getNode("France", false),
                   "There should not be a Node for 'France'");
    }

    @Test
    public void testAutoCreateNode() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("London");
        assertNotNull(matcher.getNode("France", true),
                      "A Node for 'France' should be created");
        assertNotNull(matcher.getNode("France", false),
                      "The newly created Node for 'France' should be available");
    }

    @Test
    public void testPayload() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRule("London", "old");
        matcher.addRule("East London", "medium");
        assertMatchesPayload(matcher, "East London is burning",
                             Arrays.asList("East London", "London"), Arrays.asList("medium", "old"));
    }

    @Test
    public void testPartialPayloads() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRule("London", "old");
        matcher.addRule("East London");
        assertMatchesPayload(matcher, "East London is burning",
                             Arrays.asList("East London", "London"), Arrays.asList(null, "old"));
    }

    @Test
    public void testMatchShortest() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.shortest);
        matcher.setSkipMatching(true);
        assertMatches(matcher, "Come visit East London in the fall",
                      "East", "London");
    }

    @Test
    public void testMatchLongest() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.longest);
        matcher.setSkipMatching(true);
        assertMatches(matcher, "Come visit East London in the fall",
                      "East London");

    }

    @Test
    public void testMatchLongestNoSkip() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.longest);
        matcher.setSkipMatching(false);
        assertMatches(matcher, "Come visit East London in the fall",
                      "East London", "London");

    }

    @Test
    public void testMatchShortestNoSkip() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.shortest);
        matcher.setSkipMatching(false);
        assertMatches(matcher, "Come visit East London in the fall",
                      "East", "London");
    }

    @Test
    public void testNoLeading() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London", "Come");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.all);
        matcher.setSkipMatching(false);
        matcher.setLeading((char) 0); // default
        assertMatches(matcher, "Come visit East-London in the fall",
                      "Come", "East", "London");
    }

    @Test
    public void testLeading() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London", "Come");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.all);
        matcher.setSkipMatching(false);
        matcher.setLeading(' ');
        assertMatches(matcher, "Come visit East-London in the fall",
                      "Come", "East");
    }

    @Test
    public void testNoFollowing() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London", "Come", "fall");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.all);
        matcher.setSkipMatching(false);
        matcher.setFollowing((char) 0); // default
        assertMatches(matcher, "Come visit East-London in the fall",
                      "Come", "East", "London", "fall");
    }

    @Test
    public void testFollowing() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London", "Come", "fall");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.all);
        matcher.setSkipMatching(false);
        matcher.setFollowing(' ');
        assertMatches(matcher, "Come visit East-London in the fall",
                      "Come", "London", "fall");
    }

    @Test
    public void testSpecificProblem() throws IOException {
        Path INPUT = Paths.get("/home/te/tmp/sumfresh/sites/aviser/names_and_coordinates.dat");
        if (!Files.exists(INPUT)) {
            return;
        }
        CollectingMatcher matcher = new CollectingMatcher();
        BufferedReader in = new BufferedReader(new FileReader(INPUT.toFile(), StandardCharsets.UTF_8));
        String line;
        int count = 0;
        while ((line = in.readLine()) != null) {
            count++;
            String[] tokens = line.split(",");
            try {
                if (count == 34) {
                    System.out.println("Entering problematic phase with " + line);
                }
                matcher.addRules(tokens[0], tokens[1] + "," + tokens[2]);
            } catch (Exception e) {
                throw new RuntimeException("Exception adding line " + count + ": '" + line + "'");
            }
        }
    }

    @Test
    public void testLeadingAndFollowingWithSkip() {
        CollectingMatcher matcher = new CollectingMatcher();
        matcher.addRules("East", "London", "East London", "Come", "fall");
        matcher.setMatchMode(VerbatimMatcher.MATCH_MODE.all);
        matcher.setSkipMatching(true);
        matcher.setLeading(' ');
        matcher.setFollowing(' ');
        assertMatches(matcher, "Come visit East-London in the fall",
                      "Come", "fall");
    }

    private void assertMatchesPayload(
            CollectingMatcher matcher, String source, List<String> verbatims, List<String> payloads) {
        assertEquals(verbatims.size(), matcher.findMatches(source),
                     "There should be the right number of matches");
        for (int i = 0 ; i < verbatims.size() ; i++) {
            assertEquals(verbatims.get(i), matcher.matches.get(i),
                         "Match " + i + " verbatim should be as expected");
            assertEquals(payloads.get(i), matcher.payloads.get(i),
                         "Match " + i + " payload should be as expected");
        }
    }

    private void assertMatches(CollectingMatcher matcher, String source, String... matches) {
        assertEquals(matches.length, matcher.findMatches(source),
                     "There should be the right number of matches");
        for (int i = 0 ; i < matches.length ; i++) {
            assertEquals(matches[i], matcher.matches.get(i),
                         "Match " + i + " should be as expected");
        }
    }

    private void assertMatches(CollectingMatcher matcher, Pattern delimiter, String source, String... matches) {
        assertEquals(matches.length, matcher.findMatches(source, delimiter),
                     "There should be the right number of matches");
        for (int i = 0 ; i < matches.length ; i++) {
            assertEquals(matches[i], matcher.matches.get(i),
                         "Match " + i + " should be as expected");
        }
    }

    private static class CollectingMatcher extends VerbatimMatcher<String> {
        public final List<String> matches = new ArrayList<>();
        public final List<String> payloads = new ArrayList<>();

        @Override
        public void callback(String match, String payload) {
            matches.add(match);
            payloads.add(payload);
        }
    }
}