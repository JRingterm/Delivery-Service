
package com.example.deliver.domain.order.repository;

import com.example.deliver.domain.order.dto.OrderSearchCondition;
import com.example.deliver.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {

    //복잡한 검색 조건을 위해 만든 Repository
    Page<Order> searchOwnerOrders(Long ownerId, OrderSearchCondition condition, Pageable pageable);
}