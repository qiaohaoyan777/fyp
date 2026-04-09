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

    // 🚨 新增：注入消息服务来统计未读数
    @Resource
    private MessageService messageService;

    /**
     * 打开或创建一个聊天对话
     */
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
     * 获取当前用户的所有聊天对话
     */
    @GetMapping("/my")
    public BaseResponse<List<Conversation>> my(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        List<Conversation> conversationList = conversationService.listMy(user.getId());

        if (conversationList != null) {
            for (Conversation conv : conversationList) {
                // --- 逻辑 A: 填充商品名 ---
                try {
                    if (conv.getGoodsId() != null) {
                        Goods goods = goodsService.getById(conv.getGoodsId());
                        if (goods != null) conv.setItemName(goods.getTitle());
                    }
                } catch (Exception e) {
                    conv.setItemName("Item Details");
                }

                // --- 逻辑 B: 统计未读数 (加了保护，防止 500 错误) ---
                // --- 逻辑 B: 统计未读数 (加了保护，防止 500 错误) ---
                // --- 逻辑 B: 统计未读数 ---
                try {
                    QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
                    // 🚨 根据你的截图，准确使用这些列名：conversationId, senderId, is_read
                    queryWrapper.eq("conversationId", conv.getId())
                                .ne("senderId", user.getId()) // 🚨 ne 代表 Not Equal (不等于)。发送人不是我，就是发给我的
                                .eq("is_read", 0);            // 未读状态
                    
                    long unread = messageService.count(queryWrapper);
                    conv.setUnreadCount((int) unread);
                } catch (Exception e) {
                    System.err.println("Unread count SQL Error: " + e.getMessage());
                    conv.setUnreadCount(0);
                }
            }
        }
        return ResultUtils.success(conversationList);
    }
    // 🚨 新增：清除未读红点接口
    @PostMapping("/clearUnread")
    public BaseResponse<String> clearUnread(@RequestParam Long conversationId, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if (user == null) {
            return new BaseResponse<>(1, null, "未登录");
        }

        // 🚨 把在这个对话里、发送人不是我的、且未读的消息，设为已读(1)
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Message> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.eq("conversationId", conversationId)
                     .ne("senderId", user.getId()) // 🚨 发送者不是我
                     .eq("is_read", 0)
                     .set("is_read", 1); // 🚨 注意这里 set 的是数据库列名 is_read

        messageService.update(updateWrapper);

        return ResultUtils.success("清除成功");
    }
} 