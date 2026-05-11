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
    //Owner 주문 전체 조회
    @Transactional(readOnly = true)
    public List<OrderResponse> findOwnerOrders(String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        validateOwnerRole(owner); //Owner인지 확인.

        //Owner가 가진 Store의 주문 목록 가져오기.
        return orderRepository.findAllByStoreOwnerId(owner.getId()).stream()
                .map(OrderResponse::toResponse)
                .toList();
    }
    //Owner 주문 단건 조회
    @Transactional(readOnly = true)
    public OrderResponse findOwnerOrder(String ownerEmail, Long orderId) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        validateOwnerRole(owner);

        //orderId + ownerId로 주문 조회. 따라서 다른 점주의 주문은 보이지 않는다.
        Order order = orderRepository.findByIdAndStoreOwnerId(orderId, owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        return OrderResponse.toResponse(order);
    }
    //Owner 주문 수락
    @Transactional
    public OrderResponse acceptOrder(String ownerEmail, Long orderId) { //CREATED 상태만 ACCEPTED로 변경 가능.
        return updateOwnerOrderStatus(ownerEmail, orderId, OrderStatus.CREATED, OrderStatus.ACCEPTED);
    }
    //Owner 주문 거절
    @Transactional
    public OrderResponse rejectOrder(String ownerEmail, Long orderId) { //CREATED 상태만 REJECTED로 변경 가능.
        return updateOwnerOrderStatus(ownerEmail, orderId, OrderStatus.CREATED, OrderStatus.REJECTED);
    }
    //Owner 조리 시작
    @Transactional
    public OrderResponse startCooking(String ownerEmail, Long orderId) { //ACCEPTED 상태만 COOKING 가능.
        return updateOwnerOrderStatus(ownerEmail, orderId, OrderStatus.ACCEPTED, OrderStatus.COOKING);
    }
    //Owner 배달 준비 완료
    @Transactional
    public OrderResponse markReadyForDelivery(String ownerEmail, Long orderId) { //COOKING 상태만 READY_FOR_DELIVERY 가능.
        return updateOwnerOrderStatus(ownerEmail, orderId, OrderStatus.COOKING, OrderStatus.READY_FOR_DELIVERY);
    }
    //Rider 픽업
    @Transactional
    public OrderResponse pickupOrder(String riderEmail, Long orderId) {
        User rider = userRepository.findByEmail(riderEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        //Rider인지 확인.
        if (rider.getRole() != UserRole.RIDER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "RIDER만 배달 상태를 변경할 수 있습니다.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));
        //READY_FOR_DELIVERY 상태인 주문만 픽업 가능.
        if (order.getStatus() != OrderStatus.READY_FOR_DELIVERY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "READY_FOR_DELIVERY 상태 주문만 픽업할 수 있습니다.");
        }

        order.updateStatus(OrderStatus.DELIVERING);
        return OrderResponse.toResponse(order);
    }
    //Rider 배달 완료
    @Transactional
    public OrderResponse completeDelivery(String riderEmail, Long orderId) {
        User rider = userRepository.findByEmail(riderEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        //Rider인지 확인.
        if (rider.getRole() != UserRole.RIDER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "RIDER만 배달 상태를 변경할 수 있습니다.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        //DELIVERING 상태인 주문만 배달 완료 가능.
        if (order.getStatus() != OrderStatus.DELIVERING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DELIVERING 상태 주문만 배달 완료할 수 있습니다.");
        }

        order.updateStatus(OrderStatus.COMPLETED);
        return OrderResponse.toResponse(order);
    }

    //각 주문 상태에서 다음 단계로 갈때마다 검사해줘야 하는 것을 하나로 통합.
    private OrderResponse updateOwnerOrderStatus(String ownerEmail, Long orderId, OrderStatus expectedStatus, OrderStatus targetStatus) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        validateOwnerRole(owner);

        //Owner 가게의 주문인지 확인.
        Order order = orderRepository.findByIdAndStoreOwnerId(orderId, owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        //Order의 현재 상태 검사. ACCEPTED 상태가 아닌데 COOKING을 요청하는 등. 잘못된 상태변경을 잡아냄.
        if (order.getStatus() != expectedStatus) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 주문 상태 변경 요청입니다.");
        }

        //상태 변경 후 반환.
        order.updateStatus(targetStatus);
        return OrderResponse.toResponse(order);
    }

    //Owner 검증 메소드. 중복해서 사용하므로 따로 메소드로 분리.
    private void validateOwnerRole(User owner) {
        if (owner.getRole() != UserRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "OWNER만 주문을 관리할 수 있습니다.");
        }
    }
}