package com.example.task2.exception.custom;

public class DuplicateNameException extends RuntimeException {

    public DuplicateNameException(String resource, String resourceName) {
        super(String.format("%s with name %s already exists.", 
                resource, resourceName));
    }
}
