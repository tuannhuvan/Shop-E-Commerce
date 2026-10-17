package org.example.demofullstackspringboot.exception;

public class StockEmptyException extends RuntimeException {
    public StockEmptyException(String message) {
        super(message);
    }
}
