package com.omar.razerpay.payment.Simulator;

import com.omar.razerpay.common.enums.ChaosMode;
import com.omar.razerpay.common.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "payment.simulator")
@Getter
@Setter
public class SimulatorConfig {
    private Integer pollIntervalMs = 2000;
    private ChaosMode chaosMode = ChaosMode.NORMAL;
    private Map<String, MethodSimulatorConfig> methods = new HashMap<>();

    public SimulatorConfig.MethodSimulatorConfig configFor(PaymentMethod method){
        return methods.getOrDefault(method.name(), new MethodSimulatorConfig());
    }

    @Getter
    @Setter
    public static class MethodSimulatorConfig{
        private Integer minDelaySeconds = 2; 
        private Integer maxDelaySeconds = 6;
        private Integer successRate = 80;
    }

}


//values are given sensible default to prevent application from crashing in case anyone changes the application.yml file