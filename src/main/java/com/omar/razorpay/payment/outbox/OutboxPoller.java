package com.omar.razorpay.payment.outbox;

import com.omar.razorpay.common.config.KafkaProperties;
import com.omar.razorpay.common.enums.OutboxStatus;
import com.omar.razorpay.payment.entity.OutboxEvent;
import com.omar.razorpay.payment.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPoller {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;
    private final OutboxResultHandler outboxResultHandler;

    @Scheduled(fixedRate = 5000)
    public void poll() {
        try {
            List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
            for (OutboxEvent event : pendingEvents) {
                String topic = kafkaProperties.topicFor(event.getAggregateType());
                String key = extractMerchantId(event.getPayload());

                Map<String, Object> envelope = Map.of(
                        "eventType", event.getEventType(),
                        "aggregateType", event.getAggregateType(),
                        "aggregateId", event.getAggregateId(),
                        "data", event.getPayload()
                );

                kafkaTemplate.send(topic, key, envelope)
                        .get(5, TimeUnit.SECONDS);

                outboxResultHandler.handleEventPublished(event);
            }
        } catch (Exception e) {
            log.error("Error while polling outbox events: ", e);
        }
    }



    private String extractMerchantId(Map<String, Object> payload) {
        Object merchantId = payload.get("merchantId");
        return merchantId != null ? merchantId.toString() : "Unknown";
    }
}
