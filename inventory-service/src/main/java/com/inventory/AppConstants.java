package com.inventory;

public class AppConstants {

    public static final String DLQ = "❌ Simulated failure for retry/DLQ test";

    public static final String INVENTORY_UPDATED = "✅ Inventory updated, sent status INVENTORY_UPDATED";

    public static final String INVENTORY_FAILED = "❌ Inventory update failed, sent status ORDER_FAILED to compensation topic";

    public static final String INVENTORY_EXCEPTION = "🔥 Exception while processing inventory, sent to compensation: {}";

    public static final String UPDATING_STOCK = "Updating stock for product: {}, quantity: {}, price: {}";

    public static final String INVENTORY_NOT_FOUND = "❌ Inventory not found for brand: {}, model: {}";

    public static final String INSUFFICIENT_STOCK = "❌ Insufficient stock for product: {}, available: {}, requested: {}";

    public static final String STOCK_UPDATED = "✅ Stock updated successfully for brand: {}, model: {}";

    public static final String GET_STOCK = "Looking up inventory for brand: {} and model: {}";

    public static final String NO_INVENTORY = "No inventory found for the product : ";

    public static final String CREATE_INVENTORY = "Creating or updating inventory for brand: {}, model: {}";
}
