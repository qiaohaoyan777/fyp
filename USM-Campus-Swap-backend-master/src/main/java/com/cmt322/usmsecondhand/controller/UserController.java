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
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.lang3.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

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

    // 🌟 修改点 1：清理注册接口中的 userAccount
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String username = userRegisterRequest.getUserName();
        // 删除了 String userAccount = ...
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String usmEmail = userRegisterRequest.getUsmEmail();
        String campus   = userRegisterRequest.getCampus();
        String studentId = userRegisterRequest.getStudentId();
        String school = userRegisterRequest.getSchool();
        String phone = userRegisterRequest.getPhone();
        String emailCode = userRegisterRequest.getEmailCode(); 

        // 移除了 userAccount 的非空校验
        if (StringUtils.isAnyBlank(username, userPassword, checkPassword, usmEmail, campus, school, emailCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Required parameters missing");
        }
        
        // 移除了传给 service 层的 userAccount 参数
        long result = userService.userRegister(username, userPassword, checkPassword, usmEmail, campus, studentId, school, phone, emailCode);
        return ResultUtils.success(result);
    }

    // 🌟 修改点 2：将登录接口的账号改为邮箱
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将 userAccount 改成了 usmEmail
        String usmEmail = userLoginRequest.getUsmEmail();
        String userPassword = userLoginRequest.getUserPassword();
        
        if (StringUtils.isAnyBlank(usmEmail, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 传给 service 层的参数改为 usmEmail
        User user = userService.userLogin(usmEmail, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
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


    @PostMapping("/update")
    public BaseResponse<String> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {

        if (userUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 2. 准备更新对象
        User user = new User();
        user.setId(loginUser.getId()); // 必填：ID

        // 3. 🛡️ 手动赋值
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

        // 4. 执行更新
        boolean result = userService.updateById(user);

        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Update failed");
        }

        // 5. 刷新 Session
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

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        boolean result = userService.updatePassword(
                changePasswordRequest.getOldPassword(),
                changePasswordRequest.getNewPassword(),
                loginUser
        );

        return ResultUtils.success(result);
    }

    /**
     * 分页获取用户列表（仅管理员）
     */
    @GetMapping("/list/page")
    public BaseResponse<IPage<User>> listUserByPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            HttpServletRequest request) {
        // 权限校验
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Page<User> userPage = new Page<>(current, size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(username), "username", username);
        queryWrapper.orderByDesc("createTime");

        return ResultUtils.success(userService.page(userPage, queryWrapper));
    }

    /**
     * 更新用户状态（封禁/启用）
     */
    @PostMapping("/update/status")
    public BaseResponse<Boolean> updateUserStatus(@RequestBody UserStatusUpdateRequest request, HttpServletRequest httpRequest) {
        if (!userService.isAdmin(httpRequest)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        // 使用 UpdateWrapper 强制更新
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", request.getId());
        updateWrapper.set("isDelete", request.getStatus());

        return ResultUtils.success(userService.update(null, updateWrapper));
    }

}