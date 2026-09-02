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

/**
 * Configurable parser that turns XML input (a {@link String}, {@link InputStream} or
 * {@link InputSource}) into a W3C DOM {@link Document}.
 *
 * <p>Instances are created through {@link #newInstance()} and configured fluently via the
 * {@code setX(..)} setters before calling one of the {@code toDOM(..)} methods, e.g.
 * {@code XMLParser.newInstance().setNamespaceAware(true).toDOM(xmlString)}.</p>
 *
 * <p>By default parsing is namespace-aware and otherwise uses the defaults of the underlying
 * {@link DocumentBuilderFactory}. Parse and I/O errors are reported as an unchecked
 * {@link XMLException}.</p>
 */
public class XMLParser {

    private boolean namespaceAware = true;
    private boolean ignoringComments = false;
    private boolean ignoreWhitespace = false;
    private boolean validateDTD = false;
    private Schema schema = null;

    /**
     * @return {@code true} if this parser resolves XML namespaces (the default), {@code false}
     *         otherwise.
     */
    public boolean namespaceAware() {
        return namespaceAware;
    }

    /**
     * Creates a new parser with default settings: namespace-aware, comments and whitespace kept,
     * no DTD validation and no schema.
     *
     * @return a new {@link XMLParser} instance.
     */
    public static XMLParser newInstance() {
        return new XMLParser();
    }

    /**
     * Sets whether the parser should process XML namespaces.
     *
     * @param namespaceAware {@code true} to make the resulting DOM reflect declared namespaces.
     * @return this instance for fluent configuration.
     */
    public XMLParser setNamespaceAware(final boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
        return this;
    }

    /**
     * Sets whether comment nodes should be dropped from the resulting DOM.
     *
     * @param ignoringComments {@code true} to discard comments.
     * @return this instance for fluent configuration.
     */
    public XMLParser setIgnoringComments(final boolean ignoringComments) {
        this.ignoringComments = ignoringComments;
        return this;
    }

    /**
     * Sets whether element-content whitespace should be ignored. Enabling this also forces
     * validation, as required by the underlying {@link DocumentBuilderFactory}.
     *
     * @param ignoreWhitespace {@code true} to discard element-content whitespace.
     * @return this instance for fluent configuration.
     */
    public XMLParser setIgnoreWhitespace(final boolean ignoreWhitespace) {
        this.ignoreWhitespace = ignoreWhitespace;
        return this;
    }

    /**
     * Sets whether the document should be validated against its DTD ( Document Type Declaration).
     *
     * @param validateDTD {@code true} to enable DTD validation.
     * @return this instance for fluent configuration.
     */
    public XMLParser setValidateDTD(final boolean validateDTD) {
        this.validateDTD = validateDTD;
        return this;
    }

    /**
     * Sets a schema to validate parsed documents against. Note that a schema violation does not
     * necessarily abort parsing unless an {@code ErrorHandler} is configured on the underlying
     * parser.
     *
     * @param schema the schema to validate against, or {@code null} for no schema validation.
     * @return this instance for fluent configuration.
     */
    public XMLParser setSchema(final Schema schema) {
        this.schema = schema;
        return this;
    }

    /**
     * Parses an XML document from a String to a DOM.
     *
     * @param xmlString a String containing an XML document.
     * @return The document in a DOM.
     * @throws XMLException if the String cannot be parsed as XML.
     */
    public Document toDOM(String xmlString) throws XMLException {
        return toDOM(new InputSource(new StringReader(xmlString)));
    }

    /**
     * Parses an XML document from a stream to a DOM. The stream is not closed by this method.
     *
     * @param xmlStream a stream containing an XML document.
     * @return The document in a DOM.
     * @throws XMLException if the stream cannot be read or parsed as XML.
     */
    public Document toDOM(InputStream xmlStream) throws XMLException {
        return toDOM(new InputSource(xmlStream));
    }

    /**
     * Parses an XML document from an {@link InputSource} to a DOM.
     *
     * @param xmlStream the input source containing an XML document.
     * @return The document in a DOM.
     * @throws XMLException if the source cannot be read or parsed as XML.
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
            throw new XMLException(
                    "Parser configuration error when parsing XML stream: " + e.getMessage(),
                    e);
        }
        return documentBuilder;
    }

    /**
     * Creates a new, empty DOM {@link Document} using the current parser configuration.
     *
     * @return an empty document, ready to be populated.
     * @throws XMLException if the underlying parser cannot be configured.
     */
    public Document newDocument() {
        DocumentBuilder documentBuilder = createDocumentBuilder();
        return documentBuilder.newDocument();

    }
}
