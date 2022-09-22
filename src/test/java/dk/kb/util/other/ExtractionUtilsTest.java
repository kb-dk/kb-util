package dk.kb.util.other;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
class ExtractionUtilsTest {

    @Test
    void testPartitionList() {
        Stream<List<Integer>> out = ExtractionUtils.splitToLists(Stream.of(1, 2, 3, 4, 5), 2);

        List<List<Integer>> outLists = out.collect(Collectors.toList());

        assertEquals(3, outLists.size(), "There should be 3 streams in the output");

        assertEquals(2, outLists.get(0).size(), "The first stream should contain a list of the expected size");
        assertEquals(2, outLists.get(1).size(), "The second stream should contain a list of the expected size");
        assertEquals(1, outLists.get(2).size(), "The third stream should contain a list of the expected size");

        assertEquals(4, outLists.get(1).get(1), "The second element in the second stream should be correct");
    }

    @Test
    void testPartitionStream() {
        Stream<Stream<Integer>> out = ExtractionUtils.splitToStreams(Stream.of(1, 2, 3, 4, 5), 2);

        List<List<Integer>> outLists = out.
                map(stream -> stream.collect(Collectors.toList())).
                collect(Collectors.toList());

        assertEquals(3, outLists.size(), "There should be 3 streams in the output");

        assertEquals(2, outLists.get(0).size(), "The first stream should contain a stream of the expected size");
        assertEquals(2, outLists.get(1).size(), "The second stream should contain a stream of the expected size");
        assertEquals(1, outLists.get(2).size(), "The third stream should contain a stream of the expected size");

        assertEquals(4, outLists.get(1).get(1), "The second element in the second stream should be correct");
    }

    @Test
    void testPartitionStreamparallel() {
        Stream<Stream<Integer>> out = ExtractionUtils.splitToStreams(Stream.of(1, 2, 3, 4, 5).parallel(), 2);

        List<List<Integer>> outLists = out.
                map(stream -> stream.collect(Collectors.toList())).
                collect(Collectors.toList());

        assertEquals(3, outLists.size(), "There should be 3 streams in the output");

        assertEquals(2, outLists.get(0).size(), "The first stream should contain a stream of the expected size");
        assertEquals(2, outLists.get(1).size(), "The second stream should contain a stream of the expected size");
        assertEquals(1, outLists.get(2).size(), "The third stream should contain a stream of the expected size");
        
        List<Integer> sorted = outLists.stream().flatMap(Collection::stream).sorted().collect(Collectors.toList());
        assertEquals("[1, 2, 3, 4, 5]", sorted.toString(), "All values should be present in the output");
    }

    @Test
    void testSample() {
        final Random r = new Random(87);
        Optional<Integer> sample = Stream.of(1, 2, 3, 4).collect(ExtractionUtils.sample(r));
        // We seed the Random with a fixed seed, so we know what the "random" value will be
        assertEquals(3, sample.orElseThrow(), "The \"random\" sample should be as expected");
    }

    @Test
    void testSampleRandom() {
        Optional<Integer> sample = Stream.of(1, 2, 3, 4).collect(ExtractionUtils.sample());
        assertTrue(sample.isPresent(), "A sample should be present");
        // We don't seed the Random, so we don't know which value is returned
    }

    @Test
    void testSampleSet() {
        final Random r = new Random(87);
        LinkedHashSet<Integer> input = new LinkedHashSet<>(List.of(0, 1, 2, 3, 4));
        List<Integer> sample = ExtractionUtils.samples(input, 2, r);
        // We seed the Random with a fixed seed, so we know what the "random" value will be
        assertEquals(List.of(0, 4), sample, "The \"random\" sample should be as expected");
    }

    @Test
    void testSampleOrder() {
        List<Integer> input = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        // Unit testing by probabilities. Improvement suggestions welcome
        for (int RUNS = 0; RUNS < 100; RUNS++) {
            List<Integer> sample = ExtractionUtils.samples(input, 2);
            assertEquals(2, sample.size(), "Sample count should be as expected");
            assertTrue(sample.get(0) < sample.get(1), "Element order should be preserved");
        }
    }

    @Test
    void testMinCollection() {
        List<Integer> samples = List.of(4, 5, 1, 3, 2, 5, 5, 1);
        assertEquals(List.of(1, 1), ExtractionUtils.minima(samples));
    }

    @Test
    void testMinStrings() {
        List<String> samples = List.of("b", "a", "a", "z", "b");
        assertEquals(List.of("a", "a"), ExtractionUtils.minima(samples));
    }

    @Test
    void testMinStream() {
        Stream<Integer> samples = Stream.of(4, 5, 1, 3, 2, 5, 5, 1);
        assertEquals(List.of(1, 1), ExtractionUtils.minima(samples));
    }

    @Test
    void testMaxCollection() {
        List<Integer> samples = List.of(4, 5, 1, 3, 2, 5, 5, 1);
        assertEquals(List.of(5, 5, 5), ExtractionUtils.maxima(samples));
    }

    @Test
    void testMaxStream() {
        Stream<Integer> samples = Stream.of(4, 5, 1, 3, 2, 5, 5, 1);
        assertEquals(List.of(5, 5, 5), ExtractionUtils.maxima(samples));
    }

    @Test
    void testMinStreamCustom() {
        Stream<Integer> samples = Stream.of(4, 5, 1, 3, 2, 5, 5, 1);
        assertEquals(List.of(3), ExtractionUtils.minima(samples, ExtractionUtilsTest::fakeComparator));
    }

   @Test
    void testMinCollectionCustom() {
        List<Integer> samples = List.of(4, 5, 1, 3, 2, 5, 5, 1);
        assertEquals(List.of(3), ExtractionUtils.minima(samples, ExtractionUtilsTest::fakeComparator));
    }

    @Test
    void testMaxStreamCustom() {
        Stream<Integer> samples = Stream.of(4, 5, 1, 3, 2, 5, 5, 1);
        assertEquals(List.of(2), ExtractionUtils.maxima(samples, ExtractionUtilsTest::fakeComparator));
    }

   @Test
    void testMaxCollectionCustom() {
        List<Integer> samples = List.of(4, 5, 1, 3, 2, 5, 5, 1);
        assertEquals(List.of(2), ExtractionUtils.maxima(samples, ExtractionUtilsTest::fakeComparator));
    }

    /**
     * Natural order comparison for all integers except 3 which is seen as lowest, and 2 which is seen as highest.
     */
    private static int fakeComparator(Integer v1, Integer v2) {
        if (v1 == 3) {
            return v2 == 3 ? 0 : -1;
        }
        if (v2 == 3) {
            return 1;
        }

        if (v1 == 2) {
            return v2 == 2 ? 0 : 1;
        }
        if (v2 == 2) {
            return -1;
        }

        return Integer.compare(v1, v2);
    }

}