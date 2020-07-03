package com.migu.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.migu.base.ApiResponse;

@Controller
public class HomeController {
	
	@GetMapping("/")
	public String index() {
		return "index";
	}
	
	@GetMapping("/get")
	@ResponseBody
	public ApiResponse get() {
		return ApiResponse.ofMessage(200, "成功了");
	}
	
	@GetMapping("/404") 
	public String notFoundPage() {
		return "404";
	}
	
	@GetMapping("/403") 
	public String accessError() {
		return "403";
	}
	
	@GetMapping("/500") 
	public String internalError() {
		return "500";
	}
	
	@GetMapping("/logout/page") 
	public String logoutPage() {
		return "logout";
	}
}
