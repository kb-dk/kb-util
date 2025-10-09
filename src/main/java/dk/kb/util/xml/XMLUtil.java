/* $Id$
 *
 * The Summa project.
 * Copyright (C) 2005-2008  The Royal Danish Library
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package dk.kb.util.xml;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import dk.kb.util.reader.ReplaceFactory;
import dk.kb.util.reader.ReplaceReader;
import dk.kb.util.string.Strings;
import org.xml.sax.SAXException;

import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.StringReader;

import static dk.kb.util.xml.DOMUtil.logger;

/**
 * Misc. helpers for XML handling.
 */
public class XMLUtil {


    private static final ThreadLocal<ReplaceReader> localEncoder =
            new ThreadLocal<ReplaceReader>() {
                @Override
                protected ReplaceReader initialValue() {
                    return ReplaceFactory.getReplacer("&", "&amp;",
                            "\"", "&quot;",
                            "<", "&lt;",
                            ">", "&gt;",
                            "'", "&apos;");
                }
            };
    /**
     * Performs a simple entity-encoding of input, making it safe to include in XML.
     *
     * @param input the text to encode.
     * @return the text with &amp;, ", &lt; and &gt; encoded.
     */
    public static String encode(String input) {
        ReplaceReader r = localEncoder.get();
        r.setSource(new StringReader(input));
        return Strings.flushLocal(r);
    }

    /**
     * Converts an {@link XMLEvent}-id to String. Used for primarily
     * debugging and error messages.
     *
     * @param eventType the XMLEvent-id.
     * @return the event as human redable String.
     */
    public static String eventID2String(int eventType) {
        switch (eventType) {
            case XMLEvent.START_ELEMENT:
                return "START_ELEMENT";
            case XMLEvent.END_ELEMENT:
                return "END_ELEMENT";
            case XMLEvent.PROCESSING_INSTRUCTION:
                return "PROCESSING_INSTRUCTION";
            case XMLEvent.CHARACTERS:
                return "CHARACTERS";
            case XMLEvent.COMMENT:
                return "COMMENT";
            case XMLEvent.START_DOCUMENT:
                return "START_DOCUMENT";
            case XMLEvent.END_DOCUMENT:
                return "END_DOCUMENT";
            case XMLEvent.ENTITY_REFERENCE:
                return "ENTITY_REFERENCE";
            case XMLEvent.ATTRIBUTE:
                return "ATTRIBUTE";
            case XMLEvent.DTD:
                return "DTD";
            case XMLEvent.CDATA:
                return "CDATA";
            case XMLEvent.SPACE:
                return "SPACE";
            default:
                return "UNKNOWN_EVENT_TYPE " + "," + eventType;
        }
    }

    /**
     * Checks for XML equivalence between two XML objects, ignoring whitespace and element order.
     * @return true if the two documents are semantically equivalent.
     */
    public static boolean areXmlEquivalent(String xml1, String xml2) {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        try {
            Diff diffs = XMLUnit.compareXML(xml1, xml2);
            if (diffs.similar()) {
                return true;
            } else {
                for (Object diff : new DetailedDiff(diffs).getAllDifferences()) {
                    logger.debug("Found metadata diff: {}", diff);
                }
                return false;
            }
        } catch (SAXException | IOException e) {
            logger.warn("areXmlEquivalent(): ", e);
            return false;
        }
    }

}
