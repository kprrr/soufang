package com.migu.entity;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Data;

@Entity // jpa的注解，需要加
@Table(name = "user") // 指定数据库的表名
@Data // lombok
public class User implements UserDetails {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//	 @Column(name="id")
//    @GenericGenerator(name = "system-uuid", strategy = "uuid")
	private Long id;

    private String name;

    private String password;

    private String email;
    
    @Column(name="phone_number")
    private String phoneNumber;

    private Integer status;
    
    @Column(name="create_time")
    private Date createTime;

    @Column(name="last_login_time")
    private Date lastLoginTime;
    
    @Column(name="last_update_time")
    private Date lastUpateTime;
    
    private String avatar;

    @Transient //jpa忽略字段
    private List<GrantedAuthority> authorityList;
    
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return this.authorityList;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}


}
