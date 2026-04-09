package com.cmt322.usmsecondhand.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cmt322.usmsecondhand.common.BaseResponse;
import com.cmt322.usmsecondhand.common.ErrorCode;
import com.cmt322.usmsecondhand.common.ResultUtils;
import com.cmt322.usmsecondhand.exception.BusinessException;
import com.cmt322.usmsecondhand.model.Goods;
import com.cmt322.usmsecondhand.model.Message;
import com.cmt322.usmsecondhand.model.User;
import com.cmt322.usmsecondhand.model.request.GoodsPublishRequest;
import com.cmt322.usmsecondhand.model.request.GoodsUpdateRequest;
import com.cmt322.usmsecondhand.model.vo.GoodsVO;
import com.cmt322.usmsecondhand.service.GoodsService;
import com.cmt322.usmsecondhand.service.MessageService;
import com.cmt322.usmsecondhand.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品接口
 */
@RestController
@RequestMapping("/goods")
@Slf4j
public class GoodsController {

    @Resource
    private UserService userService;

    @Resource
    private GoodsService goodsService;

    @Resource
    private MessageService messageService;

    // --- 1. 管理员下架功能 (新增) ---

    /**
     * 下架请求参数类
     */
    @Data
    public static class TakedownRequest {
        private Long goodsId;
        private Long sellerId;
        private String goodsTitle;
        private String reason;
    }

    /**
     * 管理员下架商品并自动发送系统通知
     */
    @PostMapping("/admin/takedown")
    public BaseResponse<Boolean> adminTakedownGoods(@RequestBody TakedownRequest request, HttpServletRequest httpRequest) {
        // 🔒 权限校验：必须是管理员
        if (!userService.isAdmin(httpRequest)) {
            // 这里使用了你 ErrorCode.java 中真实的 NO_AUTH
            throw new BusinessException(ErrorCode.NO_AUTH, "Admin authority required.");
        }

        if (request.getGoodsId() == null || request.getSellerId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 1. 将商品状态设为 2 (下架状态)
        Goods goods = new Goods();
        goods.setId(request.getGoodsId());
        goods.setStatus(2); 
        boolean updateResult = goodsService.updateById(goods);

        // 2. 发送系统私信通知卖家
        if (updateResult) {
            Message systemMsg = new Message();
            systemMsg.setConversationId(0L); // 系统级对话
            systemMsg.setSenderId(0L);       // 发送者为系统 (ID: 0)
            systemMsg.setReceiverId(request.getSellerId());
            systemMsg.setType(1);            // 文本消息
            systemMsg.setIsRead(0);          // 标记未读，触发红点提示
            
            // 拼写英文通知内容
            String content = "📢 [System Notice]: Your item \"" + request.getGoodsTitle() + 
                             "\" has been taken down due to violation. Reason: " + request.getReason() + 
                             ". Please comply with university marketplace regulations.";
            systemMsg.setContent(content);
            
            messageService.save(systemMsg);
        }

        return ResultUtils.success(true);
    }

    // --- 2. 基础查询接口 ---

    @GetMapping("/recommend")
    public BaseResponse<List<GoodsVO>> getRecommendGoods(HttpServletRequest request) {
        List<GoodsVO> list = goodsService.searchGoods(null, null);
        return ResultUtils.success(list);
    }

    @GetMapping("/search")
    public BaseResponse<List<GoodsVO>> searchGoods(@RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Long categoryId) {
        List<GoodsVO> list = goodsService.searchGoods(keyword, categoryId);
        return ResultUtils.success(list);
    }

    @GetMapping("/list/page/vo")
    public BaseResponse<IPage<GoodsVO>> listGoodsVOByPage(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {

        IPage<GoodsVO> result = goodsService.listGoodsVOByPage(current, size, title, categoryId, status);
        return ResultUtils.success(result);
    }

    @GetMapping("/my/list")
    public BaseResponse<List<GoodsVO>> listMyGoods(@RequestParam(required = false) Integer status, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<GoodsVO> list = goodsService.listMyGoods(loginUser, status);
        return ResultUtils.success(list);
    }

    // --- 3. 详情获取接口 ---

    @GetMapping("/get/{id}")
    public BaseResponse<GoodsVO> getGoodsDetail(@PathVariable Long id, HttpServletRequest httpRequest) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        GoodsVO goodsVO = goodsService.getGoodsDetail(id, httpRequest);
        return ResultUtils.success(goodsVO);
    }

    @GetMapping("/{id}")
    public BaseResponse<GoodsVO> getGoodsVOById(@PathVariable Long id, HttpServletRequest httpRequest) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        GoodsVO goodsVO = goodsService.getGoodsDetail(id, httpRequest);
        return ResultUtils.success(goodsVO);
    }

    // --- 4. 业务操作接口 ---

    @PostMapping("/publish")
    public BaseResponse<Long> publishGoods(@RequestBody GoodsPublishRequest request, HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpRequest);
        long goodsId = goodsService.publishGoods(request, loginUser);
        return ResultUtils.success(goodsId);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateGoods(@RequestBody GoodsUpdateRequest request, HttpServletRequest httpRequest) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid goods data");
        }
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = goodsService.updateGoods(request, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteGoods(@RequestBody long id, HttpServletRequest httpRequest) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpRequest);
        boolean isAdmin = userService.isAdmin(httpRequest);
        boolean result = goodsService.deleteGoods(id, loginUser, isAdmin);
        return ResultUtils.success(result);
    }

    @PostMapping("/status")
    public BaseResponse<Boolean> updateGoodsStatus(@RequestBody GoodsUpdateRequest request, HttpServletRequest httpRequest) {
        if (request == null || request.getId() == null || request.getStatus() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = goodsService.updateStatus(request.getId(), request.getStatus(), loginUser);
        return ResultUtils.success(result);
    }
}