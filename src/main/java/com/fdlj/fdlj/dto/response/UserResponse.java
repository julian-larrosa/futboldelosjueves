package com.fdlj.fdlj.dto.response;

import com.fdlj.fdlj.entity.enums.Role;

public record UserResponse(
		Long id,
		String username,
		String email,
		Role role
) {
}
