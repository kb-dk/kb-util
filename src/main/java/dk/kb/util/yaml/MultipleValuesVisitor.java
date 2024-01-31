package dk.kb.util.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class MultipleValuesVisitor implements YAMLVisitor{
    List<String> extractedValues = new ArrayList<>();

    String keyToExtract;

    /**
     * Visit the input YAML and traverse its structure.
     * @param yaml
     */
    @Override
    public void visit(YAML yaml) {
        traverseYaml(yaml);
    }


    /**
     * @param yamlElement
     */
    private void traverseYaml(Object yamlElement) {
        if (yamlElement instanceof Map) {
            Map<?, ?> yamlMap = (Map<?, ?>) yamlElement;
            yamlMap.forEach((key, value) -> {
                if (key.equals(keyToExtract) && !(value instanceof Map) ){
                    extractedValues.add((String) value);
                }
                traverseYaml(value);
            });
        } else if (yamlElement instanceof Iterable) {
            Iterable<?> yamlList = (Iterable<?>) yamlElement;
            for (Object item : yamlList) {
                traverseYaml(item);
            }
        }
    }

    public List<String> getExtractedValues() {
        return extractedValues;
    }

    public void setExtractedValues(List<String> extractedValues) {
        this.extractedValues = extractedValues;
    }

    public String getKeyToExtract() {
        return keyToExtract;
    }

    public void setKeyToExtract(String keyToExtract) {
        this.keyToExtract = keyToExtract;
    }
}
