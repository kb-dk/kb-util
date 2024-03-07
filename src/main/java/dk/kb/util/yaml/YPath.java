package dk.kb.util.yaml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * YPath helper for YAML traversal.
 * Handles getting, setting and removing elements from the YPath.
 */
public class YPath {
    private static final Logger log = LoggerFactory.getLogger(YPath.class);

    /**
     * The list containing the YPath elements
     */
    private List<String> yPath;

    /**
     * Construct a {@code YPath} from a string. The string will be split by dots.
     * @param path to create YPat from eg: foo.bar.zoo
     */
    public YPath(String path){
        if (path.startsWith(".")) {
            path = path.substring(1);
        }
        this.yPath = splitPath(path);
    }

    /**
     * Construct YPath from an already existing YPath.
     * @param original YPath which the new YPath is a copy of.
     */
    public YPath(YPath original){
        this.yPath = new ArrayList<>(original.yPath);
    }

    /**
     * Get the first element in the YPath.
     */
    public String getFirst(){
        return yPath.get(0);
    }

    /**
     * Remove the first entry from a YPath
     */
    public YPath removeFirst(){
        if (yPath.isEmpty()){
            return this;
        } else {
            YPath shortenedPath = new YPath(this);
            shortenedPath.yPath = shortenedPath.yPath.subList(1, yPath.size());
            return shortenedPath;
        }
    }

    /**
     * Replace the first entry in a YPath.
     * @param replacement the value, which is to replace the first value in the {@code YPath}.
     * @return a new YPath object, with the first string replaced.
     */
    public YPath replaceFirst(String replacement){
        YPath replacedYPath = new YPath(this);
        replacedYPath.yPath.set(0, replacement);

        return replacedYPath;
    }

    /**
     * Check if the internal YPath list is empty.
     */
    public boolean isEmpty(){
        return yPath.isEmpty();
    }



    /**
     * Splits the given path on {@code .}, with support for quoting with single {@code '} and double {@code "} quotes.
     * {@code foo.bar."baz.zoo".'eni.meni' -> [foo, bar, baz.zoo, eni.meni]}.
     * @param path a YAML path with dots {@code .} as dividers.
     * @return the path split on {@code .}.
     */
    private List<String> splitPath(String path) {
        List<String> tokens = new ArrayList<>();
        // Ensure all path elements are separated by a singular dot
        path = path.replaceAll("([^.])\\[", "$1.[");
        Matcher matcher = QUOTE_DOT_SPLIT.matcher(path);
        while (matcher.find()) {
            // Getting group(0) would not remove quote characters so a check for capturing group is needed
            tokens.add(matcher.group(1) == null ? matcher.group(2) : matcher.group(1));
        }
        return tokens;
    }
    private final Pattern QUOTE_DOT_SPLIT = Pattern.compile("[\"']([^\"']*)[\"']|([^.]+)");


    /**
     * Get the size of the internal YPath element.
     */
    public int size() {
        return yPath.size();
    }
}
