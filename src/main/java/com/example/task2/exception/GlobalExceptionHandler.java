package com.example.task2.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import com.example.task2.exception.custom.DuplicateNameException;
import com.example.task2.exception.custom.FileUploadProcessingException;
import com.example.task2.exception.custom.InvalidFileFormatException;
import com.example.task2.exception.custom.ResourceNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateNameException(
            DuplicateNameException ex, WebRequest request) {

        HttpStatus status = HttpStatus.CONFLICT;
        
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                ex.getMessage()
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        HttpStatus status = HttpStatus.NOT_FOUND;
        
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                ex.getMessage()
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        String detailedMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                detailedMessage
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler({
        InvalidFileFormatException.class,
        FileUploadProcessingException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleFileProcessingExceptions(
            RuntimeException ex, WebRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;
        
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                ex.getMessage()
        );

        return new ResponseEntity<>(errorResponse, status);
    }
}
