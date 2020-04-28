package com.polytech.dictionaryapi.controller;

import com.polytech.dictionaryapi.DictionaryApiApplication;
import com.polytech.dictionaryapi.exception.BadResourceException;
import com.polytech.dictionaryapi.exception.ResourceAlreadyExistsException;
import com.polytech.dictionaryapi.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class DictionaryExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(DictionaryApiApplication.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    private ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
        logger.debug("ResourceNotFoundException caught: ", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Message", ex.getMessage()).build();
    }

    @ExceptionHandler(BadResourceException.class)
    private ResponseEntity<Object> handleBadResource(BadResourceException ex) {
        logger.debug("BadResourceException caught: ", ex);
        return  ResponseEntity.status(HttpStatus.BAD_REQUEST).header("Message", ex.getMessage()).build();
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    private ResponseEntity<Object> handleAlreadyExists(ResourceAlreadyExistsException ex) {
        logger.debug("ResourceAlreadyExistsException caught: ", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).header("Message", ex.getMessage()).build();
    }

    @ExceptionHandler(Exception.class)
    private void defaultExceptionHandler(Exception ex) {
        logger.debug("Exception caught: ", ex);
    }
}
