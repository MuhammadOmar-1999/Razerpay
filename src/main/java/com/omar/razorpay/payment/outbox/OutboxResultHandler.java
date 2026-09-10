package com.omar.razorpay.payment.outbox;

import com.omar.razorpay.common.enums.OutboxStatus;
import com.omar.razorpay.payment.entity.OutboxEvent;
import com.omar.razorpay.payment.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OutboxResultHandler {
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void handleEventPublished(OutboxEvent event) {
        event.setStatus(OutboxStatus.PUBLISHED);
        event.setPublishedAt(LocalDateTime.now());
    }
}
