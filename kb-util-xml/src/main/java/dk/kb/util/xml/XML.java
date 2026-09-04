package dk.kb.util.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class XML {


    public static XmlParser parser(){
        return new XmlParser();
    }

    /**
     * Serialises the given Document as a (human-readable) String with indents and linebreaks
     * @param dom the dom
     * @return the doc in string form
     * @see #domToString(Node, boolean) for a more compact machine-readable version
     * @see DOM#domToString(Node)
     */
    public static String domToString(Node dom) {
        return DOM.serializer().withIndent(true).domToString(dom);
    }

    /**
     * Serialiseses the given Document as a String, with optional indent.
     * @param dom the dom
     * @param indent if true, the output will be indented. If false, output will be a single line.
     * @return the doc in string form
     * @throws XMLException if the transformation failed
     * @see #domToString(Node) for an indented version of the same string
     * @see DOM#domToString(Node, boolean, boolean)
     */
    public static String domToString(Node dom, boolean indent)  {
        return DOM.serializer().withIndent(indent).domToString(dom);
    }



    /**
     * Marshall the given object as xml
     * @param object the object to convert to xml
     * @param <T> the type of object
     * @return the object serialised as xml (UTF-8)
     * @throws XMLException if something failed
     */
    public static <T> String marshall(T object) {
        //TODO does this work?
        try {
            JAXBContext jc = JAXBContext.newInstance(object.getClass());
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                marshaller.marshal(object, out);
                out.flush();
                return out.toString(StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } catch (JAXBException e) {
            throw new XMLException(e);
        }
    }
    
    /**
     * Unmarshal the given xml back to a java object
     * @param xml the xml string
     * @param type the class of object to create
     * @param <T> the type of object
     * @return an instance of Type
     * @throws XMLException  if the xml parsing failed
     * @throws UncheckedIOException If reading the xml string failed (should not happen)
     */
    @SuppressWarnings("unchecked")
    public static <T> T unmarshall(String xml, Class<T> type) {
        try {
            JAXBContext jc = JAXBContext.newInstance(type);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            try (ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
                if (type.isAnnotationPresent(XmlRootElement.class)) {
                    return (T) unmarshaller.unmarshal(in);
                } else {
                    return unmarshaller.unmarshal(new StreamSource(in), type).getValue();
                }
            }
        } catch (JAXBException e) {
            throw new XMLException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    /**
     * Parses an XML document from a String to a DOM.
     *
     * @param xmlString      a String containing an XML document.
     * @param namespaceAware if {@code true} the parsed DOM will reflect any
     *                       XML namespaces declared in the document
     * @return The document in a DOM
     * @throws XMLException  if the xml parsing failed
     * @throws UncheckedIOException If reading the xml string failed (should not happen)
     */
    public static Document fromXML(String xmlString,
                                   boolean namespaceAware) throws UncheckedIOException, XMLException {
        return XML.parser().withNamespaceAware(namespaceAware).toDOM(xmlString);
    }
    
    /**
     * Parses a XML document from a stream to a DOM. The Stream will NOT be closed.
     *
     * @param xmlStream      a stream containing an XML document.
     * @param namespaceAware if {@code true} the constructed DOM will reflect
     *                       the namespaces declared in the XML document
     * @return The document in a DOM
     * @throws XMLException  if the xml parsing failed
     * @throws UncheckedIOException If reading the xml string failed (should not happen)
     */
    public static Document fromXML(InputStream xmlStream,
                                   boolean namespaceAware) throws UncheckedIOException, XMLException {
        return XML.parser().withNamespaceAware(namespaceAware).toDOM(xmlStream);
    }
}
