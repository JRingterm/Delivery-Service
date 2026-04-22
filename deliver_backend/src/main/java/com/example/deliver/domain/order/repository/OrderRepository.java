package com.example.deliver.domain.order.repository;

import com.example.deliver.domain.order.entity.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    //모든 주문 내역
    @EntityGraph(attributePaths = {"store", "orderItems", "orderItems.menu"})
    List<Order> findAllByCustomerId(Long customerId);

    //단건 주문 내역
    @EntityGraph(attributePaths = {"store", "orderItems", "orderItems.menu", "customer"})
    Optional<Order> findByIdAndCustomerId(Long id, Long customerId);
}