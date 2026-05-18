package com.example.deliver.domain.order.repository;

import com.example.deliver.domain.order.dto.OrderSearchCondition;
import com.example.deliver.domain.order.entity.Order;
import com.example.deliver.domain.order.entity.QOrder;
import com.example.deliver.domain.order.entity.QOrderItem;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QOrder order = QOrder.order;
    private static final QOrderItem orderItem = QOrderItem.orderItem;

    //실제 조회 쿼리
    @Override
    public Page<Order> searchOwnerOrders(Long ownerId, OrderSearchCondition condition, Pageable pageable) {
        //Order는 OrderItem을 여러개 가질 수 있는 1:N 관계(OneToMany)라서, fetch join과 Pageable을 같이쓰면 페이징이 깨질 수 있다.
        //예를들어, 한 Order에 3개의 OrderItem이 있을 때, fetch join시 DB입장에서는 Order 1개가 아닌, row 3개로 보인다.
        //여기에 페이징을 바로 걸어버리면, 문제가 발생하는 것.
        //fetch join 대상이 ManyToOne인 Review와 Store는 row가 폭발하지 않으므로 조치하지 않음.

        //따라서 1차적으로, 조건에 맞는 Order ID만 가져와서 먼저 페이징해버린다.
        List<Long> orderIds = queryFactory
                .select(order.id)
                .from(order)
                .where(
                        order.store.owner.id.eq(ownerId), //자기 자신의 가게만 출력.
                        statusEq(condition),
                        totalPriceGoe(condition),
                        totalPriceLoe(condition)
                )
                .orderBy(getOrderSpecifiers(pageable).toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        //2차적으로, 페이징된 Order ID에 해당하는 주문들을 fetch join으로 다시 조회한다.
        List<Order> content = new ArrayList<>();
        if (!orderIds.isEmpty()) {
            List<Order> fetchedOrders = queryFactory
                    .selectFrom(order)
                    .distinct()
                    //성능 개선 fetch join
                    .leftJoin(order.store).fetchJoin()
                    .leftJoin(order.customer).fetchJoin()
                    .leftJoin(order.orderItems, orderItem).fetchJoin()
                    .leftJoin(orderItem.menu).fetchJoin()
                    .where(order.id.in(orderIds)) //SQL의 IN 조건은 순서 보장x
                    .fetch();

            //where order.id.in(orderIds)는 조회 순서를 보장하지 않을 수 있다.
            //그래서 처음 페이징된 orderIds 순서로 다시 content를 재정렬해준다.
            Map<Long, Order> orderMap = new LinkedHashMap<>();
            for (Order fetchedOrder : fetchedOrders) {
                orderMap.putIfAbsent(fetchedOrder.getId(), fetchedOrder); //같은 주문 ID가 여러번 나와도 처음 한번만 저장한다.
            }
            for (Long orderId : orderIds) {
                Order found = orderMap.get(orderId);
                if (found != null) {
                    content.add(found);
                }
            }
        }
        //count 쿼리
        Long total = queryFactory
                .select(order.count()) //검색 조건 쿼리의 결과에 대한 개수를 가져온다.
                .from(order)
                .where(
                        order.store.owner.id.eq(ownerId),
                        statusEq(condition),
                        totalPriceGoe(condition),
                        totalPriceLoe(condition)
                )
                .fetchOne();

        //조회결과, 페이징, 전체 개수를 PageImpl로 만들어 반환한다.
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    //상태 조건.
    private BooleanExpression statusEq(OrderSearchCondition condition) {
        return condition != null && condition.status() != null //요청에 status가 있으면 상태 조건을 추가한다.
                ? order.status.eq(condition.status())
                : null;
    }

    //최소금액 조건.
    private BooleanExpression totalPriceGoe(OrderSearchCondition condition) {
        return condition != null && condition.minTotalPrice() != null //요청에 있으면 추가.
                ? order.totalPrice.goe(condition.minTotalPrice())
                : null;
    }

    //최대금액 조건.
    private BooleanExpression totalPriceLoe(OrderSearchCondition condition) {
        return condition != null && condition.maxTotalPrice() != null //요청에 있으면 추가.
                ? order.totalPrice.loe(condition.maxTotalPrice())
                : null;
    }

    private List<OrderSpecifier<?>> getOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (pageable.getSort().isUnsorted()) {
            orders.add(order.id.desc()); //디폴트 내림차순
            return orders;
        }

        //PathBuilder 사용 시, 오류발생.
        //허용된 필드만 명시적으로 정렬하는 방식으로 변경.
        for (Sort.Order sortOrder : pageable.getSort()) {
            boolean ascending = sortOrder.isAscending();

            switch (sortOrder.getProperty()) {
                case "id" -> orders.add(ascending ? order.id.asc() : order.id.desc()); //문자열이 들어오면 직접 매핑. "id" -> order.id
                case "totalPrice" -> orders.add(ascending ? order.totalPrice.asc() : order.totalPrice.desc());
                case "status" -> orders.add(ascending ? order.status.asc() : order.status.desc());
                default -> orders.add(order.id.desc()); //매핑된 것 외의 문자열이 들어오면 id,desc 정렬.
            }
        }

        return orders;
    }
}