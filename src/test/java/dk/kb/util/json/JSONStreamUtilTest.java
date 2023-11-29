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
package dk.kb.util.json;

import org.apache.commons.io.input.CharSequenceInputStream;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class JSONStreamUtilTest {

    public static final String RECORD1 = "{\"id\": \"id1\", \"mTime\": \"123\"}";
    public static final String RECORD2 = "{\"id\": \"id2\", \"mTime\": \"124\"}";
    public static final String RECORDS0 = "[]";
    public static final String RECORDS1 = "[" + RECORD1 + "]";
    public static final String RECORDS2 = "[" + RECORD1 + ", " + RECORD2 + "]";

    @Test
    public void testRecords0() throws IOException {
        try (Stream<DsRecordDto> deserialized = JSONStreamUtil.jsonToObjectsStream(
                new CharSequenceInputStream(RECORDS0, StandardCharsets.UTF_8, 1024), DsRecordDto.class)) {
            List<DsRecordDto> records = deserialized.collect(Collectors.toList());
            assertTrue(records.isEmpty(), "There should be no records, but there were " + records.size());
        }
    }

    @Test
    public void testRecords1() throws IOException {
        try (Stream<DsRecordDto> deserialized = JSONStreamUtil.jsonToObjectsStream(
                new CharSequenceInputStream(RECORDS1, StandardCharsets.UTF_8, 1024), DsRecordDto.class)) {
            List<DsRecordDto> records = deserialized.collect(Collectors.toList());
            assertEquals(1, records.size(), "There should be the right number of records");
            assertEquals("id1", records.get(0).getId(), "The first record should have the expected ID");
        }
    }

    @Test
    public void testRecords2() throws IOException {
        try (Stream<DsRecordDto> deserialized = JSONStreamUtil.jsonToObjectsStream(
                new CharSequenceInputStream(RECORDS2, StandardCharsets.UTF_8, 1024), DsRecordDto.class)) {
            List<DsRecordDto> records = deserialized.collect(Collectors.toList());
            assertEquals(2, records.size(), "There should be the right number of records");
            assertEquals("id1", records.get(0).getId(), "The first record should have the expected ID");
            assertEquals(123L, records.get(0).getmTime(), "The first record should have the expected mTime");
            assertEquals("id2", records.get(1).getId(), "The second record should have the expected ID");
        }
    }

    public static class DsRecordDto {
        public static final String JSON_PROPERTY_ID = "id";
        private String id;

        public static final String JSON_PROPERTY_M_TIME = "mTime";
        private Long mTime;

        public String getId() {
            return id;
        }

        public Long getmTime() {
            return mTime;
        }
    }
}