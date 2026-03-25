package com.rwa.settlement.controller;

import com.rwa.settlement.entity.TradeSaga;
import com.rwa.settlement.service.SettlementService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/settlement")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping("/trade")
    public TradeSaga initiateTrade(@RequestBody TradeRequest request) {
        return settlementService.initiateTrade(
                request.getBuyerId(), 
                request.getAssetId(), 
                request.getFractions(), 
                request.getPriceAmount()
        );
    }

    @org.springframework.web.bind.annotation.GetMapping("/{id}")
    public TradeSaga getTradeSaga(@org.springframework.web.bind.annotation.PathVariable UUID id) {
        return settlementService.getTradeSaga(id);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeRequest {
        private UUID buyerId;
        private UUID assetId;
        private BigDecimal fractions;
        private BigDecimal priceAmount;
    }
}
