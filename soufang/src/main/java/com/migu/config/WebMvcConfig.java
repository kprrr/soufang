package com.migu.config;

import org.modelmapper.ModelMapper;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.extras.springsecurity4.dialect.SpringSecurityDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

@Configuration
//public class WebMvcConfig extends WebMvcConfigurerAdapter  implements ApplicationContextAware{
public class WebMvcConfig  implements ApplicationContextAware,WebMvcConfigurer {
	
	
	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	
	
	/**
	 * 静态资源加载配置
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
	}

	/**
	 * 模板资源解析器
	 * @return
	 */
	@Bean
	@ConfigurationProperties(prefix = "spring.thymeleaf")
	public SpringResourceTemplateResolver templateResolver() {
		SpringResourceTemplateResolver templateResolver =new SpringResourceTemplateResolver();
		templateResolver.setApplicationContext(this.applicationContext);;
		templateResolver.setCharacterEncoding("UTF-8");
		
		return templateResolver;
	}
	
	/**
	 * Thymeleaf标准方言解释器
	 */
	@Bean
	public SpringTemplateEngine springTemplateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(templateResolver());
		//支持Spring EL表达式
		templateEngine.setEnableSpringELCompiler(true);
		
		//支持SpringSecurity方言
		SpringSecurityDialect securityDialect = new SpringSecurityDialect();
		templateEngine.addDialect(securityDialect);
		return templateEngine;
	}
	
	/**
	 * 视图解析器
	 */
	@Bean
	public ThymeleafViewResolver viewResolver() {
		ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
		viewResolver.setTemplateEngine(springTemplateEngine());
		return viewResolver;
	}
	
	/**
	 * Bean util
	 */
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
	
}
