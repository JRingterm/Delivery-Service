package com.example.deliver.domain.menu.service;

import com.example.deliver.domain.menu.dto.MenuCreateRequest;
import com.example.deliver.domain.menu.dto.MenuResponse;
import com.example.deliver.domain.menu.entity.Menu;
import com.example.deliver.domain.menu.repository.MenuRepository;
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
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    //점주가 메뉴 등록하는 기능.
    @Transactional
    public MenuResponse createMenu(Long storeId, MenuCreateRequest request, String requesterEmail) {
        //로그인한 사용자 찾기
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        //점주인지 권한 체크
        if (requester.getRole() != UserRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "점주(OWNER)만 메뉴를 등록할 수 있습니다.");
        }

        //가게 조회.
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다."));

        //점주 자신의 가게인지 체크.
        if (!store.getOwner().getId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 가게의 점주만 메뉴를 등록할 수 있습니다.");
        }
        //메뉴 생성 @Builder 활용.
        Menu menu = Menu.builder()
                .name(request.name())
                .price(request.price())
                .description(request.description())
                .store(store)
                .build();

        Menu savedMenu = menuRepository.save(menu);
        return MenuResponse.toResponse(savedMenu);
    }

    //메뉴 조회 기능
    @Transactional(readOnly = true)
    public List<MenuResponse> findMenusByStore(Long storeId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다.");
        }

        return menuRepository.findAllByStoreId(storeId).stream()
                .map(MenuResponse::toResponse)
                .toList();
    }
    //메뉴 단건 조회 기능
    @Transactional(readOnly = true)
    public MenuResponse findMenuByStore(Long storeId, Long menuId) {
        if (!storeRepository.existsById(storeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "가게를 찾을 수 없습니다.");
        }

        Menu menu = menuRepository.findByIdAndStoreId(menuId, storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."));

        return MenuResponse.toResponse(menu);
    }
}