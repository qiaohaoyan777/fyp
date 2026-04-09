package com.cmt322.usmsecondhand.controller;

import com.cmt322.usmsecondhand.common.BaseResponse;
import com.cmt322.usmsecondhand.common.ErrorCode;
import com.cmt322.usmsecondhand.common.ResultUtils;
import com.cmt322.usmsecondhand.exception.BusinessException;
import com.cmt322.usmsecondhand.model.User;
import com.cmt322.usmsecondhand.model.request.ReviewAddRequest;
import com.cmt322.usmsecondhand.service.UserReviewService;
import com.cmt322.usmsecondhand.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
public class UserReviewController {

    @Resource
    private UserReviewService userReviewService;

    @Resource
    private UserService userService;

    /**
     * 提交评价接口
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addReview(@RequestBody ReviewAddRequest request, HttpServletRequest httpServletRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(httpServletRequest);
        
        // 调用 Service 执行保存逻辑
        boolean result = userReviewService.addReview(request, loginUser);
        
        return ResultUtils.success(result);
    }
}
