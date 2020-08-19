package dk.kb.util.xml;

import dk.kb.util.Resolver;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class XMLTest {
    
    @Test
    void domToString() throws IOException, ParserConfigurationException, SAXException, TransformerException {
        String xmlFileContents = Resolver.readFileFromClasspath("xml/test.xml");
        Document document = XML.fromXML(xmlFileContents, true);
        String xmloutput = XML.domToString(document);
        String[] splitExpected = xmlFileContents.split("\n");
        String[] splitActual = xmloutput.split("\n");
        assertThat(splitExpected.length, is(splitActual.length));
        for (int i = 0; i < splitExpected.length; i++) { //Fix to compare line for line, clearing indention
            assertThat(splitActual[i].trim(), is(splitExpected[i].trim()));
        }
    }
    
    @Test
    void marshall() throws JAXBException {
        MarshallTestObject object = new MarshallTestObject("foo", new Weird("bar"));
        String result = XML.marshall(object);
        assertThat(result,
                   is("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<marshallTestObject>\n    <key>foo</key>\n    <value>\n        <string>bar</string>\n    </value>\n</marshallTestObject>\n"));
    }
    
    @Test
    void unmarshall() throws JAXBException {
        MarshallTestObject object = new MarshallTestObject("foo", new Weird("bar"));
        String intermediate = XML.marshall(object);
        MarshallTestObject result = XML.unmarshall(intermediate, MarshallTestObject.class);
        assertThat(result, is(object));
    }
    
    @XmlRootElement
    public static class MarshallTestObject {
        private String key;
        private Weird value;
        
        
        public MarshallTestObject() {
        }
        
        public MarshallTestObject(String key, Weird value) {
            this.key = key;
            this.value = value;
        }
        
        public String getKey() {
            return key;
        }
        
        public Weird getValue() {
            return value;
        }
        
        public void setKey(String key) {
            this.key = key;
        }
        
        public void setValue(Weird value) {
            this.value = value;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MarshallTestObject that = (MarshallTestObject) o;
            return Objects.equals(key, that.key) &&
                   Objects.equals(value, that.value);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }
    }
    
    public static class Weird {
        private String string;
        
        public Weird() {
        }
        
        public Weird(String string) {
            this.string = string;
        }
        
        public String getString() {
            return string;
        }
        
        public void setString(String string) {
            this.string = string;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Weird weird = (Weird) o;
            return Objects.equals(string, weird.string);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(string);
        }
    }
    
    
    @Test
    void testFromXMLStream() throws IOException, ParserConfigurationException, SAXException, TransformerException {
        try (InputStream inputStream = Resolver.openFileFromClasspath("xml/test.xml")) {
            Document doc = XML.fromXML(inputStream, true);
            String xmloutput = XML.domToString(doc);
            String xmlFileContents = Resolver.readFileFromClasspath("xml/test.xml");
            String[] splitExpected = xmlFileContents.split("\n");
            String[] splitActual = xmloutput.split("\n");
            assertThat(splitExpected.length, is(splitActual.length));
            for (int i = 0; i < splitExpected.length; i++) { //Fix to compare line for line, clearing indention
                assertThat(splitActual[i].trim(), is(splitExpected[i].trim()));
            }
        }
    }
    
   
}