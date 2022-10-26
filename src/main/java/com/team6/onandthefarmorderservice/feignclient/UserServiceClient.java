package com.team6.onandthefarmorderservice.feignclient;


import com.team6.onandthefarmorderservice.vo.feignclient.UserVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service")
public interface UserServiceClient {
    /**
     * 유저ID를 이용해서 유저 정보를 가져오는 것
     * @param userId
     * @return
     */
    @GetMapping("/api/user/members/member-service/{user-no}")
    UserVo findByUserId(@PathVariable("user-no") Long userId);
}
