package com.migu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@RestController
public class SoufangApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoufangApplication.class, args);
	}
	
	@GetMapping("/hello")
	public String hello() {
		return "Hellow";
	}

}
