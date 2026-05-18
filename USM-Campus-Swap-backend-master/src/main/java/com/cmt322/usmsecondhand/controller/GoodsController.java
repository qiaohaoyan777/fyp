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

    // --- 1. 管理员下架功能 ---

    @Data
    public static class TakedownRequest {
        private Long goodsId;
        private Long sellerId;
        private String goodsTitle;
        private String reason;
    }

    @PostMapping("/admin/takedown")
    public BaseResponse<Boolean> adminTakedownGoods(@RequestBody TakedownRequest request, HttpServletRequest httpRequest) {
        if (!userService.isAdmin(httpRequest)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "Admin authority required.");
        }

        if (request.getGoodsId() == null || request.getSellerId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Goods goods = new Goods();
        goods.setId(request.getGoodsId());
        goods.setStatus(2); 
        boolean updateResult = goodsService.updateById(goods);

        if (updateResult) {
            Message systemMsg = new Message();
            systemMsg.setConversationId(0L); 
            systemMsg.setSenderId(0L);       
            systemMsg.setReceiverId(request.getSellerId());
            systemMsg.setType(1);            
            systemMsg.setIsRead(0);          
            
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

    // 🌟 核心修复区：专为前端管理员大本营打造的真实数据接口
    // 🌟 修复图片裂开问题：返回格式化好的 GoodsVO，自带图片解析！
    @GetMapping("/admin/list")
    public BaseResponse<List<GoodsVO>> getAdminGoodsList(HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "Access Denied: Admins only");
        }
        // 利用现有的分页服务，传入极大的 pageSize 来一次性查出所有商品，这样就自动执行了 VO 的图片格式化转换！
        IPage<GoodsVO> result = goodsService.listGoodsVOByPage(1, 99999, null, null, null);
        return ResultUtils.success(result.getRecords());
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
        
        // 🌟 核心修复区：管理员专属“一票否决”通道
        // 如果是管理员发起的请求，并且带有 status 参数（说明是在面板点击了“下架/重新上架”）
        if (userService.isAdmin(httpRequest) && request.getStatus() != null) {
            Goods goods = new Goods();
            goods.setId(request.getId());
            goods.setStatus(request.getStatus());
            // 直接强行更新状态，绕过普通卖家的身份校验限制！
            return ResultUtils.success(goodsService.updateById(goods));
        }

        // 普通卖家修改自己的商品逻辑
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
    /**
     * 🚨 管理员功能：彻底删除违规商品
     */
   /**
     * 🚨 管理员专属功能：彻底删除违规商品
     */
    @PostMapping("/admin/delete")  // 👈 就是把这里改了，加上 /admin，避开原有的接口！
    public BaseResponse<Boolean> deleteGoodsAdmin(@RequestBody java.util.Map<String, Object> params) {
        // 下面的代码完全不用动...
        if (params.get("id") == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "缺少商品ID");
        }
        Long id = Long.valueOf(params.get("id").toString());
        boolean result = goodsService.removeById(id);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败，该商品可能已被删除或不存在");
        }
        System.out.println("🗑️ [系统最高指令] 商品已被彻底抹除，Goods ID: " + id);
        return ResultUtils.success(true);
    }
}