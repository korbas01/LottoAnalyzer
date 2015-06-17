package yimscompany.lottoanalyzer.Exceptions;

/**
 * Created by shyim on 15-06-10.
 */
public class InvalidConnectionException extends Exception {
    public InvalidConnectionException() {
        super();
    }

    public InvalidConnectionException(String detailMessage) {
        super(detailMessage);


    }

    public InvalidConnectionException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public InvalidConnectionException(Throwable throwable) {
        super(throwable);
    }

}
