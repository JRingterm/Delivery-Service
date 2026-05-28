package com.example.deliver.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.deliver.domain.menu.entity.Menu;
import com.example.deliver.domain.menu.repository.MenuRepository;
import com.example.deliver.domain.order.dto.OrderResponse;
import com.example.deliver.domain.order.entity.Order;
import com.example.deliver.domain.order.entity.OrderItem;
import com.example.deliver.domain.order.entity.OrderStatus;
import com.example.deliver.domain.order.repository.OrderRepository;
import com.example.deliver.domain.payment.entity.Payment;
import com.example.deliver.domain.payment.entity.PaymentMethod;
import com.example.deliver.domain.payment.entity.PaymentStatus;
import com.example.deliver.domain.payment.repository.PaymentRepository;
import com.example.deliver.domain.store.entity.Store;
import com.example.deliver.domain.store.repository.StoreRepository;
import com.example.deliver.domain.user.entity.User;
import com.example.deliver.domain.user.entity.UserRole;
import com.example.deliver.domain.user.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void CREATED_상태_주문_취소_성공() {
        Fixture fixture = createFixture(OrderStatus.CREATED);

        OrderResponse response = orderService.cancelMyOrder(fixture.customer.getEmail(), fixture.order.getId());

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    void ACCEPTED_상태_주문_취소_성공() {
        Fixture fixture = createFixture(OrderStatus.ACCEPTED);

        OrderResponse response = orderService.cancelMyOrder(fixture.customer.getEmail(), fixture.order.getId());

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    void PAID_결제가_있는_주문_취소시_결제도_CANCELED() {
        Fixture fixture = createFixture(OrderStatus.CREATED);

        Payment payment = paymentRepository.save(Payment.builder()
                .order(fixture.order)
                .customer(fixture.customer)
                .amount(fixture.order.getTotalPrice())
                .method(PaymentMethod.MOCK)
                .status(PaymentStatus.PAID)
                .build());

        orderService.cancelMyOrder(fixture.customer.getEmail(), fixture.order.getId());

        Payment found = paymentRepository.findById(payment.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(PaymentStatus.CANCELED);
    }

    @Test
    void 결제정보_없는_주문도_취소_가능() {
        Fixture fixture = createFixture(OrderStatus.CREATED);

        OrderResponse response = orderService.cancelMyOrder(fixture.customer.getEmail(), fixture.order.getId());

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    void CUSTOMER가_아닌_사용자_취소시_403() {
        Fixture fixture = createFixture(OrderStatus.CREATED);

        assertThatThrownBy(() -> orderService.cancelMyOrder(fixture.owner.getEmail(), fixture.order.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void 본인_주문이_아닌_주문_취소시_404() {
        Fixture fixture = createFixture(OrderStatus.CREATED);
        String unique = UUID.randomUUID().toString().substring(0, 8);
        User anotherCustomer = userRepository.save(User.builder()
                .email("another-customer-" + unique + "@test.com")
                .password("pw")
                .nickname("another-customer-" + unique)
                .role(UserRole.CUSTOMER)
                .build());

        assertThatThrownBy(() -> orderService.cancelMyOrder(anotherCustomer.getEmail(), fixture.order.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void COOKING_상태_주문_취소시_400() {
        Fixture fixture = createFixture(OrderStatus.COOKING);

        assertThatThrownBy(() -> orderService.cancelMyOrder(fixture.customer.getEmail(), fixture.order.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void COMPLETED_상태_주문_취소시_400() {
        Fixture fixture = createFixture(OrderStatus.COMPLETED);

        assertThatThrownBy(() -> orderService.cancelMyOrder(fixture.customer.getEmail(), fixture.order.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void 이미_CANCELED_상태_주문_취소시_400() {
        Fixture fixture = createFixture(OrderStatus.CANCELED);

        assertThatThrownBy(() -> orderService.cancelMyOrder(fixture.customer.getEmail(), fixture.order.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void READY_FOR_DELIVERY_주문을_RIDER가_픽업하면_DELIVERING으로_변경된다() {
        PickupFixture fixture = createPickupFixture(OrderStatus.READY_FOR_DELIVERY);

        OrderResponse response = orderService.pickupOrder(fixture.rider.getEmail(), fixture.order.getId());

        assertThat(response.status()).isEqualTo(OrderStatus.DELIVERING);
    }

    @Test
    void 픽업_성공시_riderId가_응답에_포함된다() {
        PickupFixture fixture = createPickupFixture(OrderStatus.READY_FOR_DELIVERY);

        OrderResponse response = orderService.pickupOrder(fixture.rider.getEmail(), fixture.order.getId());

        assertThat(response.riderId()).isEqualTo(fixture.rider.getId());
    }

    @Test
    void CUSTOMER가_픽업시도하면_403() {
        PickupFixture fixture = createPickupFixture(OrderStatus.READY_FOR_DELIVERY);

        assertThatThrownBy(() -> orderService.pickupOrder(fixture.customer.getEmail(), fixture.order.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void OWNER가_픽업시도하면_403() {
        PickupFixture fixture = createPickupFixture(OrderStatus.READY_FOR_DELIVERY);

        assertThatThrownBy(() -> orderService.pickupOrder(fixture.owner.getEmail(), fixture.order.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void CREATED_상태_주문_픽업시_400() {
        PickupFixture fixture = createPickupFixture(OrderStatus.CREATED);

        assertThatThrownBy(() -> orderService.pickupOrder(fixture.rider.getEmail(), fixture.order.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void 이미_DELIVERING_상태인_주문_픽업시_400() {
        PickupFixture fixture = createPickupFixture(OrderStatus.DELIVERING);

        assertThatThrownBy(() -> orderService.pickupOrder(fixture.rider.getEmail(), fixture.order.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void 이미_rider가_배정된_주문_픽업시_409() {
        PickupFixture fixture = createPickupFixture(OrderStatus.READY_FOR_DELIVERY);
        orderService.pickupOrder(fixture.rider.getEmail(), fixture.order.getId());

        String unique = UUID.randomUUID().toString().substring(0, 8);
        User anotherRider = userRepository.save(User.builder()
                .email("rider2-" + unique + "@test.com")
                .password("pw")
                .nickname("rider2-" + unique)
                .role(UserRole.RIDER)
                .build());

        assertThatThrownBy(() -> orderService.pickupOrder(anotherRider.getEmail(), fixture.order.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    private Fixture createFixture(OrderStatus orderStatus) {
        String unique = UUID.randomUUID().toString().substring(0, 8);

        User owner = userRepository.save(User.builder()
                .email("owner-" + unique + "@test.com")
                .password("pw")
                .nickname("owner-" + unique)
                .role(UserRole.OWNER)
                .build());

        User customer = userRepository.save(User.builder()
                .email("customer-" + unique + "@test.com")
                .password("pw")
                .nickname("customer-" + unique)
                .role(UserRole.CUSTOMER)
                .build());

        Store store = storeRepository.save(Store.builder()
                .name("테스트 가게")
                .description("설명")
                .owner(owner)
                .build());

        Menu menu = menuRepository.save(Menu.builder()
                .name("김밥")
                .price(5000)
                .description("기본 김밥")
                .store(store)
                .build());

        Order order = Order.builder()
                .customer(customer)
                .store(store)
                .totalPrice(10000)
                .status(orderStatus)
                .build();

        order.addOrderItem(OrderItem.builder()
                .menu(menu)
                .quantity(2)
                .price(5000)
                .build());

        return new Fixture(owner, customer, orderRepository.save(order));
    }

    private PickupFixture createPickupFixture(OrderStatus orderStatus) {
        String unique = UUID.randomUUID().toString().substring(0, 8);

        User owner = userRepository.save(User.builder()
                .email("owner-p-" + unique + "@test.com")
                .password("pw")
                .nickname("owner-p-" + unique)
                .role(UserRole.OWNER)
                .build());

        User customer = userRepository.save(User.builder()
                .email("customer-p-" + unique + "@test.com")
                .password("pw")
                .nickname("customer-p-" + unique)
                .role(UserRole.CUSTOMER)
                .build());

        User rider = userRepository.save(User.builder()
                .email("rider-" + unique + "@test.com")
                .password("pw")
                .nickname("rider-" + unique)
                .role(UserRole.RIDER)
                .build());

        Store store = storeRepository.save(Store.builder()
                .name("픽업 테스트 가게")
                .description("설명")
                .owner(owner)
                .build());

        Menu menu = menuRepository.save(Menu.builder()
                .name("라면")
                .price(7000)
                .description("기본 라면")
                .store(store)
                .build());

        Order order = Order.builder()
                .customer(customer)
                .store(store)
                .totalPrice(7000)
                .status(orderStatus)
                .build();

        order.addOrderItem(OrderItem.builder()
                .menu(menu)
                .quantity(1)
                .price(7000)
                .build());

        return new PickupFixture(owner, customer, rider, orderRepository.save(order));
    }

    private record Fixture(User owner, User customer, Order order) {
    }

    private record PickupFixture(User owner, User customer, User rider, Order order) {
    }
}