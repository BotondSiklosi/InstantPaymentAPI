package hu.java.instantpaymentapi.exception;

public class AccountBalanceException extends RuntimeException {
    public AccountBalanceException(String msg) {
        super(msg);
    }
}
