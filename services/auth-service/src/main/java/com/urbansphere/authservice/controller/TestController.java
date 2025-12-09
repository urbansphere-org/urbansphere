package com.urbansphere.authservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Value("${spring.flyway.locations}")
    String loc;

    @GetMapping("/fly")
    public String test() {
        return loc;
    }
}
