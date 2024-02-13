package dk.kb.util.yaml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultipleValuesVisitor implements YAMLVisitor {
    private static final Logger log = LoggerFactory.getLogger(MultipleValuesVisitor.class);

    List<Object> extractedValues = new ArrayList<>();
    List<String> matchingPaths = new ArrayList<>();
    List<String> inputPathElements = new ArrayList<>();
    String inputPath;

    public MultipleValuesVisitor(String inputPath){
        this.inputPath = inputPath;
        this.inputPathElements = splitPath(inputPath);
    }

    @Override
    public void visit(YAML yaml) {
        // Traverse the full yaml by not giving it a path here.
        // The path variable is needed for the following iterations.
        traverseYaml("", yaml);
    }

    private void traverseYaml(String path, Object yamlEntry) {
        // Handle maps by appending .key to the current path and then traversing again.
        if (yamlEntry instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) yamlEntry;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = entry.getKey().toString();
                Object value = entry.getValue();
                traverseYaml(path + "." + key, value);
            }
        // Handle lists by appending [i] to the current path and then getting that value.
        } else if (yamlEntry instanceof List) {
            List<?> list = (List<?>) yamlEntry;
            for (int i = 0; i < list.size(); i++) {
                traverseYaml(path + "[" + i + "]", list.get(i));
            }
        // Handle scalar values by checking that the path matches
        // then adding the scalar to extracted values if paths match.
        } else {
            if (path.startsWith(".")) {
                path = path.substring(1);
            }

            // Should match every entry of the input path element, when the input path starts with "*."
            if (inputPath.startsWith("*.") && path.endsWith((inputPathElements.get(1)))){
                extractedValues.add(yamlEntry.toString());
                return;
            }

            boolean pathMatches = compareCurrentPathToInput(path, inputPathElements);
            if (pathMatches){
                matchingPaths.add(path);
                extractedValues.add(yamlEntry.toString());
            }
        }
    }

    private boolean compareCurrentPathToInput(String path, List<String> inputPathElements) {
        List<String> currentPathElements = splitPath(path);

        boolean isMatchingPath = compareStringLists(inputPathElements, currentPathElements);
        return isMatchingPath;
    }

    public static boolean compareStringLists(List<String> listWithPlaceholder, List<String> listWithValuesFromYaml) {
        
        int index1 = 0;
        int index2 = 0;

        while (index1 < listWithPlaceholder.size() && index2 < listWithValuesFromYaml.size()) {
            String queryPath = listWithPlaceholder.get(index1);
            String pathInYaml = listWithValuesFromYaml.get(index2);

            // Handle array lookup
            if (queryPath.contains("[*]")) {
                // Extract the prefix before '[*]'
                String prefix = queryPath.substring(0, queryPath.indexOf("["));

                // Check if pathInYaml starts with the same prefix followed by '['
                if (pathInYaml.startsWith(prefix + "[")) {
                    // If the prefix matches, move to the next elements in both lists
                    if (index1 == listWithPlaceholder.size() - 1) {
                        return true;
                    } else {
                        // Find the next non-placeholder string in listWithPlaceholder.
                        String nextNonPlaceholder = listWithPlaceholder.get(index1 + 1);
                        int placeholderMatches = 0;
                        // Count the number of matches in listWithValuesFromYaml until the next non-placeholder string is found
                        while (!pathInYaml.equals(nextNonPlaceholder) && index2 < listWithValuesFromYaml.size()) {
                            index2++;
                            if (index2 < listWithValuesFromYaml.size())
                                pathInYaml = listWithValuesFromYaml.get(index2);
                            placeholderMatches++;
                        }
                        // If the placeholder doesn't match any elements in listWithValuesFromYaml, return false
                        if (placeholderMatches == 0) {
                            return false;
                        }
                        index1++;
                    }
                    index1++;
                    index2++;
                } else {
                    // If the prefix doesn't match, return false
                    return false;
                }
            } else if (queryPath.equals("*")) {
                // Treat '*' or '[*]' as a placeholder for one or more strings
                // If '*' or '[*]' is the last element in listWithPlaceholder, it matches any remaining elements in listWithValuesFromYaml
                if (index1 == listWithPlaceholder.size() - 1) {
                    return true;
                } else {
                    // Find the next non-placeholder string in listWithPlaceholder.
                    String nextNonPlaceholder = listWithPlaceholder.get(index1 + 1);
                    int placeholderMatches = 0;
                    // Count the number of matches in listWithValuesFromYaml until the next non-placeholder string is found
                    while (!pathInYaml.equals(nextNonPlaceholder) && index2 < listWithValuesFromYaml.size()) {
                        index2++;
                        if (index2 < listWithValuesFromYaml.size())
                            pathInYaml = listWithValuesFromYaml.get(index2);
                        placeholderMatches++;
                    }
                    // If the placeholder doesn't match any elements in listWithValuesFromYaml, return false
                    if (placeholderMatches == 0) {
                        return false;
                    }
                    index1++;
                }
            } else {
                // If both strings are not placeholders, compare them. If they are not equal, then there is no match.
                if (!queryPath.equals(pathInYaml)) {
                    return false;
                }
                // Move to the next elements in both lists
                index1++;
                index2++;
            }
        }

        // If both lists have been traversed completely we have found a match.
        return index1 == listWithPlaceholder.size() && index2 == listWithValuesFromYaml.size();
    }

    /**
     * Splits the given path on {@code .}, with support for quoting with single {@code '} and double {@code "} quotes.
     * {@code foo.bar."baz.zoo".'eni.meni' -> [foo, bar, baz.zoo, eni.meni]}.
     * @param path a YAML path with dots {@code .} as dividers.
     * @return the path split on {@code .}.
     */
    private List<String> splitPath(String path) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = QUOTE_DOT_SPLIT.matcher(path);
        while (matcher.find()) {
            // Getting group(0) would not remove quote characters so a check for capturing group is needed
            tokens.add(matcher.group(1) == null ? matcher.group(2) : matcher.group(1));
        }
        return tokens;
    }
    private final Pattern QUOTE_DOT_SPLIT = Pattern.compile("[\"']([^\"']*)[\"']|([^.]+)");
}
