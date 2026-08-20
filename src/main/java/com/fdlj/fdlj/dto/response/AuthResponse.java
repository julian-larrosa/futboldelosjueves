package com.fdlj.fdlj.dto.response;

public record AuthResponse(
		String token,
		String tokenType,
		UserResponse user,
		PlayerResponse player,
		boolean mustChangePassword
) {

	public static AuthResponse of(String token, UserResponse user, PlayerResponse player, boolean mustChangePassword) {
		return new AuthResponse(token, "Bearer", user, player, mustChangePassword);
	}
}
