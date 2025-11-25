package com.example.quiz.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 标记为 REST 接口控制器（返回 JSON 数据）
@RestController
// 所有接口统一前缀 /api（方便前端统一配置）
@RequestMapping("/api")
public class TestController {

    // 测试接口：GET 请求，路径 /api/hello
    @GetMapping("/hello")
    public String hello() {
        return "后端部署成功！前后端已打通～";
    }

    // 可选：再写一个带参数的接口，测试传参
    @GetMapping("/greet")
    public String greet(String name) {
        return "Hello " + (name == null ? "陌生人" : name) + "！欢迎使用 Vue+SpringBoot 项目～";
    }
}