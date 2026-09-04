package dk.kb.util.xml;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

public class XmlParser {

    private boolean namespaceAware = true;
    private boolean ignoringComments = false;
    private boolean ignoreWhitespace = false;
    private boolean validateDTD = false;
    private Schema schema = null;

    public boolean namespaceAware() {
        return namespaceAware;
    }

    public static XmlParser newInstance() {
        return new XmlParser();
    }

    public XmlParser withNamespaceAware(final boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
        return this;
    }

    public XmlParser withIgnoringComments(final boolean ignoringComments) {
        this.ignoringComments = ignoringComments;
        return this;
    }

    public XmlParser withIgnoreWhitespace(final boolean ignoreWhitespace) {
        this.ignoreWhitespace = ignoreWhitespace;
        return this;
    }

    public XmlParser withValidateDTD(final boolean validateDTD) {
        this.validateDTD = validateDTD;
        return this;
    }

    public XmlParser withSchema(final Schema schema) {
        this.schema = schema;
        return this;
    }

    /**
     * Parses an XML document from a String to a DOM.
     *
     * @param xmlString      a String containing an XML document.
     * @return The document in a DOM
     */
    public Document toDOM(String xmlString) throws XMLException {
        return toDOM(new InputSource(new StringReader(xmlString)));
    }

    /**
     * Parses a XML document from a stream to a DOM or return
     * {@code null} on error.
     *
     * @param xmlStream      a stream containing an XML document.

     * @return The document in a DOM
     */
    public Document toDOM(InputStream xmlStream) throws XMLException {
        return toDOM(new InputSource(xmlStream));
    }

    /**
     * Parses a XML document from a stream to a DOM or return
     * {@code null} on error.
     *
     * @param xmlStream      a stream containing an XML document.

     * @return The document in a DOM
     */
    public Document toDOM(InputSource xmlStream) throws XMLException {
        DocumentBuilder documentBuilder = createDocumentBuilder();
        try {
            return documentBuilder.parse(xmlStream);
        } catch (IOException e) {
            throw new XMLException("I/O error when parsing stream :" + e.getMessage(), e);
        } catch (SAXException e) {
            throw new XMLException("Parse error when parsing stream :" + e.getMessage(), e);
        }
    }

    private DocumentBuilder createDocumentBuilder() {
        DocumentBuilder documentBuilder;
        try {
            DocumentBuilderFactory dbFact = DocumentBuilderFactory.newInstance();
            dbFact.setNamespaceAware(namespaceAware);
            dbFact.setIgnoringComments(ignoringComments);
            dbFact.setValidating(validateDTD);
            if (ignoreWhitespace) {
                dbFact.setIgnoringElementContentWhitespace(ignoreWhitespace);
                dbFact.setValidating(true);
            }
            dbFact.setSchema(schema);
            documentBuilder = dbFact.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new XMLException("Parser configuration error when parsing XML stream: "
                                   + e.getMessage(), e);
        }
        return documentBuilder;
    }

    public Document newDocument() {
        DocumentBuilder documentBuilder = createDocumentBuilder();
        return documentBuilder.newDocument();

    }
}
