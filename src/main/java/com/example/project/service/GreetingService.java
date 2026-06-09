package com.example.project.service;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {
    
    public String getHelloMessage(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Chào bạn, hãy nhập tên của bạn nhé!";
        }
        return "Xin chào " + name + "! Chúc bạn một ngày làm việc hiệu quả.";
    }
}
