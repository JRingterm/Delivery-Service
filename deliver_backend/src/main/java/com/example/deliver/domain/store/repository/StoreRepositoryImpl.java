package com.example.deliver.domain.store.repository;

import com.example.deliver.domain.store.dto.StoreSearchCondition;
import com.example.deliver.domain.store.entity.QStore;
import com.example.deliver.domain.store.entity.Store;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QStore store = QStore.store;

    @Override
    public Page<Store> searchStores(StoreSearchCondition condition, Pageable pageable) {
        List<Store> content = queryFactory
                .selectFrom(store)
                .where(keywordContains(condition))
                .orderBy(getOrderSpecifiers(pageable).toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(store.count())
                .from(store)
                .where(keywordContains(condition))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    //키워드 검색조건
    private BooleanExpression keywordContains(StoreSearchCondition condition) {
        return condition != null && StringUtils.hasText(condition.keyword())
                ? store.name.contains(condition.keyword())
                .or(store.description.contains(condition.keyword()))
                : null;
    }

    //정렬 조건
    private List<OrderSpecifier<?>> getOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (pageable.getSort().isUnsorted()) {
            orders.add(store.id.desc());
            return orders;
        }

        for (Sort.Order sortOrder : pageable.getSort()) {
            boolean isAsc = sortOrder.isAscending();

            switch (sortOrder.getProperty()) {
                case "id" -> orders.add(isAsc ? store.id.asc() : store.id.desc());
                case "name" -> orders.add(isAsc ? store.name.asc() : store.name.desc());
                default -> orders.add(store.id.desc());
            }
        }

        return orders;
    }
}