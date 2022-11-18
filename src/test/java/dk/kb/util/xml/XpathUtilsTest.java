package dk.kb.util.xml;

import dk.kb.util.Resolver;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class XpathUtilsTest {
    
    @Test
    void createXPathSelectorMap() throws IOException, ParserConfigurationException, SAXException {
        String xmlString = Resolver.readFileFromClasspath("xml/test.xml");
        Document xmlDoc = XML.fromXML(xmlString, true);
    
        String result;
        XPathSelector xpath = XpathUtils.createXPathSelector(Map.of("marc", "http://www.loc.gov/MARC21/slim"));
        result = xpath.selectString(xmlDoc,
                                    "/marc:record/marc:datafield[@tag='084'][1]/marc:subfield[@code='a']");
        assertThat(result,is("38.79"));
    
        //Xpath lists start from 1...
        result = xpath.selectString(xmlDoc,
                                           "/marc:record/marc:datafield[@tag='084'][2]/marc:subfield[@code='a']");
        assertThat(result,is("34.51"));
    }
    
    @Test
    void createXPathSelectorVarargs() throws IOException, ParserConfigurationException, SAXException {
        String xmlString = Resolver.readFileFromClasspath("xml/test.xml");
        Document xmlDoc = XML.fromXML(xmlString, true);
        
        XPathSelector xpath = XpathUtils.createXPathSelector("marc", "http://www.loc.gov/MARC21/slim");
        String result = xpath.selectString(xmlDoc,
                                           "/marc:record/marc:datafield[@tag='084'][1]/marc:subfield[@code='a']");
        assertThat(result,is("38.79"));
    }
}