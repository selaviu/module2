package com.example.task2.exception.custom;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, UUID id) {
        super(String.format("%s with id '%s' not found.", 
                resource, id.toString()));
    }

    public ResourceNotFoundException(String resource, String ids) {
        super(String.format("%s with id '%s' not found.", 
                resource, ids.toString()));
    }
}
