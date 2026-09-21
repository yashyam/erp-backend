package com.ginning.erp.common.exception;

public class ApplicationException extends RuntimeException {

    private final String errorCode;
    private final int httpStatusCode;

    public ApplicationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatusCode = 500;
    }

    public ApplicationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatusCode = 500;
    }

    public ApplicationException(String errorCode, String message, int httpStatusCode) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
    }

    public ApplicationException(String errorCode, String message, int httpStatusCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

}
