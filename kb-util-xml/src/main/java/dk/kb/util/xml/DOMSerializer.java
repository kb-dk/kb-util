package dk.kb.util.xml;

import dk.kb.util.Resolver;
import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Serialises a W3C DOM {@link Node} to an XML {@link String} using a configurable
 * {@link Transformer}.
 *
 * <p>Instances are created through {@link #newInstance()} and configured fluently via the
 * {@code setX(..)} setters, e.g.
 * {@code DOMSerializer.newInstance().setIndent(true).setXmlDeclaration(true).domToString(node)}.</p>
 *
 * <p>When indentation is disabled (the default) a bundled {@code trim-whitespace.xslt} stylesheet is
 * applied to remove insignificant whitespace, and the resulting output is collapsed onto a single
 * line with any remaining newlines inside text nodes escaped as the {@code &#10;} entity.</p>
 */
public class DOMSerializer {

    private boolean withIndent = false;
    private boolean withXmlDeclaration = false;
    private boolean withStandAlone = false;

    /**
     * Creates a new serializer with default settings: no indentation, no XML declaration and
     * not standalone.
     *
     * @return a new {@link DOMSerializer} instance.
     */
    public static DOMSerializer newInstance(){
        return new DOMSerializer();
    }

    /**
     * Sets whether the output should be indented for human readability.
     *
     * @param withIndent {@code true} to indent the output, {@code false} to trim whitespace and
     *                   produce a single-line representation.
     * @return this instance for fluent configuration.
     */
    public DOMSerializer setIndent(final boolean withIndent) {
        this.withIndent = withIndent;
        return this;
    }

    /**
     * Sets whether an XML declaration ({@code <?xml ...?>}) should be prepended to the output.
     *
     * @param withXmlDeclaration {@code true} to include the declaration, {@code false} to omit it.
     * @return this instance for fluent configuration.
     */
    public DOMSerializer setXmlDeclaration(final boolean withXmlDeclaration) {
        this.withXmlDeclaration = withXmlDeclaration;
        return this;
    }

    /**
     * Sets the {@code standalone} attribute of the generated XML declaration. Only has an effect
     * when {@link #setXmlDeclaration(boolean)} is enabled.
     *
     * @param withStandAlone {@code true} for {@code standalone="yes"}, {@code false} for
     *                       {@code standalone="no"}.
     * @return this instance for fluent configuration.
     */
    public DOMSerializer setStandAlone(final boolean withStandAlone) {
        this.withStandAlone = withStandAlone;
        return this;
    }

    /**
     * Serialises the given DOM {@link Node} to an XML string according to the current
     * configuration.
     *
     * @param dom the DOM node to serialise.
     * @return the XML representation of the node.
     * @throws XMLException if the transformer cannot be configured (e.g. the whitespace-trimming
     *                      XSLT resource is missing) or the transformation fails.
     */
    public String domToString(Node dom) {
        Transformer t;

        try {
            if (withIndent) {
                t = TransformerFactory.newInstance().newTransformer();
            } else {
                String xsltResource = "trim-whitespace.xslt";
                try (InputStream xsltStream = Resolver.resolveStream(xsltResource)) {
                    t = TransformerFactory.newInstance().newTransformer(new StreamSource(xsltStream));
                } catch (IOException e) {
                    throw new TransformerConfigurationException(
                            "Unable to retrieve and compile XSLT resource '" + xsltResource + "'", e);
                }
            }
            t.setOutputProperty(OutputKeys.INDENT, yesNo(withIndent));

        } catch (TransformerConfigurationException e) {
            throw new XMLException(e);
        }

        t.setOutputProperty(OutputKeys.METHOD, "xml");
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, yesNo(!withXmlDeclaration));
        t.setOutputProperty(OutputKeys.STANDALONE, yesNo(withStandAlone));


        /* Transformer */
        StringWriter sw = new StringWriter();
        try {
            t.transform(new DOMSource(dom), new StreamResult(sw));
            // After transformation, the only newlines are in text elements where they can be replaced
            // with the newline entity
            return withIndent ? sw.toString() : sw.toString().replace("\n", "&#10;");
        } catch (TransformerException e) {
            throw new XMLException(e);
        }
    }

    private String yesNo(final boolean bool) {
        if (bool) {
            return "yes";
        }
        return "no";
    }
}
