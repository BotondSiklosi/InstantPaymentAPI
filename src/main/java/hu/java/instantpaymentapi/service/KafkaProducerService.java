package hu.java.instantpaymentapi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Retryable(backoff = @Backoff(delay = 1000))
    public void sendMessage(String topic, String TransactionId, String message){
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, TransactionId, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("Message sent successfully: " + result.getRecordMetadata().offset());
            } else {
                System.err.println("Failed to send message: " + ex.getMessage());
            }
        });
    }
}
