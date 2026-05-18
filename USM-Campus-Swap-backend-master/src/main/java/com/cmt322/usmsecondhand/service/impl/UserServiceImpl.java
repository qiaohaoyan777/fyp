package com.cmt322.usmsecondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmt322.usmsecondhand.common.ErrorCode;
import com.cmt322.usmsecondhand.constant.UserConstant;
import com.cmt322.usmsecondhand.exception.BusinessException;
import com.cmt322.usmsecondhand.mapper.UserMapper;
import com.cmt322.usmsecondhand.model.User;
import com.cmt322.usmsecondhand.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cmt322.usmsecondhand.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author 米老头
 * @description 针对表【user(USM Secondhand Trading Platform User Table)】的数据库操作Service实现
 * @createDate 2025-11-22 14:08:16
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService{

    @Resource
    private UserMapper userMapper;

    // 👇 引入发邮件的服务
    @Resource
    private GmailEmailService gmailEmailService;

    // 👇 使用内存安全的 Map 存储验证码和过期时间（避免使用 Redis，方便本地开发）
    private static final Map<String, String> EMAIL_CODE_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Long> EMAIL_CODE_EXPIRE_MAP = new ConcurrentHashMap<>();

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "cmt322";

    // 👇 新增方法：专门用来发送验证码
    @Override
    public boolean sendRegistrationCode(String email) {
        if (StringUtils.isBlank(email) || !email.toLowerCase().endsWith(".usm.my")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Please use a valid USM email (@student.usm.my or @usm.my)");
        }

        // 检查邮箱是否已被注册
        QueryWrapper<User> emailWrapper = new QueryWrapper<>();
        emailWrapper.eq("usmEmail", email);
        if (this.count(emailWrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "This email is already registered");
        }

        // 生成 6 位随机验证码
        String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));
        boolean sent = gmailEmailService.sendVerificationCode(email, code);

        if (sent) {
            EMAIL_CODE_MAP.put(email, code);
            // 设置 5 分钟过期
            EMAIL_CODE_EXPIRE_MAP.put(email, System.currentTimeMillis() + 5 * 60 * 1000);
            return true;
        }
        return false;
    }

    // 🌟 修改点 1：方法参数中彻底删除了 userAccount
    @Override
    public long userRegister(String username, String userPassword,
                             String checkPassword, String usmEmail, String campus,
                             String studentId, String school, String phone, String emailCode) {
        // Validation (将 userAccount 从空值校验中移除，加入 usmEmail)
        if (StringUtils.isAnyBlank(username, usmEmail, userPassword, checkPassword, emailCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Required parameters (including verification code) cannot be empty");
        }
        if (username.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Nickname cannot exceed 20 characters");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Password is too short");
        }
        
        // Passwords must match
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Passwords do not match");
        }
        // USM email validation
        if (!usmEmail.toLowerCase().endsWith(".usm.my")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Please use USM email (@usm.my) to register");
        }
        // Email format validation
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!Pattern.matches(emailRegex, usmEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid email format");
        }

        // 👇 核心逻辑：比对邮箱验证码
        String storedCode = EMAIL_CODE_MAP.get(usmEmail);
        Long expireTime = EMAIL_CODE_EXPIRE_MAP.get(usmEmail);

        if (storedCode == null || expireTime == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Please request a verification code first");
        }
        if (System.currentTimeMillis() > expireTime) {
            // 过期了就清理掉
            EMAIL_CODE_MAP.remove(usmEmail);
            EMAIL_CODE_EXPIRE_MAP.remove(usmEmail);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Verification code has expired");
        }
        if (!storedCode.equals(emailCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Incorrect verification code");
        }
        // 👆 验证码比对结束

        // Phone number format validation (if provided)
        if (StringUtils.isNotBlank(phone)) {
            if (phone.length() < 12 || phone.length() > 13) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid phone number format");
            }
            // Verify if the rest are pure numbers
            String numberPart = phone.substring(3);
            if (!numberPart.matches("^[0-9]+$")) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Phone number contains invalid characters");
            }
        }

        // Student ID format validation (if provided)
        if (StringUtils.isNotBlank(studentId)) {
            if (studentId.length() < 6) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid student ID format");
            }
        }

        // 🌟 修改点 2：移除了校验 userAccount 是否重复的代码，现在只校验 usmEmail
        QueryWrapper<User> emailWrapper = new QueryWrapper<>();
        emailWrapper.eq("usmEmail", usmEmail);
        long emailCount = this.count(emailWrapper);
        if (emailCount > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "This USM email is already registered");
        }

        // Student ID cannot be duplicated (if provided)
        if (StringUtils.isNotBlank(studentId)) {
            QueryWrapper<User> studentIdWrapper = new QueryWrapper<>();
            studentIdWrapper.eq("studentId", studentId);
            long studentIdCount = this.count(studentIdWrapper);
            if (studentIdCount > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "This student ID is already registered");
            }
        }

        // Phone number cannot be duplicated (if provided)
        if (StringUtils.isNotBlank(phone)) {
            QueryWrapper<User> phoneWrapper = new QueryWrapper<>();
            phoneWrapper.eq("phone", phone);
            long phoneCount = this.count(phoneWrapper);
            if (phoneCount > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "This phone number is already registered");
            }
        }

        // Encryption
        String dealPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        User user = new User();
        user.setUsername(username);
        // 🌟 修改点 3：移除了 user.setUserAccount(userAccount);
        user.setUserPassword(dealPassword);
        user.setUsmEmail(usmEmail);
        user.setEmailVerified(0); 
        user.setCampus(campus);
        user.setStudentId(studentId);
        user.setSchool(school);
        user.setPhone(phone);
        user.setGender(0); // Default: unknown gender
        user.setUserStatus(1); // Normal status
        user.setUserRole(0); // Normal user
        user.setAddress(""); // Empty address, can be set later
        user.setIsDelete(0);
        boolean result = this.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Registration failed");
        }

        // 注册成功后，立刻销毁内存中的验证码，防止重放攻击
        EMAIL_CODE_MAP.remove(usmEmail);
        EMAIL_CODE_EXPIRE_MAP.remove(usmEmail);

        return user.getId();
    }

    // 🌟 修改点 4：参数 userAccount 变成了 usmEmail
    @Override
    public User userLogin(String usmEmail, String userPassword, HttpServletRequest request) {
        // 1. 校验逻辑 (移除了 account 的特殊字符和长度限制)
        if (StringUtils.isAnyBlank(usmEmail, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Email or password cannot be empty.");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Password length must be at least 8 characters");
        }

        
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3. 🌟 核心：使用 usmEmail 进行查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("usmEmail", usmEmail);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);

        // 用户不存在或密码错误
        if (user == null) {
            log.info("user login failed, usmEmail cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Incorrect email or password");
        }

        if (user.getIsDelete() != null && user.getIsDelete() == 1) {
            log.warn("Login failed: Account is banned/deleted - {}", usmEmail);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Account has been banned/deleted.");
        }

        // 用户脱敏
        User safetyUser = getSafetyUser(user);

        // 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();

        // 基本信息
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        // 🌟 修改点 5：移除了 safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setUsmEmail(originUser.getUsmEmail());

        safetyUser.setEmailVerified(originUser.getEmailVerified());
        safetyUser.setBalance(originUser.getBalance());
        safetyUser.setAddress(originUser.getAddress());

        // 学校信息
        safetyUser.setCampus(originUser.getCampus());
        safetyUser.setSchool(originUser.getSchool());
        safetyUser.setStudentId(originUser.getStudentId());

        // 系统信息
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());

        safetyUser.setUpdateTime(originUser.getUpdateTime());

        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果不是管理员，只允许更新当前（自己的）信息
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    @Override
    public boolean updatePassword(String oldPassword, String newPassword, User loginUser) {
        // 1. 参数校验
        if (StringUtils.isAnyBlank(oldPassword, newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Password cannot be empty");
        }

        // 2. 新旧密码不能相同
        if (oldPassword.equals(newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "New password cannot be the same as old password");
        }

        // 3. 新密码长度验证（至少8位）
        if (newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "New password must be at least 8 characters");
        }

        // 4. 验证旧密码
        Long userId = loginUser.getId();
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "User not found");
        }

        String encryptedOldPassword = DigestUtils.md5DigestAsHex((SALT + oldPassword).getBytes());
        if (!user.getUserPassword().equals(encryptedOldPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Old password is incorrect");
        }

        // 5. 加密新密码并更新
        String encryptedNewPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        user.setUserPassword(encryptedNewPassword);

        return this.updateById(user);
    }
}