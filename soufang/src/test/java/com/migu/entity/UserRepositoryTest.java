package com.migu.entity;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.migu.ApplicationTests;
import com.migu.repository.UserRepository;

public class UserRepositoryTest extends ApplicationTests{
	@Autowired
	private UserRepository userRepository;
	
	
	@Test
	public void testFindOne() {
		User user = userRepository.findById(1L).get();
		Assert.assertEquals("waliwali", user.getName());
	}
}
