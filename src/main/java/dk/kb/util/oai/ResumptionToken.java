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
 * Modelled from <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#ListIdentifiers">OAI-PMH resumptionToken</a>.
 */
public class ResumptionToken {
    private static final Logger log = LoggerFactory.getLogger(ResumptionToken.class);

    // http://an.oa.org/OAI-script?verb=ListIdentifiers&resumptionToken=xxx45abttyz
    private String expirationDate;
    private Long completeListSize;
    private long cursor;

    public ResumptionToken expirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
        return this;
    }
    public String getExpirationDate() {
        return expirationDate;
    }
    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public ResumptionToken completeListSize(Long completeListSize) {
        this.completeListSize = completeListSize;
        return this;
    }
    public Long getCompleteListSize() {
        return completeListSize;
    }
    public void setCompleteListSize(Long completeListSize) {
        this.completeListSize = completeListSize;
    }

    public ResumptionToken cursor(long cursor) {
        this.cursor = cursor;
        return this;
    }
    public long getCursor() {
        return cursor;
    }
    public void setCursor(long cursor) {
        this.cursor = cursor;
    }
}
