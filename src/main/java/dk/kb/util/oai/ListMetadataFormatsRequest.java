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
 * Request for <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#ListMetadataFormats">ListMetadataFormats</a>.
 */
public class ListMetadataFormatsRequest {
    private static final Logger log = LoggerFactory.getLogger(ListMetadataFormatsRequest.class);

    // http://www.perseus.tufts.edu/cgi-bin/pdataprov?verb=ListMetadataFormats&identifier=oai:perseus.tufts.edu:Perseus:text:1999.02.0119
    private String identifier;

    public ListMetadataFormatsRequest identifier(String identifier) {
        this.identifier = identifier;
        return this;
    }
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
