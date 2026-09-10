package com.omar.razorpay.payment.Processor.dto;

public sealed interface PaymentProcessorResponse {

    record Pending(String processorReference) implements PaymentProcessorResponse {}

    record Success(String processorReference, String bankReference)  implements PaymentProcessorResponse {}

    record Failed(String errorCode, String errorDescription)  implements PaymentProcessorResponse {}
}
