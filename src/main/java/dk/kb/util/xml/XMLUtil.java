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

import dk.kb.util.reader.ReplaceFactory;
import dk.kb.util.reader.ReplaceReader;
import dk.kb.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * Misc. helpers for XML handling.
 */
public class XMLUtil {

    static final Logger logger = LoggerFactory.getLogger(XMLUtil.class);

    private static final ThreadLocal<ReplaceReader> localEncoder = ThreadLocal.withInitial(() ->
            ReplaceFactory.getReplacer("&", "&amp;", "\"", "&quot;", "<", "&lt;", ">", "&gt;", "'", "&apos;")
    );

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
     * Creates an empty Document object with the specified XML version and whether it is standalone.
     * @param xmlVersion The used XML version.
     * @param standalone Whether it is standalone in the XML sense of the word
     *          (see for example <a href="https://stackoverflow.com/questions/5578645/what-does-the-standalone-directive-mean-in-xml">"https://stackoverflow.com/questions/5578645/what-does-the-standalone-directive-mean-in-xml"</a>)
     * @return A new empty Document object.
     */
    public static Document createDocument(String xmlVersion, boolean standalone) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document newDocument = documentBuilder.newDocument();
            newDocument.setXmlVersion(xmlVersion);
            newDocument.setXmlStandalone(standalone);
            return newDocument;
        } catch (ParserConfigurationException e) {
            logger.error("An error occurred while creating a DocumentBuilder for a new Document.", e);
            throw new RuntimeException("An error occurred while creating a DocumentBuilder.", e);
        }
    }

    /**
     * This is the simpler version of {@link #createDocument(String, boolean)}
     * where the values are set with default: xmlVersion="1.0" and standalone=true.
     * @return A new empty Document object.
     */
    public static Document createDocument() {
        return createDocument("1.0", true);
    }

    /**
     * Takes an XML fragment represented as a String object and parses it to a Document object.
     * @param xml The XML fragment represented as a string.
     * @return A document object, parsed from the given XML string.
     */
    public static Document parseDocument(String xml) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.error("An error occurred while parsing XML from string: {} to a Document.", xml, e);
            throw new RuntimeException("An error occurred while parsing XML from string to a Document.", e);
        }
    }

    /**
     * It is so to speak the reverse of {@link #parseDocument(String)}.
     * @param document The document to be transformed into a string.
     * @param omitXmlDeclaration Whether the returned string will omit/include the XML declaration header (<?xml version="1.0" ...?>)
     *                           true->no XML declaration is added,
     *                           false->string including XML declaration header.
     * @return A string representation of the given Document.
     */
    public static String getStringFromDocument(Document document, boolean omitXmlDeclaration) {
        DOMSource domSource = new DOMSource(document);
        StringWriter stringWriter = new StringWriter();
        StreamResult streamResult = new StreamResult(stringWriter);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            String omitXmlDeclarationKey = booleanToYesNo(omitXmlDeclaration);
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclarationKey);
            transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            transformer.transform(domSource, streamResult);
            return stringWriter.toString();
        } catch (TransformerException e) {
            throw new RuntimeException("An error occurred while trying to transform a document to a string.", e);
        }

    }

    /**
     * This is the simpler version of {@link #getStringFromDocument(Document, boolean)}
     * where the boolean value for omitting XML declaration is set to 'true' (no XML declaration header).
     * @param document The document to be transformed into a string.
     * @return A string representation of the given Document.
     */
    public static String getStringFromDocument(Document document) {
        return getStringFromDocument(document, true);
    }

    /**
     * Takes a document and adds a root element to it with the specified tag name and attributes.
     * @param document The document to which the specified root element is added.
     * @param rootElementTagName The name of the new root element.
     * @param xmlns The default namespace the document in which the fragment is being created.
     * @param extraAttributes A map containing (key, value) pairs of extra attributes. This may be an empty map.
     * @return The newly created root element with the following structure:
     * <rootElementTagName xmlns="..." key1="value1" key2="value2">
     *     ...
     * </rootElementTagName>
     */
    public static Element addRootElementToDocument(Document document, String rootElementTagName,
                                                   String xmlns, Map<String, String> extraAttributes) {
        Element rootElement = document.createElement(rootElementTagName);
        Objects.requireNonNull(xmlns, "Method requires 'xmlns' to be non-null, but it is.");
        rootElement.setAttribute("xmlns", xmlns);
        for (Map.Entry<String, String> entry : extraAttributes.entrySet()) {
            rootElement.setAttribute(entry.getKey(), entry.getValue());
        }
        document.appendChild(rootElement);
        return rootElement;
    }

    /**
     * This is the simpler version of {@link #addRootElementToDocument(Document, String, String, Map)}
     * where the map of extra attributes is empty (meaning no extra attributes to set).
     * @param document The document to which the specified root element is added.
     * @param rootElementTagName The name of the new root element.
     * @param xmlns The default namespace the document in which the fragment is being created.
     * @return The newly created root element with the following structure:
     * <rootElementTagName xmlns="...">
     *     ...
     * </rootElementTagName>
     */
    public static Element addRootElementToDocument(Document document, String rootElementTagName, String xmlns) {
        return addRootElementToDocument(document, rootElementTagName, xmlns, Map.of());
    }

    /**
     * This is a method to add a child element with no content to an existing element.
     * Similar to method {@link #addElementWithTextContent(Element, String, String)} which additionally also
     * gives the new element some text content.
     * @param parentElement The parent element of the newly created element.
     * @param elementTagName The name of the newly created element: <elementTagName/>.
     * @return The element object of the newly created element which has been added (appended) to the given parentElement.
     */
    public static Element addElement(Element parentElement, String elementTagName) {
        Element newElement = parentElement.getOwnerDocument().createElement(elementTagName);
        parentElement.appendChild(newElement);
        return newElement;
    }

    @Deprecated(since = "5.2.0", forRemoval = true)
    public static Element addElement(Document document, Element parentElement, String elementTagName) {
        if (parentElement.getOwnerDocument() != document) {
            throw new IllegalArgumentException("When document is supplied, it must be the parentElement’s owner document");
        }
        return addElement(parentElement, elementTagName);
    }

    /**
     * This is a method to add a child element with som text content to an existing element.
     * Similar to method {@link #addElement(Element, String)} except it does not give the new element
     * any text content.
     * @param parentElement The parent element of the newly created element.
     * @param elementTagName The name of the newly created element: <elementTagName>...</elementTagName>.
     * @param textContent The content of the newly created element: <elementTagName>textContent</elementTagName>
     * @return The element object of the newly created element which has been added (appended) to the given parentElement.
     */
    public static Element addElementWithTextContent(Element parentElement, String elementTagName, String textContent) {
        Element newElement = addElement(parentElement, elementTagName);
        newElement.setTextContent(textContent);
        return newElement;
    }

    @Deprecated(since = "5.2.0", forRemoval = true)
    public static Element addElementWithTextContent(Document document, Element parentElement, String elementTagName, String textContent) {
        if (parentElement.getOwnerDocument() != document) {
            throw new IllegalArgumentException("When document is supplied, it must be the parentElement’s owner document");
        }
        return addElementWithTextContent(parentElement, elementTagName, textContent);
    }

    /**
     * Removes the element specified by 'elementTag' from the root element of the given XML.
     * @param xmlString the XML, represented as a string, for which the specified element is to be removed from.
     * @param elementTag the tag name of the element to be removed. Must be a child of the root element.
     * @return If the specified element is found, the new XML (represented as a string) with the specified element removed.
     * Otherwise, the initial xmlString given as argument is returned.
     */
    public static String removeElementFromXml(String xmlString, String elementTag) {
        Document document = parseDocument(xmlString);
        Element rootElement = document.getDocumentElement();
        NodeList childElementNodeList = rootElement.getElementsByTagName(elementTag);
        if (childElementNodeList.getLength() > 0) {
            Node childElementNode = childElementNodeList.item(0);
            rootElement.removeChild(childElementNode);
            return getStringFromDocument(document);
        } else {
            logger.warn("No child element was found for tag {}. Returning original xml string.", elementTag);
            return xmlString;
        }
    }

    /**
     * Takes a boolean value and returns a string of 'yes' or 'no' depending on the input.
     * @param bool The value to convert/translate.
     * @return A string representing the parameter input value: true->'yes' and false->'no'.
     */
    private static String booleanToYesNo(boolean bool) {
        return bool ? "yes" : "no";
    }

}
