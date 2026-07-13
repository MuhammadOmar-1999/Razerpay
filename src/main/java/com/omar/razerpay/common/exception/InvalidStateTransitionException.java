package com.omar.razerpay.common.exception;

import com.omar.razerpay.common.enums.PaymentEvent;
import com.omar.razerpay.common.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidStateTransitionException extends RuntimeException {
    private final PaymentStatus currentStatus;
    private final PaymentEvent event;

    public InvalidStateTransitionException(PaymentStatus currentStatus, PaymentEvent event) {
        super("Invalid transition: " + currentStatus + " -> " + event);
        this.currentStatus = currentStatus;
        this.event = event;
    }
}
