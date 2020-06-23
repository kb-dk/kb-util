package dk.kb.util;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

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
class CollectionUtilsTest {

    @Test
    public void testForEachLeaf() {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("direct", true);

        Set<Integer> numbers = new LinkedHashSet<>();
        Stream.of(100, 200, 300).forEach(numbers::add);
        map.put("num", numbers);

        Map<String, Object> subMap = new LinkedHashMap<>();
        subMap.put("foo", "bar");

        map.put("subMap", subMap);

        StringBuilder sb = new StringBuilder();
        CollectionUtils.forEachLeaf(map, (path, value) -> {
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append(path).append("=").append(value);
        });
        assertEquals("direct=true, num.0=100, num.1=200, num.2=300, subMap.foo=bar", sb.toString());
    }

}