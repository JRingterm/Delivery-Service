package com.example.deliver.domain.order.entity;

import com.example.deliver.domain.store.entity.Store;
import com.example.deliver.domain.user.entity.User;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order { //주문서 전체. (누가? 어느가게에? 총 가격? 주문 상태?)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    //양방향 연관관계. cascade를 ALL로 해서 Order에서 한 작업을 OrderItem에도 반영되도록.
    //orphanRemoval은 고아객체를 자동으로 지워주는 역할.
    //따라서 cascade로 부모(주문)에게 한 작업을 자식(주문 항목)에게 전파하고, orphanRemoval로 부모(주문)에게서 떨어져나간 자식(주문 항목)을 고아로 보고 DB에서도 삭제한다.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder
    public Order(User customer, Store store, Integer totalPrice, OrderStatus status) {
        this.customer = customer;
        this.store = store;
        this.totalPrice = totalPrice;
        this.status = status;
    }
    //양방향 연관관계의 연결을 한 번에 안전하게 처리하기 위해 만든 메소드.
    //JPA에서는 연관관계의 주인쪽인 OrderItem 쪽만 DB에 반영시킨다. 연관관계의 주인에 외래키(order_id)가 있기 때문에.
    //따라서 "객체의 상태"와 "DB 반영 상태"를 동시에 맞춰주기 위해 하나의 메소드로 연결한다.
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem); //Order 쪽에 orderItem 추가. 주문서가 주문 항목을 기억하게 한다.
        orderItem.setOrder(this); //OrderItem 쪽에 order 추가. 실제 DB에 반영되는 곳. 주문 항목이 자기 소속 주문을 기억하게 한다.
    }
    // orderItem.setOrder(order);만 하게되면, 외래키를 가진 orderItem DB에는 반영되지만, 메모리상의 자바 리스트(orderItems)에는 안넣어져서,
    // 자바 객체 입장에서는 order.getOrderItems() 호출 시, 결과가 기대와 다를 수 있다.

    // 반대로, order.getOrderItems().add(orderItem);만 하게되면, 자바 리스트에는 넣어지지만,
    // 연관관계의 주인인 OrderItem.order가 설정되지 않아서
    // DB 저장 시에 OrderItem 쪽의 order_id가 제대로 반영되지 않을 수 있다.

    // 따라서 이 둘을 충족시키기 위해, addOrderItem() 같은 메소드를 따로 만들어서,
    // 양쪽 값을 한번에 맞춰주는 것.
}