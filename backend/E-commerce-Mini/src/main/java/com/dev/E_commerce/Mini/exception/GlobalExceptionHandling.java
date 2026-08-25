package com.dev.E_commerce.Mini.exception;

import com.dev.E_commerce.Mini.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandling {
    private static final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlerAppException(AppException exception){
        ErrorCode errorCode = exception.getErrorCode();

        ApiResponse apiResponse = ApiResponse.builder()
                .code(errorCode.code)
                .message(errorCode.message)
                .build();
        return ResponseEntity
                .status(errorCode.statusCode)
                .body(apiResponse);
    }

    @ExceptionHandler(value = AuthorizationDeniedException.class)
    ResponseEntity<ApiResponse> handlerAccessDeniedException(){
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(errorCode.statusCode)
                .body(ApiResponse.builder()
                        .code(errorCode.code)
                        .message(errorCode.message)
                        .build());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlerMethodArgumentNotValidException(MethodArgumentNotValidException exception){
        String enumKey = exception.getBindingResult().getFieldError().getDefaultMessage();

        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String,Object> attributes = null;
        try {
            errorCode = ErrorCode.valueOf(enumKey);

            var constraintViolation =
                    exception.getBindingResult()
                            .getAllErrors()
                            .get(0)
                            .unwrap(ConstraintViolation.class);
            attributes = constraintViolation
                    .getConstraintDescriptor()
                    .getAttributes();

        }catch (IllegalArgumentException e){

        }

        ApiResponse apiResponse = new ApiResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(Objects.nonNull(attributes) ? mapAttribute(errorCode.getMessage(), attributes) : errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    private String mapAttribute(String message, Map<String, Object> attributes){
        String minValue = String.valueOf( attributes.get(MIN_ATTRIBUTE));
        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}
