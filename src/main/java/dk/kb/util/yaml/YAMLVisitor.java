package dk.kb.util.yaml;

import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.MappingEndEvent;
import org.yaml.snakeyaml.events.MappingStartEvent;
import org.yaml.snakeyaml.events.ScalarEvent;
import org.yaml.snakeyaml.events.SequenceEndEvent;
import org.yaml.snakeyaml.events.SequenceStartEvent;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class YAMLVisitor {

        public static void visitYAML(String yamlPath) throws IOException {
            YAML yaml = YAML.resolveLayeredConfigs(yamlPath);
            Iterable<Event> events = yaml.parse(new YamlDocumentReader(yaml.toString()));

            YamlNodeVisitor visitor = new YamlNodeVisitor();
            for (Event event : events) {
                event.accept(visitor);
            }
        }
    }

    class YamlNodeVisitor implements EventVisitor {

        @Override
        public void visit(ScalarEvent event) {
            System.out.println("Scalar value: " + event.getValue());
            // Add your logic for scalar nodes
        }

        @Override
        public void visit(MappingStartEvent event) {
            System.out.println("Mapping start");
            // Add your logic for mapping start
        }

        @Override
        public void visit(MappingEndEvent event) {
            System.out.println("Mapping end");
            // Add your logic for mapping end
        }

        @Override
        public void visit(SequenceStartEvent event) {
            System.out.println("Sequence start");
            // Add your logic for sequence start
        }

        @Override
        public void visit(SequenceEndEvent event) {
            System.out.println("Sequence end");
            // Add your logic for sequence end
        }
    }

    class YamlDocumentReader extends Reader {
        private String content;

        public YamlDocumentReader(String content) {
            this.content = content;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            // Implementation to read from content
            // This is a simplified example; you may need to implement a proper reader
            // based on your requirements.
            // Make sure to handle end of content appropriately.

            int length = Math.min(len, content.length());
            content.getChars(0, length, cbuf, off);
            content = content.substring(length);

            return length;
        }

        @Override
        public void close() throws IOException {
            // Close resources if needed
        }
    }

}
