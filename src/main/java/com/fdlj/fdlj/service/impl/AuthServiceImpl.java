package com.fdlj.fdlj.service.impl;

import com.fdlj.fdlj.dto.request.LoginRequest;
import com.fdlj.fdlj.dto.request.RegisterRequest;
import com.fdlj.fdlj.dto.response.AuthResponse;
import com.fdlj.fdlj.dto.response.PlayerResponse;
import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.entity.PlayerAttribute;
import com.fdlj.fdlj.entity.User;
import com.fdlj.fdlj.entity.enums.AttributeType;
import com.fdlj.fdlj.entity.enums.Role;
import com.fdlj.fdlj.exception.InvalidCredentialsException;
import com.fdlj.fdlj.exception.ResourceAlreadyExistsException;
import com.fdlj.fdlj.mapper.PlayerMapper;
import com.fdlj.fdlj.mapper.UserMapper;
import com.fdlj.fdlj.repository.PlayerAttributeRepository;
import com.fdlj.fdlj.repository.PlayerRepository;
import com.fdlj.fdlj.repository.UserRepository;
import com.fdlj.fdlj.security.JwtService;
import com.fdlj.fdlj.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

	private static final String INVALID_CREDENTIALS_MESSAGE = "Email o contraseña incorrectos.";

	private final UserRepository userRepository;
	private final PlayerRepository playerRepository;
	private final PlayerAttributeRepository attributeRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
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

		Player player = new Player();
		player.setNombre(request.nombre().trim());
		player.setApellido(request.apellido().trim());
		player.setEmail(email);
		player.setPosicion(request.posicion());
		player.setActivo(true);
		player.setUser(user);

		user.setPlayer(player);

		userRepository.save(user);
		playerRepository.save(player);

		for (AttributeType type : AttributeType.values()) {
			PlayerAttribute attribute = new PlayerAttribute();
			attribute.setPlayer(player);
			attribute.setAttributeType(type);
			attribute.setCurrentValue(5.0);
			attributeRepository.save(attribute);
		}

		log.info("Nuevo usuario registrado: {} ({})", username, email);
		String token = jwtService.generateToken(user);
		return AuthResponse.of(token, userMapper.toResponse(user), playerMapper.toResponse(player));
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

		log.info("Login exitoso: {}", email);
		Player player = user.getPlayer();
		PlayerResponse playerResponse = player != null ? playerMapper.toResponse(player) : null;
		String token = jwtService.generateToken(user);
		return AuthResponse.of(token, userMapper.toResponse(user), playerResponse);
	}

	private void validateUniqueness(String username, String email) {
		if (userRepository.existsByUsername(username)) {
			throw new ResourceAlreadyExistsException("Ya existe un usuario con el username: " + username);
		}
		if (userRepository.existsByEmail(email)) {
			throw new ResourceAlreadyExistsException("Ya existe un usuario con el email: " + email);
		}
		if (playerRepository.existsByEmailAndActivoTrue(email)) {
			throw new ResourceAlreadyExistsException("Ya existe un jugador con el email: " + email);
		}
	}
}
