package com.ssafy.videoconference.config.security.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import com.ssafy.videoconference.config.util.TokenUtils;
import com.ssafy.videoconference.model.user.bean.User;
import com.ssafy.videoconference.model.user.bean.UserDetail;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class CustomLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private TokenUtils tokenUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        User user = ((UserDetail)authentication.getPrincipal()).getUser();
        String token = tokenUtils.generateJwtToken(user);
        response.addHeader("Authorization", "Bearer " + token);
    }

}