package hu.java.instantpaymentapi.service;

import static org.junit.jupiter.api.Assertions.*;

import hu.java.instantpaymentapi.entity.Account;
import hu.java.instantpaymentapi.entity.Transaction;
import hu.java.instantpaymentapi.repository.AccountRepository;
import hu.java.instantpaymentapi.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaymentServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessPayment_Success() {
        String senderAccountId = "123";
        String receiverAccountId = "456";
        BigDecimal amount = BigDecimal.valueOf(100);
        String currency = "USD";

        Account sender = new Account(senderAccountId, BigDecimal.valueOf(1000), currency);
        Account receiver = new Account(receiverAccountId, BigDecimal.valueOf(500), currency);

        when(accountRepository.findByAccountId(senderAccountId)).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountId(receiverAccountId)).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        Map<String, String> result = paymentService.processPayment(senderAccountId, receiverAccountId, amount, currency);

        assertEquals("SUCCESS", result.get("status"));
        verify(accountRepository, times(1)).save(sender);
        verify(accountRepository, times(1)).save(receiver);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString());
    }

    @Test
    void testProcessPayment_InsufficientBalance() {
        String senderAccountId = "123";
        String receiverAccountId = "456";
        BigDecimal amount = BigDecimal.valueOf(1000);
        String currency = "USD";

        Account sender = new Account(senderAccountId, BigDecimal.valueOf(500), currency);

        when(accountRepository.findByAccountId(senderAccountId)).thenReturn(Optional.of(sender));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            paymentService.processPayment(senderAccountId, receiverAccountId, amount, currency);
        });

        assertEquals("Insufficient balance: " + senderAccountId, exception.getMessage());
    }
}