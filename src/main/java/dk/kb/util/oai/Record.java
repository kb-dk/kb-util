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
 * Modelled from <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#Record">OAI Record</a>.
 */
public class Record {
    private static final Logger log = LoggerFactory.getLogger(Record.class);

    private Header header;
    private String metadata;
    private List<String> abouts;

    public Record header(Header header) {
        this.header = header;
        return this;
    }
    public Header getHeader() {
        return header;
    }
    public void setHeader(Header header) {
        this.header = header;
    }

    public Record metadata(String metadata) {
        this.metadata = metadata;
        return this;
    }
    public String getMetadata() {
        return metadata;
    }
    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Record abouts(List<String> abouts) {
        this.abouts = abouts;
        return this;
    }
    public List<String> getAbouts() {
        return abouts == null ? Collections.emptyList() : abouts;
    }
    public Record addAbout(String about) {
        if (abouts == null) {
            abouts = new ArrayList<>();
        }
        if (!(abouts instanceof ArrayList || abouts instanceof LinkedList)) {
            abouts = new ArrayList<>(abouts); // Ensure mutability
        }
        abouts.add(about);
        return this;
    }
    public void setAbouts(List<String> abouts) {
        this.abouts = abouts;
    }

}
