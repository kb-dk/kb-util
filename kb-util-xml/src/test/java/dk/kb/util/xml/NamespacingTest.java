package dk.kb.util.xml;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.in;

class NamespacingTest {


    @Test
    public void testDefaultNamespaceWhenNotSet() {
        var doc = XML.parser().toDOM(noNamespace);
        var namespacedDoc = Namespacing.ensureDefaultNamespace(doc, "http://jplylyser.test.namespace");

        assertAllInNewNamespace(namespacedDoc.getDocumentElement(), "http://jplylyser.test.namespace");
        String xml = DOM.serializer().domToString(namespacedDoc);
        assertThat(xml, containsString("xmlns="));
        assertThat(xml.split("xmlns").length, is(2));
    }

    @Test
    public void testDefaultNamespaceWhenSetOnSubset() {
        var doc = XML.parser().toDOM(multiNamespace);
        var namespacedDoc = Namespacing.ensureDefaultNamespace(doc, "http://jplylyser.test.namespace");

        assertAllInNewNamespace(namespacedDoc.getDocumentElement(), "http://jplylyser.test.namespace", "someOtherNamespace");
        String xml = DOM.serializer().domToString(namespacedDoc);
        assertThat(xml, containsString("xmlns="));
        assertThat(xml.split("xmlns").length, is(3));
    }

    @Test
    public void testForceNamespaceWhenSetOnSubset() {
        var doc = XML.parser().toDOM(multiNamespace);
        var namespacedDoc = Namespacing.overruleRootNamespaceRecursively(doc, "http://jplylyser.test.namespace");

        assertAllInNewNamespace(namespacedDoc.getDocumentElement(), "http://jplylyser.test.namespace");
        String xml = DOM.serializer().domToString(namespacedDoc);
        assertThat(xml, containsString("xmlns="));
        assertThat(xml.split("xmlns").length, is(2));
    }

    @Test
    public void testForceNamespaceWhenAlreadySet() {
        var doc = XML.parser().toDOM(otherNamespace);
        var namespacedDoc = Namespacing.overruleRootNamespaceRecursively(doc, "http://jplylyser.test.namespace");
        assertAllInNewNamespace(namespacedDoc.getDocumentElement(), "http://jplylyser.test.namespace");

        String xml = DOM.serializer().domToString(namespacedDoc);
        assertThat(xml, containsString("xmlns="));
        assertThat(xml.split("xmlns").length, is(2));
    }


    private void assertAllInNewNamespace(Node node, String... namespace) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            assertThat(node.getNamespaceURI(), is(in(namespace)));
            var children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                assertAllInNewNamespace(children.item(i), namespace);
            }
        }
    }

    //language=XML
    String noNamespace = """
                         <jpylyzer>
                             <toolInfo>
                                 <toolName>jpylyzer.py</toolName>
                                 <toolVersion>1.10.1</toolVersion>
                             </toolInfo>
                             <fileInfo>
                                 <fileName>B400027101338-RT1_400027101338-09_1795-05-26-01_adresseavisen1759-1795-05-26-01-0485B.jp2</fileName>
                                 <filePath>
                                     /avisbits/avis/B400027101338-RT1_400027101338-09_1795-05-26-01_adresseavisen1759-1795-05-26-01-0485B.jp2
                                 </filePath>
                                 <fileSizeInBytes>2431798</fileSizeInBytes>
                                 <fileLastModified>Wed Dec 17 10:35:43 2014</fileLastModified>
                             </fileInfo>
                         </jpylyzer>
                         """;

    //language=XML
    String multiNamespace = """
                         <jpylyzer>
                             <toolInfo>
                                 <toolName>jpylyzer.py</toolName>
                                 <toolVersion>1.10.1</toolVersion>
                             </toolInfo>
                             <fileInfo xmlns='someOtherNamespace'>
                                 <fileName>B400027101338-RT1_400027101338-09_1795-05-26-01_adresseavisen1759-1795-05-26-01-0485B.jp2</fileName>
                                 <filePath>
                                     /avisbits/avis/B400027101338-RT1_400027101338-09_1795-05-26-01_adresseavisen1759-1795-05-26-01-0485B.jp2
                                 </filePath>
                                 <fileSizeInBytes>2431798</fileSizeInBytes>
                                 <fileLastModified>Wed Dec 17 10:35:43 2014</fileLastModified>
                             </fileInfo>
                         </jpylyzer>
                         """;

    //language=XML
    String otherNamespace = """
                            <histogram xmlns="http://www.statsbiblioteket.dk/avisdigitalisering/histogram/1/0/">
                                <colorScheme>
                                    <colorSpace>Greyscale</colorSpace>
                                    <colorDepth>8 bits</colorDepth>
                                </colorScheme>
                                <colors>
                                    <color>
                                        <code>0</code>
                                        <count>0</count>
                                    </color>
                                    <color>
                                        <code>1</code>
                                        <count>0</count>
                                    </color>
                                    <color>
                                        <code>2</code>
                                        <count>0</count>
                                    </color>
                                    <color>
                                        <code>3</code>
                                        <count>0</count>
                                    </color>
                                    <color>
                                        <code>4</code>
                                        <count>0</count>
                                    </color>
                                    <color>
                                        <code>5</code>
                                        <count>0</count>
                                    </color>
                                    <color>
                                        <code>6</code>
                                        <count>0</count>
                                    </color>
                                    <color>
                                        <code>7</code>
                                        <count>0</count>
                                    </color>
                                    <color>
                                        <code>8</code>
                                        <count>5</count>
                                    </color>
                                    <color>
                                        <code>9</code>
                                        <count>29</count>
                                    </color>
                                    <color>
                                        <code>10</code>
                                        <count>177</count>
                                    </color>
                                    <color>
                                        <code>11</code>
                                        <count>770</count>
                                    </color>
                                    <color>
                                        <code>12</code>
                                        <count>1958</count>
                                    </color>
                                    <color>
                                        <code>13</code>
                                        <count>4031</count>
                                    </color>
                                    <color>
                                        <code>14</code>
                                        <count>5882</count>
                                    </color>
                                    <color>
                                        <code>15</code>
                                        <count>7585</count>
                                    </color>
                                    <color>
                                        <code>16</code>
                                        <count>8836</count>
                                    </color>
                                </colors>
                            </histogram>
                            """;

}