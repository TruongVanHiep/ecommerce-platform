package com.dev.E_commerce.Mini.exception;

public class AppException extends RuntimeException {
    private ErrorCode errorCode;
    public AppException(ErrorCode errorCode){
        super(errorCode.message);
        this.errorCode = errorCode;
    }
    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
