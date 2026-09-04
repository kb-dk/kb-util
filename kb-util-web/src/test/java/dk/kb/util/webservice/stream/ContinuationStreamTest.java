package dk.kb.util.webservice.stream;


import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
class ContinuationStreamTest {


    public static final String RECORD1 = "{\"id\": \"id1\", \"mTime\": \"123\"}";
    public static final String RECORD2 = "{\"id\": \"id2\", \"mTime\": \"124\"}";
    public static final String RECORDS0 = "[]";
    public static final String RECORDS2 = "[" + RECORD1 + ", " + RECORD2 + "]";

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

    @Test
    public void testMultiLevel() throws IOException {
        try (ContinuationInputStream<Long> is =
                     new ContinuationInputStream<>(
                             new ByteArrayInputStream(RECORDS2.getBytes(StandardCharsets.UTF_8)),
                             124L, true, 2L);
             ContinuationStream<DsRecordDto, Long> recordStream = is.stream(DsRecordDto.class)) {
            List<DsRecordDto> records = recordStream.toList();
            assertEquals(2, records.size(), "There should be the right number of records");
            assertEquals("id1", records.get(0).getId(), "The first record should have the expected ID");
            assertEquals(is.getContinuationToken(), records.get(records.size() - 1).getmTime(),
                         "The continuation token should match the last record");
            assertEquals(true, is.hasMore(), "The has more flag should be transfered");
            assertEquals(records.size(), is.getRecordCount(), "Record count should match");
        }
    }

    @Test
    public void testEmpty() throws IOException {
        try (ContinuationInputStream<Long> is =
                     new ContinuationInputStream<>(
                             new ByteArrayInputStream(RECORDS0.getBytes(StandardCharsets.UTF_8)),
                             null, false, 0L);
             ContinuationStream<DsRecordDto, Long> recordStream = is.stream(DsRecordDto.class)) {
            List<DsRecordDto> records = recordStream.toList();
            assertTrue(records.isEmpty(), "There should be no records");
            assertNull(is.getContinuationToken(), "There should be no continuation token");
            assertEquals(false, is.hasMore(), "The has more flag should be transfered");
            assertEquals(0, is.getRecordCount(), "Record count should be 0");
        }
    }

}