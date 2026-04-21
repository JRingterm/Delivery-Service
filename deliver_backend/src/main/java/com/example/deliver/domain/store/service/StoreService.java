package com.example.deliver.domain.store.service;

import com.example.deliver.domain.store.dto.StoreCreateRequest;
import com.example.deliver.domain.store.dto.StoreResponse;
import com.example.deliver.domain.store.entity.Store;
import com.example.deliver.domain.store.repository.StoreRepository;
import com.example.deliver.domain.user.entity.User;
import com.example.deliver.domain.user.entity.UserRole;
import com.example.deliver.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    //점주의 가게 등록
    @Transactional
    public StoreResponse createStore(StoreCreateRequest request, String ownerEmail) {
        //로그인한 사용자 조회
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        //점주인지 권한 체크
        if (owner.getRole() != UserRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "점주(OWNER)만 가게를 생성할 수 있습니다.");
        }
        //가게 생성
        Store store = Store.builder()
                .name(request.name())
                .description(request.description())
                .owner(owner)
                .build();

        Store savedStore = storeRepository.save(store);
        return StoreResponse.toResponse(savedStore);
    }

    //가게 전체 조회
    @Transactional(readOnly = true)
    public List<StoreResponse> findStores() {
        return storeRepository.findAll().stream()
                .map(StoreResponse::toResponse)
                .toList();
    }
    //가게 단건 조회
    @Transactional(readOnly = true)
    public StoreResponse findStore(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다."));
        return StoreResponse.toResponse(store);
    }
}