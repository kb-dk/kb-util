package dk.kb.util.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.function.Predicate;

/**
 * Utility methods for manipulating XML namespaces on DOM documents.
 * <p>
 * The class supports two common operations:
 * <ul>
 *     <li>{@link #ensureDefaultNamespace(Document, String)}: assigns a namespace only
 *         to elements that do not currently have one, leaving existing namespaced elements untouched.</li>
 *     <li>{@link #setNamespace(Document, String)}: forcibly overrides the namespace of the
 *         root element and every descendant, regardless of any existing namespace.</li>
 * </ul>
 * Both operations modify the given document in place and return it for convenience.
 */
public class Namespacing {

    /**
     * Ensures that all elements which currently have no namespace are assigned the given namespace.
     * Elements that already have a namespace are left untouched.
     *
     * @param documentToSetNamespaceIn  the document to modify. This is modified in place and also returned for convenience.
     * @param defaultNamespaceToSet the namespace URI to assign to namespaceless elements.
     * @return the same document, modified in place.
     */
    public static Document ensureDefaultNamespace(final Document documentToSetNamespaceIn, String defaultNamespaceToSet) {
        overruleNamespace(documentToSetNamespaceIn.getDocumentElement(),
                          defaultNamespaceToSet,
                          node -> node.getNodeType() == Node.ELEMENT_NODE && node.getNamespaceURI() == null);
        return documentToSetNamespaceIn;
    }

    /**
     * Overrides the namespace of the root element and all of its descendants recursively,
     * regardless of whether they already had a namespace or not.
     *
     * @param documentToSetNamespaceIn  the document to modify. This is modified in place and also returned for convenience.
     * @param namespaceToSet the namespace URI to assign to all elements.
     * @return the same document, modified in place.
     */
    public static Document setNamespace(final Document documentToSetNamespaceIn, String namespaceToSet) {
        overruleNamespace(documentToSetNamespaceIn.getDocumentElement(),
                          namespaceToSet,
                          node -> node.getNodeType() == Node.ELEMENT_NODE);
        return documentToSetNamespaceIn;
    }

    /**
     * Recursively overrules the namespace of the given node and its descendants for all nodes that passes the test
     *
     * @param node      the node to process.
     * @param namespace the namespace URI to assign.
     * @param test      predicate deciding which nodes should have their namespace changed.
     */
    private static void overruleNamespace(final Node node, String namespace, Predicate<Node> test) {
        if (test.test(node)) {
            node.getOwnerDocument().renameNode(
                    node,
                    namespace,
                    node.getLocalName());

            var children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                overruleNamespace(children.item(i), namespace, test);
            }
        }
    }
}
