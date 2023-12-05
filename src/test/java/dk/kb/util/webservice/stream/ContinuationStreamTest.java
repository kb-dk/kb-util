package dk.kb.util.webservice.stream;

import dk.kb.util.json.JSONStreamUtilTest;
import dk.kb.util.json.JSONStreamUtilTest.DsRecordDto;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

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
class ContinuationStreamTest {

    @Test
    public void testMultiLevel() throws IOException {
        try (ContinuationInputStream<Long> is =
                     new ContinuationInputStream<>(
                             new ByteArrayInputStream(JSONStreamUtilTest.RECORDS2.getBytes(StandardCharsets.UTF_8)),
                             124L, true, 2L) ;
             ContinuationStream<DsRecordDto, Long> recordStream = is.stream(DsRecordDto.class)) {
            List<DsRecordDto> records = recordStream.collect(Collectors.toList());
            assertEquals(2, records.size(), "There should be the right number of records");
            assertEquals("id1", records.get(0).getId(), "The first record should have the expected ID");
            assertEquals(is.getContinuationToken(), records.get(records.size()-1).getmTime(),
                    "The continuation token should match the last record");
            assertEquals(true, is.hasMore(), "The has more flag should be transfered");
            assertEquals(records.size(), is.getRecordCount(), "Record count should match");
        }
    }

    @Test
    public void testEmpty() throws IOException {
        try (ContinuationInputStream<Long> is =
                     new ContinuationInputStream<>(
                             new ByteArrayInputStream(JSONStreamUtilTest.RECORDS0.getBytes(StandardCharsets.UTF_8)),
                             null, false, 0L) ;
             ContinuationStream<DsRecordDto, Long> recordStream = is.stream(DsRecordDto.class)) {
            List<DsRecordDto> records = recordStream.collect(Collectors.toList());
            assertTrue(records.isEmpty(), "There should be no records");
            assertNull(is.getContinuationToken(), "There should be no continuation token");
            assertEquals(false, is.hasMore(), "The has more flag should be transfered");
            assertEquals(0, is.getRecordCount(), "Record count should be 0");
        }
    }

}