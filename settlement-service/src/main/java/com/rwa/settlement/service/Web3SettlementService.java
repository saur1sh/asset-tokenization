package com.rwa.settlement.service;

import com.rwa.common.constants.RwaConstants;
import com.rwa.common.events.TradeMatchedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;

@Service
@Slf4j
@RequiredArgsConstructor
public class Web3SettlementService {

    private final Web3j web3j;
    private final SmartContractService smartContractService;

    @KafkaListener(topics = RwaConstants.TOPIC_TRADE_MATCHES, groupId = RwaConstants.GROUP_SETTLEMENT_SERVICE)
    public void handleTradeMatched(TradeMatchedEvent event) {
        log.info("Starting on-chain settlement for trade: Buyer {} -> Seller {} (Asset: {})",
                event.getBuyerId(), event.getSellerId(), event.getAssetId());

        // Simulation of address resolution and amount scaling
        String buyerAddress = "0xBuyerAddress...";
        String sellerAddress = "0xSellerAddress...";
        java.math.BigInteger amount = event.getQuantity().toBigInteger(); // simplified

        smartContractService.transferOnChain(sellerAddress, buyerAddress, amount);
    }
}
