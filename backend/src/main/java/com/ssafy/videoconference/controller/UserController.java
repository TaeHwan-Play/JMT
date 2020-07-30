package com.ssafy.videoconference.controller;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.videoconference.config.util.TokenUtils;
import com.ssafy.videoconference.model.user.bean.User;
import com.ssafy.videoconference.model.user.bean.UserRole;
import com.ssafy.videoconference.model.user.service.IUserService;

import lombok.NonNull;

//http://localhost:8080/videoconference/swagger-ui.html
@CrossOrigin(origins = { "*" }, maxAge = 6000)
@RestController
//@RequestMapping("/api")
public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	private static final String SUCCESS = "success";
	private static final String FAIL = "fail";

//	@ApiOperation(value = "로그인", response = String.class)
//	@PostMapping("/login")
//	public ResponseEntity<String> login(){
//		
//		int a = 1;
//		if(a == 1) {
//			return ResponseEntity.ok(SUCCESS);
//		}else {
//			return ResponseEntity.ok(FAIL);
//		}
//	}
	@Resource(name = "userService")
	private IUserService userService;

	@Autowired
	private TokenUtils tokenUtils;

	@NonNull
	private BCryptPasswordEncoder passwordEncoder;

	@GetMapping(value = "/init")
	public String createAdmin(@ModelAttribute User user) {
		user.setId("[admin@naver.com](mailto:%22admin@naver.com)");
		user.setPw(passwordEncoder.encode("test"));
		user.setRole(UserRole.ADMIN);
		if (userService.register(user) == null) {
			System.out.println("Create Admin Error");
		}
		return "redirect:/index";
	}
}
