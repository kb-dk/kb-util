package dk.kb.util.xml;

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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

public class DOMUtil {

    static final Logger logger = LoggerFactory.getLogger(DOMUtil.class);


    /**
     * Creates an empty Document object with the specified XML version and whether it is standalone.
     * @param xmlVersion The used XML version.
     * @param standalone Whether it is standalone in the XML sense of the word
     *          (see for example <a href="https://stackoverflow.com/questions/5578645/what-does-the-standalone-directive-mean-in-xml">"https://stackoverflow.com/questions/5578645/what-does-the-standalone-directive-mean-in-xml"</a>)
     * @return A new empty Document object.
     */
    public static Document createDocument(String xmlVersion, boolean standalone) {
        Document document = XML.parser().newDocument();
        document.setXmlVersion(xmlVersion);
        document.setXmlStandalone(standalone);
        return document;
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

    /**
     * This is a method to add a child element with no content to an existing element.
     * Similar to method {@link #addElementWithTextContent(Element, String, String)} which additionally also
     * gives the new element some text content.
     * @param parentElement The parent element of the newly created element.
     * @param xmlns the namespace of the element
     * @param elementTagName The name of the newly created element: <elementTagName/>.
     * @return The element object of the newly created element which has been added (appended) to the given parentElement.
     */
    public static Element addElement(Element parentElement, String elementTagName, String xmlns) {
        Element newElement = parentElement.getOwnerDocument().createElementNS(xmlns, elementTagName);
        parentElement.appendChild(newElement);
        return newElement;
    }

    /**
     * Removes the element specified by 'elementTag' from the root element of the given XML.
     * @param xmlString the XML, represented as a string, for which the specified element is to be removed from.
     * @param elementTag the tag name of the element to be removed. Must be a child of the root element.
     * @return If the specified element is found, the new XML (represented as a string) with the specified element removed.
     * Otherwise, the initial xmlString given as argument is returned.
     */
    public static String removeElementFromXml(String xmlString, String elementTag) {
        Document document =  XmlParser.newInstance().toDOM(xmlString);
        Element rootElement = document.getDocumentElement();
        removeElementFromXml(rootElement, elementTag);
        return DomSerializer.newInstance().domToString(document);
    }

    /**
     * Takes an XML fragment represented as a String object and parses it to a Document object.
     * @param xml The XML fragment represented as a string.
     * @return A document object, parsed from the given XML string.
     */
    public static Document parseDocument(String xml) {
        return XML.parser().withNamespaceAware(true).toDOM(xml);
    }

    /**
     * Removes the element specified by 'elementTag' from the root element of the given XML.
     * @param xmlString the XML, represented as a string, for which the specified element is to be removed from.
     * @param elementTag the tag name of the element to be removed. Must be a child of the root element.
     * @return If the specified element is found, the new XML (represented as a string) with the specified element removed.
     * Otherwise, the initial xmlString given as argument is returned.
     */
    public static String removeElementFromXml(String xmlString, String elementTag, String xmlns) {
        Document document = XmlParser.newInstance().toDOM(xmlString);
        if (document == null) {
            return xmlString;
        }
        Element rootElement = document.getDocumentElement();
        removeElementFromXml(rootElement, elementTag, xmlns);
        return DOM.serializer().domToString(document);
    }


    /**
     * Removes the element specified by 'elementTag' from the root element of the given XML.
     * @param node the XML, represented as a string, for which the specified element is to be removed from.
     * @param elementTag the tag name of the element to be removed. Must be a child of the root element.
     * @return If the specified element is found, the new XML (represented as a string) with the specified element removed.
     * Otherwise, the initial xmlString given as argument is returned.
     */
    public static Element removeElementFromXml(Element node, String elementTag) {
        NodeList childElementNodeList = node.getElementsByTagName(elementTag);
        if (childElementNodeList.getLength() > 0) {
            Node childElementNode = childElementNodeList.item(0);
            node.removeChild(childElementNode);
        }
        return node;
    }

    /**
     * Removes the element specified by 'elementTag' from the root element of the given XML.
     * @param node the XML, represented as a string, for which the specified element is to be removed from.
     * @param elementTag the tag name of the element to be removed. Must be a child of the root element.
     * @return If the specified element is found, the new XML (represented as a string) with the specified element removed.
     * Otherwise, the initial xmlString given as argument is returned.
     */
    public static Element removeElementFromXml(Element node, String elementTag, String xmlns) {
        NodeList childElementNodeList = node.getElementsByTagNameNS(xmlns, elementTag);
        if (childElementNodeList.getLength() > 0) {
            Node childElementNode = childElementNodeList.item(0);
            node.removeChild(childElementNode);
        }
        return node;
    }
}
