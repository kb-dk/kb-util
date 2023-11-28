package dk.kb.util.webservice.stream;

import dk.kb.util.json.JSONStreamUtil;
import dk.kb.util.json.JSONStreamUtilTest;
import dk.kb.util.json.JSONStreamUtilTest.DsRecordDto;
import org.apache.commons.io.input.CharSequenceInputStream;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
class ContinuationStreamTest {

    @Test
    public void testMultiLevel() throws IOException {
        try (ContinuationInputStream<Long> is =
                     new ContinuationInputStream<>(
                             new CharSequenceInputStream(JSONStreamUtilTest.RECORDS2, StandardCharsets.UTF_8, 1024),
                             124L, true) ;
             ContinuationStream<DsRecordDto, Long> recordStream = is.stream(DsRecordDto.class)) {
            List<DsRecordDto> records = recordStream.collect(Collectors.toList());
            assertEquals(2, records.size(), "There should be the right number of records");
            assertEquals("id1", records.get(0).getId(), "The first record should have the expected ID");
            assertEquals(is.getContinuationToken(), records.get(records.size()-1).getmTime(),
                    "The continuation token should match the last record");
        }
    }

}