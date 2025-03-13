package hu.java.instantpaymentapi.exception;

public class ConcurrentTransactionException extends RuntimeException {
    public ConcurrentTransactionException(String msg) {
        super(msg);
    }
}
