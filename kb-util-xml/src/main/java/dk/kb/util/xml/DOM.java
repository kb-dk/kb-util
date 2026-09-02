/* $Id$
 *
 * The Summa project.
 * Copyright (C) 2005-2008  The State and University Library
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

/**
 * Helpers for doing DOM parsing and manipulations. The methods are thread-safe
 * and allows for parallel execution of the same xpath.
 */
public class DOM {
    private static Logger log = LoggerFactory.getLogger(DOM.class);

    public static final String XML_HEADER =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    private static final SynchronousXPathSelector selector =
            new SynchronousXPathSelector(null, 50);

    /**
     * Extracts all textual and CDATA content from the given node and its
     * children.
     *
     * @param node the node to get the content from.
     * @return the textual content of node.
     */
    public static String getElementNodeValue(Node node) {
        StringWriter sw = new StringWriter(2000);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            NodeList all = node.getChildNodes();
            for (int i = 0; i < all.getLength(); i++) {
                if (all.item(i).getNodeType() == Node.TEXT_NODE ||
                    all.item(i).getNodeType() == Node.CDATA_SECTION_NODE) {
                    // TODO: Check if we exceed the limit for getNodeValue
                    sw.append(all.item(i).getNodeValue());
                }
            }
        }
        return sw.toString();
    }

    public static DomSerializer serializer() {
        return new DomSerializer();
    }

    /* **************************************** */

    /**
     * Parses an XML document from a String to a DOM.
     *
     * @param xmlString      a String containing an XML document.
     * @param namespaceAware if {@code true} the parsed DOM will reflect any
     *                       XML namespaces declared in the document
     * @return The document in a DOM or {@code null} on errors.
     */
    public static Document stringToDOM(String xmlString,
                                       boolean namespaceAware) {
        return XML.parser().withNamespaceAware(namespaceAware).toDOM(xmlString);
    }

    /**
     * Parses an XML document from a String disregarding namespaces
     *
     * @param xmlString a String containing an XML document.
     * @return The document in a DOM or {@code null} on errors.
     */
    public static Document stringToDOM(String xmlString) throws XMLException {
        return XML.parser().withNamespaceAware(false).toDOM(xmlString);
    }

    /**
     * Parses a XML document from a stream to a DOM or return
     * {@code null} on error.
     *
     * @param xmlStream      a stream containing an XML document.
     * @param namespaceAware if {@code true} the constructed DOM will reflect
     *                       the namespaces declared in the XML document
     * @return The document in a DOM or {@code null} in case of errors
     */
    public static Document streamToDOM(InputStream xmlStream,
                                       boolean namespaceAware) {
        return XML.parser().withNamespaceAware(namespaceAware).toDOM(xmlStream);
    }

    /**
     * Parses a XML document from a stream to a DOM disregarding namespaces.
     * Returns {@code null} on error.
     *
     * @param xmlStream a stream containing an XML document.
     * @return The document in a DOM or {@code null} in case of errors
     */
    public static Document streamToDOM(InputStream xmlStream) throws XMLException {
        return XML.parser().withNamespaceAware(false).toDOM(xmlStream);
    }

    /**
     * Convert the given DOM to an UTF-8 XML String.
     *
     * @param dom the Document to convert.
     * @return the dom as an XML String.
     */
    public static String domToString(Node dom) {
        return DOM.serializer()
                  .withXmlDeclaration(false)
                  .domToString(dom);
    }

    /**
     * Convert the given DOM to an UTF-8 XML String.
     *
     * @param dom                the Document to convert.
     * @param withXmlDeclaration if trye, an XML-declaration is prepended.
     * @return the dom as an XML String.
     * @throws XMLException if the dom could not be converted.
     */
    public static String domToString(Node dom, boolean withXmlDeclaration) throws XMLException {
        return DOM.serializer()
                  .withXmlDeclaration(withXmlDeclaration)
                  .withStandAlone(false)
                  .domToString(dom);
    }

    /**
     * Convert the given DOM to an UTF-8 XML String.
     *
     * @param dom                the Document to convert.
     * @param withXmlDeclaration if trye, an XML-declaration is prepended.
     * @param standAlone         Whether it is standalone in the XML sense of the word
     *          (see for example <a href="https://stackoverflow.com/questions/5578645/what-does-the-standalone-directive-mean-in-xml">"https://stackoverflow.com/questions/5578645/what-does-the-standalone-directive-mean-in-xml"</a>)
     * @return the dom as an XML String.
     * @throws XMLException if the dom could not be converted.
     */
    public static String domToString(Node dom, boolean withXmlDeclaration, boolean standAlone) throws XMLException {
        return DOM.serializer()
                  .withXmlDeclaration(withXmlDeclaration)
                  .withStandAlone(standAlone)
                  .domToString(dom);
    }

    /**
     * Create a new {@link XPathSelector} instance with a given namespace
     * mapping. The arguments are parsed as
     * {@code prefix1, uri1, prefix2, uri2, ...}.
     *
     * If you want to apply XPath expressions without namespaces use the static
     * {@code select*} methods directly on the {@code DOM} class.
     *
     * Note that if you want to apply XPath selections on a DOM constructed from
     * either {@link DOM#streamToDOM(InputStream, boolean)} or
     * {@link DOM#stringToDOM(String, boolean)} you must pass
     * {@code namespaceAware=true} as the boolean argument. Namespaced
     * selections will fail on a DOM constructed without namespaces.
     *
     * @param nsContext prefix, uri pairs
     * @return a newly allocated {@link XPathSelector}
     * @throws IllegalArgumentException if an uneven number of arguments are
     *                                  passed
     */
    public static XPathSelector createXPathSelector(String... nsContext) {
        return new XPathSelectorImpl(
                new DefaultNamespaceContext(null, nsContext), 50);
    }

    /**
     * Extract an integer value from {@code node} or return {@code defaultValue}
     * if it is not found.
     *
     * @param node         the node with the wanted attribute.
     * @param xpath        the XPath to extract.
     * @param defaultValue the default value.
     * @return the value of the path, if existing, else
     *         defaultValue
     */
    public static Integer selectInteger(Node node, String xpath, Integer defaultValue) {
        return selector.selectInteger(node, xpath, defaultValue);
    }

    /**
     * Extract an integer value from {@code node} or return {@code null} if it
     * is not found
     *
     * @param node  the node with the wanted attribute.
     * @param xpath the XPath to extract.
     * @return the value of the path or {@code null}
     */
    public static Integer selectInteger(Node node, String xpath) {
        return selector.selectInteger(node, xpath);
    }

    /**
     * Extract a double precision floating point value from {@code node} or
     * return {@code defaultValue} if it is not found
     *
     * @param node         the node with the wanted attribute.
     * @param xpath        the XPath to extract.
     * @param defaultValue the default value.
     * @return the value of the path, if existing, else
     *         defaultValue
     */
    public static Double selectDouble(Node node,
                                      String xpath, Double defaultValue) {
        return selector.selectDouble(node, xpath, defaultValue);
    }

    /**
     * Extract a double precision floating point value from {@code node} or
     * return {@code null} if it is not found
     *
     * @param node  the node with the wanted attribute.
     * @param xpath the XPath to extract.
     * @return the value of the path or {@code null}
     */
    public static Double selectDouble(Node node, String xpath) {
        return selector.selectDouble(node, xpath);
    }

    /**
     * Extract a boolean value from {@code node} or return {@code defaultValue}
     * if there is no boolean value at {@code xpath}
     *
     * @param node         the node with the wanted attribute.
     * @param xpath        the path to extract.
     * @param defaultValue the default value.
     * @return the value of the path, if existing, else
     *         {@code defaultValue}
     */
    public static Boolean selectBoolean(Node node,
                                        String xpath, Boolean defaultValue) {
        return selector.selectBoolean(node, xpath, defaultValue);
    }

    /**
     * Extract a boolean value from {@code node} or return {@code false}
     * if there is no boolean value at {@code xpath}
     *
     * @param node  the node with the wanted attribute.
     * @param xpath the path to extract.
     * @return the value of the path, if existing, else
     *         {@code false}
     */
    public static Boolean selectBoolean(Node node, String xpath) {
        return selector.selectBoolean(node, xpath);
    }

    /**
     * Extract the given value from the node as a String or if the value cannot
     * be extracted, {@code defaultValue} is returned.
     *
     * Example: To get the value of the attribute "foo" in the node, specify
     * "@foo" as the path.
     *
     * Note: This method does not handle namespaces explicitely.
     *
     * @param node         the node with the wanted attribute
     * @param xpath        the XPath to extract.
     * @param defaultValue the default value
     * @return the value of the path, if existing, else
     *         {@code defaultValue}
     */
    public static String selectString(Node node, String xpath, String defaultValue) {
        return selector.selectString(node, xpath, defaultValue);
    }

    /**
     * Extract the given value from the node as a String or if the value cannot
     * be extracted, the empty string is returned
     *
     * Example: To get the value of the attribute "foo" in the node, specify
     * "@foo" as the path.
     *
     * Note: This method does not handle namespaces explicitely.
     *
     * @param node  the node with the wanted attribute
     * @param xpath the XPath to extract
     * @return the value of the path, if existing, else
     *         the empty string
     */
    public static String selectString(Node node, String xpath) {
        return selector.selectString(node, xpath);
    }

    /**
     * Select the List of Nodes, not NodeList with the given XPath.
     *
     * Note: This is a convenience method that logs exceptions instead of
     * throwing them.
     *
     * @param node  the root document.
     * @param xpath the xpath for the Node list.
     * @return the List of Nodes requested or an empty List if unattainable
     */
    public static List<Node> selectNodeList(Node node, String xpath) {
        return selector.selectNodeList(node, xpath);
    }

    /**
     * Select the Node with the given XPath.
     *
     * Note: This is a convenience method that logs exceptions instead of
     * throwing them.
     *
     * @param dom   the root document.
     * @param xpath the xpath for the node.
     * @return the Node or null if unattainable.
     */
    public static Node selectNode(Node dom, String xpath) {
        return selector.selectNode(dom, xpath);
    }

    /**
     * Package private method to clear the expression cache
     * - used for unit testing
     */
    static void clearXPathCache() {
        selector.clearCache();
    }
}
