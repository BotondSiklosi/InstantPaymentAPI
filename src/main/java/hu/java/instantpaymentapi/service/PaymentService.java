package hu.java.instantpaymentapi.service;

import hu.java.instantpaymentapi.entity.Account;
import hu.java.instantpaymentapi.entity.Transaction;
import hu.java.instantpaymentapi.exception.AccountBalanceException;
import hu.java.instantpaymentapi.exception.AccountNotFoundException;
import hu.java.instantpaymentapi.exception.ConcurrentTransactionException;
import hu.java.instantpaymentapi.model.enums.TransactionStatusEnum;
import hu.java.instantpaymentapi.repository.AccountRepository;
import hu.java.instantpaymentapi.repository.TransactionRepository;
import jakarta.persistence.PessimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AccountRepository accountRepository;

    private final TransactionRepository transactionRepository;

    private final KafkaProducerService kafkaProducerService;

    @Transactional
    public Map<String, String> processPayment(String senderAccountId, String receiverAccountId, BigDecimal amount, String currency, String message) throws ConcurrentTransactionException {

        Map<String, String> paymentProcessResponse = new HashMap<>();
        Transaction transaction;
        // checks for possible failures like Account not found or Insufficient balance
        // Creates a transaction and save it into the Transaction Table
        // Send notification using kafka producer
        // returns response JSON with the transaction information
        try {
            Account sender = accountRepository.findByAccountIdForUpdate(senderAccountId)
                    .orElseThrow(() -> new AccountNotFoundException("Sender account not found: " + senderAccountId));

            if (amount.compareTo(sender.getBalance()) > 0) {
                throw new AccountBalanceException("Insufficient balance for: " + senderAccountId);
            }

            Account receiver = accountRepository.findByAccountIdForUpdate(receiverAccountId)
                    .orElseThrow(() -> new AccountNotFoundException("Receiver account not found: " + receiverAccountId));

            sender.setBalance(sender.getBalance().subtract(amount));
            accountRepository.save(sender);
            receiver.setBalance(receiver.getBalance().add(amount));
            accountRepository.save(receiver);

            transaction = saveTransactionIntoDB(senderAccountId, receiverAccountId, amount, currency, TransactionStatusEnum.SUCCESS, message);

        } catch (PessimisticLockException e) {
            throw new ConcurrentTransactionException("Transaction failed due to concurrent update.");
        } catch (RuntimeException e) {
            transaction = saveTransactionIntoDB(senderAccountId, receiverAccountId, amount, currency, TransactionStatusEnum.FAILED, message);
        }

        if (transaction.getStatus().equals(TransactionStatusEnum.SUCCESS)) {
            kafkaProducerService.sendMessage("transaction-notifications", message, transaction.getId().toString());
        }

        paymentProcessResponse.put("transactionId", transaction.getId().toString());
        paymentProcessResponse.put("status", transaction.getStatus().name());
        paymentProcessResponse.put("message", transaction.getStatus().equals(TransactionStatusEnum.SUCCESS) ? "Payment processed successfully" : "Payment processing failed");

        return paymentProcessResponse;
    }

    private Transaction saveTransactionIntoDB(String senderAccountId, String receiverAccountId, BigDecimal amount, String currency, TransactionStatusEnum status, String message) {
        Transaction transaction = Transaction.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .currency(currency)
                .status(status)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }


}
