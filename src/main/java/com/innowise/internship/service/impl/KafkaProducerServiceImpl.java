package com.innowise.internship.service.impl;

import com.innowise.internship.dto.kafka.OrderCreatedEvent;
import com.innowise.internship.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerServiceImpl implements KafkaProducerService {

    @Value("${app.kafka.topic.create-order}")
    private String createOrderTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void sendOrderCreatedEvent(OrderCreatedEvent orderCreatedEvent) {

        kafkaTemplate.send(createOrderTopic, orderCreatedEvent.getOrderId(), orderCreatedEvent);

    }
}
