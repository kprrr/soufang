package com.migu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.migu.security.AuthProvider;
import com.migu.security.LoginAuthFailHandler;
import com.migu.security.LoginUrlEntryPoint;


@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

	/**
	 * HTTP权限控制
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//资源访问权限
		http.authorizeRequests()
			.antMatchers("/admin/login").permitAll()//管理员登录入口
			.antMatchers("static/**").permitAll()//静态资源
			.antMatchers("/user/login").permitAll()//用户登录入口
			.antMatchers("/admin/**").hasRole("ADMIN")
			.antMatchers("/user/**").hasAnyRole("ADMIN","USER")
			.antMatchers("/api/user/**").hasAnyRole("ADMIN","USER")
			.and()
			.formLogin()
			.loginProcessingUrl("/login")//配置角色登录处理入口
			.failureHandler(loginAuthFailHandler())
			.and()
			.logout()//配置登出
			.logoutUrl("/logout/page")
			.deleteCookies("JSESSIONID")
			.invalidateHttpSession(true)
			.and()
			.exceptionHandling()
			.authenticationEntryPoint(loginUrlEntryPoint())
			.accessDeniedPage("/403");//登录权限控制
		
		//开发关闭
		http.csrf().disable();
		http.headers().frameOptions().sameOrigin();
	}
	
	/**
	 * 自定义认证策略
	 * @throws Exception 
	 */
	@Autowired
	public void configGlobal(AuthenticationManagerBuilder auth) throws Exception {
//		auth.inMemoryAuthentication().withUser("admin")
//		.password("admin")
//		.roles("ADMIN").and()
//		.passwordEncoder(new CustomPasswordEncoder());
		auth.authenticationProvider(authProvider());
	}
	
	@Bean
	public AuthProvider authProvider() {
		return new AuthProvider();
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
	    return new BCryptPasswordEncoder();
	}
	
	@Bean
	public LoginUrlEntryPoint loginUrlEntryPoint() {
		return new LoginUrlEntryPoint("/user/login");
	}
	
	@Bean
	public LoginAuthFailHandler loginAuthFailHandler() {
		return new LoginAuthFailHandler(loginUrlEntryPoint());
	}
}
