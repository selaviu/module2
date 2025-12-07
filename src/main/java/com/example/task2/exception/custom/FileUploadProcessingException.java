package com.example.task2.exception.custom;

public class FileUploadProcessingException extends RuntimeException {
    public FileUploadProcessingException(String message){ 
        super(message); 
    }
}
