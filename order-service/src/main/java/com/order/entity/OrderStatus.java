package com.order.entity;

public enum OrderStatus {
    ORDER_PLACED,
    INVENTORY_UPDATED,
    INVENTORY_FAILED,
    ORDER_FAILED,
    ROLLBACK_INITIATED
}
