package com.example.quiz.controller;

import com.example.quiz.util.DocxParseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired
    private DocxParseUtil util;

    // POST接口：上传 Word 文件解析题库
    @PostMapping("/upload")
    public List<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws Exception {
        return util.parse(file);
    }
}
