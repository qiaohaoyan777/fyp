package com.cmt322.usmsecondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmt322.usmsecondhand.common.ErrorCode;
import com.cmt322.usmsecondhand.exception.BusinessException;
import com.cmt322.usmsecondhand.mapper.GoodsMapper;
import com.cmt322.usmsecondhand.mapper.UserReviewMapper;
import com.cmt322.usmsecondhand.model.UserReview;
import com.cmt322.usmsecondhand.model.Category;
import com.cmt322.usmsecondhand.model.Goods;
import com.cmt322.usmsecondhand.model.User;
import com.cmt322.usmsecondhand.model.Wishlist; 
import com.cmt322.usmsecondhand.service.WishlistService;
import com.cmt322.usmsecondhand.model.request.GoodsPublishRequest;
import com.cmt322.usmsecondhand.model.request.GoodsUpdateRequest;
import com.cmt322.usmsecondhand.model.vo.GoodsVO;
import com.cmt322.usmsecondhand.model.vo.UserVO;
import com.cmt322.usmsecondhand.service.CategoryService;
import com.cmt322.usmsecondhand.service.GoodsService;
import com.cmt322.usmsecondhand.service.UserService;
import com.google.gson.Gson;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    @Resource
    private UserService userService;

    @Resource
    private CategoryService categoryService;

    @Lazy
    @Resource
    private WishlistService wishlistService;

    @Resource
    private UserReviewMapper userReviewMapper;

    private final Gson gson = new Gson();

    @Override
    public long publishGoods(GoodsPublishRequest request, User loginUser) {
        if (request == null || StringUtils.isAnyBlank(request.getTitle(), request.getDescription())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (request.getPrice() == null || request.getPrice().doubleValue() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Price cannot be negative");
        }

        Goods goods = new Goods();
        BeanUtils.copyProperties(request, goods);
        goods.setUserId(loginUser.getId());
        goods.setStatus(1); 
        goods.setViewCount(0);
        goods.setLikeCount(0);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            goods.setImages(gson.toJson(request.getImages()));
            if (StringUtils.isBlank(goods.getCoverImage())) {
                goods.setCoverImage(request.getImages().get(0));
            }
        }

        if (request.getContactTypes() != null && !request.getContactTypes().isEmpty()) {
            goods.setContactType(request.getContactTypes().get(0));
        }

        boolean result = this.save(goods);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return goods.getId();
    }

    @Override
    @Transactional
    public boolean updateGoods(GoodsUpdateRequest request, User loginUser) {
        Goods goods = this.getById(request.getId());
        if (goods == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "Goods not found");
        }

        if (!goods.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        BeanUtils.copyProperties(request, goods);
        Goods oldGoods = this.getById(request.getId());
        goods.setUserId(oldGoods.getUserId());
        if (request.getImages() != null) {
            goods.setImages(gson.toJson(request.getImages()));
            if (StringUtils.isBlank(goods.getCoverImage()) && !request.getImages().isEmpty()) {
                goods.setCoverImage(request.getImages().get(0));
            }
        }

        return this.updateById(goods);
    }

    @Override
    public List<GoodsVO> searchGoods(String keyword, Long categoryId) {
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.eq("isDelete", 0);

        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(qw -> qw.like("title", keyword).or().like("description", keyword));
        }
        if (categoryId != null && categoryId > 0) {
            queryWrapper.eq("categoryId", categoryId);
        }

        queryWrapper.orderByDesc("createTime");
        List<Goods> goodsList = this.list(queryWrapper);
        return goodsList.stream().map(this::getGoodsVO).collect(Collectors.toList());
    }

    @Override
    public boolean deleteGoods(long id, User loginUser, boolean isAdmin) {
        Goods goods = this.getById(id);
        if (goods == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (!isAdmin && !goods.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return this.removeById(id); 
    }

    @Override
    public IPage<Goods> listGoods(Integer pageNum, Integer pageSize, String keyword, Long categoryId) {
        Page<Goods> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.like(StringUtils.isNotBlank(keyword), Goods::getTitle, keyword);
        queryWrapper.eq(categoryId != null && categoryId > 0, Goods::getCategoryId, categoryId);
        queryWrapper.eq(Goods::getStatus, 1);
        queryWrapper.eq(Goods::getIsDelete, 0);
        queryWrapper.orderByDesc(Goods::getCreateTime);

        return this.page(page, queryWrapper);
    }

    @Override
    public GoodsVO getGoodsDetail(long id, HttpServletRequest request) {
        Goods goods = this.getById(id);
        if (goods == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        GoodsVO goodsVO = GoodsVO.objToVo(goods);
        goodsVO.setUserId(goods.getUserId());

        long wishlistCount = wishlistService.count(new QueryWrapper<Wishlist>().eq("goodsId", goods.getId()));
        goodsVO.setWishlistCount((int) wishlistCount);

        Long userId = goods.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                
                // 1. 统计在售商品
                long items = this.count(new LambdaQueryWrapper<Goods>().eq(Goods::getUserId, userId).eq(Goods::getStatus, 1));
                userVO.setItemCount((int) items);
                
                // 2. 高精度计算平均评分
                List<UserReview> reviews = userReviewMapper.selectList(new QueryWrapper<UserReview>().eq("seller_id", userId));
                if (reviews != null && !reviews.isEmpty()) {
                    userVO.setReviewCount(reviews.size());
                    
                    // 计算总分
                    double sum = reviews.stream().mapToDouble(UserReview::getRating).sum();
                    double avg = sum / reviews.size();
                    
                    // 🌟 核心改进：使用 BigDecimal 进行四舍五入，保留一位小数
                    BigDecimal bd = new BigDecimal(avg);
                    double finalRating = bd.setScale(1, RoundingMode.HALF_UP).doubleValue();
                    userVO.setRating(finalRating);
                } else {
                    userVO.setReviewCount(0);
                    userVO.setRating(5.0); 
                }

                goodsVO.setUser(userVO);
            }
        }

        Long categoryId = goods.getCategoryId();
        if (categoryId != null && categoryId > 0) {
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                goodsVO.setCategoryName(category.getName());
            }
        }

        return goodsVO;
    }

    // 👇👇👇 核心修改位置：分页获取商品并支持全字段模糊搜索 👇👇👇
    @Override
    public IPage<GoodsVO> listGoodsVOByPage(int current, int size, String keyword, Long categoryId, Integer status) {
        Page<Goods> goodsPage = new Page<>(current, size);
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();

        // 确保永远不会查出被逻辑删除的商品
        queryWrapper.eq("isDelete", 0);

        // 模糊搜索逻辑：只要标题 OR 描述中包含关键字，就搜出来！
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(qw -> qw.like("title", keyword).or().like("description", keyword));
        }

        queryWrapper.eq(categoryId != null && categoryId > 0, "categoryId", categoryId);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.orderByDesc("createTime");

        this.page(goodsPage, queryWrapper);

        List<Goods> goodsList = goodsPage.getRecords();

        List<GoodsVO> goodsVOList = goodsList.stream().map(goods -> {
            GoodsVO goodsVO = new GoodsVO();
            BeanUtils.copyProperties(goods, goodsVO);
            goodsVO.setUserId(goods.getUserId());

            long wishlistCount = wishlistService.count(new QueryWrapper<Wishlist>().eq("goodsId", goods.getId()));
            goodsVO.setWishlistCount((int) wishlistCount);

            User seller = userService.getById(goods.getUserId());
            if (seller != null) {
                goodsVO.setUserName(seller.getUsername());   
                goodsVO.setUserAvatar(seller.getAvatarUrl()); 
            }
            return goodsVO;
        }).collect(Collectors.toList());

        Page<GoodsVO> goodsVOPage = new Page<>(current, size, goodsPage.getTotal());
        goodsVOPage.setRecords(goodsVOList);

        return goodsVOPage;
    }
    // 👆👆👆 修改结束 👆👆👆

    @Override
    public List<GoodsVO> listMyGoods(User loginUser, Integer status) {
        QueryWrapper<Goods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());

        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("createTime");

        List<Goods> goodsList = this.list(queryWrapper);

        return goodsList.stream().map(goods -> {
            GoodsVO vo = GoodsVO.objToVo(goods);
            vo.setUserId(goods.getUserId());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean updateStatus(Long goodsId, Integer status, User loginUser) {
        Goods goods = this.getById(goodsId);
        if (goods == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (!goods.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        Goods updateGoods = new Goods();
        updateGoods.setId(goodsId);
        updateGoods.setStatus(status);
        return this.updateById(updateGoods);
    }

    private GoodsVO getGoodsVO(Goods goods) {
        GoodsVO goodsVO = GoodsVO.objToVo(goods);
        goodsVO.setUserId(goods.getUserId()); 
        Long userId = goods.getUserId();
        if (userId != null && userId > 0) {
            User seller = userService.getById(userId);
            if (seller != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(seller, userVO);
                
                // 1. 统计在售商品
                long items = this.count(new LambdaQueryWrapper<Goods>().eq(Goods::getUserId, userId).eq(Goods::getStatus, 1));
                userVO.setItemCount((int) items);
                
                // 2. 高精度计算平均评分
                List<UserReview> reviews = userReviewMapper.selectList(new QueryWrapper<UserReview>().eq("seller_id", userId));
                if (reviews != null && !reviews.isEmpty()) {
                    userVO.setReviewCount(reviews.size());
                    double sum = reviews.stream().mapToDouble(UserReview::getRating).sum();
                    double avg = sum / reviews.size();
                    
                    BigDecimal bd = new BigDecimal(avg);
                    double finalRating = bd.setScale(1, RoundingMode.HALF_UP).doubleValue();
                    userVO.setRating(finalRating);
                } else {
                    userVO.setReviewCount(0);
                    userVO.setRating(5.0); 
                }

                goodsVO.setUser(userVO);
            }
        }
        return goodsVO;
    }
}