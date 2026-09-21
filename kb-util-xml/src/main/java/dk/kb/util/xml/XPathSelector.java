package dk.kb.util.xml;

import org.w3c.dom.Node;

import java.util.List;

public interface XPathSelector {

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
    public Integer selectInteger(Node node, String xpath, Integer defaultValue);


    /**
     * Extract an integer value from {@code node} or return {@code null} if it
     * is not found
     *
     * @param node  the node with the wanted attribute.
     * @param xpath the XPath to extract.
     * @return the value of the path or {@code null}
     */
    public Integer selectInteger(Node node, String xpath);

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
    public Double selectDouble(Node node, String xpath, Double defaultValue);


    /**
     * Extract a double precision floating point value from {@code node} or
     * return {@code null} if it is not found
     *
     * @param node  the node with the wanted attribute.
     * @param xpath the XPath to extract.
     * @return the value of the path or {@code null}
     */
    public Double selectDouble(Node node, String xpath);

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
    public Boolean selectBoolean(Node node, String xpath, Boolean defaultValue);

    /**
     * Extract a boolean value from {@code node} or return {@code false}
     * if there is no boolean value at {@code xpath}
     *
     * @param node  the node with the wanted attribute.
     * @param xpath the path to extract.
     * @return the value of the path, if existing, else
     *         {@code false}
     */
    public Boolean selectBoolean(Node node, String xpath);

    /**
     * <p>Extract the given value from the node as a String or if the value cannot
     * be extracted, {@code defaultValue} is returned.</p>
     *
     * <p>Example: To get the value of the attribute "foo" in the node, specify
     * "@foo" as the path.</p>
     *
     * <p>Note: This method does not handle namespaces explicitly.</p>
     *
     * @param node         the node with the wanted attribute
     * @param xpath        the XPath to extract.
     * @param defaultValue the default value
     * @return the value of the path, if existing, else
     *         {@code defaultValue}
     */
    public String selectString(Node node, String xpath, String defaultValue);

    /**
     * <p>Extract the given value from the node as a String or if the value cannot
     * be extracted, the empty string is returned</p>
     *
     * <p>Example: To get the value of the attribute "foo" in the node, specify
     * "@foo" as the path.</p>
     *
     * <p>Note: This method does not handle namespaces explicitly.</p>
     *
     * @param node  the node with the wanted attribute
     * @param xpath the XPath to extract
     * @return the value of the path, if existing, else
     *         the empty string
     */
    public String selectString(Node node, String xpath);

    /**
     * <p>Select the Node list with the given XPath.</p>
     *
     * <p>Note: This is a convenience method that logs exceptions instead of
     * throwing them.</p>
     *
     * @param node  the root document.
     * @param xpath the xpath for the Node list.
     * @return the List of Node requested or an empty List if unattainable
     */
    List<Node> selectNodeList(Node node, String xpath);
    
    List<String> selectStringList(Node dom, String xpath);

    /**
     * <p>Select the Node with the given XPath.</p>
     *
     * <p>Note: This is a convenience method that logs exceptions instead of
     * throwing them.</p>
     *
     * @param dom   the root document.
     * @param xpath the xpath for the node.
     * @return the Node or null if unattainable.
     */
    public Node selectNode(Node dom, String xpath);


}
