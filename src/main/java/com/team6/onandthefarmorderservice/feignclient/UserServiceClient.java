package com.team6.onandthefarmorderservice.feignclient;

import com.team6.onandthefarmorderservice.vo.product.Product;
import com.team6.onandthefarmorderservice.vo.user.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service")
public interface UserServiceClient {
    @GetMapping("/api/user/member-service/user/{user-no}")
    public User findByUserId(@PathVariable("user-no") Long userId);
}
