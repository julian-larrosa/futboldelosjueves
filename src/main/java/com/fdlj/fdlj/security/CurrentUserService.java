package com.fdlj.fdlj.security;

import com.fdlj.fdlj.entity.User;
import com.fdlj.fdlj.exception.ResourceNotFoundException;
import com.fdlj.fdlj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new ResourceNotFoundException("No hay un usuario autenticado");
		}
		String email = authentication.getName();
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Usuario autenticado no encontrado"));
	}
}
