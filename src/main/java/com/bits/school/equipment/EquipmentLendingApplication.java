package com.bits.school.equipment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication(
        scanBasePackages = {
                "com.bits.school.equipment.controller",
                "com.bits.school.equipment.service",
                "com.bits.school.equipment.service.impl",
                "com.bits.school.equipment.dao",
                "com.bits.school.equipment.dao.impl",
                "com.bits.school.equipment.entity",
                "com.bits.school.equipment.dto",
                "com.bits.school.equipment.repository"
        }
)
public class EquipmentLendingApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(EquipmentLendingApplication.class);
        Map<String, Object> props = new HashMap<>();
        // default port and context-path can be overridden in application.yml
        props.put("server.port", "8083");
        props.put("server.servlet.context-path", "/equipment-service");
        app.setDefaultProperties(props);
        app.run(args);
    }
}

