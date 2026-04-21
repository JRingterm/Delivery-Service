package com.example.deliver.domain.menu.repository;

import com.example.deliver.domain.menu.entity.Menu;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    //메뉴 목록 조회
    List<Menu> findAllByStoreId(Long storeId);
    //메뉴 단건 조회
    Optional<Menu> findByIdAndStoreId(Long id, Long storeId);
}