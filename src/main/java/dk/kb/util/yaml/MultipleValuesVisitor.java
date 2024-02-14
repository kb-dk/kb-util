package dk.kb.util.yaml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MultipleValuesVisitor implements YAMLVisitor {
    private static final Logger log = LoggerFactory.getLogger(MultipleValuesVisitor.class);

    private static final String PLACEHOLDER = "*";
    List<Object> extractedValues = new ArrayList<>();
    List<String> matchingPaths = new ArrayList<>();
    List<String> inputPathElements = new ArrayList<>();
    String inputPath;
    String currentPath;

    public MultipleValuesVisitor(String inputPath){
        this.inputPath = inputPath;
        this.inputPathElements = splitPath(inputPath);
    }

    public MultipleValuesVisitor(){}

    @Override
    public void visit(Object yamlEntry) {
        this.inputPathElements = splitPath(inputPath);
        // Traverse the full yamlEntry by not giving it a path here.
        // The path variable is needed for the following iterations.
        // Should match every entry of the input path element, when the input path starts with "*."
        if (inputPath.startsWith(PLACEHOLDER + ".") && currentPath.endsWith((inputPathElements.get(1)))){
            extractedValues.add(yamlEntry);
            return;
        }

        boolean pathMatches = compareCurrentPathToInput(currentPath, inputPathElements);
        if (pathMatches){
            matchingPaths.add(currentPath);
            extractedValues.add(yamlEntry);
        }
    }


    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
        this.inputPathElements = splitPath(inputPath);
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    private boolean compareCurrentPathToInput(String path, List<String> inputPathElements) {
        List<String> currentPathElements = splitPath(path);

        boolean isMatchingPath = compareStringLists(inputPathElements, currentPathElements);
        return isMatchingPath;
    }

    public boolean compareStringLists(List<String> listWithPlaceholder, List<String> listWithValuesFromYaml) {
        int index1 = 0;
        int index2 = 0;

        while (index1 < listWithPlaceholder.size() && index2 < listWithValuesFromYaml.size()) {
            String queryPath = listWithPlaceholder.get(index1);
            String pathInYaml = listWithValuesFromYaml.get(index2);

            // Handle [**] and ** (traverse the full path)
            if (queryPath.equals("**") || queryPath.contains("[**]")) {
                // If ** or [**] is the last element in listWithPlaceholder, it matches any remaining elements in listWithValuesFromYaml
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
            }
            // Handle [*] and * (match a single level)
            else if (queryPath.equals("*") || queryPath.contains("[*]")) {
                // If * or [*] is the last element in listWithPlaceholder, it matches any remaining elements in listWithValuesFromYaml
                if (index1 == listWithPlaceholder.size() - 1) {
                    return true;
                } else {
                    // Move to the next elements in both lists
                    index1++;
                    index2++;
                }
            }
            // Handle array lookup with conditions like [foo=bar] and [foo!=bar]
            else if (queryPath.contains("[") && queryPath.endsWith("]")) {
                log.info("Here some logic happens");

                // If the condition doesn't match, return false
                return false;

            }
            // Handle other non-placeholder strings
            else {
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
