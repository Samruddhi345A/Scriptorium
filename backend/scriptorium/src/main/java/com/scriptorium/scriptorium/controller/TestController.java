package com.scriptorium.scriptorium.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public Map<String,String> testapi(){

        return Map.of("status","ok");
    }
}
