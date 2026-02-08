package com.utgaming.testconcurrency.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic stockDeductTopic() {
        return TopicBuilder.name("stock_deduct")
                .partitions(3)
                .replicas(3)
                .config("min.insync.replicas", "2")
                .build();
    }
}
