package dk.kb.util.webservice.stream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Writer;
import java.util.regex.Pattern;

/**
 * Wrapper that handles streamed output of entries, either as a single valid JSON or
 * JSON Lines (1 independent JSON/line). This wrapper can include a metaobject containing information on records with errors.
 * <br/>
 * This StreamWriter is almost identical with {@link JSONStreamWriter} which returns a JSON array of data. This writer returns an object containing a data array and an errors
 * object.
 */
public class JSONObjectStreamWriter extends ExportWriter {
    private static final Logger log = LoggerFactory.getLogger(JSONObjectStreamWriter.class);
    private final ObjectWriter jsonWriter;

    public enum FORMAT { json, jsonl }

    private final FORMAT format;
    private boolean first = true;
    private boolean isclosing = false; // If the writer is in the process of closing (breaks infinite recursion)

    // Used in write(String), e.g. for replacing newlines with spaces for JSON-Lines
    private Pattern adjustPattern = null;
    private String adjustReplacement = "";
    private String preOutput;
    private String postOutput;
    private String elementDivider;
    private final ErrorList errorList;

    /**
     * Wrap the given inner Writer in the JSONObjectStreamWriter. Calls to {@link #write(Object)} writes directly to inner,
     * so the JSONObjectStreamWriter holds no cached data. The inner {@link Writer#flush()} is not called during write.
     * null-values in objects given to {@link #write(Object)} will not be written. To control this, use
     * the {@link JSONObjectStreamWriter(Writer, FORMAT, boolean, ErrorList)} constructor.
     * @param inner  the Writer to send te result to.
     * @param format Valid JSON or JSON Lines.
     */
    public JSONObjectStreamWriter(Writer inner, FORMAT format) {
        this(inner, format, false, null);
    }

    /**
     * Wrap the given inner Writer in the JSONStreamWriter. Calls to {@link #write} writes directly to inner,
     * so the JSONStreamWriter holds no cached data. The inner {@link Writer#flush()} is not called.
     * @param inner  the Writer to send te result to.
     * @param format Valid JSON or JSON Lines.
     * @param writeNulls if true, null values are written as {@code "key" : null}, if false they are skipped.
     */
    public JSONObjectStreamWriter(Writer inner, FORMAT format, boolean writeNulls, ErrorList errorList) {
        super(inner);
        this.format = format;
        this.errorList = errorList;

        if (inner == null) {
            throw new IllegalArgumentException("Inner Writer was null, but must be defined");
        }
        if (format == null) {
            throw new IllegalArgumentException("Format was null, but must be defined");
        }

        switch (format) {
            case json: {
                preOutput = "\"data\":[\n";
                postOutput = "\n]\n";
                elementDivider = ",\n";
                break;
            }
            case jsonl: {
                adjustPattern = Pattern.compile("\n");
                adjustReplacement = " ";
                preOutput = "\"data\":[";
                postOutput = "]\n";
                elementDivider = "\n";
                break;
            }
            default: throw new UnsupportedOperationException("The format '" + format + "' is unknown");
        }

        ObjectMapper mapper = createMapper();
        mapper.setSerializationInclusion(writeNulls ? JsonInclude.Include.ALWAYS : JsonInclude.Include.NON_NULL);
        jsonWriter = mapper.writer(new MinimalPrettyPrinter());
    }





    public void writeObjectEnd() {
        this.write("}");
    }

    /**
     * Write a JSON expression that has already been serialized to String.
     * It is the responsibility of the caller to ensure that jsonStr is valid standalone JSON.
     * If {@link #format} is {@link FORMAT#jsonl}, newlines in jsonStr will be replaced by spaces.
     * @param jsonStr a valid JSON.
     */
    @Override
    public void write(String jsonStr) {
        if (adjustPattern != null) {
            jsonStr = adjustPattern.matcher(jsonStr).replaceAll(adjustReplacement);
        }

        if (first) {
            super.write("{\n");
            super.write(preOutput);
            first = false;
        } else {
            super.write(elementDivider);
        }

        super.write(jsonStr);
    }

    /**
     * Use {@link #jsonWriter} to serialize the given object to String JSON and write the result, ensuring the
     * invariants of {@link #format} holds.
     * @param annotatedObject a Jackson annotated Object.
     */
    @Override
    public void write(Object annotatedObject) {
        if (annotatedObject == null) {
            log.warn("Internal inconsistency: write(null) called. This should not happen");
            return;
        }
        try {
            write(jsonWriter.writeValueAsString(annotatedObject));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JsonProcessingException attempting to write " + annotatedObject, e);
        }
    }

    /**
     * Finishes the JSON stream by writing closing statements (if needed).
     */
    @Override
    public void close() {
        if (isclosing) {
            return; // Avoid infinite recursion
        }
        isclosing = true;
        if (first) {
            super.write("{\n");
            super.write(preOutput);
        }
        if (errorList != null){
            super.write(postOutput + ",");
            super.write("\"errors\":" + errorList.getOverview().toString());
            super.write("}");
        } else {
            super.write(postOutput);
            super.write("}");
        }

        super.close();
    }

    public void closeEntry() {
        if (isclosing) {
            return; // Avoid infinite recursion
        }
        isclosing = true;
        if (first) {
            super.write("{\n");
            super.write(preOutput);
        }
        super.write(postOutput);
        super.write(elementDivider);

        //reset first state
        first = true;
    }

    /**
     * Used with {@link #adjustReplacement} in {@link #write(String)} to adjust the content before writing it.
     * Example: This is used with JSON-Lines to replace newlines with spaces to uphold the single line contract.
     * @param pattern used for adjusting the content in {@link #write(String)}. null means no adjustment.
     */
    public void setAdjustPattern(Pattern pattern) {
        this.adjustPattern = pattern;
    }

    /**
     * Used with {@link #adjustPattern} in {@link #write(String)} to adjust the content before writing it.
     * Example: This is used with JSON-Lines to replace newlines with spaces to uphold the single line contract.
     * @param replacement used for adjusting the content in {@link #write(String)}. null means no adjustment.
     */
    public void setAdjustReplacement(String replacement) {
        this.adjustReplacement = replacement;
    }

    /**
     * Written on first call to any {@link #write} or on {@link #close()} if there has been any writes.
     * @param header written before any other content.
     */
    public void setPreOutput(String header) {
        this.preOutput = header;
    }

    /**
     * Written on {@link #close()}.
     * @param footer written after any other content.
     */
    public void setPostOutput(String footer) {
        this.postOutput = footer;
    }

    /**
     * @param divider written before content in {@link #write} if write has been called previously.
     */
    public void setElementDivider(String divider) {
        this.elementDivider = divider;
    }
}

