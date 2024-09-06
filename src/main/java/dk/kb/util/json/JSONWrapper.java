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
package dk.kb.util.json;

import org.json.JSONObject;

/**
 * Not implemented.
 * Had been thought to be a wrapper for JSoup for easier access to the JSON elements.
 */
@Deprecated(forRemoval = true)
public class JSONWrapper {

    /**
     * Not implemented.
     * @throws UnsupportedOperationException always
     */
    public static Object getObject(JSONObject json, String path) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Not implemented.
     * @throws UnsupportedOperationException always
     */
    public Object getObjects(String path) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
