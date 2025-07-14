package com.tj.tjp.controller.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthTestController {

    @GetMapping("/login/success")
    public String loginSuccess(@RequestParam String token) {
        return "로그인 성공! JWT토큰: "+token;
    }
}
