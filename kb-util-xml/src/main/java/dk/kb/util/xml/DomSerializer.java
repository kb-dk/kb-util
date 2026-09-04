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

public class DomSerializer {

    private boolean withIndent = false;
    private boolean withXmlDeclaration = false;
    private boolean withStandAlone = false;

    public static DomSerializer newInstance(){
        return new DomSerializer();
    }

    public DomSerializer withIndent(final boolean withIndent) {
        this.withIndent = withIndent;
        return this;
    }

    public DomSerializer withXmlDeclaration(final boolean withXmlDeclaration) {
        this.withXmlDeclaration = withXmlDeclaration;
        return this;
    }

    public DomSerializer withStandAlone(final boolean withStandAlone) {
        this.withStandAlone = withStandAlone;
        return this;
    }

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
