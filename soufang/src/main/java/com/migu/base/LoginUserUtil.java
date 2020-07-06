package com.migu.base;

import org.springframework.security.core.context.SecurityContextHolder;

import com.migu.entity.User;

public class LoginUserUtil {

	public static User load() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		if(principal != null && principal instanceof User) {
			return (User)principal;
		}
		return null;
	}
	
	public static Long getLoginUserId() {
		User user = load();
		if(user == null) {
			return -1L;
		}
		
		return user.getId();
	}
}
