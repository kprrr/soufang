package com.migu.config;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * 文件上传配置
 * @author Lee
 *
 */
@Configuration
@ConditionalOnClass({Servlet.class,StandardServletMultipartResolver.class,
	MultipartConfigElement.class})
@ConditionalOnProperty(prefix = "spring.http.multipart", name = "enabled",matchIfMissing = true)
@EnableConfigurationProperties(MultipartProperties.class) //允许spring默认配置
public class WebFileUploadConfig {
	private final MultipartProperties multipartProperties;

	public WebFileUploadConfig(MultipartProperties multipartProperties) {
		this.multipartProperties = multipartProperties;
	}
	
	/**
	 * 上传配置
	 */
	@Bean
	@ConditionalOnMissingBean
	public MultipartConfigElement multipartConfigElement() {
		return this.multipartProperties.createMultipartConfig();
	}
	
	/**
	 * 注册解析器
	 */
	@Bean(name = DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
	@ConditionalOnMissingBean(MultipartProperties.class)
	public StandardServletMultipartResolver multipartResolver() {
		StandardServletMultipartResolver multipartResolver = new StandardServletMultipartResolver();
		multipartResolver.setResolveLazily(this.multipartProperties.isResolveLazily());
		return multipartResolver;
	}
	
}
