package com.cmt322.usmsecondhand.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.cmt322.usmsecondhand.common.BaseResponse;
import com.cmt322.usmsecondhand.common.ResultUtils;
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
    public BaseResponse<Boolean> send(@RequestBody Message msg, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if (user == null) {
            return ResultUtils.error(40100, "未登录", "");
        }
        msg.setSenderId(user.getId());
        Boolean result = messageService.send(msg);
        return ResultUtils.success(result);
    }

    /**
     * 获取聊天记录 (🚀 核心修复：缝合真实的头像和昵称)
     */
    @GetMapping("/list")
    public BaseResponse<List<Message>> list(@RequestParam Long conversationId) {
        if (conversationId == null || conversationId <= 0) {
            return ResultUtils.error(40000, "参数错误", "");
        }
        
        // 1. 获取原始聊天记录 (只有 ID)
        List<Message> messageList = messageService.list(conversationId);
        
        // 2. 🚀 遍历消息，去 User 表里查出真实的头像和名字，塞进 Message 对象里！
        // 2. 🚀 遍历消息，缝合头像和名字
        for (Message msg : messageList) {
            if (msg.getSenderId() != null) {
                User sender = userService.getById(msg.getSenderId());
                if (sender != null) {
                    // 如果数据库里名字为空，强制给个名字测试
                    String name = sender.getUsername() != null ? sender.getUsername() : "User_" + sender.getId();
                    String avatar = sender.getAvatarUrl() != null ? sender.getAvatarUrl() : "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png";
                    
                    msg.setSenderName(name);
                    msg.setSenderAvatar(avatar);
                    
                    // 打个日志，看看后端到底缝合成功没有！
                    System.out.println("✅ 成功缝合消息！发送者: " + name);
                }
            }
        }
        
        // 3. 将缝合好名字和头像的完整数据返回给前端
        return ResultUtils.success(messageList);
    }

    /**
     * 清除某个会话的未读消息红点
     */
    @PostMapping("/clearUnread")
    public BaseResponse<Boolean> clearUnread(@RequestParam("conversationId") Long conversationId, HttpServletRequest request) {
        if (conversationId == null || conversationId <= 0) {
            return ResultUtils.error(40000, "参数错误", "");
        }
        
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            return ResultUtils.error(40100, "未登录", "");
        }
        
        UpdateWrapper<Message> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("conversationId", conversationId)
                     .ne("senderId", loginUser.getId())
                     .eq("is_read", 0)                    
                     .set("is_read", 1);                  

        Boolean result = messageService.update(updateWrapper);
        return ResultUtils.success(result);
    }
}