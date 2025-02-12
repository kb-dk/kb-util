/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package dk.kb.util.webservice.stream;

import dk.kb.util.webservice.exception.InternalServiceException;
import dk.kb.util.webservice.exception.InvalidArgumentServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * Helper for streaming export from web services. Wraps an OutputStream providing serialization of Jackson annotated
 * Objects to either JSON, JSON Lines or CSV.
 */
public class ExportWriterFactory {
    private static final Logger log = LoggerFactory.getLogger(ExportWriterFactory.class);

    /**
     * Wrap the given OutputStream and return an ExportWriter, serializing to the export format derived from httpHeaders
     * and format. Sample usage from ServiceImpl:
     * <pre>
     public StreamingOutput getRecordsModifiedAfter(String recordBase, Long mTime, Long maxRecords, String format) {
         return output -> {
             try (ExportWriter writer = ExportWriterFactory.wrap(
                     output, httpServletResponse, httpHeaders,
                     format, ExportWriterFactory.FORMAT.jsonl, false, "records")) {
             Stream<RecordDto> records = Store.getRecordStream(recordBase, mTime, maxRecords);
             records.forEach(record -> writer.write(record);
         }
        };
     * </pre>
     *
     * @param output      the destination stream.
     * @param response    used for setting the proper contentType.
     * @param httpHeaders headers from the calling client. The {@code accept} MIME types are used to determine the
     *                    export format. Accepted MIME types are stated in {@link FORMAT}.
     * @param format      user provided format String. If specified, it overrides the export format derived from
     *                    httpHeaders. Acceptable formats are stated in {@link FORMAT}.
     * @param defaultType if no export format could be derived from httpHeaders or format, use this format.
     * @param writeNulls  if true, null-values are exported as {@code "key":null} for JSON and JSONL.
     *                    If false, null-values are not stated.
     *                    For CSV export this has no effect.
     * @param rootElement the name of the outer element containing the data elements
     *                    {@code <outer> <inner>1</inner> <inner>2</inner> ... </outer>}
     *                    Only used with XML.
     * @return a writer that takes Jackson annotated objects and streams a serialization to output.
     */
    public static ExportWriter wrap(
            OutputStream output, HttpServletResponse response, HttpHeaders httpHeaders,
            String format, FORMAT defaultType, boolean writeNulls, String rootElement) {
        FORMAT streamFormat = null;
        try {
            streamFormat = format == null ? null : FORMAT.valueOf(format.toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            // Acceptable as we fall through to using headers
        }
        if (streamFormat == null) {
            streamFormat = FORMAT.getCompatible(httpHeaders.getAcceptableMediaTypes(), defaultType, defaultType);
        }
        if (streamFormat == null) {
            throw new InvalidArgumentServiceException(
                    "Unable to determine streaming export format and default was null");
        }

        return wrap(output, response, streamFormat, writeNulls, rootElement);
    }

    /**
     * Wrap the given OutputStream and return an ExportWriter, serializing to the given export format.
     * Consider using {@link #wrap(OutputStream, HttpServletResponse, FORMAT, boolean, String)} to support
     * CSV, JSON, JSONL as well as XML as export formats.
     * Sample usage from ServiceImpl:
     * <pre>
     public StreamingOutput getRecordsModifiedAfter(String recordBase, Long mTime, Long maxRecords) {
         return output -> {
             try (ExportWriter writer = ExportWriterFactory.wrap(
                     output, httpServletResponse, ExportWriterFactory.FORMAT.json, false, "records")) {
                 Stream<RecordDto> records = Store.getRecordStream(recordBase, mTime, maxRecords);
                 records.forEach(record -> writer.write(record);
             }
         };
     }
     * </pre>
     * @param output      the destination stream.
     * @param response    used for setting the proper contentType. Can be null.
     * @param format      the format to export to.
     * @param writeNulls  if true, null-values are exported as {@code "key":null} for JSON and JSONL.
     *                    If false, null-values are not stated.
     *                    For CSV export this has no effect.
     * @param rootElement the name of the outer element containing the data elements
     *                    {@code <outer> <inner>1</inner> <inner>2</inner> ... </outer>}
     *                    Only used with XML.
     * @return a writer that takes Jackson annotated objects and streams a serialization to output.
     */
    public static ExportWriter wrap(OutputStream output, HttpServletResponse response,
                                    FORMAT format, boolean writeNulls, String rootElement) {
        if (response == null) {
            log.warn("wrap: No HttpServletResponse given so the content MIME type could not be set");
        } else {
            format.setContentType(response);
        }
        Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
        switch (format) {
            case jsonl: return new JSONStreamWriter(writer, JSONStreamWriter.FORMAT.jsonl, writeNulls);
            case json: return new JSONStreamWriter(writer, JSONStreamWriter.FORMAT.json, writeNulls);
            case xml: return new ExportXMLStreamWriter(writer, rootElement, writeNulls);
            case csv: return new CSVStreamWriter(writer);
            default: throw new InternalServiceException("The export format '" + format + "' is unsupported");
        }
    }

    /**
     *
     * @param output      the destination stream.
     * @param response    used for setting the proper contentType. Can be null.
     * @param format      the format to export to.
     * @param writeNulls  if true, null-values are exported as {@code "key":null} for JSON and JSONL.
     *                    If false, null-values are not stated.
     * @param errorList   An ErrorList which contains information on records that have failed processing.
     * @return a writer that takes Jackson annotated objects and streams a serialization to output.
     */
    public static ExportWriter wrapWithErrors(OutputStream output, HttpServletResponse response,
                                    FORMAT format, boolean writeNulls, ErrorList errorList){
        if (response == null) {
            log.warn("wrap: No HttpServletResponse given so the content MIME type could not be set");
        } else {
            format.setContentType(response);
        }

        Writer writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
        switch (format) {
            case jsonl: return new JSONObjectStreamWriter(writer, JSONObjectStreamWriter.FORMAT.jsonl, writeNulls, errorList);
            case json: return new JSONObjectStreamWriter(writer, JSONObjectStreamWriter.FORMAT.json, writeNulls, errorList);
            default: throw new InternalServiceException("This method only handles JSON formats JSON and JSONL. The export format '" + format + "' is unsupported");
        }

    }

    /**
     * The possible export types, with support for deriving the type from {@link MediaType}s.
     */
    public enum FORMAT {
        jsonl("application", "x-ndjson"),
        json("application", "json"),
        xml("application", "xml"),
        csv("text", "csv");

        private final MediaType mime;
        FORMAT(String type, String subType) {
            this.mime = new MediaType(type, subType);
        }

        /**
         * True if the MIME type for this enum is matched by the given major type.
         * @param major MIME type that might match this.
         * @return true if matching.
         */
        public boolean matches(MediaType major) {
            return major.isCompatible(mime);
        }

        public void setContentType(HttpServletResponse response) {
            // TODO: This does not seem to work when an accept header is set
            response.setContentType(mime.toString());
        }

        /**
         * Searches through the acceptable media types stated in the httpHeaders, checking if any candidate matches
         * any of the STREAM_FORMAT types.
         * @param httpHeaders headers from the client calling the web service.
         * @param wildCardType the type to return if a wildcard is specified.
         * @param defaultType the type to return if there are no matches.
         * @return a FORMAT matching a candidate or null if there are no matches.
         */
        public static FORMAT getCompatible(
                HttpHeaders httpHeaders, FORMAT wildCardType, FORMAT defaultType) {
            return getCompatible(httpHeaders.getAcceptableMediaTypes(), wildCardType, defaultType);
        }

        /**
         * Searches through the given candidates to see is any candidate matches any of the STREAM_FORMAT types.
         * @param candidates possible matching MIME types.
         * @param wildCardType the type to return if a wildcard is specified.
         * @param defaultType the type to return if there are no matches.
         * @return a FORMAT matching a candidate or null if there are no matches.
         */
        public static FORMAT getCompatible(List<MediaType> candidates, FORMAT wildCardType, FORMAT defaultType) {
            for (MediaType candidate: candidates) {
                if (candidate.isWildcardType()) {
                    return wildCardType;
                }
                for (FORMAT streamFormat: FORMAT.values()) {
                    if (streamFormat.matches(candidate)) {
                        return streamFormat;
                    }
                }
            }
            return defaultType;
        }
    }

}
