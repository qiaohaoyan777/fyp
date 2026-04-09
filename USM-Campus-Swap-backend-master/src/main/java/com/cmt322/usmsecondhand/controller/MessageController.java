package com.cmt322.usmsecondhand.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.cmt322.usmsecondhand.model.Message;
import com.cmt322.usmsecondhand.model.User;
import com.cmt322.usmsecondhand.service.MessageService;
import com.cmt322.usmsecondhand.service.UserService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    @Resource
    private UserService userService;

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Boolean send(@RequestBody Message msg, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        msg.setSenderId(user.getId());
        return messageService.send(msg);
    }

    /**
     * 获取聊天记录
     */
    @GetMapping("/list")
    public List<Message> list(@RequestParam Long conversationId) {
        return messageService.list(conversationId);
    }

    /**
     * 🌟 新增：清除某个会话的未读消息红点
     */
    @PostMapping("/clearUnread")
    public Boolean clearUnread(@RequestParam("conversationId") Long conversationId, HttpServletRequest request) {
        if (conversationId == null || conversationId <= 0) {
            return false;
        }
        
        // 1. 获取当前正在看消息的登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 2. 告诉数据库：在这个会话里，只要发送人不是我，且还没读过的消息，全部标记为已读（is_read = 1）
        UpdateWrapper<Message> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("conversationId", conversationId)
                     .ne("senderId", loginUser.getId()) // 🌟 核心修改：ne 代表 Not Equal (不等于)
                     .eq("is_read", 0)                    
                     .set("is_read", 1);                  

        // 3. 执行更新
        return messageService.update(updateWrapper);
    }
}