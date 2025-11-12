package com.innowise.internship.service;

import com.innowise.internship.dto.kafka.PaymentCreatedEvent;

public interface KafkaConsumerService {

    void handlePaymentCreatedEvent(PaymentCreatedEvent paymentCreatedEvent);

}
