package com.cmt322.usmsecondhand.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 聊天会话实体类
 */
@Data
@TableName("conversation")
public class Conversation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long goodsId;

    private Long buyerId;

    private Long sellerId;

    /**
     * ★★★ 新增：会话类型 ★★★
     * 1: Normal Chat (普通私聊)
     * 2: System Notification (系统通知)
     */
    private Integer type;

    /**
     * 这里的 exist 属性请根据你数据库真实情况调整：
     * 如果数据库有这列，就删掉 @TableField；如果没有，就保留。
     */
    @TableField(exist = false)
    private Date createTime;
    
    @TableField(exist = false)
    private Date updateTime;

    /**
     * 🚨 第一步新增：未读消息数量
     * exist = false 表示仅用于后端计算后传给前端，不存入数据库
     */
    @TableField(exist = false)
    private Integer unreadCount;

    /**
     * 临时字段：商品名称
     */
    @TableField(exist = false)
    private String itemName;

    /**
     * 临时字段：对方的名字
     */
    @TableField(exist = false)
    private String targetName;

    /**
     * 临时字段：对方的头像
     */
    @TableField(exist = false)
    private String targetAvatar;

    private static final long serialVersionUID = 1L;
}