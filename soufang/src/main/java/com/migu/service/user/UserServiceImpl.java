package com.migu.service.user;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
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
import com.migu.service.ServiceResult;
import com.migu.web.dto.UserDTO;

@Service
public class UserServiceImpl implements IUserService{

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private ModelMapper modelMapper;
	
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
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_"+
        role.getName())));
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

}
