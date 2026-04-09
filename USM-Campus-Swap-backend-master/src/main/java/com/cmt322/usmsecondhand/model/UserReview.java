package com.cmt322.usmsecondhand.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户评价表实体类
 */
@Data
@TableName("user_review")
public class UserReview implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 买家ID（评价人）
     */
    @TableField("buyer_id")
    private Long buyerId;

    /**
     * 卖家ID（被评价人）
     */
    @TableField("seller_id")
    private Long sellerId;

    /**
     * 星级打分 (1-5)
     */
    private Integer rating;

    /**
     * 评价具体内容
     */
    private String content;

    /**
     * 评价时间
     */
    @TableField("create_time")
    private Date createTime;
}