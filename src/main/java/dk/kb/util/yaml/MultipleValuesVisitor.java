package dk.kb.util.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This Visitor visits YAML files and extract all values that are associated with the key configured as {@link #keyToExtract}.
 * These values are saved in the list {@link #extractedValues} and can be retrieved by calling {@link #getExtractedValues()}.
 */
public class MultipleValuesVisitor implements YAMLVisitor {
    /**
     * The values extracted from {@link #visit(YAML)} are stored in this list.
     */
    List<String> extractedValues = new ArrayList<>();

    /**
     * The visitor visits YAML files and extracts values that are listed with this key.
     */
    String keyToExtract;

    /**
     * Visit the input YAML, traverse its structure and save all values that are associated with the {@link #keyToExtract}
     * to the {@link #extractedValues} list.
     * @param yaml file to extract values from.
     */
    @Override
    public void visit(YAML yaml) {
        traverseYaml(yaml);
    }


    /** Recursively traverse a YAML file and add values that are associated with the {@link #keyToExtract} to the {@link #extractedValues} list.
     * @param yamlElement to traverse and extract values from. Typically, this is a complete YAML file
     */
    private void traverseYaml(Object yamlElement) {
        if (yamlElement instanceof Map) {
            traverseMap((Map<?, ?>) yamlElement);
        } else if (yamlElement instanceof Iterable) {
            traverseList((Iterable<?>) yamlElement);
        }
    }

    /**
     * Traverse a YAML map and add values to {@link #extractedValues} if they have the {@link #keyToExtract}.
     * @param yamlElement which is an instance of {@link Map}.
     */
    private void traverseMap(Map<?, ?> yamlElement) {
        yamlElement.forEach((key, value) -> {
            if (key.equals(keyToExtract) && !(value instanceof Map) ){
                extractedValues.add((String) value);
            }
            traverseYaml(value);
        });
    }

    /**
     * Traverse a YAML map and add values to {@link #extractedValues} if they have the {@link #keyToExtract}.
     * @param yamlElement which is an instance of {@link Iterable}.
     */
    private void traverseList(Iterable<?> yamlElement) {
        for (Object item : yamlElement) {
            traverseYaml(item);
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
