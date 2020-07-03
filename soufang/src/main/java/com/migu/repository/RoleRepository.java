package com.migu.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.migu.entity.Role;

/**
 * 角色数据Dao
 * @author Lee
 *
 */
public interface RoleRepository extends CrudRepository<Role, Long>{
	
	List<Role> findRolesByUserId(Long userId);
}
