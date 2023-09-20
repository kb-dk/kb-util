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
 * Request for <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#ListIdentifiers">ListIdentifiers</a>.
 */
public class ListIdentifiersRequest {
    private static final Logger log = LoggerFactory.getLogger(ListIdentifiersRequest.class);

    // http://an.oa.org/OAI-script?verb=ListIdentifiers&from=1998-01-15&metadataPrefix=oldArXiv&set=physics:hep
    
    private String from;
    private String until;
    private String set;
    private String resumptionToken;

    public ListIdentifiersRequest from(String from) {
        this.from = from;
        return this;
    }
    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }

    public ListIdentifiersRequest to(String until) {
        this.until = until;
        return this;
    }
    public String getUntil() {
        return until;
    }
    public void setUntil(String until) {
        this.until = until;
    }

    public ListIdentifiersRequest set(String set) {
        this.set = set;
        return this;
    }
    public String getSet() {
        return set;
    }
    public void setSet(String set) {
        this.set = set;
    }

    public ListIdentifiersRequest resumptionToken(String resumptionToken) {
        this.resumptionToken = resumptionToken;
        return this;
    }
    public String getResumptionToken() {
        return resumptionToken;
    }
    public void setResumptionToken(String resumptionToken) {
        this.resumptionToken = resumptionToken;
    }
}
