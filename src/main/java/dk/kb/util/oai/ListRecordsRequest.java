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

/**
 * Encapsulation class consisting solely of attributes and mutators + accessors.
 * <p>
 * Request for <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#ListRecords">ListRecords</a>.
 */
public class ListRecordsRequest extends ListIdentifiersRequest {
    private static final Logger log = LoggerFactory.getLogger(ListRecordsRequest.class);

    // http://an.oa.org/OAI-script?verb=ListRecords&from=1998-01-15&set=physics:hep&metadataPrefix=oai_rfc1807
    private String metadataPrefix;

    public ListIdentifiersRequest metadataPrefix(String metadataPrefix) {
        this.metadataPrefix = metadataPrefix;
        return this;
    }
    public String getMetadataPrefix() {
        return metadataPrefix;
    }
    public void setMetadataPrefix(String metadataPrefix) {
        this.metadataPrefix = metadataPrefix;
    }
}
