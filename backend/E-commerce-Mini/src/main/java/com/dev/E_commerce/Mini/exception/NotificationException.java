package com.dev.E_commerce.Mini.exception;

/**
 * Thrown when sending a notification (email, SMS, ...) fails.
 * Swap the cause for the real provider's exception (e.g. MailException) once
 * a real notification channel is wired up — @Retryable already targets this type.
 */
public class NotificationException extends RuntimeException {
    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
