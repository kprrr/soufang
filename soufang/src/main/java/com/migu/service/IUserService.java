package com.migu.service;

import com.migu.entity.User;
import com.migu.web.dto.UserDTO;

/**
 * 用户服务
 * 
 * @author Lee
 *
 */
public interface IUserService {
	User findUserByName(String userName);

	ServiceResult<UserDTO> findById(Long adminId);

	/**
	 * 根据电话号码寻找用户
	 * 
	 * @param telephone
	 * @return
	 */
	User findUserByTelephone(String telephone);

	/**
	 * 通过手机号注册用户
	 * 
	 * @param telehone
	 * @return
	 */
	User addUserByPhone(String telehone);

	/**
	 * 修改指定属性值
	 * 
	 * @param profile
	 * @param value
	 * @return
	 */
	ServiceResult modifyUserProfile(String profile, String value);
}
