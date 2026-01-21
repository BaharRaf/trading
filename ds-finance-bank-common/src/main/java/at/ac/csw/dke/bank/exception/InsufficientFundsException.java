package at.ac.csw.dke.bank.exception;

/**
 * Exception thrown when the bank doesn't have enough trading budget
 * to execute a buy operation.
 */
public class InsufficientFundsException extends Exception {
    
    public InsufficientFundsException(String message) {
        super(message);
    }
    
    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }
}
