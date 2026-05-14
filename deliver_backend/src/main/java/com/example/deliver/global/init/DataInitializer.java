package com.example.deliver.global.init;

import com.example.deliver.domain.menu.entity.Menu;
import com.example.deliver.domain.menu.repository.MenuRepository;
import com.example.deliver.domain.order.entity.Order;
import com.example.deliver.domain.order.entity.OrderItem;
import com.example.deliver.domain.order.entity.OrderStatus;
import com.example.deliver.domain.order.repository.OrderRepository;
import com.example.deliver.domain.review.entity.Review;
import com.example.deliver.domain.review.repository.ReviewRepository;
import com.example.deliver.domain.store.entity.Store;
import com.example.deliver.domain.store.repository.StoreRepository;
import com.example.deliver.domain.user.entity.User;
import com.example.deliver.domain.user.entity.UserRole;
import com.example.deliver.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration //Spirng 설정 클래스 라는 의미.
@RequiredArgsConstructor
public class DataInitializer { //애플리케이션 실행 시 테스트 데이터 자동 생성.

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() { //CommandLineRunner는 Spring Boot 실행 직후 딱 한번만 실행되는 코드이다.
        return args -> { //실행하게 할 로직 작성
            if (userRepository.count() > 0) { //초기데이터 중복생성 방지.
                return;
            }

            User owner = userRepository.save(User.builder()
                    .email("owner1@test.com")
                    .password(passwordEncoder.encode("12345678"))
                    .nickname("owner1")
                    .role(UserRole.OWNER)
                    .build());

            User rider = userRepository.save(User.builder()
                    .email("rider@test.com")
                    .password(passwordEncoder.encode("12345678"))
                    .nickname("rider1")
                    .role(UserRole.RIDER)
                    .build());

            Store store = storeRepository.save(Store.builder()
                    .name("테스트 분식집")
                    .description("초기 데이터용 테스트 가게")
                    .owner(owner)
                    .build());

            Menu menu1 = menuRepository.save(Menu.builder()
                    .name("떡볶이")
                    .price(5000)
                    .description("매콤달콤 기본 떡볶이")
                    .store(store)
                    .build());

            Menu menu2 = menuRepository.save(Menu.builder()
                    .name("순대")
                    .price(4500)
                    .description("쫄깃한 순대")
                    .store(store)
                    .build());

            Menu menu3 = menuRepository.save(Menu.builder()
                    .name("튀김 모둠")
                    .price(6000)
                    .description("김말이/야채/오징어 튀김")
                    .store(store)
                    .build());

            //테스트를 위한 리뷰 7개 생성.
            List<Integer> ratings = List.of(5, 4, 5, 3, 4, 5, 2);
            List<String> contents = List.of(
                    "배달도 빠르고 음식이 정말 맛있어요!",
                    "무난하게 맛있고 양도 괜찮았습니다.",
                    "떡볶이가 특히 맛있었어요. 재주문 의사 있습니다.",
                    "맛은 괜찮았지만 배달이 조금 늦었어요.",
                    "순대와 튀김이 바삭하고 좋았습니다.",
                    "가성비 최고예요. 다음에도 주문할게요.",
                    "제 입맛에는 조금 짰지만 전체적으로 괜찮아요."
            );
            List<Menu> orderMenus = List.of(menu1, menu2, menu3, menu1, menu2, menu3, menu1);
            List<Integer> quantities = List.of(1, 2, 1, 3, 1, 2, 1);

            //1주문 1리뷰라 Customer 7명 생성.
            for (int i = 1; i <= 7; i++) {
                User customer = userRepository.save(User.builder()
                        .email("customer" + i + "@test.com")
                        .password(passwordEncoder.encode("12345678"))
                        .nickname("customer" + i)
                        .role(UserRole.CUSTOMER)
                        .build());

                Menu selectedMenu = orderMenus.get(i - 1);
                int quantity = quantities.get(i - 1);

                Order order = Order.builder()
                        .customer(customer)
                        .store(store)
                        .totalPrice(selectedMenu.getPrice() * quantity)
                        .status(OrderStatus.COMPLETED)
                        .build();

                OrderItem orderItem = OrderItem.builder()
                        .menu(selectedMenu)
                        .quantity(quantity)
                        .price(selectedMenu.getPrice())
                        .build();
                order.addOrderItem(orderItem);

                Order savedOrder = orderRepository.save(order);

                reviewRepository.save(Review.builder()
                        .order(savedOrder)
                        .store(store)
                        .customer(customer)
                        .rating(ratings.get(i - 1))
                        .content(contents.get(i - 1))
                        .build());
            }
        };
    }
}