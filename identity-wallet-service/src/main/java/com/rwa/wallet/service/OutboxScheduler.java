package com.rwa.wallet.service;

import com.rwa.wallet.entity.OutboxEvent;
import com.rwa.wallet.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> events = outboxEventRepository.findAllByOrderByCreatedAtAsc(PageRequest.of(0, 50));
        for (OutboxEvent event : events) {
            String topic = event.getType();
            log.info("Publishing event {} to Kafka topic {}", event.getId(), topic);
            kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload());
            outboxEventRepository.delete(event);
        }
    }
}
