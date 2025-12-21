package com.urbansphere.authservice.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Data
@RestController
@ConfigurationProperties("spring.flyway")
public class TestController {
    String locations;
    String user;

    @GetMapping("/fly")
    public String test() {
        log.info("fly called!");
        return locations;
    }
}
