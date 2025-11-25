package com.example.quiz.util;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DocxParseUtil {

    private static final List<String> OPTION_KEYS = Arrays.asList("A","B","C","D","E","F");

    // 选项匹配：A: A： A. A．
    private static final Pattern OPTION_PATTERN = Pattern.compile(
            "([A-F])[：:．.]\\s*([^A-F]*)"
    );

    public List<Map<String, Object>> parse(MultipartFile file) throws Exception {

        List<Map<String, Object>> result = new ArrayList<>();
        XWPFDocument doc = new XWPFDocument(file.getInputStream());

        StringBuilder currentQuestion = new StringBuilder();
        Map<String, StringBuilder> currentOptions = initOptionMap();
        String currentAnswer = null;
        String currentParsingOption = null;

        for (XWPFParagraph p : doc.getParagraphs()) {
            String line = p.getText().trim();
            if (line.isEmpty()) continue;

            // ========== 题目 Q: 或 Q： ==========
            if (line.startsWith("Q:") || line.startsWith("Q：")) {

                // 保存上一题
                if (currentQuestion.length() > 0 && currentAnswer != null) {
                    result.add(pack(currentQuestion, currentOptions, currentAnswer));
                }

                currentQuestion = new StringBuilder(line.substring(2).trim());
                currentOptions = initOptionMap();
                currentAnswer = null;
                currentParsingOption = null;
                continue;
            }

            // ========== 答案 ANS: ==========
            if (line.startsWith("ANS:") || line.startsWith("ANS：")) {
                currentAnswer = line.substring(4).trim();
                currentParsingOption = null;
                continue;
            }

            // ========== 解析选项（支持一行多个）==========
            Matcher om = OPTION_PATTERN.matcher(line);
            boolean matchedOption = false;

            while (om.find()) {
                matchedOption = true;
                String key = om.group(1);
                String text = om.group(2).trim();

                currentOptions.get(key).append(" ").append(text);
                currentParsingOption = key;
            }

            if (matchedOption) continue;

            // ========== 跨段落追加 ==========
            if (currentParsingOption != null) {
                currentOptions.get(currentParsingOption).append(" ").append(line);
            } else if (currentAnswer == null && currentQuestion.length() > 0) {
                currentQuestion.append(" ").append(line);
            }
        }

        // 最后一题
        if (currentQuestion.length() > 0 && currentAnswer != null) {
            result.add(pack(currentQuestion, currentOptions, currentAnswer));
        }

        return result;
    }

    private Map<String, StringBuilder> initOptionMap() {
        Map<String, StringBuilder> map = new LinkedHashMap<>();
        for (String k : OPTION_KEYS) map.put(k, new StringBuilder());
        return map;
    }

    private Map<String, Object> pack(
            StringBuilder question,
            Map<String, StringBuilder> options,
            String answer
    ) {
        Map<String, Object> map = new HashMap<>();
        map.put("question", question.toString().trim());

        List<Map<String, String>> list = new ArrayList<>();
        options.forEach((k, v) -> {
            if (v.length() > 0) {
                Map<String, String> m = new HashMap<>();
                m.put("key", k);
                m.put("value", v.toString().trim());
                list.add(m);
            }
        });

        map.put("options", list);
        map.put("answer", answer);
        return map;
    }
}
