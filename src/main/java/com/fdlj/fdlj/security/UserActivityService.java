package com.fdlj.fdlj.security;

import com.fdlj.fdlj.entity.User;
import com.fdlj.fdlj.entity.enums.Role;
import com.fdlj.fdlj.repository.HinchaRepository;
import com.fdlj.fdlj.repository.PlayerRepository;
import com.fdlj.fdlj.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fuente de verdad central para saber si un usuario autenticado sigue activo.
 * Como User no tiene campo activo, la actividad se deriva del perfil asociado
 * (Player.activo o Hincha.activo); el ADMIN siempre esta activo.
 */
@Service
@RequiredArgsConstructor
public class UserActivityService {

	private final UserRepository userRepository;
	private final PlayerRepository playerRepository;
	private final HinchaRepository hinchaRepository;

	@Transactional(readOnly = true)
	public boolean isActiveUser(String email) {
		return userRepository.findByEmail(email)
				.map(this::isUserActive)
				.orElse(false);
	}

	@Transactional(readOnly = true)
	public boolean isUserActive(User user) {
		if (user.getRole() == Role.ADMIN) {
			return true;
		}
		if (user.getRole() == Role.PLAYER) {
			return playerRepository.existsByUserIdAndActivoTrue(user.getId());
		}
		if (user.getRole() == Role.HINCHADA) {
			return hinchaRepository.existsByUserIdAndActivoTrue(user.getId());
		}
		return false;
	}
}