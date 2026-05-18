package com.cmt322.usmsecondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cmt322.usmsecondhand.common.BaseResponse;
import com.cmt322.usmsecondhand.common.ErrorCode;
import com.cmt322.usmsecondhand.common.ResultUtils;
import com.cmt322.usmsecondhand.exception.BusinessException;
import com.cmt322.usmsecondhand.model.Conversation;
import com.cmt322.usmsecondhand.model.Goods;
import com.cmt322.usmsecondhand.model.Message;
import com.cmt322.usmsecondhand.model.User;
import com.cmt322.usmsecondhand.service.ConversationService;
import com.cmt322.usmsecondhand.service.GoodsService;
import com.cmt322.usmsecondhand.service.MessageService;
import com.cmt322.usmsecondhand.service.UserService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Resource
    private ConversationService conversationService;

    @Resource
    private UserService userService;

    @Resource
    private GoodsService goodsService;

    @Resource
    private MessageService messageService;

    @PostMapping("/open")
    public BaseResponse<Long> open(@RequestParam Long goodsId, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if (user == null) {
            return new BaseResponse<>(1, null, "User not logged in");
        }

        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            return new BaseResponse<>(1, null, "Goods not found");
        }

        Long conversationId = conversationService.open(goodsId, user.getId());
        if (conversationId == null) {
            return new BaseResponse<>(1, null, "Failed to create conversation");
        }

        return new BaseResponse<>(0, conversationId, "Success");
    }

    /**
     * 获取当前用户的所有聊天对话（🚀 终极防弹缝合版）
     */
    @GetMapping("/my")
    public BaseResponse<List<Conversation>> my(HttpServletRequest request) {
        // 🚨 强制控制台打印日志
        System.out.println("=================================================");
        System.out.println("🔥 SYSTEM NOTICE: 进入了 /conversation/my 接口！");
        System.out.println("=================================================");

        User user = userService.getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        System.out.println("👉 当前登录用户 ID (My UID): " + user.getId());

        List<Conversation> conversationList = conversationService.listMy(user.getId());

        if (conversationList != null && !conversationList.isEmpty()) {
            System.out.println("📦 共查出 " + conversationList.size() + " 条原始会话记录，开始处理...");
            
            for (Conversation conv : conversationList) {
                System.out.println("---------------------------------------------");
                System.out.println("🛠️ 正在处理会话 ID: " + conv.getId() + " | 买家 ID: " + conv.getBuyerId() + " | 卖家 ID: " + conv.getSellerId());

                // --- 🚀 智能查找并缝合聊天对象的真实信息 ---
                try {
                    Long targetUserId = null;
                    // 智能身份判定：如果我是买家，对面就是卖家；如果我是卖家，对面就是买家
                    if (user.getId().equals(conv.getBuyerId())) {
                        targetUserId = conv.getSellerId();
                        System.out.println("   [身份判定] 我是买家，聊天对象是卖家(ID: " + targetUserId + ")");
                    } else {
                        targetUserId = conv.getBuyerId();
                        System.out.println("   [身份判定] 我是卖家，聊天对象是买家(ID: " + targetUserId + ")");
                    }

                    if (targetUserId != null) {
                        User targetUser = userService.getById(targetUserId);
                        if (targetUser != null) {
                            conv.setTargetId(targetUser.getId());
                            conv.setTargetName(targetUser.getUsername());
                            conv.setTargetAvatar(targetUser.getAvatarUrl());
                            System.out.println("   ✅ 成功缝合用户信息！[昵称]: " + targetUser.getUsername() + " | [头像]: " + targetUser.getAvatarUrl());
                        } else {
                            conv.setTargetName("Unknown User");
                            System.out.println("   ❌ 缝合失败：在 user 表里找不到 ID 为 " + targetUserId + " 的用户资料！");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("   💥 缝合聊天对象身份时发生未知异常: " + e.getMessage());
                }

                // --- 逻辑 A: 填充商品名 ---
                try {
                    if (conv.getGoodsId() != null) {
                        Goods goods = goodsService.getById(conv.getGoodsId());
                        if (goods != null) {
                            conv.setItemName(goods.getTitle());
                            System.out.println("   📦 商品名称绑定成功: " + goods.getTitle());
                        }
                    }
                } catch (Exception e) {
                    conv.setItemName("Item Details");
                }

                // --- 逻辑 B: 统计未读数 ---
                try {
                    QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("conversationId", conv.getId())
                                .ne("senderId", user.getId()) 
                                .eq("is_read", 0);            
                    
                    long unread = messageService.count(queryWrapper);
                    conv.setUnreadCount((int) unread);
                    System.out.println("   📩 未读消息数统计: " + unread);
                } catch (Exception e) {
                    conv.setUnreadCount(0);
                }
            }
        } else {
            System.out.println("⚠️ 警告: 该用户的原始会话列表在数据库中为空！");
        }

        System.out.println("=================================================");
        return ResultUtils.success(conversationList);
    }

    @PostMapping("/clearUnread")
    public BaseResponse<String> clearUnread(@RequestParam Long conversationId, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if (user == null) {
            return new BaseResponse<>(1, null, "未登录");
        }

        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Message> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.eq("conversationId", conversationId)
                     .ne("senderId", user.getId()) 
                     .eq("is_read", 0)
                     .set("is_read", 1); 

        messageService.update(updateWrapper);
        return ResultUtils.success("清除成功");
    }
}