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
 * Modelled from <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#Identify">OAI Identify</a>.
 */
public class IdentifyResponse {
    private static final Logger log = LoggerFactory.getLogger(IdentifyResponse.class);

    // Mandatorys
    private String repositoryName;
    private String baseURL;
    private String protocolVersion;
    private String earliestDatestamp;
    private String deletion;    // no, transient, persistent
    private String granularity; // YYYY-MM-DD and YYYY-MM-DDThh:mm:ssZ
    private List<String> adminEmails;

    // Optionals
    private String compression;
    private List<String> descriptions;

    public IdentifyResponse repositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
        return this;
    }
    public String getRepositoryName() {
        return repositoryName;
    }
    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public IdentifyResponse baseURL(String baseURL) {
        this.baseURL = baseURL;
        return this;
    }
    public String getBaseURL() {
        return baseURL;
    }
    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public IdentifyResponse protocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
        return this;
    }
    public String getProtocolVersion() {
        return protocolVersion;
    }
    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public IdentifyResponse earliestDatestamp(String earliestDatestamp) {
        this.earliestDatestamp = earliestDatestamp;
        return this;
    }
    public String getEarliestDatestamp() {
        return earliestDatestamp;
    }
    public void setEarliestDatestamp(String earliestDatestamp) {
        this.earliestDatestamp = earliestDatestamp;
    }

    public IdentifyResponse deletion(String deletion) {
        this.deletion = deletion;
        return this;
    }
    public String getDeletion() {
        return deletion;
    }
    public void setDeletion(String deletion) {
        this.deletion = deletion;
    }

    public IdentifyResponse granularity(String granularity) {
        this.granularity = granularity;
        return this;
    }
    public String getGranularity() {
        return granularity;
    }
    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    public IdentifyResponse adminEmails(List<String> adminEmails) {
        this.adminEmails = adminEmails;
        return this;
    }
    public List<String> getAdminEmails() {
        return adminEmails;
    }
    public IdentifyResponse addAdminEmail(String adminEmail) {
        if (adminEmails == null) {
            adminEmails = new ArrayList<>();
        }
        if (!(adminEmails instanceof ArrayList || adminEmails instanceof LinkedList)) {
            adminEmails = new ArrayList<>(adminEmails); // Ensure mutability
        }
        adminEmails.add(adminEmail);
        return this;
    }
    public void setAdminEmails(List<String> adminEmails) {
        this.adminEmails = adminEmails;
    }

    public IdentifyResponse compression(String compression) {
        this.compression = compression;
        return this;
    }
    public String getCompression() {
        return compression;
    }
    public void setCompression(String compression) {
        this.compression = compression;
    }

    public IdentifyResponse descriptions(List<String> descriptions) {
        this.descriptions = descriptions;
        return this;
    }
    public List<String> getDescriptions() {
        return descriptions;
    }
    public IdentifyResponse addDescription(String description) {
        if (descriptions == null) {
            descriptions = new ArrayList<>();
        }
        if (!(descriptions instanceof ArrayList || descriptions instanceof LinkedList)) {
            descriptions = new ArrayList<>(descriptions); // Ensure mutability
        }
        adminEmails.add(description);
        return this;
    }
    public void setDescriptions(List<String> descriptions) {
        this.descriptions = descriptions;
    }
}
