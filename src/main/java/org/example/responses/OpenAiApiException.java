package org.example.responses;

public class OpenAiApiException extends RuntimeException {
    private final String errorCode;

    public OpenAiApiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}