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
package dk.kb.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class CollectionUtils {

    /**
     * Recursive visitor of leafs, ordered and depth-first.
     *
     * Assumes input to be a construction of {@code Map<String, Object>}, {@link Collection<Object>} and other types.
     * Maps and collections are traversed recursively, extending the path underways. Other types are fed directly to
     * action.
     * Note: Since this expects maps to have {@code String} as the key type, it will fail for all other key types.
     * @param input  a map, collection or something third. Only maps and collections gets special treatment.
     * @param action the action to perform on all leafs.
     */
    public static void forEachLeaf(Object input, BiConsumer<? super String, ? super Object> action) {
        forEachLeaf(input, "", action);
    }

    @SuppressWarnings("unchecked")
    private static void forEachLeaf(
            Object input, String path, BiConsumer<? super String, ? super Object> action) {

        // Maps are treated as nodes and a recursive call is initiated
        if (input instanceof Map) {
            ((Map<String, Object>) input).forEach((key, value) -> {
                String subPath = (path.isEmpty() ? "" : path + ".") + key;
                forEachLeaf(value, subPath, action);
            });
            return;
        }

        // Collections are iterated with the path being extended with an index into the collection
        if (input instanceof Collection) {
            int index = 0;
            for (Object item : (Collection<Object>) input) {
                String collectionPath = (path.isEmpty() ? "" : path + ".") + index++;
                forEachLeaf(item, collectionPath, action);
            }
            return;
        }

        // Leafs are handled directly
        action.accept(path, input);
    }
}
