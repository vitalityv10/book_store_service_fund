package com.epam.rd.autocode.spring.project.exception;

import lombok.Data;

import java.util.Date;

@Data
public class AuthException extends RuntimeException {

    private String message;
    private int status;
    private Date timestamp;

    public AuthException(String message, int status) {
        this.message = message;
        this.status = status;
        this.timestamp = new Date();
    }

}
