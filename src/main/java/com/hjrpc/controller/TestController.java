package com.hjrpc.controller;

import com.hjrpc.signature.annoation.Signature;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {

    @PostMapping("testSignature/{id}")
    @Signature("TEST_CODE1")
    public String testSignature(@RequestBody UserEntity userEntity, @PathVariable("id") String id
            , @RequestParam String companyName) {
        System.out.println(userEntity);
        System.out.println("id:" + id);
        System.out.println("companyName:" + companyName);
        return "success";
    }

    @Data
    static class UserEntity {
        private String username;
        private Integer age;
    }
}
