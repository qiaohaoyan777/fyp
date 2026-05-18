package com.cmt322.usmsecondhand.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cmt322.usmsecondhand.common.BaseResponse;
import com.cmt322.usmsecondhand.common.ErrorCode;
import com.cmt322.usmsecondhand.common.ResultUtils;
import com.cmt322.usmsecondhand.constant.UserConstant;
import com.cmt322.usmsecondhand.exception.BusinessException;
import com.cmt322.usmsecondhand.model.User;
import com.cmt322.usmsecondhand.model.request.*;
import com.cmt322.usmsecondhand.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.lang3.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

import static com.cmt322.usmsecondhand.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author hanyin
 */
@Slf4j
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true")
public class UserController {
    @Resource
    private UserService userService;

    // 👇 发送邮箱验证码
    @GetMapping("/send-code")
    public BaseResponse<String> sendCode(@RequestParam String email) {
        if (StringUtils.isBlank(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Email cannot be empty");
        }
        boolean result = userService.sendRegistrationCode(email);
        if (result) {
            return ResultUtils.success("Verification code sent successfully");
        } else {
            return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "Failed to send verification code");
        }
    }

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String username = userRegisterRequest.getUserName();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String usmEmail = userRegisterRequest.getUsmEmail();
        String campus   = userRegisterRequest.getCampus();
        String studentId = userRegisterRequest.getStudentId();
        String school = userRegisterRequest.getSchool();
        String phone = userRegisterRequest.getPhone();
        String emailCode = userRegisterRequest.getEmailCode(); 

        if (StringUtils.isAnyBlank(username, userPassword, checkPassword, usmEmail, campus, school, emailCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Required parameters missing");
        }
        
        long result = userService.userRegister(username, userPassword, checkPassword, usmEmail, campus, studentId, school, phone, emailCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String usmEmail = userLoginRequest.getUsmEmail();
        String userPassword = userLoginRequest.getUserPassword();
        
        if (StringUtils.isAnyBlank(usmEmail, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        User user = userService.userLogin(usmEmail, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();

        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    // 🌟 新增：获取所有真实用户列表（管理员专用），彻底解决前端 404 报错！
    @GetMapping("/list")
    public BaseResponse<List<User>> listUsers(HttpServletRequest request) {
        // 1. 严格校验管理员权限
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "Access Denied: Admins only");
        }
        // 2. 查询数据库中所有用户
        List<User> userList = userService.list();
        // 3. 安全脱敏（防止将所有用户的密码发给前端）
        List<User> safeUserList = userList.stream()
                .map(user -> userService.getSafetyUser(user))
                .collect(Collectors.toList());
                
        return ResultUtils.success(safeUserList);
    }

    @PostMapping("/update")
    public BaseResponse<String> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        User user = new User();
        user.setId(loginUser.getId()); 

        if (userUpdateRequest.getAvatarUrl() != null) {
            user.setAvatarUrl(userUpdateRequest.getAvatarUrl());
        }
        if (userUpdateRequest.getUsername() != null) {
            user.setUsername(userUpdateRequest.getUsername());
        }
        if (userUpdateRequest.getPhone() != null) {
            user.setPhone(userUpdateRequest.getPhone());
        }
        if (userUpdateRequest.getGender() != null) {
            user.setGender(userUpdateRequest.getGender());
        }
        if (userUpdateRequest.getCampus() != null) {
            user.setCampus(userUpdateRequest.getCampus());
        }
        if (userUpdateRequest.getStudentId() != null) {
            user.setStudentId(userUpdateRequest.getStudentId());
        }
        if (userUpdateRequest.getSchool() != null) {
            user.setSchool(userUpdateRequest.getSchool());
        }
        if (userUpdateRequest.getAddress() != null) {
            user.setAddress(userUpdateRequest.getAddress());
        }

        boolean result = userService.updateById(user);

        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Update failed");
        }

        User newUser = userService.getById(user.getId());
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, newUser);

        return ResultUtils.success("Update success");
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUsers(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    @PostMapping("/change-password")
    public BaseResponse<Boolean> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest,
                                                HttpServletRequest request) {
        if (changePasswordRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        boolean result = userService.updatePassword(
                changePasswordRequest.getOldPassword(),
                changePasswordRequest.getNewPassword(),
                loginUser
        );

        return ResultUtils.success(result);
    }

    @GetMapping("/list/page")
    public BaseResponse<IPage<User>> listUserByPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Page<User> userPage = new Page<>(current, size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(username), "username", username);
        queryWrapper.orderByDesc("createTime");

        return ResultUtils.success(userService.page(userPage, queryWrapper));
    }

    // 🌟 后端封禁用户的专用接口，注意它接收的参数是 status，修改的是 isDelete
    @PostMapping("/update/status")
    public BaseResponse<Boolean> updateUserStatus(@RequestBody UserStatusUpdateRequest request, HttpServletRequest httpRequest) {
        if (!userService.isAdmin(httpRequest)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", request.getId());
        updateWrapper.set("isDelete", request.getStatus());

        return ResultUtils.success(userService.update(null, updateWrapper));
    }
    /**
     * 🚨 管理员功能：警告违规用户
     */
    /**
     * 🚨 管理员功能：警告违规用户 (真实数据库写入版)
     */
    @PostMapping("/warn")
    public BaseResponse<Boolean> warnUser(@RequestBody java.util.Map<String, Object> params) {
        if (params.get("id") == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "缺少用户ID");
        }
        
        Long id = Long.valueOf(params.get("id").toString());
        String message = (String) params.get("message");
        
        // 🚀 1. 实例化一个只有 ID 和警告信息的 User 对象
        // 注意：这里假设你的 User 实体类里已经加上了 warningMsg 字段
        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setWarningMsg(message); // 把前端传来的警告内容塞进去
        
        // 🚀 2. 真正写入数据库
        // 注意：如果你的 service 叫别的名字，请改成你自己的 userService
        boolean result = userService.updateById(updateUser);
        
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "警告发送失败，用户不存在");
        }
        
        return ResultUtils.success(true);
    }
    /**
     * 🚨 获取用户的最新状态（包含警告信息）
     */
    @GetMapping("/get")
    public BaseResponse<User> getUserById(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "缺少用户ID");
        }
        // 直接从数据库生抠出最新数据返回，确保 warningMsg 绝对存在！
        User user = userService.getById(id);
        
        // 为了安全，把密码抹掉再传给前端
        if (user != null) {
            user.setUserPassword(null);
        }
        return ResultUtils.success(user);
    }
    /**
     * 🚨 用户自行清空警告信息（点击“我明白”后触发）
     */
    @PostMapping("/clearWarning")
    public BaseResponse<Boolean> clearWarning(HttpServletRequest request) {
        // 1. 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        
        // 2. 强行把该用户的 warningMsg 设为空字符串
        User user = new User();
        user.setId(loginUser.getId());
        user.setWarningMsg(""); // 👈 关键点：清空！
        
        // 3. 更新数据库
        boolean result = userService.updateById(user);
        return ResultUtils.success(result);
    }
}