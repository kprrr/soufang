package com.migu.security;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.util.AntPathMatcher;

/**
 * 基于角色登录入口控制器
 * @author Lee
 *
 */
public class LoginUrlEntryPoint extends LoginUrlAuthenticationEntryPoint{

	private AntPathMatcher pathmatcher = new AntPathMatcher();
	
	private final Map<String, String> authEntryPointMap;
	
	public LoginUrlEntryPoint(String loginFormUrl) {
		super(loginFormUrl);
		authEntryPointMap = new HashMap<>();
		
		//普通用户登录入口映射
		authEntryPointMap.put("/user/**", "/user/login");
		//管理员登录入口映射
		authEntryPointMap.put("/admin/**", "/admin/login");
		
	}
	
	/**
	 * 根据请求跳转到指定的页面，父类是默认使用loginFormUrl
	 */
	@Override
	protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) {
		String uri = request.getRequestURI().replaceAll(request.getContextPath(), "");
		
		for(Map.Entry<String, String> auEntry:this.authEntryPointMap.entrySet()) {
			if(this.pathmatcher.match(auEntry.getKey(), uri)) {
				return auEntry.getValue();
			}
		}
		
		return super.determineUrlToUseForThisRequest(request, response, exception);
	}

}
