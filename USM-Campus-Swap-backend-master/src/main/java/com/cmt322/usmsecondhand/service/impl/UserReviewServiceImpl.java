package com.cmt322.usmsecondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmt322.usmsecondhand.common.ErrorCode;
import com.cmt322.usmsecondhand.exception.BusinessException;
import com.cmt322.usmsecondhand.mapper.UserReviewMapper;
import com.cmt322.usmsecondhand.model.User;
import com.cmt322.usmsecondhand.model.UserReview;
import com.cmt322.usmsecondhand.model.request.ReviewAddRequest;
import com.cmt322.usmsecondhand.service.UserReviewService;
import org.springframework.stereotype.Service;

/**
 * 评价表 Service 实现类
 */
@Service
public class UserReviewServiceImpl extends ServiceImpl<UserReviewMapper, UserReview> implements UserReviewService {

    @Override
    public boolean addReview(ReviewAddRequest request, User loginUser) {
        // 1. 参数基本校验
        if (request == null || request.getOrderId() == null || request.getSellerId() == null || request.getRating() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Missing required review information");
        }
        
        // 2. 防刷机制：检查这个订单是不是已经评价过了 (通过 order_id 查重)
        long count = this.count(new QueryWrapper<UserReview>().eq("order_id", request.getOrderId()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "You have already reviewed this order!");
        }

        // 3. 构建评价对象并保存到数据库
        UserReview review = new UserReview();
        review.setOrderId(request.getOrderId());
        review.setBuyerId(loginUser.getId()); // 当前登录的人就是买家
        review.setSellerId(request.getSellerId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());

        boolean saved = this.save(review);
        
        // 💡 进阶预留：如果你想计算卖家的平均分，可以在这里触发更新 User 表的逻辑
        
        return saved;
    }
}