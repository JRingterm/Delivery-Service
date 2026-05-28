package com.example.deliver.domain.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.deliver.domain.menu.entity.Menu;
import com.example.deliver.domain.menu.repository.MenuRepository;
import com.example.deliver.domain.order.entity.Order;
import com.example.deliver.domain.order.entity.OrderItem;
import com.example.deliver.domain.order.entity.OrderStatus;
import com.example.deliver.domain.order.repository.OrderRepository;
import com.example.deliver.domain.payment.client.TossPaymentClient;
import com.example.deliver.domain.payment.dto.PaymentCreateRequest;
import com.example.deliver.domain.payment.dto.TossPaymentConfirmRequest;
import com.example.deliver.domain.payment.dto.TossPaymentConfirmResponse;
import com.example.deliver.domain.payment.entity.Payment;
import com.example.deliver.domain.payment.entity.PaymentMethod;
import com.example.deliver.domain.payment.entity.PaymentStatus;
import com.example.deliver.domain.payment.repository.PaymentRepository;
import com.example.deliver.domain.store.entity.Store;
import com.example.deliver.domain.store.repository.StoreRepository;
import com.example.deliver.domain.user.entity.User;
import com.example.deliver.domain.user.entity.UserRole;
import com.example.deliver.domain.user.repository.UserRepository;
import com.example.deliver.global.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc //MockMvc 자동 생성. 이걸로 GET / POST 요청을 가짜로 보낼 수 있다.
@ActiveProfiles("test")
@Transactional
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

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

    @MockBean //실제 TOSS 서버를 호출하는 것을 막는다.
    private TossPaymentClient tossPaymentClient;

    @Test
    void 결제_성공() throws Exception {
        Fixture fixture = createFixture();
        String token = bearerToken(fixture.customer.getEmail());
        PaymentCreateRequest request = new PaymentCreateRequest(fixture.order.getTotalPrice(), PaymentMethod.MOCK);

        mockMvc.perform(post("/api/orders/{orderId}/payments", fixture.order.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void 결제_조회_성공() throws Exception {
        Fixture fixture = createFixture();
        String token = bearerToken(fixture.customer.getEmail());

        paymentRepository.save(Payment.builder()
                .order(fixture.order)
                .customer(fixture.customer)
                .amount(fixture.order.getTotalPrice())
                .method(PaymentMethod.MOCK)
                .status(PaymentStatus.PAID)
                .build());

        mockMvc.perform(get("/api/orders/{orderId}/payment", fixture.order.getId())
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(fixture.order.getId()))
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void 인증없이_결제요청시_401() throws Exception {
        Fixture fixture = createFixture();
        PaymentCreateRequest request = new PaymentCreateRequest(fixture.order.getTotalPrice(), PaymentMethod.MOCK);

        mockMvc.perform(post("/api/orders/{orderId}/payments", fixture.order.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void OWNER_토큰으로_결제요청시_403() throws Exception {
        Fixture fixture = createFixture();
        String ownerToken = bearerToken(fixture.owner.getEmail());
        PaymentCreateRequest request = new PaymentCreateRequest(fixture.order.getTotalPrice(), PaymentMethod.MOCK);

        mockMvc.perform(post("/api/orders/{orderId}/payments", fixture.order.getId())
                        .header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 중복_결제요청시_409() throws Exception {
        Fixture fixture = createFixture();
        String token = bearerToken(fixture.customer.getEmail());

        paymentRepository.save(Payment.builder()
                .order(fixture.order)
                .customer(fixture.customer)
                .amount(fixture.order.getTotalPrice())
                .method(PaymentMethod.MOCK)
                .status(PaymentStatus.PAID)
                .build());

        PaymentCreateRequest request = new PaymentCreateRequest(fixture.order.getTotalPrice(), PaymentMethod.CARD);

        mockMvc.perform(post("/api/orders/{orderId}/payments", fixture.order.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void 금액_불일치시_400() throws Exception {
        Fixture fixture = createFixture();
        String token = bearerToken(fixture.customer.getEmail());
        PaymentCreateRequest request = new PaymentCreateRequest(fixture.order.getTotalPrice() + 500, PaymentMethod.MOCK);

        mockMvc.perform(post("/api/orders/{orderId}/payments", fixture.order.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void Toss_결제_승인_성공() throws Exception {
        Fixture fixture = createFixture();
        String token = bearerToken(fixture.customer.getEmail());
        TossPaymentConfirmRequest request = new TossPaymentConfirmRequest(
                "payment-key-" + fixture.order.getId(),
                fixture.order.getId().toString(),
                fixture.order.getTotalPrice()
        );
        //when()으로 가짜 응답을 지정.
        when(tossPaymentClient.confirm(request)).thenReturn(new TossPaymentConfirmResponse(
                request.paymentKey(),
                request.orderId(),
                "카드",
                request.amount(),
                "DONE",
                "2026-05-28T10:15:30+09:00"
        ));

        mockMvc.perform(post("/api/payments/toss/confirm")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(fixture.order.getId()))
                .andExpect(jsonPath("$.method").value("TOSS"))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.pgOrderId").value(fixture.order.getId().toString()))
                .andExpect(jsonPath("$.approvedAt").exists());
    }

    @Test
    void Toss_결제_금액_불일치시_400() throws Exception {
        Fixture fixture = createFixture();
        String token = bearerToken(fixture.customer.getEmail());
        TossPaymentConfirmRequest request = new TossPaymentConfirmRequest(
                "payment-key-" + fixture.order.getId(),
                fixture.order.getId().toString(),
                fixture.order.getTotalPrice() + 500
        );

        mockMvc.perform(post("/api/payments/toss/confirm")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        //verify(... never())로 금액 검증 실패 시, 아예 PG 호출 자체를 안했는지 검증하는 테스트.
        verify(tossPaymentClient, never()).confirm(any(TossPaymentConfirmRequest.class));
    }

    @Test
    void Mock_결제_API에서_TOSS_method_요청시_400() throws Exception {
        Fixture fixture = createFixture();
        String token = bearerToken(fixture.customer.getEmail());
        PaymentCreateRequest request = new PaymentCreateRequest(fixture.order.getTotalPrice(), PaymentMethod.TOSS);

        mockMvc.perform(post("/api/orders/{orderId}/payments", fixture.order.getId())
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private String bearerToken(String email) {
        return "Bearer " + jwtTokenProvider.createAccessToken(email);
    }

    private Fixture createFixture() { //테스트 데이터 생성
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