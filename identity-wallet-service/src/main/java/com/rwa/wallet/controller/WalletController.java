package com.rwa.wallet.controller;

import com.rwa.common.enums.ComplianceStatus;
import com.rwa.wallet.entity.Wallet;
import com.rwa.wallet.service.WalletService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public Wallet createWallet(@RequestBody CreateWalletRequest request) {
        return walletService.createWallet(request.getUserId(), request.getInitialBalance(), request.getCurrency());
    }

    @PostMapping("/{userId}/kyc")
    public void updateKycStatus(@PathVariable UUID userId, @RequestBody KycStatusRequest request) {
        walletService.updateKycStatus(userId, request.getStatus());
    }

    @GetMapping("/{userId}")
    public Wallet getWallet(@PathVariable UUID userId) {
        return walletService.getWallet(userId);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KycStatusRequest {
        private ComplianceStatus status;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateWalletRequest {
        private UUID userId;
        private BigDecimal initialBalance;
        private String currency;
    }
}
