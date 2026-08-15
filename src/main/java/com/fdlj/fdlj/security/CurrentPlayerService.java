package com.fdlj.fdlj.security;

import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.exception.ResourceNotFoundException;
import com.fdlj.fdlj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentPlayerService {

	private final UserRepository userRepository;

	public Player getCurrentPlayer() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new ResourceNotFoundException("No hay un usuario autenticado");
		}
		String email = authentication.getName();
		return userRepository.findByEmail(email)
				.map(user -> user.getPlayer())
				.orElseThrow(() -> new ResourceNotFoundException(
						"No existe un jugador asociado al usuario autenticado"));
	}
}
