package dk.kb.util;

public class InvalidTypeException extends YAMLException{
    
    
    public InvalidTypeException(String message, String path) {
        super(message, path);
    }
    
    public InvalidTypeException(String message, String path, Throwable cause) {
        super(message, path, cause);
    }
    
    public InvalidTypeException(Throwable cause, String path) {
        super(cause, path);
    }
    
    public InvalidTypeException(String message,
                                String path,
                                Throwable cause,
                                boolean enableSuppression,
                                boolean writableStackTrace) {
        super(message, path, cause, enableSuppression, writableStackTrace);
    }
}
