package com.cmt322.usmsecondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmt322.usmsecondhand.common.ErrorCode;
import com.cmt322.usmsecondhand.exception.BusinessException;
import com.cmt322.usmsecondhand.mapper.ConversationMapper;
import com.cmt322.usmsecondhand.model.Conversation;
import com.cmt322.usmsecondhand.model.Goods;
import com.cmt322.usmsecondhand.model.User;
import com.cmt322.usmsecondhand.service.ConversationService;
import com.cmt322.usmsecondhand.service.GoodsService;
import com.cmt322.usmsecondhand.service.UserService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class ConversationServiceImpl
        extends ServiceImpl<ConversationMapper, Conversation>
        implements ConversationService {

    @Resource
    private GoodsService goodsService;

    @Resource
    private UserService userService;

    @Override
    public Long open(Long goodsId, Long buyerId) {
        Goods goods = goodsService.getById(goodsId);
        if (goods == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "Item not found");
        }

        Long sellerId = goods.getUserId();

        if (buyerId.equals(sellerId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "You cannot chat with yourself.");
        }

        // 普通聊天，type = 1 (或者 null 兼容老数据)
        Conversation c = this.getOne(
                new QueryWrapper<Conversation>()
                        .eq("goodsId", goodsId)
                        .eq("buyerId", buyerId)
                        .ne("type", 2) // 排除系统通知
        );

        if (c != null) return c.getId();

        Conversation nc = new Conversation();
        nc.setGoodsId(goodsId);
        nc.setBuyerId(buyerId);
        nc.setSellerId(sellerId);
        nc.setType(1); // 1 = 普通私聊
        this.save(nc);

        return nc.getId();
    }

    // ★★★ 核心新增：专门为用户创建/获取唯一的系统通知聊天框 ★★★
    @Override
    public Long openSystemConversation(Long userId) {
        // 找找看这个用户是不是已经有系统聊天框了
        Conversation c = this.getOne(
                new QueryWrapper<Conversation>()
                        .eq("type", 2) // 2 = 系统通知
                        .eq("sellerId", userId) // 统一下发给 sellerId 位置的用户
        );

        if (c != null) return c.getId();

        // 如果没有，就建一个全新的系统通知专属聊天框
        Conversation nc = new Conversation();
        nc.setGoodsId(0L); // 0 代表这是全局通知，不绑定特定商品
        nc.setBuyerId(0L); // 0 代表系统官方
        nc.setSellerId(userId); // 接收通知的用户
        nc.setType(2); // ★★★ 2 = 标记为系统会话
        this.save(nc);

        return nc.getId();
    }

    @Override
    public List<Conversation> listMy(Long currentUserId) {
        QueryWrapper<Conversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper.eq("buyerId", currentUserId).or().eq("sellerId", currentUserId));
        queryWrapper.orderByDesc("lastTime"); 
        List<Conversation> conversationList = this.list(queryWrapper);

        for (Conversation conv : conversationList) {
            // ★★★ 核心修改：利用 type 彻底区分系统与普通用户 ★★★
            if (conv.getType() != null && conv.getType() == 2) {
                // 如果是系统会话，直接赋予官方名字和头像
                conv.setTargetName("System Notification");
                conv.setTargetAvatar("https://api.dicebear.com/7.x/bottts/svg?seed=USMSystem&backgroundColor=00897B");
            } else {
                // 普通用户的逻辑保持不变
                Long targetId = conv.getSellerId().equals(currentUserId) ? conv.getBuyerId() : conv.getSellerId();
                User targetUser = userService.getById(targetId);

                if (targetUser != null) {
                    conv.setTargetName(targetUser.getUsername());
                    conv.setTargetAvatar(targetUser.getAvatarUrl());
                } else {
                    conv.setTargetName("Unknown User");
                }
            }
        }
        return conversationList;
    }
}