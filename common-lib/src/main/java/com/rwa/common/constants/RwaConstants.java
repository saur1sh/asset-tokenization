package com.rwa.common.constants;

/**
 * Centralized constants for the RWA Tokenization Engine.
 * Following Sonar best practices for utility classes.
 */
public final class RwaConstants {

    // Kafka Topics
    public static final String TOPIC_TRADE_INITIATED = "trade-events";
    public static final String TOPIC_TRADE_FAILED = "trade-failed-events";
    public static final String TOPIC_WALLET_EVENTS = "wallet-events";
    public static final String TOPIC_INVENTORY_EVENTS = "inventory-events";
    public static final String TOPIC_PRICE_EVENTS = "price-events";
    public static final String TOPIC_TRADE_MATCHES = "trade-matches";
    public static final String TOPIC_COMPLIANCE_STATUS = "compliance-status";

    // Aggregate Types
    public static final String AGGREGATE_ASSET = "Asset";
    public static final String AGGREGATE_WALLET = "Wallet";
    public static final String AGGREGATE_TRADE = "Trade";
    public static final String TOPIC_YIELD_EVENTS = "yield-events";

    // Kafka Consumer Groups
    public static final String GROUP_ASSET_TRADE = "asset-trade-group";
    public static final String GROUP_ASSET_PRICE = "asset-price-group";
    public static final String GROUP_WALLET_TRADE = "wallet-trade-group";
    public static final String GROUP_WALLET_YIELD = "wallet-yield-group";
    public static final String GROUP_SETTLEMENT_SERVICE = "settlement-service-group";
    public static final String GROUP_ORDER_SERVICE = "order-service-group";
    public static final String GROUP_PRICING_SERVICE = "pricing-service-group";
    public static final String GROUP_WALLET_SERVICE = "wallet-service-group";

    private RwaConstants() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
