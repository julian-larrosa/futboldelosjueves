package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import com.fdlj.fdlj.dto.request.LoginRequest;
import com.fdlj.fdlj.dto.request.MatchCommentRequest;
import com.fdlj.fdlj.dto.request.RatingRequest;
import com.fdlj.fdlj.entity.Hincha;
import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.entity.User;
import com.fdlj.fdlj.entity.enums.PlayerPosition;
import com.fdlj.fdlj.entity.enums.Role;
import com.fdlj.fdlj.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InactiveUserTest extends IntegrationTestBase {

	@Value("${app.jwt.secret}")
	private String jwtSecret;

	@Test
	void inactivePlayer_withOldToken_cannotRate_returns401() throws Exception {
		String admin = adminToken();
		RatingSetup setup = setupFinishedMatchForRating(admin);

		mockMvc.perform(delete("/api/players/" + setup.calificador().playerId())
						.header("Authorization", bearer(admin)))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/matches/" + setup.matchId() + "/ratings")
						.header("Authorization", bearer(setup.calificador().token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new RatingRequest(setup.calificado(), 8))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void inactivePlayer_withOldToken_cannotConvocar_returns401() throws Exception {
		String admin = adminToken();
		PlayerInfo player = registerPlayer("Desactivado");
		Long matchId = createMatch(admin);
		openConvocatoria(admin, matchId);

		mockMvc.perform(delete("/api/players/" + player.playerId())
						.header("Authorization", bearer(admin)))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/matches/" + matchId + "/participations")
						.header("Authorization", bearer(player.token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"playerId\":" + player.playerId() + "}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void inactiveHincha_withOldToken_cannotComment_returns401() throws Exception {
		String admin = adminToken();
		HinchaInfo hincha = registerHincha("HinchaDesactivado");
		Long matchId = createMatch(admin);

		Hincha hinchaEntity = hinchaRepository.findById(hincha.hinchaId()).orElseThrow();
		hinchaEntity.setActivo(false);
		hinchaRepository.save(hinchaEntity);

		mockMvc.perform(post("/api/matches/" + matchId + "/comments")
						.header("Authorization", bearer(hincha.token()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new MatchCommentRequest("hola"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void login_inactivePlayer_returns401() throws Exception {
		String email = "inactivo" + UUID.randomUUID() + "@example.com";
		User user = new User();
		user.setUsername("usuario" + UUID.randomUUID());
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode("password123"));
		user.setRole(Role.PLAYER);
		userRepository.save(user);

		Player player = new Player();
		player.setNombre("Inactivo");
		player.setApellido("Login");
		player.setEmail(email);
		player.setPosicion(PlayerPosition.DELANTERO);
		player.setActivo(false);
		player.setUser(user);
		playerRepository.save(player);

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequest(email, "password123"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void jwtExpired_returns401() throws Exception {
		User user = createUserWithActivePlayer();
		JwtService expiredJwtService = new JwtService(jwtSecret, -1000);
		String expiredToken = expiredJwtService.generateToken(user);

		mockMvc.perform(get("/api/players")
						.header("Authorization", bearer(expiredToken)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void jwtMalformed_returns401() throws Exception {
		mockMvc.perform(get("/api/players")
						.header("Authorization", bearer("no-es-un-jwt")))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void jwtSignedWithWrongKey_returns401() throws Exception {
		User user = createUserWithActivePlayer();
		String otherSecret = "eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eHh4eA==";
		JwtService otherJwtService = new JwtService(otherSecret, 60000);
		String wrongToken = otherJwtService.generateToken(user);

		mockMvc.perform(get("/api/players")
						.header("Authorization", bearer(wrongToken)))
				.andExpect(status().isUnauthorized());
	}

	private User createUserWithActivePlayer() {
		String email = "activo" + UUID.randomUUID() + "@example.com";
		User user = new User();
		user.setUsername("usuario" + UUID.randomUUID());
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode("password123"));
		user.setRole(Role.PLAYER);
		userRepository.save(user);

		Player player = new Player();
		player.setNombre("Activo");
		player.setApellido("Jwt");
		player.setEmail(email);
		player.setPosicion(PlayerPosition.DELANTERO);
		player.setActivo(true);
		player.setUser(user);
		playerRepository.save(player);
		return user;
	}
}