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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Encapsulation class consisting solely of attributes and mutators + accessors.
 * <p>
 * Modelled from <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#ErrorConditions">OAI-PMH Error Conditions</a>.
 */
public class Error {
    private static final Logger log = LoggerFactory.getLogger(Error.class);

    public enum CODE { badArgment, badResumptionToken, badVerb, cannotDisseminateFormat, idDoesNotExist,
        noRecordsMatch, noMetadataFormats, noSetHierarchy };

    private CODE code;
    private String description;

    public Error code(String code) {
        this.code = CODE.valueOf(code);
        return this;
    }
    public Error code(CODE code) {
        this.code = code;
        return this;
    }
    public CODE getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = CODE.valueOf(code);
    }
    public void setCode(CODE code) {
        this.code = code;
    }

    public Error description(String description) {
        this.description = description;
        return this;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
