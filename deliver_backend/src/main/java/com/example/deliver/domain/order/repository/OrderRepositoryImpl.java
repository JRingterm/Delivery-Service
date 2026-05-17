package com.example.deliver.domain.order.repository;

import com.example.deliver.domain.order.dto.OrderSearchCondition;
import com.example.deliver.domain.order.entity.Order;
import com.example.deliver.domain.order.entity.QOrder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QOrder order = QOrder.order;


    //실제 조회 쿼리
    @Override
    public Page<Order> searchOwnerOrders(Long ownerId, OrderSearchCondition condition, Pageable pageable) {
        List<Order> content = queryFactory
                .selectFrom(order)
                .where(
                        order.store.owner.id.eq(ownerId), //자기 가게 주문만 조회.
                        statusEq(condition),              //상태 조건.
                        totalPriceGoe(condition),         //최소 금액 조건.
                        totalPriceLoe(condition)          //최대 금액 조건.
                )
                .orderBy(getOrderSpecifiers(pageable).toArray(OrderSpecifier[]::new))  //정렬.
                .offset(pageable.getOffset())             //페이징.
                .limit(pageable.getPageSize())
                .fetch();

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

        PathBuilder<Order> pathBuilder = new PathBuilder<>(Order.class, "order");

        for (Sort.Order sortOrder : pageable.getSort()) {
            com.querydsl.core.types.Order direction = sortOrder.isAscending()
                    ? com.querydsl.core.types.Order.ASC
                    : com.querydsl.core.types.Order.DESC;
            orders.add(new OrderSpecifier<>(
                    direction,
                    pathBuilder.getComparable(sortOrder.getProperty(), Comparable.class)
            ));
        }

        return orders;
    }
}