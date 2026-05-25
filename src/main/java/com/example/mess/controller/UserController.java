package com.example.mess.controller;

import com.example.mess.dto.ApiResponse;
import com.example.mess.dto.UserDto;
import com.example.mess.entity.User;
import com.example.mess.exception.ResourceNotFoundException;
import com.example.mess.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户管理相关API")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "获取所有用户", description = "返回所有用户的列表")
    public ApiResponse<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ApiResponse.success("获取用户列表成功", users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户", description = "根据用户ID获取用户信息")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(u -> ApiResponse.success("获取用户成功", u))
                   .orElseThrow(() -> new ResourceNotFoundException("用户不存在，ID: " + id));
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名获取用户", description = "根据用户名获取用户信息")
    public ApiResponse<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.getUserByUsername(username);
        return user.map(u -> ApiResponse.success("获取用户成功", u))
                   .orElseThrow(() -> new ResourceNotFoundException("用户不存在，用户名: " + username));
    }

    @PostMapping
    @Operation(summary = "创建新用户", description = "创建一个新的用户")
    public ApiResponse<User> createUser(@Valid @RequestBody UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());
        
        User createdUser = userService.createUser(user);
        log.info("创建新用户成功: {}", createdUser.getUsername());
        return ApiResponse.success("创建用户成功", createdUser);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息", description = "根据用户ID更新用户信息")
    public ApiResponse<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        User userDetails = new User();
        userDetails.setUsername(userDto.getUsername());
        userDetails.setEmail(userDto.getEmail());
        userDetails.setName(userDto.getName());
        
        User updatedUser = userService.updateUser(id, userDetails);
        log.info("更新用户成功: {}", updatedUser.getUsername());
        return ApiResponse.success("更新用户成功", updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "根据用户ID删除用户")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        log.info("删除用户成功，ID: {}", id);
        return ApiResponse.success("删除用户成功");
    }
}