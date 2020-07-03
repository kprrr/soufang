package com.migu.service;

import com.migu.entity.User;

/**
 * 用户服务
 * @author Lee
 *
 */
public interface IUserService {
	User findUserByName(String userName);
}
