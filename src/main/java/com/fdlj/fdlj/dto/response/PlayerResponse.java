package com.fdlj.fdlj.dto.response;

import com.fdlj.fdlj.entity.enums.PlayerPosition;

public record PlayerResponse(
		Long id,
		String nombre,
		String apellido,
		String email,
		PlayerPosition posicion,
		boolean activo
) {
}
