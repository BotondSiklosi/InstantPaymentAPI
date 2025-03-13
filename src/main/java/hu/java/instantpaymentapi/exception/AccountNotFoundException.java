package hu.java.instantpaymentapi.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String msg) {
        super(msg);
    }
}
