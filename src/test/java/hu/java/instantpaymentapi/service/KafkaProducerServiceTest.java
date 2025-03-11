package hu.java.instantpaymentapi.service;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    @Test
    public void testSendMessage_Success() throws Exception {
        String topic = "test-topic";
        String message = "test-message";

        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition(topic, 0), 0, 0, 0, 0L, 0, 0);
        SendResult<String, String> sendResult = new SendResult<>(null, recordMetadata);
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(sendResult);

        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        kafkaProducerService.sendMessage(topic, message);

        verify(kafkaTemplate, times(1)).send(topic, message);

        String logs = outputStream.toString();
        assertTrue(logs.contains("Message sent successfully"));
    }

    @Test
    public void testSendMessage_Failure() throws Exception {
        String topic = "test-topic";
        String message = "test-message";

        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Failed to send message"));
        when(kafkaTemplate.send(topic, message)).thenReturn(future);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(outputStream));

        try {
            kafkaProducerService.sendMessage(topic, message);

            verify(kafkaTemplate, times(1)).send(topic, message);

            String logs = outputStream.toString();
            assertTrue(logs.contains("Failed to send message"), "Logs should contain failure message");
        } finally {
            System.setErr(originalErr);
        }
    }
}