package net.froihofer.dsfinance.bank.exception;

/**
 * Exception thrown when a customer doesn't own enough shares
 * to execute a sell operation.
 */
public class InsufficientSharesException extends Exception {
    
    public InsufficientSharesException(String message) {
        super(message);
    }
    
    public InsufficientSharesException(String message, Throwable cause) {
        super(message, cause);
    }
}
