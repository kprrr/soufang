package com.migu.service;

import com.migu.entity.User;
import com.migu.web.dto.UserDTO;

/**
 * 用户服务
 * @author Lee
 *
 */
public interface IUserService {
	User findUserByName(String userName);

	ServiceResult<UserDTO> findById(Long adminId);
}
