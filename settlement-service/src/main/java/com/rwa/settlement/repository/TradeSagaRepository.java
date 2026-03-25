package com.rwa.settlement.repository;

import com.rwa.settlement.entity.TradeSaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TradeSagaRepository extends JpaRepository<TradeSaga, UUID> {
}
