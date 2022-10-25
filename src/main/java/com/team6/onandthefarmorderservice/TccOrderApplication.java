package com.team6.onandthefarmorderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TccOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(TccOrderApplication.class, args);
    }

}
