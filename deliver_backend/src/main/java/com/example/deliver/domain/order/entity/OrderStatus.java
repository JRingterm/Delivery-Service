package com.example.deliver.domain.order.entity;

public enum OrderStatus {
    CREATED,
    ACCEPTED,
    REJECTED,
    COOKING,
    READY_FOR_DELIVERY,
    DELIVERING,
    COMPLETED,
    CANCELED
}