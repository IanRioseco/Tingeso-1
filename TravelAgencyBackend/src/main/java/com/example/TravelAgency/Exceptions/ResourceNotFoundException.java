// ResourceNotFoundException.java
package com.example.TravelAgency.Exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}