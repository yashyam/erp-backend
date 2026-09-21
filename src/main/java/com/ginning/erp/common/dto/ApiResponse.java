package com.ginning.erp.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String message;
    private ErrorDetails error;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public ApiResponse(boolean success, ErrorDetails error) {
        this.success = success;
        this.error = error;
    }

    // Success response builder
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }

    // Error response builder
    public static <T> ApiResponse<T> error(String code, String message, int httpStatus) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = new ErrorDetails(code, message, httpStatus);
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ErrorDetails getError() {
        return error;
    }

    public void setError(ErrorDetails error) {
        this.error = error;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {
        private String code;
        private String message;
        private int httpStatus;

        public ErrorDetails() {
        }

        public ErrorDetails(String code, String message, int httpStatus) {
            this.code = code;
            this.message = message;
            this.httpStatus = httpStatus;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getHttpStatus() {
            return httpStatus;
        }

        public void setHttpStatus(int httpStatus) {
            this.httpStatus = httpStatus;
        }
    }

}
