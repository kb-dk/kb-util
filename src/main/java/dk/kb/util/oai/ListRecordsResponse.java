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
package dk.kb.util.oai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Encapsulation of a
 * <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#ListRecords">OAI-PMH ListRecords</a>
 * response.
 * <p>
 * Low heap and garbage collector load: External calls are batched using the OAI-OMH {@code resumptionToken} and
 * internal XML processing is streaming.
 */
public class ListRecordsResponse implements Iterator<Record> {
    private static final Logger log = LoggerFactory.getLogger(ListRecordsResponse.class);

    // http://www.perseus.tufts.edu/cgi-b:in/pdataprov?verb=ListRecords&from=2002-05-01T14:15:00Z&until=2002-05-01T14:20:00Z&metadataPrefix=oai_dc
    private String lastResponseDate;
    private ListIdentifiersRequest request;
    private ResumptionToken lastResumptionToken;

    @Override
    public boolean hasNext() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }

    @Override
    public Record next() {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
