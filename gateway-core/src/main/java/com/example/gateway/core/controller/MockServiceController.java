package com.example.gateway.core.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/service")
public class MockServiceController {

    @GetMapping("/data")
    public String getData() {
        return "{\"data\": \"This is core service data behind the gateway\"}";
    }
}
