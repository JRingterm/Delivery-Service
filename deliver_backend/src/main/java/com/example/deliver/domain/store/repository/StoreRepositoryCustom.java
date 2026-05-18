package com.example.deliver.domain.store.repository;

import com.example.deliver.domain.store.dto.StoreSearchCondition;
import com.example.deliver.domain.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreRepositoryCustom {
    //Querydsl 추가
    Page<Store> searchStores(StoreSearchCondition condition, Pageable pageable);
}