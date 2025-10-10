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

import org.apache.commons.lang3.builder.DiffBuilder;
import dk.kb.util.reader.ReplaceFactory;
import dk.kb.util.reader.ReplaceReader;
import dk.kb.util.string.Strings;
import javax.xml.stream.events.XMLEvent;
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
     * Checks for XML equivalence between two XML strings, ignoring whitespace, attribute order, and element order.
     * Elements and attributes are compared semantically, not textually.
     *
     * @param xml1 the first XML string
     * @param xml2 the second XML string
     * @return true if the two documents are semantically equivalent
     */
    public static boolean areXmlEquivalent(String xml1, String xml2) {
        try {
            javax.xml.parsers.DocumentBuilderFactory factory =
                    javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setIgnoringElementContentWhitespace(true);

            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();

            org.w3c.dom.Document doc1 = builder.parse(
                    new org.xml.sax.InputSource(new StringReader(xml1)));
            org.w3c.dom.Document doc2 = builder.parse(
                    new org.xml.sax.InputSource(new StringReader(xml2)));

            // Normalize documents (collapses whitespace, removes empty text nodes)
            doc1.normalizeDocument();
            doc2.normalizeDocument();

            boolean equivalent = areNodesEquivalent(doc1.getDocumentElement(),
                    doc2.getDocumentElement());

            if (!equivalent) {
                logger.debug("XML documents are not equivalent");
            }

            return equivalent;

        } catch (Exception e) {
            logger.warn("areXmlEquivalent(): ", e);
            return false;
        }
    }

    /**
     * Recursively compares two DOM nodes for semantic equivalence.
     * Ignores attribute order, element order, and whitespace-only text nodes.
     */
    private static boolean areNodesEquivalent(org.w3c.dom.Node node1, org.w3c.dom.Node node2) {
        // Check node types
        if (node1.getNodeType() != node2.getNodeType()) {
            return false;
        }

        // Check node names (consider namespace)
        if (!equals(node1.getLocalName(), node2.getLocalName()) ||
                !equals(node1.getNamespaceURI(), node2.getNamespaceURI())) {
            return false;
        }

        // Check node values (for text nodes, etc.)
        if (!equals(normalizeWhitespace(node1.getNodeValue()),
                normalizeWhitespace(node2.getNodeValue()))) {
            return false;
        }

        // Compare attributes (order-independent)
        if (node1.hasAttributes() || node2.hasAttributes()) {
            if (!areAttributesEquivalent(node1.getAttributes(), node2.getAttributes())) {
                return false;
            }
        }

        // Compare child nodes (order-independent)
        org.w3c.dom.NodeList children1 = node1.getChildNodes();
        org.w3c.dom.NodeList children2 = node2.getChildNodes();

        // Filter out whitespace-only text nodes
        java.util.List<org.w3c.dom.Node> filteredChildren1 = filterSignificantNodes(children1);
        java.util.List<org.w3c.dom.Node> filteredChildren2 = filterSignificantNodes(children2);

        if (filteredChildren1.size() != filteredChildren2.size()) {
            return false;
        }

        // Match children in any order
        return areChildrenEquivalentInAnyOrder(filteredChildren1, filteredChildren2);
    }

    /**
     * Compares two lists of child nodes for equivalence, ignoring order.
     * Uses a greedy matching algorithm to find equivalent nodes.
     */
    private static boolean areChildrenEquivalentInAnyOrder(
            java.util.List<org.w3c.dom.Node> children1,
            java.util.List<org.w3c.dom.Node> children2) {

        if (children1.size() != children2.size()) {
            return false;
        }

        // Track which nodes from children2 have been matched
        java.util.Set<Integer> matchedIndices = new java.util.HashSet<>();

        // For each node in children1, find a matching node in children2
        for (org.w3c.dom.Node child1 : children1) {
            boolean foundMatch = false;

            for (int i = 0; i < children2.size(); i++) {
                // Skip already matched nodes
                if (matchedIndices.contains(i)) {
                    continue;
                }

                org.w3c.dom.Node child2 = children2.get(i);

                // Check if these nodes are equivalent
                if (areNodesEquivalent(child1, child2)) {
                    matchedIndices.add(i);
                    foundMatch = true;
                    break;
                }
            }

            if (!foundMatch) {
                return false;
            }
        }

        return true;
    }

    /**
     * Filters out insignificant whitespace-only text nodes.
     */
    private static java.util.List<org.w3c.dom.Node> filterSignificantNodes(
            org.w3c.dom.NodeList nodeList) {
        java.util.List<org.w3c.dom.Node> result = new java.util.ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            org.w3c.dom.Node node = nodeList.item(i);
            if (node.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                String text = node.getNodeValue();
                if (text != null && text.trim().isEmpty()) {
                    continue; // Skip whitespace-only text nodes
                }
            }
            result.add(node);
        }
        return result;
    }

    /**
     * Compares two attribute maps for equivalence (order-independent).
     */
    private static boolean areAttributesEquivalent(org.w3c.dom.NamedNodeMap attrs1, org.w3c.dom.NamedNodeMap attrs2) {
        if (attrs1 == null && attrs2 == null) return true;
        if (attrs1 == null || attrs2 == null || attrs1.getLength() != attrs2.getLength()) return false;

        // Create a map for attrs2 for easier lookup using qualified names
        java.util.Map<String, String> attrs2Map = new java.util.HashMap<>();
        for (int i = 0; i < attrs2.getLength(); i++) {
            org.w3c.dom.Node attr2 = attrs2.item(i);
            attrs2Map.put(attr2.getNodeName(), attr2.getNodeValue());
        }

        // Check each attribute in attrs1 against the map of attrs2
        for (int i = 0; i < attrs1.getLength(); i++) {
            org.w3c.dom.Node attr1 = attrs1.item(i);
            String qualifiedName = attr1.getNodeName();
            String attrValue2 = attrs2Map.get(qualifiedName);

            // Check using local name and namespace URI if not found
            if (attrValue2 == null) {
                if ("xmlns".equals(attr1.getLocalName())) continue; // Skip namespace declarations

                String localName = attr1.getLocalName();
                String namespaceURI = attr1.getNamespaceURI();
                boolean found = false;

                // Check for matching attributes by local name and namespace URI
                for (int j = 0; j < attrs2.getLength(); j++) {
                    org.w3c.dom.Node attr2 = attrs2.item(j);
                    if ("xmlns".equals(attr2.getLocalName())) continue; // Skip namespace declarations

                    if (localName.equals(attr2.getLocalName()) &&
                            (namespaceURI == null ? attr2.getNamespaceURI() == null : namespaceURI.equals(attr2.getNamespaceURI()))) {
                        attrValue2 = attr2.getNodeValue();
                        found = true;
                        break;
                    }
                }

                if (!found) return false; // Attribute not found in attrs2
            }

            if (!equals(attr1.getNodeValue(), attrValue2)) return false; // Attribute values do not match
        }

        return true;
    }

    /**
     * Normalizes whitespace in a string (null-safe).
     */
    private static String normalizeWhitespace(String str) {
        if (str == null) {
            return null;
        }
        return str.trim().replaceAll("\\s+", " ");
    }

    /**
     * Null-safe string equality check.
     */
    private static boolean equals(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        return s1.equals(s2);
    }
}
