package hu.java.instantpaymentapi.service;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    private final String topic = "test-topic";
    private final String message = "test-message";
    private final String transactionId = "123";

    @Test
    public void testSendMessage_Success() {

        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition(topic, 0), 0, 0, 0, 0L, 0, 0);
        SendResult<String, String> sendResult = new SendResult<>(null, recordMetadata);
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(sendResult);

        when(kafkaTemplate.send(topic, transactionId, message)).thenReturn(future);

        kafkaProducerService.sendMessage(topic, transactionId, message);

        verify(kafkaTemplate, times(1)).send(topic, transactionId, message);
    }

    @Test
    public void testSendMessage_Failure() {

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(outContent));

        when(kafkaTemplate.send(topic, transactionId, message))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka send failed")));

        kafkaProducerService.sendMessage(topic, transactionId, message);

        String consoleOutput = outContent.toString();
        assertThat(consoleOutput).contains("Failed to send message:");
        verify(kafkaTemplate, times(1)).send(topic, transactionId, message);

    }
}