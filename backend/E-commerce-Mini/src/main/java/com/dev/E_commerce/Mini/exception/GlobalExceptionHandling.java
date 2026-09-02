package com.dev.E_commerce.Mini.exception;

import com.dev.E_commerce.Mini.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;
import java.util.Objects;

@Slf4j
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
        // getFieldError() có thể null khi ràng buộc đặt ở mức class (không gắn field),
        // trước đây trường hợp đó gây NPE ngay trong handler.
        var fieldError = exception.getBindingResult().getFieldError();
        String enumKey = fieldError != null
                ? fieldError.getDefaultMessage()
                : ErrorCode.INVALID_INPUT.name();

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

    /**
     * Vi phạm ràng buộc trên @RequestParam/@PathVariable (các controller có @Validated).
     * Trước đây không có handler nên mọi vi phạm kiểu này trả về 500.
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    ResponseEntity<ApiResponse> handlerConstraintViolationException(ConstraintViolationException exception){
        ErrorCode errorCode = exception.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> resolveErrorCode(violation.getMessage()))
                .orElse(ErrorCode.INVALID_INPUT);
        return buildResponse(errorCode);
    }

    /**
     * JSON sai cú pháp, sai kiểu dữ liệu, hoặc giá trị enum không tồn tại
     * (vd status: "ABC"). Mặc định Spring trả 500 với format khác hẳn API.
     */
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse> handlerHttpMessageNotReadableException(){
        return buildResponse(ErrorCode.MALFORMED_REQUEST);
    }

    /** Request body vượt quá giới hạn cấu hình ở application.yaml. */
    @ExceptionHandler(value = MaxUploadSizeExceededException.class)
    ResponseEntity<ApiResponse> handlerMaxUploadSizeExceededException(){
        return buildResponse(ErrorCode.PAYLOAD_TOO_LARGE);
    }

    /**
     * Vi phạm ràng buộc ở tầng DB (chuỗi dài hơn cột, trùng unique...).
     * Trả 400 thay vì 500 và KHÔNG lộ thông tin schema ra ngoài.
     */
    @ExceptionHandler(value = DataIntegrityViolationException.class)
    ResponseEntity<ApiResponse> handlerDataIntegrityViolationException(DataIntegrityViolationException exception){
        log.warn("Vi phạm ràng buộc dữ liệu: {}", exception.getMostSpecificCause().getMessage());
        return buildResponse(ErrorCode.INVALID_INPUT);
    }

    /**
     * Lưới an toàn cuối cùng: mọi lỗi chưa được xử lý. Ghi log đầy đủ ở server
     * nhưng chỉ trả về thông báo chung chung cho client (không lộ stack trace).
     */
    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlerUncaughtException(Exception exception){
        log.error("Lỗi chưa được xử lý", exception);
        return buildResponse(ErrorCode.USER_UNCATEGORIZED);
    }

    private ErrorCode resolveErrorCode(String enumKey){
        try {
            return ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e){
            return ErrorCode.INVALID_INPUT;
        }
    }

    private ResponseEntity<ApiResponse> buildResponse(ErrorCode errorCode){
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }

    private String mapAttribute(String message, Map<String, Object> attributes){
        String minValue = String.valueOf( attributes.get(MIN_ATTRIBUTE));
        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}
