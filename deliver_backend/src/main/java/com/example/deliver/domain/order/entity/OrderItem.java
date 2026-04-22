package com.example.deliver.domain.order.entity;

import com.example.deliver.domain.menu.entity.Menu;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem { //Menu와 Order를 다대다로 묶지 않기 위한 중간 엔티티. 1(Menu)-N(OrderItem)-1(Order)
                        //주문서 한 줄 한 줄. (어떤 메뉴? 몇 개?)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer price;

    @Builder
    public OrderItem(Menu menu, Integer quantity, Integer price) {
        this.menu = menu;
        this.quantity = quantity;
        this.price = price;
    }

    //연관관계의 주인 쪽 값을 설정하는 메소드. DB에 반영되는 FK(order_id)값을 설정한다.
    void setOrder(Order order) {
        this.order = order;
    }
}