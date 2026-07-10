package org.example.currencyexchange.exception;

public class CircuitBreakerException extends RuntimeException{
    public CircuitBreakerException(String msg) {
        super(msg);
    }
}
