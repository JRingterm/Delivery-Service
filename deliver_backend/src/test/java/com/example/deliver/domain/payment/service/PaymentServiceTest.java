package com.example.deliver.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.deliver.domain.menu.entity.Menu;
import com.example.deliver.domain.menu.repository.MenuRepository;
import com.example.deliver.domain.order.entity.Order;
import com.example.deliver.domain.order.entity.OrderItem;
import com.example.deliver.domain.order.entity.OrderStatus;
import com.example.deliver.domain.order.repository.OrderRepository;
import com.example.deliver.domain.payment.dto.PaymentCreateRequest;
import com.example.deliver.domain.payment.dto.PaymentResponse;
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
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

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
    void 결제_성공() {
        //given 테스트에 필요한 상황 준비
        Fixture fixture = createFixture();

        PaymentCreateRequest request = new PaymentCreateRequest(fixture.order.getTotalPrice(), PaymentMethod.MOCK);

        //when 실제 테스트할 메서드 실행
        PaymentResponse response = paymentService.payOrder(fixture.customer.getEmail(), fixture.order.getId(), request);

        //then 결과 검증
        assertThat(response.status()).isEqualTo(PaymentStatus.PAID);
        assertThat(response.amount()).isEqualTo(fixture.order.getTotalPrice());
        assertThat(response.orderId()).isEqualTo(fixture.order.getId());
    }

    @Test
    void 중복_결제_방지() {
        Fixture fixture = createFixture();

        paymentRepository.save(Payment.builder()
                .order(fixture.order)
                .customer(fixture.customer)
                .amount(fixture.order.getTotalPrice())
                .method(PaymentMethod.MOCK)
                .status(PaymentStatus.PAID)
                .build());

        PaymentCreateRequest request = new PaymentCreateRequest(fixture.order.getTotalPrice(), PaymentMethod.CARD);

        //예외가 발생해야 정상인 케이스 테스트.
        assertThatThrownBy(() -> paymentService.payOrder(fixture.customer.getEmail(), fixture.order.getId(), request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void 결제_금액_불일치() {
        Fixture fixture = createFixture();

        PaymentCreateRequest request = new PaymentCreateRequest(fixture.order.getTotalPrice() + 100, PaymentMethod.MOCK);

        assertThatThrownBy(() -> paymentService.payOrder(fixture.customer.getEmail(), fixture.order.getId(), request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void CUSTOMER가_아닌_사용자_결제_시도() {
        Fixture fixture = createFixture();

        PaymentCreateRequest request = new PaymentCreateRequest(fixture.order.getTotalPrice(), PaymentMethod.MOCK);

        assertThatThrownBy(() -> paymentService.payOrder(fixture.owner.getEmail(), fixture.order.getId(), request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void 본인_주문이_아닌_주문_결제_시도() {
        Fixture fixture = createFixture();

        String unique = UUID.randomUUID().toString().substring(0, 8);
        User anotherCustomer = userRepository.save(User.builder()
                .email("another-customer-" + unique + "@test.com")
                .password("pw")
                .nickname("another-customer-" + unique)
                .role(UserRole.CUSTOMER)
                .build());

        PaymentCreateRequest request = new PaymentCreateRequest(fixture.order.getTotalPrice(), PaymentMethod.MOCK);

        assertThatThrownBy(() -> paymentService.payOrder(anotherCustomer.getEmail(), fixture.order.getId(), request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void CREATED가_아닌_주문은_결제할_수_없다() {
        Fixture fixture = createFixture();
        fixture.order.updateStatus(OrderStatus.ACCEPTED);

        PaymentCreateRequest request = new PaymentCreateRequest(fixture.order.getTotalPrice(), PaymentMethod.MOCK);

        assertThatThrownBy(() -> paymentService.payOrder(fixture.customer.getEmail(), fixture.order.getId(), request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void 결제_정보_조회_성공() {
        Fixture fixture = createFixture();

        Payment saved = paymentRepository.save(Payment.builder()
                .order(fixture.order)
                .customer(fixture.customer)
                .amount(fixture.order.getTotalPrice())
                .method(PaymentMethod.MOCK)
                .status(PaymentStatus.PAID)
                .build());

        PaymentResponse response = paymentService.findPaymentByOrder(fixture.customer.getEmail(), fixture.order.getId());

        assertThat(response.id()).isEqualTo(saved.getId());
        assertThat(response.orderId()).isEqualTo(fixture.order.getId());
        assertThat(response.customerId()).isEqualTo(fixture.customer.getId());
        assertThat(response.status()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void 결제_정보_없는_주문_조회() {
        Fixture fixture = createFixture();

        assertThatThrownBy(() -> paymentService.findPaymentByOrder(fixture.customer.getEmail(), fixture.order.getId()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Fixture createFixture() {
        //테스트 email의 중복 가능성을 없애기 위해, 매번 고유값을 넣기위한 변수.
        //닉네임은 길이제한이 있으므로, substring으로 일부만 고유값 넣기.
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
                .status(OrderStatus.CREATED)
                .build();

        order.addOrderItem(OrderItem.builder()
                .menu(menu)
                .quantity(2)
                .price(5000)
                .build());

        Order savedOrder = orderRepository.save(order);

        return new Fixture(owner, customer, savedOrder);
    }

    private record Fixture(User owner, User customer, Order order) {
    }
}