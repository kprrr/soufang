package com.migu.repository;

import org.springframework.data.repository.CrudRepository;

import com.migu.entity.User;

public interface UserRepository extends CrudRepository<User, Long>{
	
	User findByName(String userName);
}
