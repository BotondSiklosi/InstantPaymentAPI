package hu.java.instantpaymentapi.service;

import hu.java.instantpaymentapi.entity.Account;
import hu.java.instantpaymentapi.entity.Transaction;
import hu.java.instantpaymentapi.model.enums.TransactionStatusEnum;
import hu.java.instantpaymentapi.repository.AccountRepository;
import hu.java.instantpaymentapi.repository.TransactionRepository;
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
    public Map<String, String> processPayment(String senderAccountId, String receiverAccountId, BigDecimal amount, String currency) {

        Map<String, String> paymentProcessResponse = new HashMap<>();
        Transaction transaction;
        // checks for possible failures like Account not found or Insufficient balance
        // Creates a transaction and save it into the Transaction Table
        // Send notification using kafka producer
        // returns response JSON with the transaction information
        try {
            Account sender = accountRepository.findByAccountId(senderAccountId)
                    .orElseThrow(() -> new RuntimeException("Sender account not found: " + senderAccountId));

            if (amount.compareTo(sender.getBalance()) < 0) {
                throw new RuntimeException("Insufficient balance for: " + senderAccountId);
            }

            Account receiver = accountRepository.findByAccountId(receiverAccountId)
                    .orElseThrow(() -> new RuntimeException("Receiver account not found: " + receiverAccountId));

            sender.setBalance(sender.getBalance().subtract(amount));
            accountRepository.save(sender);
            receiver.setBalance(receiver.getBalance().add(amount));
            accountRepository.save(receiver);

            transaction = saveTransactionIntoDB(senderAccountId, receiverAccountId, amount, currency, TransactionStatusEnum.SUCCESS);

        } catch (RuntimeException e) {
            transaction = saveTransactionIntoDB(senderAccountId, receiverAccountId, amount, currency, TransactionStatusEnum.FAILED);
        }

        kafkaProducerService.sendMessage("transaction-notifications", transaction.getId().toString(), transaction.toString());

        paymentProcessResponse.put("transactionId", transaction.getId().toString());
        paymentProcessResponse.put("status", transaction.getStatus().name());
        paymentProcessResponse.put("message", transaction.getStatus().equals(TransactionStatusEnum.SUCCESS) ? "Payment processed successfully" : "Payment processing failed");

        return paymentProcessResponse;
    }

    private Transaction saveTransactionIntoDB(String senderAccountId, String receiverAccountId, BigDecimal amount, String currency, TransactionStatusEnum status) {
        Transaction transaction = Transaction.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .currency(currency)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
        return transaction;
    }


}
