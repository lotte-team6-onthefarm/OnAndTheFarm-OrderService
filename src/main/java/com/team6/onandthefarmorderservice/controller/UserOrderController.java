package com.team6.onandthefarmorderservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/orders")
public class UserOrderController {
    @GetMapping("/welcome")
    public String welcome(){
        return "hallo";
    }
}
