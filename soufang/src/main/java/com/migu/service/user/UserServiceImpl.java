package com.migu.service.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.migu.entity.Role;
import com.migu.entity.User;
import com.migu.repository.RoleRepository;
import com.migu.repository.UserRepository;
import com.migu.service.IUserService;

@Service
public class UserServiceImpl implements IUserService{

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Override
	public User findUserByName(String userName ) {
		User user = userRepository.findByName(userName);

        if (user == null) {
            return null;
        }
        
		List<Role> roles = roleRepository.findRolesByUserId(user.getId());
        if (roles == null || roles.isEmpty()) {
            throw new DisabledException("权限非法");
        }
        
        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE"+
        role.getName())));
        user.setAuthorityList(authorities);
		return user;
	}

}
