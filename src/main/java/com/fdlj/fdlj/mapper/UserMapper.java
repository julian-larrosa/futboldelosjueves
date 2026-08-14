package com.fdlj.fdlj.mapper;

import com.fdlj.fdlj.dto.response.UserResponse;
import com.fdlj.fdlj.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

	public UserResponse toResponse(User user) {
		return new UserResponse(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getRole()
		);
	}

	public String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}
}
