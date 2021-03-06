package com.ssafy.videoconference.config.security.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.videoconference.model.user.bean.User;

public class CustomLogoutHandler implements LogoutHandler {

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		System.out.println("logout - redis delete");

		String accessToken = request.getHeader("accessToken").substring(7);
		System.out.println(accessToken);
		
		String refreshTokenKey = redisTemplate.opsForValue().get(accessToken) + "_refreshToken";
		
		redisTemplate.delete(accessToken);
		redisTemplate.delete(refreshTokenKey);

		System.out.println("Logout Success");
		response.setStatus(HttpStatus.OK.value());
	}

}
