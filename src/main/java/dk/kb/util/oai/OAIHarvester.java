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

import java.util.List;
import java.util.stream.Stream;

/**
 *
 */
public class OAIHarvester {
    private static final Logger log = LoggerFactory.getLogger(OAIHarvester.class);

    // TODO: Setup mechanism for repository URL and calls

    /**
     * Issued one call to
     * <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#Identify">OAI-PMH Identify</a>
     * and return the response.
     * @return the indentify information for the OAI-PMH Repository.
     */
    public IdentifyResponse identify() {
        // http://memory.loc.gov/cgi-bin/oai?verb=Identify
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Issues one or more calls to
     * <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#ListIdentifiers">OAI-PMH ListIdentifiers</a>
     * requests, using returned {@code resumptionToken}s for paging.
     * @param request for OAI-PMH ListIdentifiers.
     * @return a stream of
     *         <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#Record">OAI-PMH Record headers</a>.
     */
    public Stream<Header> listIdentifiers(ListIdentifiersRequest request) {
        // http://an.oa.org/OAI-script?verb=ListIdentifiers&from=1998-01-15&metadataPrefix=oldArXiv&set=physics:hep
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Issues one or more calls to
     * <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#ListRecords">OAI-PMH ListRecords</a>
     * requests, using returned {@code resumptionToken}s for paging.
     * @param request for OAI-PMH ListRecords.
     * @return a stream of
     *         <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#Record">OAI-PMH Records</a>.
     */
    public Stream<Record> listRecords(ListRecordsRequest request) {
        // http://an.oa.org/OAI-script?verb=ListRecords&from=1998-01-15&set=physics:hep&metadataPrefix=oai_rfc1807
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Issues one call to
     * <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#ListMetadataFormats">OAI-PMH ListMetadataFormats</a>
     * and return the response.
     * @param request for OAI-PMH ListMetadataFormats. This can be null.
     * @return the metadata formats that the OAI-PMH Repository supports, optionally for a specific record.
     */
    public List<String> listMetadataFormats(ListMetadataFormatsRequest request) {
        if (request == null) {
            request = new ListMetadataFormatsRequest();
        }
        // http://www.perseus.tufts.edu/cgi-bin/pdataprov?verb=ListMetadataFormats&identifier=oai:perseus.tufts.edu:Perseus:text:1999.02.0119
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Issues one call to
     * <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#GetRecord">OAI-PMH GetRecord</a>
     * and return the response.
     * @param request for OAI-PMH GetRecordl.
     * @return a specific record from the OAI-PMH Repository.
     */
    public Record getRecord(GetRecordRequest request) {
        // http://www.openarchives.org/OAI/openarchivesprotocol.html#GetRecord
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // TODO: ListSets might state that no sets are supported. Should the return value be an Optional<Stream<Set>> ?
    // http://www.openarchives.org/OAI/openarchivesprotocol.html#Set
}
