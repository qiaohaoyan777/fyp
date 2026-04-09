package com.cmt322.usmsecondhand.model.request;

import lombok.Data;
import java.io.Serializable;

/**
 * 提交评价请求体
 */
@Data
public class ReviewAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 卖家ID (被评价人)
     */
    private Long sellerId;

    /**
     * 星级 (1-5)
     */
    private Integer rating;

    /**
     * 评价内容 (选填)
     */
    private String content;
}