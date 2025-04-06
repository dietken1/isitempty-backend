package com.isitempty.backend.hello;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parking-lots")
public class ParkingLotController {

    @GetMapping
    public String hello() {
        return "Hello, World!";
    }
} 