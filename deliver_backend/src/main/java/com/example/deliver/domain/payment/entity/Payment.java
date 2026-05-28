package com.example.deliver.domain.payment.entity;

import com.example.deliver.domain.order.entity.Order;
import com.example.deliver.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    private String paymentKey;

    private String pgOrderId;

    private LocalDateTime approvedAt;

    @Builder
    public Payment(
            Order order,
            User customer,
            Integer amount,
            PaymentStatus status,
            PaymentMethod method,
            String paymentKey,  //PG 결제 고유 식별값.
            String pgOrderId,   //PG 측 주문 번호.
            LocalDateTime approvedAt    //PG 승인 시각.
    ) {
        this.order = order;
        this.customer = customer;
        this.amount = amount;
        this.status = status;
        this.method = method;
        this.paymentKey = paymentKey;
        this.pgOrderId = pgOrderId;
        this.approvedAt = approvedAt;
    }

    //결제 상태를 취소 상태로 바꾸기
    public void cancel() {
        if (this.status != PaymentStatus.CANCELED) {
            this.status = PaymentStatus.CANCELED;
        }
    }
}