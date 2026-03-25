package com.rwa.asset.repository;

import com.rwa.asset.entity.DigitalAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<DigitalAsset, UUID> {
}
