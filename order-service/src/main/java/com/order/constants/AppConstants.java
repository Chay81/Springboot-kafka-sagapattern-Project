package com.order.constants;

public class AppConstants {

    public static final String ORDER_RESPONSE = "Order placed and sent to Inventory Service!";

    public static final String ORDER_FAILED = "Product is out of stock";

    public static final String ORDER_DLQ = "Order failed to process in Inventory Service," +
            " after 2 retries, sent to dead letter topic";

    public static final String INVENTORY_NOT_AVAILABLE = "Stock not available. Returning ORDER_FAILED without saving order.";

    public static final String INCOMING_ORDER = "Incoming order request: {}";

    public static final String COMPENSATION = "🔁 Compensation triggered for order ID: {}";

    public static final String COMPENSATION_FAILED = "✅ Order status updated to ORDER_FAILED for order ID: {}";

    public static final String KAFKA_HOST_PORT_NUMBER = "localhost:9092";

    public static final String STOCK_AVAILABLE = "Stock is available. Proceeding to place order.";

}
