#!/bin/bash

# Configuration
ASSET_URL="http://localhost:8081"
WALLET_URL="http://localhost:8082"
SETTLEMENT_URL="http://localhost:8083"

function wait_for_saga() {
    local saga_id=$1
    local max_attempts=15
    local attempt=1
    while [ $attempt -le $max_attempts ]; do
        status=$(curl -s "$SETTLEMENT_URL/api/settlement/$saga_id" | jq -r .status)
        if [ "$status" == "COMPLETED" ]; then
            echo "$status"
            return
        elif [ "$status" == "FAILED" ]; then
            echo "$status"
            return
        fi
        sleep 2
        attempt=$((attempt + 1))
    done
    echo "TIMEOUT"
}

echo "--------------------------------------------------"
echo "Starting Positive Test Suite for RWA Saga"
echo "--------------------------------------------------"

# 1. Setup User
USER_ID=$(uuidgen)
echo "Creating Wallet for $USER_ID with $5000 balance..."
curl -s -X POST "$WALLET_URL/api/wallets" \
     -H "Content-Type: application/json" \
     -d "{\"userId\": \"$USER_ID\", \"initialBalance\": 5000, \"currency\": \"USD\"}" > /dev/null

echo -e "\nVerifying KYC for user..."
curl -s -X POST "$WALLET_URL/api/wallets/$USER_ID/kyc" \
     -H "Content-Type: application/json" \
     -d "{\"status\": \"VERIFIED\"}" > /dev/null
echo -e "\n"

# 2. Setup Asset
echo "Creating Asset (GOLD) with 1000 total fractions..."
ASSET_ID=$(curl -s -X POST "$ASSET_URL/api/assets" \
     -H "Content-Type: application/json" \
     -d "{\"type\": \"GOLD\", \"totalFractions\": 1000}" | jq -r .id)
echo "Asset Created: $ASSET_ID"
echo -e "\n"

# 3. Initiate Trade
echo "[TEST] Positive Scenario: Valid Trade"
echo "Initiating trade... User buying 10 fractions of GOLD for $150.50"
TRADE=$(curl -s -X POST "$SETTLEMENT_URL/api/settlement/trade" \
     -H "Content-Type: application/json" \
     -d "{\"buyerId\": \"$USER_ID\", \"assetId\": \"$ASSET_ID\", \"fractions\": 10, \"priceAmount\": 150.50}")

T_ID=$(echo $TRADE | jq -r .id)
echo "Trade Initiated: $T_ID"

echo "Waiting for Saga to complete..."
FINAL_STATUS=$(wait_for_saga $T_ID)
echo "Final Saga Status: $FINAL_STATUS"

if [ "$FINAL_STATUS" == "COMPLETED" ]; then
    echo "SUCCESS: Trade completed as expected."
else
    echo "FAILURE: Expected COMPLETED but got $FINAL_STATUS"
fi

echo -e "\nVerifying Final Wallet Balance (Should be 5000 - 150.50 = 4849.5):"
NEW_BALANCE=$(curl -s "$WALLET_URL/api/wallets/$USER_ID" | jq .balance)
echo "Actual Balance: $NEW_BALANCE"

echo -e "\nVerifying Final Asset Remaining Fractions (Should be 1000 - 10 = 990):"
REMAINING_FRACTIONS=$(curl -s "$ASSET_URL/api/assets/$ASSET_ID" | jq .remainingFractions)
echo "Actual Remaining Fractions: $REMAINING_FRACTIONS"
echo "Positive Test Suite Complete."
