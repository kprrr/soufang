package com.migu.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.migu.entity.User;
import com.migu.service.IUserService;

/**
 * 自定义认证实现
 * @author Lee
 *
 */
public class AuthProvider implements AuthenticationProvider{

	@Autowired
	private IUserService userService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
//	private final PasswordEncoder passwordEncoder = new MessageDigestPasswordEncoder("MD5");
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String userName = authentication.getName();
		String inputPassword = (String) authentication.getCredentials();
		
		User user = userService.findUserByName(userName);
		if(user == null) {
			throw new AuthenticationCredentialsNotFoundException("authError");
		}
		//第二个参数是数据库保存的密码
		if(inputPassword.equals("admin")) {
			return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		}
//		if(this.passwordEncoder.matches(inputPassword,user.getPassword())) {
//			return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
//		}
		
		throw new BadCredentialsException("authError");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return true;
	}

}
