package com.dev.E_commerce.Mini.controller;

import com.dev.E_commerce.Mini.dto.request.SepayWebhookRequest;
import com.dev.E_commerce.Mini.service.SepayService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Nơi SePay gọi tới mỗi khi tài khoản ngân hàng có biến động số dư.
 *
 * <p>Không bọc trong {@code ApiResponse} như các endpoint khác: SePay chỉ coi
 * là thành công khi nhận đúng HTTP 200/201 kèm thân {@code {"success": true}}.
 * Sai định dạng là nó gửi lại tới 7 lần trong 5 tiếng.
 */
@Slf4j
@RestController
@RequestMapping("/api/payments/sepay")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SepayWebhookController {

    SepayService sepayService;

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> webhook(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
            @RequestBody SepayWebhookRequest request) {

        if (!sepayService.isAuthorized(authorization)) {
            log.warn("Webhook SePay bị từ chối: sai hoặc thiếu API key");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false));
        }

        sepayService.handleWebhook(request);
        // Trả success cho cả trường hợp IGNORED: giao dịch không liên quan đơn
        // nào vẫn là webhook hợp lệ, báo lỗi thì SePay gửi lại vô ích.
        return ResponseEntity.ok(Map.of("success", true));
    }
}
