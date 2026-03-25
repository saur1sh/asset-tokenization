package com.rwa.wallet.repository;

import com.rwa.wallet.entity.WalletLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WalletLockRepository extends JpaRepository<WalletLock, UUID> {
}
