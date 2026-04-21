package com.example.deliver.domain.menu.controller;

import com.example.deliver.domain.menu.dto.MenuCreateRequest;
import com.example.deliver.domain.menu.dto.MenuResponse;
import com.example.deliver.domain.menu.service.MenuService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stores/{storeId}/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    //점주의 메뉴 생성.
    @PostMapping
    public ResponseEntity<MenuResponse> createMenu(
            @PathVariable Long storeId,
            @Valid @RequestBody MenuCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        System.out.println(">>> createMenu userDetails = " + (userDetails == null ? "null" : userDetails.getUsername()));
        MenuResponse response = menuService.createMenu(storeId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //메뉴 목록 전체조회
    @GetMapping
    public ResponseEntity<List<MenuResponse>> findMenusByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(menuService.findMenusByStore(storeId));
    }

    //메뉴 단건 조회
    @GetMapping("/{menuId}")
    public ResponseEntity<MenuResponse> findMenuByStore(
            @PathVariable Long storeId,
            @PathVariable Long menuId
    ) {
        return ResponseEntity.ok(menuService.findMenuByStore(storeId, menuId));
    }
}