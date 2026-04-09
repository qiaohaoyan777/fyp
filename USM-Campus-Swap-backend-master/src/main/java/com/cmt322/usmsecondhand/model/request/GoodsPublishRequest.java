package com.cmt322.usmsecondhand.model.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品发布请求DTO
 */
@Data
public class GoodsPublishRequest {
    private String title;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private String coverImage;
    private List<String> images;
    private Integer condition;
    private String campus;
    private List<Integer> contactTypes;
    
    /**
     * 交易方式 (1: 面交, 2: 邮寄/送货)
     */
    private Integer deliveryMethod;
    
    /**
     * 具体交易地点 (配合前端和数据库统一使用 address)
     */
    private String address;
}