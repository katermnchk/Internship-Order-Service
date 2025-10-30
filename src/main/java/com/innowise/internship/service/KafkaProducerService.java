package com.innowise.internship.service;

import com.innowise.internship.dto.kafka.OrderCreatedEvent;

public interface KafkaProducerService {

    void sendOrderCreatedEvent(OrderCreatedEvent orderCreatedEvent);

}