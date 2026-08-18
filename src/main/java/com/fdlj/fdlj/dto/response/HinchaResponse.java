package com.fdlj.fdlj.dto.response;

public record HinchaResponse(
		Long id,
		String nombre,
		String apellido,
		boolean activo,
		String username,
		String email
) {
}
