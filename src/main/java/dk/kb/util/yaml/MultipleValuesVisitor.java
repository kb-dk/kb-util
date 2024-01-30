package dk.kb.util.yaml;

import java.util.List;
import java.util.Map;

public class MultipleValuesVisitor implements YAMLVisitor{

    @Override
    public void visit(YAML yaml) {
        traverseYaml(yaml, null, null);
    }


    public void traverseYaml(Object yamlElement, List<String> extractedValues, String keyToExtract) {
        if (yamlElement instanceof Map) {
            Map<?, ?> yamlMap = (Map<?, ?>) yamlElement;
            yamlMap.forEach((key, value) -> {
                if (key.equals(keyToExtract)){
                    extractedValues.add((String) value);
                }
                traverseYaml(value, extractedValues, keyToExtract);
            });
        } else if (yamlElement instanceof Iterable) {
            Iterable<?> yamlList = (Iterable<?>) yamlElement;
            int index = 0;
            for (Object item : yamlList) {
                traverseYaml(item, extractedValues, keyToExtract);
                index++;
            }
        } else {
            // Handle scalar values
        }
    }

}
