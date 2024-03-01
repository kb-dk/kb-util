package dk.kb.util.yaml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class MultipleValuesVisitor implements YAMLVisitor {
    /*private static final Logger log = LoggerFactory.getLogger(MultipleValuesVisitor.class);

    private static final Pattern CURRENT_KEY = Pattern.compile("([^.\\[\\]]+)$");
    private static final Pattern ARRAY_ELEMENT = Pattern.compile("^([^\\[]*)\\[([^]]*)]$");
    private static final Pattern ARRAY_CONDITIONAL = Pattern.compile(" *([^!=]+) *(!=|=) *(.*)"); // foo=bar or foo!=bar
    private static final Pattern INTEGER_LOOKUP = Pattern.compile("(\\d+)");
    private static final Pattern LAST_LOOKUP = Pattern.compile("(last)");

    private static final String PLACEHOLDER = "*";*/
    List<Object> extractedValues = new ArrayList<>();
    /*List<String> matchingPaths = new ArrayList<>();
    List<String> inputPathElements = new ArrayList<>();
    String inputPath;
    String currentPath;*/

    public MultipleValuesVisitor(){}

    @Override
    public void visit(Object yamlEntry) {
        //this.inputPathElements = splitPath(inputPath);

        this.extractedValues.add(yamlEntry);

        /*if (yamlEntry instanceof Map || yamlEntry instanceof List){
            if (inputPath.equals(currentPath)){
                //YAML current = new YAML((Map<String, Object>) yamlEntry, topYaml.extrapolateSystemProperties, topYaml.getSubstitutors());
                matchingPaths.add(currentPath);
                extractedValues.add(yamlEntry);
                log.info(String.valueOf(yamlEntry));
                log.info("Added map/list to extracted values");
            }
            // Handle lists and maps for getList and getMap
            return;
        }

        // Traverse the full yamlEntry by not giving it a path here.
        // Should match every entry of the input path element, when the input path starts with "*." or "**."
        if ((inputPath.startsWith(PLACEHOLDER + ".") ||inputPath.startsWith("**.")) && currentPath.endsWith((inputPathElements.get(1)))){
            matchingPaths.add(currentPath);
            extractedValues.add(topYaml.extrapolate(yamlEntry));
            //log.info("Extracted the value: '{}' from path: '{}'.", yamlEntry, currentPath );
            return;
        }

        boolean pathMatches = compareCurrentPathToInput(currentPath, inputPathElements, yamlEntry);
        if (pathMatches){
            log.info("Extracted the value: '{}' from path: '{}'.", yamlEntry, currentPath );
            matchingPaths.add(currentPath);
            extractedValues.add(topYaml.extrapolate(yamlEntry));
        }*/
    }

/*
    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
        this.inputPathElements = splitPath(inputPath);
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    *//**
     * Converts the current path to a list of path elements, to make placeholders work.
     * @param path current path in the YAML.
     * @param inputPathElements the path searched for in the YAML split by dots.
     * @param yamlEntry the value of the current place in the YAML.
     * @return a boolean for a match between input path and current path.
     *//*
    private boolean compareCurrentPathToInput(String path, List<String> inputPathElements, Object yamlEntry) {
        List<String> currentPathElements = splitPath(path);

        boolean isMatchingPath = compareStringLists(inputPathElements, currentPathElements, yamlEntry);
        return isMatchingPath;
    }

    *//**
     * Compares two paths split by dots. This implementation makes room for placeholders such as .* and .** in YAML paths.
     * @param listWithPlaceholder List that can contain placeholders as * and **.
     * @param listWithValuesFromYaml list containing current path in YAML.
     * @param yamlEntry the value at the current entry.
     * @return true if the paths match, false if not.
     *//*
    public boolean compareStringLists(List<String> listWithPlaceholder, List<String> listWithValuesFromYaml, Object yamlEntry) {
        int index1 = 0;
        int index2 = 0;

        while (index1 < listWithPlaceholder.size() && index2 < listWithValuesFromYaml.size()) {
            String queryPath = listWithPlaceholder.get(index1);
            String pathInYaml = listWithValuesFromYaml.get(index2);

            Matcher isArray = ARRAY_ELEMENT.matcher(queryPath);

            // Handle [**] and ** (traverse the full path) for matches
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
            else if (queryPath.equals("*") || queryPath.contains("[*]") || queryPath.contains("[]")) {
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
            else if (isArray.matches()) {
                log.info("Current path is: '{}'", currentPath);
                String arrayElement = isArray.group(2);

                Matcher getCurrectKey = CURRENT_KEY.matcher(currentPath);
                String currentKey = "";
                if (getCurrectKey.find()){
                    currentKey = getCurrectKey.group(1);
                }

                Matcher arrayMatch = ARRAY_CONDITIONAL.matcher(arrayElement);
                if (arrayMatch.matches()){
                    String key = arrayMatch.group(1);
                    boolean mustMatch = arrayMatch.group(2).equals("="); // The Pattern ensures only "!=" or "=" is in the group
                    String value = arrayMatch.group(3);

                    if ((mustMatch && currentKey.equals(key) && yamlEntry.equals(value)) || (!mustMatch && currentKey.equals(key) && !yamlEntry.equals(value))) {
                        // Move to the next elements in both lists and also add the found value to the output array
                        index1++;
                        index2++;
                        extractedValues.add(yamlEntry);
                    } else {
                        // If the condition doesn't match, return false
                        return false;
                    }
                }

                // If the YAML array path contains an integer lookup as [0] or [4] and
                // the currentPath equals the input path there is a match.
                Matcher intLookup = INTEGER_LOOKUP.matcher(arrayElement);
                Matcher lastLookup = LAST_LOOKUP.matcher(arrayElement);
                if (intLookup.matches() && currentPath.equals(inputPath)){

                    index1 ++;
                    index2 ++;

                // Clears the extracted values list and ensures that only the last value is returned
                } else if (lastLookup.matches()) {
                    extractedValues.clear();
                    index1++;
                    index2++;
                } else {
                    return false;
                }


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

    *//**
     * Splits the given path on {@code .}, with support for quoting with single {@code '} and double {@code "} quotes.
     * {@code foo.bar."baz.zoo".'eni.meni' -> [foo, bar, baz.zoo, eni.meni]}.
     * @param path a YAML path with dots {@code .} as dividers.
     * @return the path split on {@code .}.
     *//*
    private List<String> splitPath(String path) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = QUOTE_DOT_SPLIT.matcher(path);
        while (matcher.find()) {
            // Getting group(0) would not remove quote characters so a check for capturing group is needed
            tokens.add(matcher.group(1) == null ? matcher.group(2) : matcher.group(1));
        }
        return tokens;
    }
    private final Pattern QUOTE_DOT_SPLIT = Pattern.compile("[\"']([^\"']*)[\"']|([^.]+)");*/
}
