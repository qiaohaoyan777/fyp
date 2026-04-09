package com.cmt322.usmsecondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmt322.usmsecondhand.model.UserReview;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价表 Mapper 接口
 */
@Mapper
public interface UserReviewMapper extends BaseMapper<UserReview> {
}