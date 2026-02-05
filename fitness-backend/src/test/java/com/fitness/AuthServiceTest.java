package com.fitness;

import com.fitness.dto.request.LoginRequest;
import com.fitness.dto.request.RegisterRequest;
import com.fitness.dto.response.LoginResponse;
import com.fitness.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证服务测试
 */
@SpringBootTest
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    public void testRegisterAndLogin() {
        // 测试注册
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setAccount("testuser" + System.currentTimeMillis());
        registerRequest.setPassword("123456");
        registerRequest.setName("测试用户");
        registerRequest.setPhone("13800138000");
        registerRequest.setEmail("test@example.com");
        registerRequest.setRole("USER");

        Long userId = authService.register(registerRequest);
        assertNotNull(userId);
        assertTrue(userId > 0);

        // 测试登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setAccount(registerRequest.getAccount());
        loginRequest.setPassword(registerRequest.getPassword());

        LoginResponse loginResponse = authService.login(loginRequest);
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        assertEquals(registerRequest.getAccount(), loginResponse.getAccount());
        assertEquals(registerRequest.getRole(), loginResponse.getRole());

        System.out.println("测试通过！");
        System.out.println("用户ID: " + loginResponse.getUserId());
        System.out.println("Token: " + loginResponse.getToken());
    }
}
