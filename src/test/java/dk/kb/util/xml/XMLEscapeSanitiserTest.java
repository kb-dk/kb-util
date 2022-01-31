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
package dk.kb.util.xml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XMLEscapeSanitiserTest {

    @Test
    void noEscape() {
        assertEquals("<foo>Barbarian</foo>", escape("<foo>Barbarian</foo>"));
    }

    @Test
    void noEscapeNewlineHex1() {
        assertEquals("<foo>Barbarian&#x0A;</foo>", escape("<foo>Barbarian&#x0A;</foo>"));
    }

    @Test
    void badlyEscaped() {
        assertEquals("<foo>Barbarian&#0A;</foo>", escape("<foo>Barbarian&#0A;</foo>"));
    }

    @Test
    void noEscapeNewlineHex2() {
        assertEquals("<foo>Barbarian&#x000A;</foo>", escape("<foo>Barbarian&#x000A;</foo>"));
    }

    @Test
    void noEscapeNewlineDec() {
        assertEquals("<foo>Barbarian&#10;</foo>", escape("<foo>Barbarian&#10;</foo>"));
    }

    @Test
    void lowEscapeHex() {
        assertEquals("<foo>Barbarian?</foo>", escape("<foo>Barbarian&#x0;</foo>"));
    }

    @Test
    void lowEscapeDec() {
        assertEquals("<foo>Barbarian?</foo>", escape("<foo>Barbarian&#7;</foo>"));
    }

    @Test
    void highEscapeHex() {
        assertEquals("<foo>Barbarian?</foo>", escape("<foo>Barbarian&#xFDD0;</foo>"));
    }

    @Test
    void highEscapeHexLowercase() {
        assertEquals("<foo>Barbarian?</foo>", escape("<foo>Barbarian&#xfdd0;</foo>"));
    }

    @Test
    void extendedEscapeOK() {
        assertEquals("<foo>Barbarian&#x10FFFD;</foo>", escape("<foo>Barbarian&#x10FFFD;</foo>"));
    }

    @Test
    void extendedEscapeIllegal() {
        assertEquals("<foo>Barbarian?</foo>", escape("<foo>Barbarian&#x10FFFE;</foo>"));
    }

    private String escape(String xml) {
        return new XMLEscapeSanitiser().apply(xml);
    }
}