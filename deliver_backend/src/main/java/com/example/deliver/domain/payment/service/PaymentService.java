package com.example.deliver.domain.payment.service;

import com.example.deliver.domain.order.entity.Order;
import com.example.deliver.domain.payment.client.TossPaymentClient;
import com.example.deliver.domain.order.entity.OrderStatus;
import com.example.deliver.domain.order.repository.OrderRepository;
import com.example.deliver.domain.payment.dto.PaymentCreateRequest;
import com.example.deliver.domain.payment.dto.PaymentResponse;
import com.example.deliver.domain.payment.dto.TossPaymentConfirmRequest;
import com.example.deliver.domain.payment.dto.TossPaymentConfirmResponse;
import com.example.deliver.domain.payment.entity.Payment;
import com.example.deliver.domain.payment.entity.PaymentMethod;
import com.example.deliver.domain.payment.entity.PaymentStatus;
import com.example.deliver.domain.payment.repository.PaymentRepository;
import com.example.deliver.domain.user.entity.User;
import com.example.deliver.domain.user.entity.UserRole;
import com.example.deliver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final TossPaymentClient tossPaymentClient;

    @Transactional
    public PaymentResponse payOrder(String customerEmail, Long orderId, PaymentCreateRequest request) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        validateCustomerRole(customer);

        if (request.method() == PaymentMethod.TOSS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TOSS 결제는 Toss 승인 API를 사용해야 합니다.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 주문만 결제할 수 있습니다.");
        }

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CREATED 상태 주문만 결제할 수 있습니다.");
        }

        if (paymentRepository.existsByOrderIdAndStatus(orderId, PaymentStatus.PAID)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 결제된 주문입니다.");
        }

        if (!request.amount().equals(order.getTotalPrice())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "결제 금액이 주문 금액과 일치하지 않습니다.");
        }

        Payment payment = Payment.builder()
                .order(order)
                .customer(customer)
                .amount(request.amount())
                .method(request.method())
                .status(PaymentStatus.PAID)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        return PaymentResponse.toResponse(savedPayment);
    }

    //TOSS 결제
    @Transactional
    public PaymentResponse confirmTossPayment(String customerEmail, TossPaymentConfirmRequest request) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        validateCustomerRole(customer);

        Long orderId = parseOrderId(request.orderId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));
        //본인 주문인지 확인
        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 주문만 결제할 수 있습니다.");
        }
        //주문 상태 검증
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CREATED 상태 주문만 결제할 수 있습니다.");
        }
        //중복 결제 방지
        if (paymentRepository.existsByOrderIdAndStatus(orderId, PaymentStatus.PAID)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 결제된 주문입니다.");
        }
        //금액 위변조 방지. 가장 중요. 프론트단에서 조작 가능하기 때문에, DB의 실제 주문 금액과 클라이언트가 보낸 금액이 같은지 확인해야한다.
        if (!request.amount().equals(order.getTotalPrice())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "결제 금액이 주문 금액과 일치하지 않습니다.");
        }

        TossPaymentConfirmResponse tossResponse = tossPaymentClient.confirm(request);

        //TOSS는 DONE, WAITING_FOR_DEPOSIT, CANCELED, ABORTED 등의 상태가 올 수 있다.
        //실제로 결제가 완료된 건 DONE 뿐이다.
        if (!"DONE".equals(tossResponse.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Toss 결제가 완료 상태가 아닙니다.");
        }

        //프론트엔드에서 전달되는 amount 값은 조작될 수 있으므로, 백엔드에서 DB에 저장된 실제 주문 금액과 비교한다.
        if (!request.amount().equals(tossResponse.totalAmount())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Toss 승인 금액이 주문 금액과 일치하지 않습니다.");
        }

        //위 검증이 모두 완료된다면, PAID 상태로 DB에 저장.
        Payment payment = Payment.builder()
                .order(order)
                .customer(customer)
                .amount(request.amount())
                .method(PaymentMethod.TOSS)
                .status(PaymentStatus.PAID)
                .paymentKey(tossResponse.paymentKey())
                .pgOrderId(tossResponse.orderId())
                .approvedAt(parseApprovedAt(tossResponse.approvedAt()))
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        return PaymentResponse.toResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse findPaymentByOrder(String customerEmail, Long orderId) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        validateCustomerRole(customer);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));

        if (!order.getCustomer().getId().equals(customer.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 주문의 결제만 조회할 수 있습니다.");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));

        return PaymentResponse.toResponse(payment);
    }

    private Long parseOrderId(String orderId) {
        try {
            return Long.valueOf(orderId);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 주문 ID입니다.");
        }
    }

    private LocalDateTime parseApprovedAt(String approvedAt) {
        if (approvedAt == null || approvedAt.isBlank()) {
            return LocalDateTime.now();
        }

        try {
            return OffsetDateTime.parse(approvedAt).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return LocalDateTime.now();
        }
    }
    private void validateCustomerRole(User customer) {
        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "CUSTOMER만 결제를 수행할 수 있습니다.");
        }
    }
}