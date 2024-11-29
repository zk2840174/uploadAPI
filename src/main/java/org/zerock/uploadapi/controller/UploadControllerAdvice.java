package org.zerock.uploadapi.controller;


import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.zerock.uploadapi.exception.UploadException;

@RestControllerAdvice
@Log4j2
public class UploadControllerAdvice {

    @ExceptionHandler(UploadException.class)
    public ResponseEntity<String> handleUploadException(UploadException e) {

        log.error("upload exception......................");

        return ResponseEntity.status(500).body(e.getMessage());
    }
}
