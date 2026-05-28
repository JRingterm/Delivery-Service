package com.example.deliver.domain.order.repository;

import com.example.deliver.domain.order.entity.Order;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//Querydsl 적용.
public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {

    //@EntityGraph를 사용하여, 조회시 연관 객체를 미리 가져오도록 한다. (기본 LAZY 로딩 이므로, N+1 문제보완)
    //모든 주문 내역
    @EntityGraph(attributePaths = {"store", "orderItems", "orderItems.menu"})
    List<Order> findAllByCustomerId(Long customerId);

    //단건 주문 내역. orderId만 알면, A가 B의 주문을 조회할 수 있으므로, orderId만 비교하지 않고, CustomerId 까지 비교한다.
    @EntityGraph(attributePaths = {"store", "orderItems", "orderItems.menu", "customer"})
    Optional<Order> findByIdAndCustomerId(Long id, Long customerId);

    //가게의 Owner가 가진 모든 주문
    @EntityGraph(attributePaths = {"store", "customer", "orderItems", "orderItems.menu"})
    List<Order> findAllByStoreOwnerId(Long ownerId);

    //가게의 Owner가 가진 단건 주문. orderId만 알면, A가 B의 주문을 조회할 수 있으므로, OwnerId 까지 비교한다.
    @EntityGraph(attributePaths = {"store", "customer", "orderItems", "orderItems.menu"})
    Optional<Order> findByIdAndStoreOwnerId(Long id, Long ownerId);

    @EntityGraph(attributePaths = {"store", "rider", "orderItems", "orderItems.menu"})
    @Lock(LockModeType.PESSIMISTIC_WRITE) //한 주문에 라이더가 2명 배정되는 문제를 막기 위한 쓰기 락.
    @Query("select o from Order o where o.id = :orderId")
    Optional<Order> findByIdForUpdate(@Param("orderId") Long orderId); //수정 목적의 조회(주문 조회 + 쓰기 락)
}