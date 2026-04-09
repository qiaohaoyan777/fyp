package com.cmt322.usmsecondhand.controller;

import com.cmt322.usmsecondhand.common.BaseResponse;
import com.cmt322.usmsecondhand.common.ResultUtils;
import com.cmt322.usmsecondhand.service.GeminiService;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private GeminiService geminiService;

    @GetMapping("/generate")
    public BaseResponse<String> generateDesc(@RequestParam String title, @RequestParam(defaultValue = "正常使用痕迹") String condition) {
        if (title == null || title.trim().isEmpty()) {
            return new BaseResponse<>(1, null, "商品标题不能为空"); // ✅ 换成最原始的写法，绝对不报错
        }
        
        String description = geminiService.generateDescription(title, condition);
        return ResultUtils.success(description);
    }
}
