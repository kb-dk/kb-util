package dk.kb.util;

public class YAMLException extends RuntimeException {
    
    private final String path;
  
    public YAMLException(String message, String path) {
        super(path+": "+message);
        this.path = path;
    }
    
    public YAMLException(String message, String path, Throwable cause) {
        super(path+": "+message, cause);
        this.path = path;
    }
    
    public YAMLException(Throwable cause, String path) {
        super(path, cause);
        this.path = path;
    }
    
    public YAMLException(String message, String path, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(path+": "+message, cause, enableSuppression, writableStackTrace);
        this.path = path;
    }
    
    public String getPath() {
        return path;
    }
    
    
}
