package com.keshe.edumanage.controller;
import com.keshe.edumanage.common.result.Result;
import com.keshe.edumanage.entity.system.User;
import com.keshe.edumanage.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * 根据用户名查询用户
     *
     * 测试接口
     */
    @GetMapping("/{username}")
    public Result<User> getUser(
            @PathVariable("username") String username
    ){
        User user = userService.findByUsername(username);
        return Result.success(user);
    }
}
