package com.example.deliver.domain.order.dto;

import com.example.deliver.domain.order.entity.Order;
import com.example.deliver.domain.order.entity.OrderItem;
import com.example.deliver.domain.order.entity.OrderStatus;
import java.util.List;

public record OrderResponse(
        Long id,
        Long storeId,
        Integer totalPrice,
        OrderStatus status,
        List<OrderItemResponse> items
) {
    public static OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getStore().getId(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getOrderItems().stream()
                        .map(OrderItemResponse::toResponse)
                        .toList()
        );
    }
    //OrderItemResponse는 OrderResponse 안에서만 의미있는 DTO이기 때문에, 중첩레코드로 만듦. (주문 Response 안에 들어가는 주문 항목 Response)
    //이렇게 함으로써, 소속을 명확히 하고, DTO 파일의 수를 줄일 수 있다. 가독성도 좋다.
    //나중에 OrderItemResponse를 여러 곳에서 재사용한다면 따로 파일을 만드는 것이 좋다.
    public record OrderItemResponse(
            Long menuId,
            String menuName,
            Integer quantity,
            Integer price
    ) {
        public static OrderItemResponse toResponse(OrderItem orderItem) {
            return new OrderItemResponse(
                    orderItem.getMenu().getId(),
                    orderItem.getMenu().getName(),
                    orderItem.getQuantity(),
                    orderItem.getPrice()
            );
        }
    }
}