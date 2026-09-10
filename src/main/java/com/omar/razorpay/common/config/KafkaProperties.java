package com.omar.razorpay.common.config;

import com.omar.razorpay.common.enums.EventAggregateType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProperties (
    Map<String, String> topics
){
  public String topicFor(EventAggregateType aggregateType){
      String topic =  topics.get(aggregateType.name().toLowerCase());
      if(topic == null){
          throw new IllegalArgumentException("No kafka topic configured for aggregate type: " + aggregateType.name());
      }
      return topic;
  }
}
