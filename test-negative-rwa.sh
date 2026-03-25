#!/bin/bash

# Configuration
ASSET_URL="http://localhost:8081"
WALLET_URL="http://localhost:8082"
SETTLEMENT_URL="http://localhost:8083"

function wait_for_saga() {
    local saga_id=$1
    local max_attempts=10
    local attempt=1
    while [ $attempt -le $max_attempts ]; do
        status=$(curl -s "$SETTLEMENT_URL/api/settlement/$saga_id" | jq -r .status)
        if [ "$status" != "PENDING" ]; then
            echo "$status"
            return
        fi
        sleep 2
        attempt=$((attempt + 1))
    done
    echo "TIMEOUT"
}

echo "--------------------------------------------------"
echo "Starting Negative Test Suite for RWA Saga"
echo "--------------------------------------------------"

# 1. Setup User
USER_ID=$(uuidgen)
echo "Creating Wallet for $USER_ID with $1000 balance..."
curl -s -X POST "$WALLET_URL/api/wallets" \
     -H "Content-Type: application/json" \
     -d "{\"userId\": \"$USER_ID\", \"initialBalance\": 1000, \"currency\": \"USD\"}"
echo -e "\nVerifying KYC for user..."
curl -s -X POST "$WALLET_URL/api/wallets/$USER_ID/kyc" \
     -H "Content-Type: application/json" \
     -d "{\"status\": \"VERIFIED\"}"
echo -e "\n"

# 2. Setup Assets
echo "Creating Asset A (Platinum) with 100 total fractions..."
ASSET_A_ID=$(curl -s -X POST "$ASSET_URL/api/assets" \
     -H "Content-Type: application/json" \
     -d "{\"type\": \"Platinum\", \"totalFractions\": 100}" | jq -r .id)
echo "Asset A ID: $ASSET_A_ID"

echo "Creating Asset B (Silver) with 1000 total fractions..."
ASSET_B_ID=$(curl -s -X POST "$ASSET_URL/api/assets" \
     -H "Content-Type: application/json" \
     -d "{\"type\": \"Silver\", \"totalFractions\": 1000}" | jq -r .id)
echo "Asset B ID: $ASSET_B_ID"
echo -e "\n"

# Scenario A: Insufficient Inventory
echo "[TEST] Scenario A: Insufficient Inventory (Requesting 200 fractions from 100 total)"
TRADE_A=$(curl -s -X POST "$SETTLEMENT_URL/api/settlement/trade" \
     -H "Content-Type: application/json" \
     -d "{\"buyerId\": \"$USER_ID\", \"assetId\": \"$ASSET_A_ID\", \"fractions\": 200, \"priceAmount\": 500}")
T_ID_A=$(echo $TRADE_A | jq -r .id)
echo "Trade initiated: $T_ID_A"
echo "Final Saga Status for Scenario A: $(wait_for_saga $T_ID_A)"
echo "Verifying Wallet Balance (Should be 1000):"
curl -s "$WALLET_URL/api/wallets/$USER_ID" | jq .balance
echo -e "\n"

# Scenario B: Insufficient Funds
echo "[TEST] Scenario B: Insufficient Funds (Requesting $2000 trade from $1000 balance)"
TRADE_B=$(curl -s -X POST "$SETTLEMENT_URL/api/settlement/trade" \
     -H "Content-Type: application/json" \
     -d "{\"buyerId\": \"$USER_ID\", \"assetId\": \"$ASSET_B_ID\", \"fractions\": 10, \"priceAmount\": 2000}")
T_ID_B=$(echo $TRADE_B | jq -r .id)
echo "Trade initiated: $T_ID_B"
echo "Final Saga Status for Scenario B: $(wait_for_saga $T_ID_B)"
# Note: In a real test we'd check if asset fractions were returned to 1000.
echo -e "\n"

# Scenario C: Non-KYC User
echo "[TEST] Scenario C: Non-KYC User"
USER_C=$(uuidgen)
curl -s -X POST "$WALLET_URL/api/wallets" \
     -H "Content-Type: application/json" \
     -d "{\"userId\": \"$USER_C\", \"initialBalance\": 1000, \"currency\": \"USD\"}"

TRADE_C=$(curl -s -X POST "$SETTLEMENT_URL/api/settlement/trade" \
     -H "Content-Type: application/json" \
     -d "{\"buyerId\": \"$USER_C\", \"assetId\": \"$ASSET_B_ID\", \"fractions\": 5, \"priceAmount\": 100}")
T_ID_C=$(echo $TRADE_C | jq -r .id)
echo "Trade initiated: $T_ID_C"
echo "Final Saga Status for Scenario C: $(wait_for_saga $T_ID_C)"
echo -e "\n"

echo "Negative Test Suite Complete."
