package com.example.deliver.domain.payment.service;

import com.example.deliver.domain.order.entity.Order;
import com.example.deliver.domain.order.entity.OrderStatus;
import com.example.deliver.domain.order.repository.OrderRepository;
import com.example.deliver.domain.payment.dto.PaymentCreateRequest;
import com.example.deliver.domain.payment.dto.PaymentResponse;
import com.example.deliver.domain.payment.entity.Payment;
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

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public PaymentResponse payOrder(String customerEmail, Long orderId, PaymentCreateRequest request) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        validateCustomerRole(customer);

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

    private void validateCustomerRole(User customer) {
        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "CUSTOMER만 결제를 수행할 수 있습니다.");
        }
    }
}