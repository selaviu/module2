package com.example.task2.exception.custom;

public class InvalidFileFormatException extends RuntimeException {
    public InvalidFileFormatException(String message){
        super(message); 
    }
}
