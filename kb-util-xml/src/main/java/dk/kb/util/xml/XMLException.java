package dk.kb.util.xml;

public class XMLException extends RuntimeException {
  public XMLException() {
  }

  public XMLException(String message) {
        super(message);
    }

  public XMLException(String message, Throwable cause) {
    super(message, cause);
  }

  public XMLException(Throwable cause) {
    super(cause);
  }

  public XMLException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
