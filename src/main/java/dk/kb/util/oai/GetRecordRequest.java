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
 * Modelled from <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#GetRecord">OAI GetRecord</a>.
 */
public class GetRecordRequest {
    private static final Logger log = LoggerFactory.getLogger(GetRecordRequest.class);

    private String identifier;
    private String metadataPrefix;

    public GetRecordRequest identifier(String identifier) {
        this.identifier = identifier;
        return this;
    }
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public GetRecordRequest metadataPrefix(String metadataPrefix) {
        this.metadataPrefix = metadataPrefix;
        return this;
    }
    public String getMetadataPrefix() {
        return metadataPrefix;
    }
    public void setMetadataPrefix(String metadataPrefix) {
        this.metadataPrefix = metadataPrefix;
    }

    public void validateRequest() {
        if (identifier == null) {
            throw new IllegalStateException("identifier is null but is required");
        }
        if (metadataPrefix == null) {
            throw new IllegalStateException("metadataPrefix is null but is required");
        }
    }
}
