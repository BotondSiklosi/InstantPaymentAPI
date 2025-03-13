package hu.java.instantpaymentapi.service;

import static org.junit.jupiter.api.Assertions.*;

import hu.java.instantpaymentapi.entity.Account;
import hu.java.instantpaymentapi.entity.Transaction;
import hu.java.instantpaymentapi.model.enums.TransactionStatusEnum;
import hu.java.instantpaymentapi.repository.AccountRepository;
import hu.java.instantpaymentapi.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private final String senderAccountId = "123";
    private final String receiverAccountId = "456";
    private final String currency = "USD";
    private final String transactionMessage = "Sending some money!";
    private final Transaction transaction = Transaction.builder().id(123123L).status(TransactionStatusEnum.SUCCESS).currency("USD").message(transactionMessage).build();

    @Test
    void testProcessPayment_Success() {
        BigDecimal amount = BigDecimal.valueOf(100);

        Account sender = new Account(senderAccountId, BigDecimal.valueOf(1000), currency);
        Account receiver = new Account(receiverAccountId, BigDecimal.valueOf(500), currency);

        when(accountRepository.findByAccountIdForUpdate(senderAccountId)).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountIdForUpdate(receiverAccountId)).thenReturn(Optional.of(receiver));
        doNothing().when(kafkaProducerService).sendMessage(any(String.class), any(String.class), any(String.class));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Map<String, String> result = paymentService.processPayment(senderAccountId, receiverAccountId, amount, currency, transactionMessage);

        assertEquals("SUCCESS", result.get("status"));
        verify(accountRepository, times(1)).save(sender);
        verify(accountRepository, times(1)).save(receiver);
        verify(kafkaProducerService, times(1)).sendMessage(any(String.class), any(String.class), any(String.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testProcessPayment_FailedWithBalanceException() {
        BigDecimal amount = BigDecimal.valueOf(1000);
        transaction.setStatus(TransactionStatusEnum.FAILED);

        Account sender = new Account(senderAccountId, BigDecimal.valueOf(600), currency);

        when(accountRepository.findByAccountIdForUpdate(senderAccountId)).thenReturn(Optional.of(sender));
        doNothing().when(kafkaProducerService).sendMessage(any(String.class), any(String.class), any(String.class));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Map<String, String> result = paymentService.processPayment(senderAccountId, receiverAccountId, amount, currency, transactionMessage);

        assertEquals("FAILED", result.get("status"));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testProcessPayment_FailedWithAccountNotFoundException() {
        BigDecimal amount = BigDecimal.valueOf(100);
        transaction.setStatus(TransactionStatusEnum.FAILED);

        Account sender = new Account(senderAccountId, BigDecimal.valueOf(600), currency);

        when(accountRepository.findByAccountIdForUpdate(senderAccountId)).thenReturn(Optional.of(sender));
        when(accountRepository.findByAccountIdForUpdate(receiverAccountId)).thenReturn(Optional.empty());
        doNothing().when(kafkaProducerService).sendMessage(any(String.class), any(String.class), any(String.class));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        Map<String, String> result = paymentService.processPayment(senderAccountId, receiverAccountId, amount, currency, transactionMessage);

        assertEquals("FAILED", result.get("status"));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}