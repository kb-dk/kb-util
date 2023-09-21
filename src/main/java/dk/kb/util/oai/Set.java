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

/**
 * Encapsulation class consisting solely of attributes and mutators + accessors.
 * <p>
 * Modelled from <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#Set">OAI-PMH Set</a>.
 */
public class Set {
    private static final Logger log = LoggerFactory.getLogger(Set.class);

    private String setSpec;
    private String setName;
    private List<String> descriptions;

    public Set setSpec(String setSpec) {
        this.setSpec = setSpec;
        return this;
    }
    public String getSetSpec() {
        return setSpec;
    }
    public void setSetSpec(String setSpec) {
        this.setSpec = setSpec;
    }

    public Set setName(String setName) {
        this.setName = setName;
        return this;
    }
    public String getSetName() {
        return setName;
    }
    public void setSetName(String setName) {
        this.setName = setName;
    }

    public Set descriptions(List<String> description) {
        this.descriptions = description;
        return this;
    }
    public List<String> getDescriptions() {
        return descriptions;
    }
    public void addDescription(String description) {

    }
    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }
}
