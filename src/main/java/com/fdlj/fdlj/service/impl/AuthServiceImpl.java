package com.fdlj.fdlj.service.impl;

import com.fdlj.fdlj.dto.request.ChangePasswordRequest;
import com.fdlj.fdlj.dto.request.LoginRequest;
import com.fdlj.fdlj.dto.request.RegisterHinchaRequest;
import com.fdlj.fdlj.dto.request.RegisterRequest;
import com.fdlj.fdlj.dto.request.ResetPasswordRequest;
import com.fdlj.fdlj.dto.response.AuthResponse;
import com.fdlj.fdlj.dto.response.PlayerResponse;
import com.fdlj.fdlj.entity.Hincha;
import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.entity.PlayerAttribute;
import com.fdlj.fdlj.entity.User;
import com.fdlj.fdlj.entity.enums.AttributeType;
import com.fdlj.fdlj.entity.enums.Role;
import com.fdlj.fdlj.exception.InvalidCredentialsException;
import com.fdlj.fdlj.exception.InvalidPasswordException;
import com.fdlj.fdlj.exception.ResourceAlreadyExistsException;
import com.fdlj.fdlj.exception.ResourceNotFoundException;
import com.fdlj.fdlj.mapper.PlayerMapper;
import com.fdlj.fdlj.mapper.UserMapper;
import com.fdlj.fdlj.repository.HinchaRepository;
import com.fdlj.fdlj.repository.PlayerAttributeRepository;
import com.fdlj.fdlj.repository.PlayerRepository;
import com.fdlj.fdlj.repository.UserRepository;
import com.fdlj.fdlj.security.JwtService;
import com.fdlj.fdlj.security.UserActivityService;
import com.fdlj.fdlj.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

	private static final String INVALID_CREDENTIALS_MESSAGE = "Email o contraseña incorrectos.";

	private static final String INVALID_PASSWORD_MESSAGE = "La contraseña actual es incorrecta.";

	private final UserRepository userRepository;
	private final PlayerRepository playerRepository;
	private final PlayerAttributeRepository attributeRepository;
	private final HinchaRepository hinchaRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final UserActivityService userActivityService;
	private final UserMapper userMapper;
	private final PlayerMapper playerMapper;

	@Override
	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String username = request.username().trim();
		String email = userMapper.normalizeEmail(request.email());
		validateUniqueness(username, email);

		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.password()));
		user.setRole(Role.PLAYER);
		user.setMustChangePassword(false);

		User savedUser = userRepository.saveAndFlush(user);
		if (playerRepository.existsByUserId(savedUser.getId())) {
			throw new ResourceAlreadyExistsException("Ya existe un perfil de jugador para este usuario");
		}

		Player player = new Player();
		player.setNombre(request.nombre().trim());
		player.setApellido(request.apellido().trim());
		player.setEmail(email);
		player.setPosicion(request.posicion());
		player.setActivo(true);
		player.setUser(savedUser);

		savedUser.setPlayer(player);
		Player savedPlayer = playerRepository.save(player);

		List<PlayerAttribute> attributes = new ArrayList<>();
		for (AttributeType type : AttributeType.values()) {
			PlayerAttribute attribute = new PlayerAttribute();
			attribute.setPlayer(savedPlayer);
			attribute.setAttributeType(type);
			attribute.setCurrentValue(5.0);
			attributes.add(attribute);
		}
		List<PlayerAttribute> savedAttributes = attributeRepository.saveAll(attributes);

		log.info("Nuevo usuario registrado: {} ({})", username, email);
		String token = jwtService.generateToken(user);
		return AuthResponse.of(token, userMapper.toResponse(user),
				playerMapper.toResponse(savedPlayer, Map.of(savedPlayer.getId(), savedAttributes)), false);
	}

	@Override
	@Transactional
	public AuthResponse registerHincha(RegisterHinchaRequest request) {
		String email = userMapper.normalizeEmail(request.email());
		validateHinchaUniqueness(email);

		String username = deriveUsername(email);

		User user = new User();
		user.setUsername(username);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(request.password()));
		user.setRole(Role.HINCHADA);
		user.setMustChangePassword(false);
		userRepository.save(user);

		if (hinchaRepository.existsByUserId(user.getId())) {
			throw new ResourceAlreadyExistsException("Ya existe un perfil de hincha para este usuario");
		}

		Hincha hincha = new Hincha();
		hincha.setNombre(request.nombre().trim());
		hincha.setApellido(request.apellido().trim());
		hincha.setActivo(true);
		hincha.setUser(user);
		hinchaRepository.save(hincha);

		log.info("Nuevo hincha registrado: {} {} ({})", request.nombre(), request.apellido(), email);
		String token = jwtService.generateToken(user);
		return AuthResponse.of(token, userMapper.toResponse(user), null, false);
	}

	@Override
	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		String email = userMapper.normalizeEmail(request.email());
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> {
					log.warn("Intento de login con email inexistente: {}", email);
					return new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
				});
		if (!passwordEncoder.matches(request.password(), user.getPassword())) {
			log.warn("Contraseña incorrecta para usuario: {}", email);
			throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
		}
		if (!userActivityService.isUserActive(user)) {
			log.warn("Intento de login de usuario inactivo: {}", email);
			throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
		}

		log.info("Login exitoso: {}", email);
		Player player = user.getPlayer();
		PlayerResponse playerResponse = player != null ? playerMapper.toResponse(player, attributesByPlayer(player.getId())) : null;
		String token = jwtService.generateToken(user);
		return AuthResponse.of(token, userMapper.toResponse(user), playerResponse, user.isMustChangePassword());
	}

	@Override
	@Transactional
	public void changePassword(String email, ChangePasswordRequest request) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Usuario autenticado no encontrado"));
		if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
			log.warn("Intento de cambio de contraseña con contraseña actual incorrecta: {}", email);
			throw new InvalidPasswordException(INVALID_PASSWORD_MESSAGE);
		}
		user.setPassword(passwordEncoder.encode(request.newPassword()));
		user.setMustChangePassword(false);
		userRepository.save(user);
		log.info("Contraseña actualizada para usuario: {}", email);
	}

	@Override
	@Transactional
	public void resetPassword(ResetPasswordRequest request) {
		String email = userMapper.normalizeEmail(request.email());
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException(
						"No existe un usuario con el email: " + email));
		user.setPassword(passwordEncoder.encode(request.newPassword()));
		user.setMustChangePassword(true);
		userRepository.save(user);
		log.info("Contraseña restablecida por un administrador para el usuario: {}", email);
	}

	private void validateUniqueness(String username, String email) {
		if (userRepository.existsByUsername(username)) {
			throw new ResourceAlreadyExistsException("Ya existe un usuario con el username: " + username);
		}
		validateEmailUniqueness(email);
	}

	private void validateHinchaUniqueness(String email) {
		validateEmailUniqueness(email);
	}

	private void validateEmailUniqueness(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new ResourceAlreadyExistsException("Ya existe un usuario con el email: " + email);
		}
		if (playerRepository.existsByEmailAndActivoTrue(email)) {
			throw new ResourceAlreadyExistsException("Ya existe un jugador con el email: " + email);
		}
	}

	private String deriveUsername(String email) {
		String base = email.substring(0, email.indexOf('@'));
		if (base.length() > 40) {
			base = base.substring(0, 40);
		}
		String username = base;
		while (userRepository.existsByUsername(username)) {
			username = base + "_" + UUID.randomUUID().toString().substring(0, 6);
		}
		return username;
	}

	private Map<Long, List<PlayerAttribute>> attributesByPlayer(Long playerId) {
		List<PlayerAttribute> attributes = attributeRepository.findByPlayerIdIn(List.of(playerId));
		return Map.of(playerId, attributes);
	}
}
