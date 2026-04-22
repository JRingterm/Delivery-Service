package com.example.deliver.domain.order.service;

import com.example.deliver.domain.menu.entity.Menu;
import com.example.deliver.domain.menu.repository.MenuRepository;
import com.example.deliver.domain.order.dto.OrderCreateRequest;
import com.example.deliver.domain.order.dto.OrderItemRequest;
import com.example.deliver.domain.order.dto.OrderResponse;
import com.example.deliver.domain.order.entity.Order;
import com.example.deliver.domain.order.entity.OrderItem;
import com.example.deliver.domain.order.entity.OrderStatus;
import com.example.deliver.domain.order.repository.OrderRepository;
import com.example.deliver.domain.store.entity.Store;
import com.example.deliver.domain.store.repository.StoreRepository;
import com.example.deliver.domain.user.entity.User;
import com.example.deliver.domain.user.entity.UserRole;
import com.example.deliver.domain.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    //주문 생성
    @Transactional
    public OrderResponse createOrder(String customerEmail, OrderCreateRequest request) {
        //주문자 체크
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        //주문은 CUSTOMER만
        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "CUSTOMER만 주문할 수 있습니다.");
        }
        //주문한 가게 체크
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다."));

        //총 가격 계산.
        int totalPrice = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        //요청으로 들어온 주문 목록을 하나씩 체크
        for (OrderItemRequest itemRequest : request.items()) {
            //가게에 있는 메뉴인지 체크
            Menu menu = menuRepository.findById(itemRequest.menuId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."));
            //주문한 메뉴를 가진 가게가, 지금 주문하려는 가게와 다를 경우. (다른 가게 메뉴 섞임 방지)
            if (!menu.getStore().getId().equals(store.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "다른 가게 메뉴를 함께 주문할 수 없습니다.");
            }

            //모든 검증을 완료했다면, 주문한 메뉴의 총 가격 계산.
            totalPrice += menu.getPrice() * itemRequest.quantity();

            //OrderItem 생성.
            OrderItem orderItem = OrderItem.builder()
                    .menu(menu)
                    .quantity(itemRequest.quantity())
                    .price(menu.getPrice())
                    .build();
            orderItems.add(orderItem);
        }
        //Order 생성.
        Order order = Order.builder()
                .customer(customer)
                .store(store)
                .totalPrice(totalPrice)
                .status(OrderStatus.CREATED)
                .build();

        //Order와 OrderItem을 연결
        orderItems.forEach(order::addOrderItem);

        Order savedOrder = orderRepository.save(order);
        return OrderResponse.toResponse(savedOrder);
    }

    //나의 주문 전체 조회
    @Transactional(readOnly = true)
    public List<OrderResponse> findMyOrders(String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "CUSTOMER만 주문 목록을 조회할 수 있습니다.");
        }
        //주문 목록 조회 후, DTO 변환.
        return orderRepository.findAllByCustomerId(customer.getId()).stream()
                .map(OrderResponse::toResponse)
                .toList();
    }
    //나의 주문 단건 조회
    @Transactional(readOnly = true)
    public OrderResponse findMyOrder(String customerEmail, Long orderId) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "CUSTOMER만 주문을 조회할 수 있습니다.");
        }
        //A 고객이 B 고객의 주문내역을 보게될 경우도 고려하여, 404에러.
        //프론트에서 "로그인한 내 주문만 보여준다" 라는 건 보안이 아니라 UI 정책.
        //이런 작은 보안은 백엔드가 다 해야함.
        Order order = orderRepository.findByIdAndCustomerId(orderId, customer.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        return OrderResponse.toResponse(order);
    }
}