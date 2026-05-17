package com.cmt322.usmsecondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmt322.usmsecondhand.model.User;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author 米老头
 * @description 针对表【user(USM Secondhand Trading Platform User Table)】的数据库操作Service
 * @createDate 2025-11-22 14:08:16
 */
public interface UserService extends IService<User> {

    /**
     * 发送注册验证码
     * @param email USM邮箱
     * @return 是否发送成功
     */
    boolean sendRegistrationCode(String email);

    /**
     * 用户注册 (已移除 userAccount)
     * @param username 用户昵称
     * @param userPassword 密码
     * @param checkPassword 校验密码
     * @param usmEmail USM邮箱
     * @param campus 校区
     * @param studentId 学号
     * @param school 学院
     * @param phone 手机号
     * @param emailCode 邮箱验证码
     * @return 新用户id
     */
    long userRegister(String username, String userPassword,
                      String checkPassword, String usmEmail, String campus,
                      String studentId, String school, String phone, String emailCode);

    /**
     * 用户登录 (已改为使用 usmEmail 登录)
     * @param usmEmail USM邮箱
     * @param userPassword 密码
     * @param request HTTP请求
     * @return User
     */
    User userLogin(String usmEmail, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser 原始用户信息
     * @return safeUser 脱敏后的用户信息
     */
    User getSafetyUser(User originUser);

    /**
     * 退出登录
     * @param request HTTP请求
     * @return 成功状态码
     */
    int userLogout(HttpServletRequest request);

    /**
     * 更新用户
     * @param user 待更新的用户信息
     * @param loginUser 当前登录用户
     * @return 成功状态码
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录用户信息
     * @param request HTTP请求
     * @return User
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param request HTTP请求
     * @return true/false
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param loginUser 登录用户对象
     * @return true/false
     */
    boolean isAdmin(User loginUser);

    /**
     * 更新密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param loginUser 当前登录用户
     * @return 是否更新成功
     */
    boolean updatePassword(String oldPassword, String newPassword, User loginUser);

}