package com.cmt322.usmsecondhand.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField; 
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @TableName message
 */
@TableName(value ="message")
@Data
public class Message {
    
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long conversationId;

    private Long senderId;

    private Long receiverId;

    private String content;

    private Integer type;

    // 🌟 核心修复：强制绑定数据库中的 is_read 列
    @TableField("is_read")
    private Integer isRead;

    private Date createTime;

    // ==========================================
    // 🚀 核心扩容：前端显示必备的“虚拟字段”
    // exist = false 表示数据库表里不需要有这列，仅用于给前端传 JSON
    // ==========================================
    
    /**
     * 发送者真实姓名
     */
    @TableField(exist = false)
    private String senderName;

    /**
     * 发送者真实头像
     */
    @TableField(exist = false)
    private String senderAvatar;
}