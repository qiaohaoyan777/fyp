package com.cmt322.usmsecondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmt322.usmsecondhand.common.ErrorCode;
import com.cmt322.usmsecondhand.exception.BusinessException;
import com.cmt322.usmsecondhand.mapper.OrdersMapper;
// 🌟 新增引入
import com.cmt322.usmsecondhand.mapper.UserReviewMapper;
import com.cmt322.usmsecondhand.model.UserReview;

import com.cmt322.usmsecondhand.model.Goods;
import com.cmt322.usmsecondhand.model.Orders;
import com.cmt322.usmsecondhand.model.User;
import com.cmt322.usmsecondhand.model.Message;
import com.cmt322.usmsecondhand.service.MessageService;
import com.cmt322.usmsecondhand.service.ConversationService;

import com.cmt322.usmsecondhand.model.request.OrderCreateRequest;
import com.cmt322.usmsecondhand.model.request.OrderPayRequest;
import com.cmt322.usmsecondhand.model.vo.OrdersVO;
import com.cmt322.usmsecondhand.service.GoodsService;
import com.cmt322.usmsecondhand.service.OrderService;
import com.cmt322.usmsecondhand.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {

    @Resource
    private GoodsService goodsService;

    @Resource
    private UserService userService;

    @Resource
    private MessageService messageService;

    @Resource
    private ConversationService conversationService;

    // 🌟 注入 ReviewMapper 用来查评价
    @Resource
    private UserReviewMapper userReviewMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Orders createOrder(OrderCreateRequest request, User loginUser) {
        Long goodsId = request.getGoodsId();
        if (goodsId == null || goodsId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Goods goods = goodsService.getById(goodsId);
        if (goods == null || goods.getStatus() != 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Item is no longer available");
        }
        if (goods.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Cannot buy your own item");
        }
        goods.setStatus(2);
        goodsService.updateById(goods);

        Orders order = new Orders();
        order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
        order.setGoodsId(goodsId);
        order.setBuyerId(loginUser.getId());
        order.setSellerId(goods.getUserId());
        order.setTotalAmount(goods.getPrice());
        order.setQuantity(1);
        order.setDeliveryMethod(request.getDeliveryMethod());
        if (request.getDeliveryMethod() != null && request.getDeliveryMethod() == 2) {
            order.setExpressAddress(request.getAddressLocation());
        } else {
            order.setPickupLocation(request.getAddressLocation());
        }
        order.setPaymentMethod(request.getPaymentMethod());
        order.setBuyerNote(request.getBuyerNote());
        order.setOrderStatus(2); // Paid
        order.setCreateTime(new Date());
        this.save(order);
        
        sendSystemNotificationToSeller(goods, loginUser, request.getPaymentMethod());

        return order;
    }

    @Override
    public List<OrdersVO> listMyOrders(User loginUser, String role) {
        Long userId = loginUser.getId();
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();

        if ("buyer".equalsIgnoreCase(role)) {
            queryWrapper.eq("buyerId", userId);
        } else if ("seller".equalsIgnoreCase(role)) {
            queryWrapper.eq("sellerId", userId);
        } else {
            queryWrapper.and(wrapper -> wrapper.eq("buyerId", userId).or().eq("sellerId", userId));
        }

        queryWrapper.orderByDesc("createTime");
        List<Orders> ordersList = this.list(queryWrapper);

        return ordersList.stream().map(order -> {
            OrdersVO vo = OrdersVO.objToVo(order);
            fillOrderVO(vo, userId);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public OrdersVO getOrderDetail(long orderId, User loginUser) {
        Orders order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long userId = loginUser.getId();
        if (!order.getBuyerId().equals(userId) &&
                !order.getSellerId().equals(userId) &&
                !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        OrdersVO vo = OrdersVO.objToVo(order);
        fillOrderVO(vo, userId);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(long orderId, User loginUser) {
        Orders order = this.getById(orderId);
        if (order == null) return false;

        if (!order.getBuyerId().equals(loginUser.getId()) && !order.getSellerId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (order.getOrderStatus() >= 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Cannot cancel completed order");
        }

        order.setOrderStatus(5); // Cancelled
        boolean updateOrder = this.updateById(order);

        Goods goods = goodsService.getById(order.getGoodsId());
        if (goods != null) {
            goods.setStatus(1); // Back to Available
            goodsService.updateById(goods);
        }
        return updateOrder;
    }

    @Override
    public boolean completeOrder(long orderId, User loginUser) {
        Orders order = this.getById(orderId);
        if (order == null) return false;
        if (!order.getBuyerId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        order.setOrderStatus(4); // Completed
        order.setCompleteTime(new Date());
        return this.updateById(order);
    }

    private void fillOrderVO(OrdersVO vo, Long currentUserId) {
        Goods goods = goodsService.getById(vo.getGoodsId());
        if (goods != null) {
            vo.setGoodsTitle(goods.getTitle());
            if (goods.getCoverImage() != null) {
                vo.setGoodsImage(goods.getCoverImage());
            } else if (goods.getImages() != null) {
                vo.setGoodsImage(goods.getImages().toString());
            }
            vo.setDeliveryMethod(goods.getDeliveryMethod());
            vo.setCampus(goods.getCampus());
            vo.setAddress(goods.getAddress());
        }
        Long counterpartyId = vo.getBuyerId().equals(currentUserId) ? vo.getSellerId() : vo.getBuyerId();
        User user = userService.getById(counterpartyId);
        if (user != null) {
            vo.setCounterpartyName(user.getUsername());
            vo.setCounterpartyAvatar(user.getAvatarUrl());
        }

        // 🌟 核心修改：这里使用 setHasReview()
        QueryWrapper<UserReview> reviewQuery = new QueryWrapper<>();
        reviewQuery.eq("order_id", vo.getId());
        UserReview review = userReviewMapper.selectOne(reviewQuery);
        
        if (review != null) {
            vo.setHasReview(true); // 使用新字段名
            vo.setReviewRating(review.getRating());
            vo.setReviewContent(review.getContent());
        } else {
            vo.setHasReview(false); // 使用新字段名
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processPayment(OrderPayRequest request, User buyer) {
        List<Long> itemIds = request.getItemIds();
        Integer payMethod = request.getPayMethod();
        BigDecimal amount = request.getAmount();

        if (itemIds == null || itemIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "No items selected");
        }

        List<Goods> goodsList = goodsService.listByIds(itemIds);
        if (goodsList.size() != itemIds.size()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Some items are invalid or removed");
        }

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        for (Goods goods : goodsList) {
            if (goods.getStatus() != 1) { 
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Item '" + goods.getTitle() + "' is already sold");
            }
            if (goods.getUserId().equals(buyer.getId())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Cannot buy your own item: " + goods.getTitle());
            }
            calculatedTotal = calculatedTotal.add(goods.getPrice());
        }

        if (request.getPayMethod() == 1) { 
            if (buyer.getBalance().compareTo(amount) < 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "Insufficient wallet balance");
            }
            buyer.setBalance(buyer.getBalance().subtract(amount));
            userService.updateById(buyer);
        }

        for (Goods goods : goodsList) {
            goods.setStatus(2);
            boolean updateGoods = goodsService.updateById(goods);
            if (!updateGoods) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Failed to update goods status");
            }

            Orders order = new Orders();
            String orderNo = "ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            order.setOrderNo(orderNo);
            order.setGoodsId(goods.getId());
            order.setBuyerId(buyer.getId());
            order.setSellerId(goods.getUserId());
            order.setTotalAmount(goods.getPrice());
            order.setQuantity(1);
            order.setPaymentMethod(payMethod);

            order.setOrderStatus(payMethod == 1 ? 2 : 1);
            if (payMethod == 1) {
                order.setPaymentTime(new Date());
            }
            order.setCreateTime(new Date());

            this.save(order);
            sendSystemNotificationToSeller(goods, buyer, payMethod);
        }

        return true;
    }

    @Override
    public IPage<OrdersVO> listOrderVOByPage(int current, int size, String orderNo, Integer status) {
        Page<Orders> orderPage = new Page<>(current, size);
        QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq(StringUtils.isNotBlank(orderNo), "orderNo", orderNo);
        queryWrapper.eq(status != null, "orderStatus", status); 
        queryWrapper.orderByDesc("createTime");

        this.page(orderPage, queryWrapper);

        List<OrdersVO> voList = orderPage.getRecords().stream().map(ordersEntity -> {
            OrdersVO vo = new OrdersVO();
            BeanUtils.copyProperties(ordersEntity, vo);

            User buyer = userService.getById(ordersEntity.getBuyerId());
            User seller = userService.getById(ordersEntity.getSellerId());
            if (buyer != null) vo.setBuyerName(buyer.getUsername());
            if (seller != null) vo.setSellerName(seller.getUsername());

            Goods goods = goodsService.getById(ordersEntity.getGoodsId());
            if (goods != null) {
                vo.setGoodsTitle(goods.getTitle());
                vo.setDeliveryMethod(goods.getDeliveryMethod());
                vo.setCampus(goods.getCampus());
                vo.setAddress(goods.getAddress());
            }

            return vo;
        }).collect(Collectors.toList());

        Page<OrdersVO> voPage = new Page<>(current, size, orderPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    private void sendSystemNotificationToSeller(Goods goods, User buyer, Integer payMethod) {
        try {
            Long conversationId = conversationService.openSystemConversation(goods.getUserId());
            Message sysMsg = new Message();
            sysMsg.setConversationId(conversationId);
            sysMsg.setSenderId(0L); 

            String payStr = (payMethod != null && payMethod == 1) ? "Wallet Balance" : "Cash/Offline Payment";
            String content = String.format(
                "📢 System Alert: Great news! Your item [%s] has been purchased by [%s] via [%s]. " + 
                "Please check your orders and arrange the transaction with the buyer soon!",
                goods.getTitle(), buyer.getUsername(), payStr
            );

            sysMsg.setContent(content);
            sysMsg.setType(1); 
            sysMsg.setCreateTime(new Date());
            
            messageService.save(sysMsg);
            log.info("System notification successfully sent to seller ID: {}", goods.getUserId());
            
        } catch (Exception e) {
            log.error("Failed to send system alert to seller for goods ID: {}", goods.getId(), e);
        }
    }
}