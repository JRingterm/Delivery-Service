package com.example.deliver.domain.store.dto;

import com.example.deliver.domain.store.entity.Store;

public record StoreResponse(
        Long id,
        String name,
        String description,
        Long ownerId,
        String ownerNickname
) {
    public static StoreResponse toResponse(Store store) {
        return new StoreResponse(
                store.getId(),
                store.getName(),
                store.getDescription(),
                store.getOwner().getId(),
                store.getOwner().getNickname()
        );
    }
}