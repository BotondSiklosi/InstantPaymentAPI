package hu.java.instantpaymentapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Transactional
    public String processPayment(String senderAccountId, String receiverAccountId, BigDecimal amount, String currency) {
        return null;
    }


}
