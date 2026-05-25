package com.example.deliver.domain.payment.repository;

import com.example.deliver.domain.payment.entity.Payment;
import com.example.deliver.domain.payment.entity.PaymentStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    //결제 완료된 주문인지 확인.
    boolean existsByOrderIdAndStatus(Long orderId, PaymentStatus status);

    //주문 기준으로 결제 정보 조회.
    @EntityGraph(attributePaths = {"order", "customer"})
    Optional<Payment> findByOrderId(Long orderId);
}