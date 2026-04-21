package com.example.deliver.domain.store.controller;

import com.example.deliver.domain.store.dto.StoreCreateRequest;
import com.example.deliver.domain.store.dto.StoreResponse;
import com.example.deliver.domain.store.service.StoreService;
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
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    //가게 등록
    @PostMapping
    public ResponseEntity<StoreResponse> createStore(
            @Valid @RequestBody StoreCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 정보가 필요합니다.");
        }

        StoreResponse response = storeService.createStore(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //가게 전체 조회
    @GetMapping
    public ResponseEntity<List<StoreResponse>> findStores() {
        return ResponseEntity.ok(storeService.findStores());
    }
    //가게 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<StoreResponse> findStore(@PathVariable Long id) {
        return ResponseEntity.ok(storeService.findStore(id));
    }
}