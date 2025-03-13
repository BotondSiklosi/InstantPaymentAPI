package hu.java.instantpaymentapi.entity;

import hu.java.instantpaymentapi.model.enums.TransactionStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "sender_account_id")
    private String senderAccountId;

    @Column(name = "receiver_account_id")
    private String receiverAccountId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "message")
    private String message;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TransactionStatusEnum status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
