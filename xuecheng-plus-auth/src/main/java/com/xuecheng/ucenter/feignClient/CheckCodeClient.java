package com.xuecheng.ucenter.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(value = "checkcode",fallbackFactory = CheckCodeClientFallBackFactory.class)
public interface CheckCodeClient {
    @PostMapping("/checkcode/verify")
    public Boolean verify(@RequestParam("key") String key, @RequestParam("code") String code);
}
