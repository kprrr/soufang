package com.migu.service.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.migu.base.LoginUserUtil;
import com.migu.entity.Role;
import com.migu.entity.User;
import com.migu.repository.RoleRepository;
import com.migu.repository.UserRepository;
import com.migu.service.IUserService;
import com.migu.service.ServiceResult;
import com.migu.web.dto.UserDTO;

@Service
public class UserServiceImpl implements IUserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public User findUserByName(String userName) {
		User user = userRepository.findByName(userName);

		if (user == null) {
			return null;
		}

		List<Role> roles = roleRepository.findRolesByUserId(user.getId());
		if (roles == null || roles.isEmpty()) {
			throw new DisabledException("权限非法");
		}

		List<GrantedAuthority> authorities = new ArrayList<>();
		roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));
		user.setAuthorityList(authorities);
		return user;
	}

	@Override
	public ServiceResult<UserDTO> findById(Long userId) {
		User user = userRepository.findById(userId).get();
		if (user == null) {
			return ServiceResult.notFound();
		}
		UserDTO userDTO = modelMapper.map(user, UserDTO.class);
		return ServiceResult.of(userDTO);
	}

	@Override
	public User findUserByTelephone(String telephone) {
		User user = userRepository.findUserByPhoneNumber(telephone);
		if (user == null) {
			return null;
		}
		List<Role> roles = roleRepository.findRolesByUserId(user.getId());
		if (roles == null || roles.isEmpty()) {
			throw new DisabledException("权限非法");
		}

		List<GrantedAuthority> authorities = new ArrayList<>();
		roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));
		user.setAuthorityList(authorities);
		return user;
	}

	@Override
	public User addUserByPhone(String telephone) {
		User user = new User();
		user.setPhoneNumber(telephone);
		user.setName(telephone.substring(0, 3) + "****" + telephone.substring(7, telephone.length()));
		Date now = new Date();
		user.setCreateTime(now);
		user.setLastLoginTime(now);
		user.setLastUpdateTime(now);
		user.setStatus(0);
		user = userRepository.save(user);

		Role role = new Role();
		role.setName("USER");
		role.setUserId(user.getId());
		roleRepository.save(role);
		user.setAuthorityList(Lists.newArrayList(new SimpleGrantedAuthority("ROLE_USER")));
		return user;
	}

	@Override
	@Transactional
	public ServiceResult modifyUserProfile(String profile, String value) {
		Long userId = LoginUserUtil.getLoginUserId();
		if (profile == null || profile.isEmpty()) {
			return new ServiceResult(false, "属性不可以为空");
		}
		switch (profile) {
		case "name":
			userRepository.updateUsername(userId, value);
			break;
		case "email":
			userRepository.updateEmail(userId, value);
			break;
		case "password":
			userRepository.updatePassword(userId, this.passwordEncoder.encode(value));
			break;
		default:
			return new ServiceResult(false, "不支持的属性");
		}
		return ServiceResult.success();
	}
}
