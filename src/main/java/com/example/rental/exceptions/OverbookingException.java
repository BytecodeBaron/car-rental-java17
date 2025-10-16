package com.example.rental.exceptions;

public class OverbookingException extends RuntimeException {
    public OverbookingException(String message) {
        super(message);
    }
}
