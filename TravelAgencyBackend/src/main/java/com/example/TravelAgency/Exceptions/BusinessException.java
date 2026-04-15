// BusinessException.java
package com.example.TravelAgency.Exceptions;

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}