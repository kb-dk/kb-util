package dk.kb.util.other;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StringListUtils {
    
    
    public static final Predicate<String> notNull = Predicate.not(Objects::isNull);
    public static final Predicate<String> notEmptyString = Predicate.not(String::isEmpty);
    public static final Predicate<String> isNotEmpty = notNull.and(notEmptyString);
    
    /**
     * Turn an iterator into a stream
     *
     * @param iterator the iterator
     * @param <T>      the the type of elements
     * @return A stream of the iterator elements in the same order
     */
    public static <T> Stream<T> asStream(Iterator<T> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false);
    }
    
    /**
     * Return the string or "" if null
     *
     * @param string the string
     * @return same or "" if null
     */
    public static String notNull(String string) {
        return Optional.ofNullable(string).orElse("");
    }
    
    /**
     * Return the first entry in the list or defaultValue if the list is empty
     *
     * @param list         the list
     * @param defaultValue the value if the list is empty
     * @return first value or default value if list is empty
     */
    public static String getFirst(List<String> list, String defaultValue) {
        if (list == null || list.isEmpty()) {
            return defaultValue;
        } else {
            return list.get(0);
        }
    }
    
    /**
     * If param is not null return that. Otherwise return the value from props[propKey]
     *
     * @param param   the value to return if notNull
     * @param props   the properties to get default from
     * @param propKey the key in props to get the default
     * @return param or props[propKey]
     */
    public static String getParamOrProps(String param, Properties props, String propKey) {
        if (param == null || param.isBlank()) {
            
            if (!props.contains(propKey)) {
                param = props.getProperty(propKey);
            } else {
                param = null;
            }
        }
        return param;
    }
    
    /**
     * Remove empty elements from the list and distinct the list.
     *
     * The order of the list will be preserved, but elements will be removed.
     *
     * if list is null, return an empty list.
     *
     * @param list the list to filter
     * @return a list without any empty elements
     */
    public static List<String> removeEmpties(List<String> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream()
                   .filter(Objects::nonNull)
                   .filter(element -> !element.trim().isEmpty())
                   .distinct()
                   .collect(Collectors.toList());
    }
    
    /**
     * Remove empty elements from the list and distinct the list.
     *
     * The order of the list will be preserved, but elements will be removed.
     *
     * if list is null, return an empty list.
     *
     * @param list the list to filter
     * @return a list without any empty elements
     */
    public static List<String> removeEmpties(String... list) {
        return Arrays.stream(list)
                     .filter(Objects::nonNull)
                     .filter(number -> !number.trim().isEmpty())
                     .distinct()
                     .collect(Collectors.toList());
    }
    
  
    
    
    /**
     * Remove any entries in the list that are contained in any other entries in the list
     *
     * @param list the list to clean
     * @param <T>  the type of list
     * @return a list without any elements that was a substring of any other element in the list
     */
    public static List<String> removeSubstrings(List<String> list) {
        List<String> coll = list;
        try {
            coll.addAll(Collections.emptyList());
        } catch (java.lang.UnsupportedOperationException e){
            coll = new ArrayList<>(list);
        }
        
        Iterator<String> firstIterator = coll.iterator();
        while (firstIterator.hasNext()) {
            String s = firstIterator.next();
            for (String t : list) {
                if (s.equals(t)) {
                    continue;
                }
                if (t.contains(s)) {
                    firstIterator.remove();
                    break;
                }
            }
        }
        
        return coll;
    }
    
   
    
    /**
     * If value is null or blank, return defaultValue. Otherwise return value
     * @param value the value
     * @param defaultValue the default value
     * @return see above
     * This is a specialisation of #orDefault(Object, Object), where we check not just null but also blank
     */
    public static String useDefaultIfNullOrEmpty(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        } else {
            return value;
        }
    }
    
    /**
     * If value is null, return defaultValue. Otherwise return value
     * @param value the value
     * @param defaultValue the default value
     * @param <T> the type
     * @return see above
     */
    public static <T> T useDefaultIfNull(T value, T defaultValue) {
        return Optional.ofNullable(value).orElse(defaultValue);
    }
    
    
    /**
     * Return the elements as a MODIFIABLE list
     *
     * @param elements the elements
     * @param <T>     the type of elements
     * @return the elements as a ArrayList
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> List<T> toModifiableList(T... elements) {
        if (elements == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<T>(Arrays.asList(elements));
        }
    }
    
    
    /**
     * Substring that allows for negative indexes and indexes beyound string length
     * @param string the string
     * @param startIndex the start index. If negative, count backwards from the end of the string.
     *                   If longer than the string, cap at string length.
     * @param endIndex the end index. If negative, count backwards from the end of the string.
     *                 If longer than the string, cap at string length.
     * @return the substring
     */
    public static String substring(String string, int startIndex, int endIndex) {
        string = notNull(string);
        
        while (startIndex < 0) {
            if (string.length() > 0) {
                //Negative index so subtract it from the length of the string
                //This allows you to use a negative index to start a number of chars from the END of the string
                startIndex = startIndex + string.length();
            } else {
                //String is length 0 so break the loop
                startIndex = 0;
            }
        }
        
        while (endIndex < 0) {
            if (string.length() > 0) {
                //Negative index so subtract it from the length of the string
                //This allows you to use a negative index to start a number of chars from the END of the string
                endIndex = endIndex + string.length();
            } else {
                //String is length 0 so break the loop
                endIndex = 0;
            }
        }
    
        //If index is beyound the string length, set it to the the string length
        startIndex = Math.min(startIndex, string.length());
        endIndex = Math.min(endIndex, string.length());
        
        if (startIndex > endIndex) {
            //If start is beyound end, substring as two strings
            //First part is from the start to the end_of_string
            String first = string.substring(startIndex);
            //Second part is from the start_of_string to endIndex
            String second = string.substring(0, endIndex);
            return first + second;
        } else {
            //StartIndex is no larger than endIndex so do a normal boring substring
            return string.substring(startIndex, endIndex);
        }
    }
    
    /**
     * Remove the middle part of the string, such that the result is no longer than maxLength
     * @param string the string to cut
     * @param maxLength the max length of the output
     * @return a string with the middle part replaced with ...
     */
    public static String truncateMiddle(String string, int maxLength) {
        
        string = notNull(string);
        if (string.length() <= maxLength) {
            return string;
        }
        String truncateString = "...";
        maxLength = maxLength - truncateString.length();
        
        int startStringLength = maxLength / 2;
        int endStringLength = Math.floorDiv(maxLength, 2);
        String startString = substring(string, 0, startStringLength);
        String endString = substring(string, string.length() - 1 - endStringLength, string.length());
        
        int numberRemovedChars = string.length() - maxLength;
        return startString + truncateString + endString;
        
    }
    
    /**
     * Cut the end of the string such that the result is no longer than maxLength
     * @param string the string
     * @param maxLength the max length
     * @return the string with the end replaced with ... if nessesary
     */
    public static String truncateEnd(String string, int maxLength) {
        string = notNull(string);
        if (string.length() <= maxLength) {
            return string;
        }
        String truncateString = "...";
        maxLength = maxLength - truncateString.length();
        
        String startString = substring(string, 0, maxLength);
        
        return startString + truncateString;
        
    }
}
