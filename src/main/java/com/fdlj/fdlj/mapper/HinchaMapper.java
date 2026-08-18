package com.fdlj.fdlj.mapper;

import com.fdlj.fdlj.dto.response.HinchaResponse;
import com.fdlj.fdlj.entity.Hincha;
import com.fdlj.fdlj.entity.User;
import org.springframework.stereotype.Component;

@Component
public class HinchaMapper {

	public HinchaResponse toResponse(Hincha hincha) {
		User user = hincha.getUser();
		return new HinchaResponse(
				hincha.getId(),
				hincha.getNombre(),
				hincha.getApellido(),
				hincha.isActivo(),
				user != null ? user.getUsername() : null,
				user != null ? user.getEmail() : null
		);
	}
}
