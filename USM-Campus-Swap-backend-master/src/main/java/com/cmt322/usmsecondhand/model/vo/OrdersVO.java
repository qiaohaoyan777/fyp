package com.cmt322.usmsecondhand.model.vo;

import com.cmt322.usmsecondhand.model.Orders;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import java.io.Serializable;

@Data
public class OrdersVO extends Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    private String goodsTitle;
    private String goodsImage;
    private Integer deliveryMethod;
    private String campus;
    private String address;

    private String counterpartyName; 
    private String counterpartyAvatar;
    private String buyerName;  
    private String sellerName; 

    // 🌟 核心修改：把 isReviewed 改成 hasReview，避开 Jackson 序列化的坑！
    private Boolean hasReview;    
    private Integer reviewRating;  
    private String reviewContent;  

    public static OrdersVO objToVo(Orders orders) {
        if (orders == null) return null;
        OrdersVO vo = new OrdersVO();
        BeanUtils.copyProperties(orders, vo);
        vo.setHasReview(false); // 默认未评价
        return vo;
    }
}