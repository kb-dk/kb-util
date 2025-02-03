package dk.kb.util.webservice;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import dk.kb.util.webservice.exception.InternalServiceException;
import dk.kb.util.webservice.exception.ServiceException;
import dk.kb.util.webservice.stream.ContinuationInputStream;
import dk.kb.util.webservice.stream.ContinuationUtil;

/**
 * Service2Service call wrapper will inject an existing OAuth token into the next call.<
 * <p>
 * Current supported:
 * <ul>
 *   <li>Standard Http Request call </li>
 *   <li>Custom ContinuationInputStream</li>
 * </ul>
 * 
 *  Http-requests and continuationStreams requests and inject existing OAuth tokens.   
 */
public class Service2ServiceRequest {
    private static final Logger log = LoggerFactory.getLogger(Service2ServiceRequest.class);
        
    /**
    * <p>
    * Make service call to another web service and set the same OAuth token on the call that was used for the initiating service call. 
    * <p>
    * Maybe this method should be extended to also take additional RequestHeaders, but implement this if situation occurs. 
    *
    * @param uri the full URI with path and parameters set.
    * @param httpMethod The http-method to use for the service call. GET, POST, DELETE etc.
    * @param objectClass The DTO type that the response should be parsed to.
    * @return DtoObject (objectClass) of the same type at given as input. 
    * @throws ServiceException If anything unexpected happens.   
    **/    
    public static <T> T httpCallWithOAuthToken (URI uri , String httpMethod, T objectClass) throws ServiceException {                 
        //The token (message) will be set if the service method that initiated this call required OAuth token. 
        String token= (String) JAXRSUtils.getCurrentMessage().get(OAuthConstants.ACCESS_TOKEN_STRING); 
        Map<String, String> requestHeaders= new HashMap<String, String>();
        if (token != null) {                                          
            requestHeaders.put("Authorization","Bearer "+token);
            log.debug("OAuth2 Bearer token added to service2service call");
        }
        else {
             log.debug("Making service2service call without OAuth token");  
        }
             
        try {
            HttpURLConnection con = getHttpURLConnection(uri, httpMethod, requestHeaders);

            int status = con.getResponseCode();
            if (status < 200 || status > 299) { // Could be mapped to a more precise exception type, but an exception here is most likely a coding error. 
                String msg="Got HTTP " + status + " establishing connection to '" + uri + "'"+ con.getResponseCode();
                log.error(msg);
                throw new InternalServiceException(msg);
                // TODO: Consider if the error stream should be logged. It can be arbitrarily large (TOES)
            }
            
            String json = IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8);          

            ObjectMapper mapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            T dto = (T) mapper.readValue(json, objectClass.getClass());           
            return dto;
        }
        catch(Exception e) { 
            log.error(e.getMessage(),e);
            throw new InternalServiceException(e.getMessage()); 
        }

    }

    /** 
     * <p>
     * Make service call to another web service and set the same OAuth token on the call that was used for the initiating service call. 
     * <p>
     * 
     * Establish a connection to the given {@code uri}, extract all headers and construct a
     * {@link ContinuationInputStream} with the headers and response stream from the {@code uri}.
     * 
     * @param uri full URI for a call to a webservice.
     * @param tokenMapper maps the String header {@link ContinuationUtil#HEADER_PAGING_CONTINUATION_TOKEN}
     *                    to the concrete token type.
     * @param requestHeaders optional headers for the connection. Can be null.
     * @return an {@code InputStream} with the response.
     * @throws IOException if the connection failed.
     */
    public static <C2> ContinuationInputStream<C2> continuationInputStreamFromWithOAUthToken(
            URI uri, Function<String, C2> tokenMapper, Map<String, String> requestHeaders) throws IOException {
  
        if (requestHeaders== null) { //in case this is called with null map.
            requestHeaders= new HashMap<String, String>(); 
        }
        
        String token= (String) JAXRSUtils.getCurrentMessage().get(OAuthConstants.ACCESS_TOKEN_STRING);   
        if (token != null) {                                          
            requestHeaders.put("Authorization","Bearer "+token);
        }
                        
        HttpURLConnection con = getHttpURLConnection(uri, "GET",requestHeaders);
        Map<String, List<String>> responseHeaders = con.getHeaderFields();
        C2 continuationToken = ContinuationUtil.getContinuationToken(responseHeaders, tokenMapper).orElse(null);
        Boolean hasMore = ContinuationUtil.getHasMore(responseHeaders).orElse(null);
        Long recordCount = ContinuationUtil.getRecordCount(responseHeaders).orElse(null);
        log.debug("Established connection with continuation token '{}', hasMore {} and recordCount {} to '{}'",
                continuationToken, hasMore, recordCount, uri);
        return new ContinuationInputStream<>(con.getInputStream(), continuationToken, hasMore, recordCount, responseHeaders);
    }
    
    
    /**
     * Invoke a HTTP of a given HttpMethod and requestHeaders.
     * 
     * @param uri the full URI with path and parameters set.
     * @param httpMethod The http-method to use for the service call. GET, POST, DELETE etc. 
     * @return HttpUrlConnection that will have the status code and response can be read with an InputStream. 
     */
     private static HttpURLConnection getHttpURLConnection(URI uri, String httpMethod, Map<String, String> requestHeaders) throws IOException {
         //Do not log requestHeader since this would expose a valid OAuth token in the log file.
         log.debug("Opening streaming connection to '{}' with, method={}", uri, httpMethod);
         HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
         con.setRequestProperty("Content-Type","application/json");
         con.setRequestMethod(httpMethod);
         con.setInstanceFollowRedirects(true);
         if (requestHeaders != null) {
             requestHeaders.forEach(con::setRequestProperty);
         }      
         return con;
     }
}
