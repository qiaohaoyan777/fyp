package com.cmt322.usmsecondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmt322.usmsecondhand.model.User;
import com.cmt322.usmsecondhand.model.UserReview;
import com.cmt322.usmsecondhand.model.request.ReviewAddRequest;

/**
 * 评价表 Service 接口
 */
public interface UserReviewService extends IService<UserReview> {

    /**
     * 提交订单评价
     * * @param request   前端传过来的评价数据
     * @param loginUser 当前登录的买家
     * @return 是否保存成功
     */
    boolean addReview(ReviewAddRequest request, User loginUser);
    
}