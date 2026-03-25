package com.rwa.asset.controller;

import com.rwa.asset.entity.DigitalAsset;
import com.rwa.asset.service.AssetService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    public DigitalAsset createAsset(@RequestBody CreateAssetRequest request) {
        return assetService.createAsset(request.getType(), request.getTotalFractions());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateAssetRequest {
        private String type;
        private BigDecimal totalFractions;
    }
}
