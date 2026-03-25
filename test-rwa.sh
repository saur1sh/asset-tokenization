#!/bin/bash

# Create Asset
ASSET_ID=$(curl -s -X POST http://localhost:8081/api/assets \
  -H "Content-Type: application/json" \
  -d '{"type": "GOLD", "totalFractions": 1000}' | jq -r '.id')

echo "Asset Created: $ASSET_ID"

# Create Wallet for Buyer
USER_ID="1C7A95EB-EC6C-4901-81F5-EEECEC70E932"
WALLET=$(curl -s -X POST http://localhost:8082/api/wallets \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"$USER_ID\", \"initialBalance\": 5000, \"currency\": \"USD\"}")

echo "Wallet Created: $WALLET"

# Update KYC Status
curl -s -X POST http://localhost:8082/api/wallets/$USER_ID/kyc \
  -H "Content-Type: application/json" \
  -d '{"status": "VERIFIED"}'

echo "KYC Status Updated for $USER_ID"

# Initiate Trade
TRADE=$(curl -s -X POST http://localhost:8083/api/settlement/trade \
  -H "Content-Type: application/json" \
  -d "{
    \"buyerId\": \"$USER_ID\",
    \"assetId\": \"$ASSET_ID\",
    \"fractions\": 10,
    \"priceAmount\": 150.50
  }")

echo "Trade Initiated: $TRADE"
