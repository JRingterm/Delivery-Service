package com.example.deliver.domain.menu.dto;

import com.example.deliver.domain.menu.entity.Menu;

public record MenuResponse(
        Long id,
        String name,
        Integer price,
        String description,
        Long storeId
) {
    public static MenuResponse toResponse(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getDescription(),
                menu.getStore().getId()
        );
    }
}