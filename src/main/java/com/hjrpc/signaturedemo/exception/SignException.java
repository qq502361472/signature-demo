package com.hjrpc.signaturedemo.exception;

import lombok.Data;

@Data
public class SignException extends RuntimeException{
    private String code;

    public SignException() {
        super();
    }

    public SignException(String message) {
        super(message);
    }

    public SignException(String code, String message) {
        super(message);
        this.code = code;
    }

    public SignException(String message, Throwable cause) {
        super(message, cause);
    }

    public SignException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public SignException(Throwable cause) {
        super(cause);
    }
}
