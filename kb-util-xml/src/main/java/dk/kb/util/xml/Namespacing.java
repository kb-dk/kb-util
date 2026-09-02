package dk.kb.util.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.function.Predicate;

public class Namespacing {

    /**
     * Ensures that all elements which currently have no namespace are assigned the given namespace.
     * Elements that already have a namespace are left untouched.
     *
     * @param document  the document to modify. This is modified in place and also returned for convenience.
     * @param namespace the namespace URI to assign to namespaceless elements.
     * @return the same document, modified in place.
     */
    public static Document ensureDefaultNamespace(final Document document, String namespace) {
        overruleNamespace(document.getDocumentElement(), namespace, node -> node.getNodeType() == Node.ELEMENT_NODE && node.getNamespaceURI() == null);
        return document;
    }

    /**
     * Overrules the namespace of the root element and all of its descendants recursively,
     * regardless of whether they already had a namespace or not.
     *
     * @param document  the document to modify. This is modified in place and also returned for convenience.
     * @param namespace the namespace URI to assign to all elements.
     * @return the same document, modified in place.
     */
    public static Document overruleRootNamespaceRecursively(final Document document, String namespace) {
        overruleNamespace(document.getDocumentElement(), namespace, node -> node.getNodeType() == Node.ELEMENT_NODE);
        return document;
    }

    /**
     * Recursively overrules the namespace of the given node and its descendants where the given test passes.
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
