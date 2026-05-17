package com.cmt322.usmsecondhand.model.vo;

import com.cmt322.usmsecondhand.model.Goods;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品详情展示对象
 * 字段名必须与 Goods 实体类完全一致
 */
@Data
public class GoodsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 标题 (原实体类叫 title)
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 分类ID
     */
    private Long categoryId;


    private String categoryName;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 封面图片 (单张 URL)
     */
    private String coverImage;

    /**
     * 商品图片列表 (原实体类定义为 Object，可能是JSON字符串)
     */
    private Object images;

    /**
     * 成色 1-New 2-Like New 3-Gently Used 4-Used
     */
    private Integer condition;

    /**
     * 状态 1-Available 2-Sold 3-Inactive
     */
    private Integer status;

    /**
     * 浏览量 (原实体类叫 viewCount)
     */
    private Integer viewCount;

    /**
     * 点赞量
     */
    private Integer likeCount;

    /**
     * 校区
     */
    private String campus;

    private String specifics;

    // 🌟 新增：交易方式 (1: 面交, 2: 邮寄/送货)
    private Integer deliveryMethod;

    // 🌟 新增：详细交易地址
    private String address;

    private String userName;

    private String userAvatar;

    /**
     * 联系方式类型
     */
    private Integer contactType;

    /**
     * 发布时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 发布者详细信息
     */
    private UserVO user;

    /**
     * 收藏量
     */
    private Integer wishlistCount;


    /**
     * 包装函数：将 Goods 实体转为 GoodsVO
     */
    public static GoodsVO objToVo(Goods goods) {
        if (goods == null) {
            return null;
        }
        GoodsVO goodsVO = new GoodsVO();

        // 🌟 因为名字完全一样，BeanUtils 会自动帮我们把新增的 address 和 deliveryMethod 也复制过去！
        BeanUtils.copyProperties(goods, goodsVO);
        return goodsVO;
    }
}