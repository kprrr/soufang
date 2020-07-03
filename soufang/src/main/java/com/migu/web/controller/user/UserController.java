package com.migu.web.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.migu.base.ApiResponse;

@Controller
public class UserController {
	
	@GetMapping("/user/login")
	public String loginPage() {
		return "user/login";
	}
	
	@GetMapping("/user/center")
	public String centerPage() {
		return "user/center";
	}
	
//	@GetMapping("/get")
//	@ResponseBody
//	public ApiResponse get() {
//		return ApiResponse.ofMessage(200, "成功了");
//	}
//	
//	@GetMapping("/404") 
//	public String notFoundPage() {
//		return "404";
//	}
//	
//	@GetMapping("/403") 
//	public String accessError() {
//		return "403";
//	}
//	
//	@GetMapping("/500") 
//	public String internalError() {
//		return "500";
//	}
//	
//	@GetMapping("/logout/page") 
//	public String logoutPage() {
//		return "logout";
//	}
}
