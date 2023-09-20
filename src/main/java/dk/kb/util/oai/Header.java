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
 * Modelled from <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#header">OAI Header</a>.
 */
public class Header {
    private static final Logger log = LoggerFactory.getLogger(Header.class);

    private String identifier;
    private String datestamp;
    private List<String> setSpecs;
    private String status;

    public Header identifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Header datestamp(String datestamp) {
        this.datestamp = datestamp;
        return this;
    }

    public String getDatestamp() {
        return datestamp;
    }

    public void setDatestamp(String datestamp) {
        this.datestamp = datestamp;
    }

    public Header setSpecs(List<String> setSpecs) {
        this.setSpecs = setSpecs;
        return this;
    }

    public List<String> getSetSpecs() {
        return setSpecs == null ? Collections.emptyList() : setSpecs;
    }

    public Header addSetSpecs(String setSpec) {
        if (setSpecs == null) {
            setSpecs = new ArrayList<>();
        }
        if (!(setSpecs instanceof ArrayList || setSpecs instanceof LinkedList)) {
            setSpecs = new ArrayList<>(setSpecs); // Ensure mutability
        }
        setSpecs.add(setSpec);
        return this;
    }

    public void setSetSpecs(List<String> setSpecs) {
        this.setSpecs = setSpecs;
    }

    public Header status(String status) {
        this.status = status;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
