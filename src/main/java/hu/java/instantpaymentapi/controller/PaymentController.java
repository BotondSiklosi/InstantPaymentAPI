package hu.java.instantpaymentapi.controller;

import hu.java.instantpaymentapi.exception.PaymentFailedException;
import hu.java.instantpaymentapi.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/sendTransaction")
    public ResponseEntity<String> sendPayment(
            @RequestParam String senderAccountId,
            @RequestParam String receiverAccountId,
            @RequestParam BigDecimal amount,
            @RequestParam String currency) {

        try {
            Map<String, String> response = paymentService.processPayment(senderAccountId, receiverAccountId, amount, currency);

            if (response.get("status").equals("FAILED")) {
                throw new PaymentFailedException("Payment failed: " + response.get("message"));
            }

            return ResponseEntity.ok(response.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
