package com.fdlj.fdlj;

import com.fdlj.fdlj.dto.request.AttributeRatingRequest;
import com.fdlj.fdlj.dto.request.MatchAttributeRatingsRequest;
import com.fdlj.fdlj.dto.request.MatchRequest;
import com.fdlj.fdlj.dto.request.MatchResultRequest;
import com.fdlj.fdlj.dto.request.MatchStatisticsUpdateRequest;
import com.fdlj.fdlj.dto.request.ParticipationRequest;
import com.fdlj.fdlj.dto.request.RegisterRequest;
import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.entity.PlayerAttribute;
import com.fdlj.fdlj.entity.User;
import com.fdlj.fdlj.entity.enums.AttributeType;
import com.fdlj.fdlj.entity.enums.PlayerPosition;
import com.fdlj.fdlj.entity.enums.Role;
import com.fdlj.fdlj.repository.PlayerAttributeRepository;
import com.fdlj.fdlj.repository.PlayerRepository;
import com.fdlj.fdlj.repository.UserRepository;
import com.fdlj.fdlj.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected JsonMapper objectMapper;

	@Autowired
	protected UserRepository userRepository;

	@Autowired
	protected PlayerRepository playerRepository;

	@Autowired
	protected PlayerAttributeRepository attributeRepository;

	@Autowired
	protected PasswordEncoder passwordEncoder;

	@Autowired
	protected JwtService jwtService;

	protected record PlayerInfo(String token, Long playerId) {
	}

	public record RatingSetup(Long matchId, PlayerInfo calificador, Long calificado) {
	}

	protected String bearer(String token) {
		return "Bearer " + token;
	}

	protected String adminToken() {
		User admin = new User();
		admin.setUsername("admin" + UUID.randomUUID());
		admin.setEmail("admin" + UUID.randomUUID() + "@fdlj.com");
		admin.setPassword(passwordEncoder.encode("admin123"));
		admin.setRole(Role.ADMIN);
		admin = userRepository.save(admin);
		return jwtService.generateToken(admin);
	}

	protected PlayerInfo registerPlayer(String nombre) throws Exception {
		String email = "player" + UUID.randomUUID() + "@example.com";
		String username = "jugador" + UUID.randomUUID();
		String apellido = "Apellido" + UUID.randomUUID().toString().substring(0, 4);
		String body = objectMapper.writeValueAsString(
				new RegisterRequest(username, email, "password123", nombre, apellido, PlayerPosition.DELANTERO));
		String response = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		JsonNode json = objectMapper.readTree(response);
		return new PlayerInfo(json.at("/data/token").asText(), json.at("/data/player/id").asLong());
	}

	protected Long createPlayer(String nombre) {
		User user = new User();
		user.setUsername("user" + UUID.randomUUID());
		user.setEmail("p" + UUID.randomUUID() + "@example.com");
		user.setPassword(passwordEncoder.encode("password123"));
		user.setRole(Role.PLAYER);
		userRepository.save(user);

		Player player = new Player();
		player.setNombre(nombre);
		player.setApellido("Apellido" + UUID.randomUUID().toString().substring(0, 4));
		player.setEmail(user.getEmail());
		player.setPosicion(PlayerPosition.DELANTERO);
		player.setActivo(true);
		player.setUser(user);
		Player savedPlayer = playerRepository.save(player);

		for (AttributeType type : AttributeType.values()) {
			PlayerAttribute attribute = new PlayerAttribute();
			attribute.setPlayer(savedPlayer);
			attribute.setAttributeType(type);
			attribute.setCurrentValue(5.0);
			attributeRepository.save(attribute);
		}

		return savedPlayer.getId();
	}

	protected Long createMatch(String adminToken) throws Exception {
		return createMatch(adminToken, OffsetDateTime.now().plusDays(1));
	}

	protected Long createMatch(String adminToken, OffsetDateTime fechaHora) throws Exception {
		String body = objectMapper.writeValueAsString(
				new MatchRequest(fechaHora, "Cancha " + UUID.randomUUID().toString().substring(0, 4)));
		String response = mockMvc.perform(post("/api/matches")
						.header("Authorization", bearer(adminToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return objectMapper.readTree(response).at("/data/id").asLong();
	}

	protected void openConvocatoria(String token, Long matchId) throws Exception {
		mockMvc.perform(post("/api/matches/" + matchId + "/convocatoria/abrir")
						.header("Authorization", bearer(token)))
				.andExpect(status().isOk());
	}

	protected void closeConvocatoria(String token, Long matchId) throws Exception {
		mockMvc.perform(post("/api/matches/" + matchId + "/convocatoria/cerrar")
						.header("Authorization", bearer(token)))
				.andExpect(status().isOk());
	}

	protected void reopenConvocatoria(String token, Long matchId) throws Exception {
		mockMvc.perform(post("/api/matches/" + matchId + "/convocatoria/reabrir")
						.header("Authorization", bearer(token)))
				.andExpect(status().isOk());
	}

	protected void convocar(String token, Long matchId, Long playerId) throws Exception {
		String body = objectMapper.writeValueAsString(new ParticipationRequest(playerId));
		mockMvc.perform(post("/api/matches/" + matchId + "/participations")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated());
	}

	protected void generateTeams(String token, Long matchId) throws Exception {
		mockMvc.perform(post("/api/matches/" + matchId + "/teams/generate")
						.header("Authorization", bearer(token)))
				.andExpect(status().isOk());
	}

	protected void startMatch(String token, Long matchId) throws Exception {
		mockMvc.perform(post("/api/matches/" + matchId + "/iniciar")
						.header("Authorization", bearer(token)))
				.andExpect(status().isOk());
	}

	protected void updateStats(String token, Long matchId, Long playerId, int goles, boolean jugoEfectivamente)
			throws Exception {
		String body = objectMapper.writeValueAsString(
				new MatchStatisticsUpdateRequest(goles, jugoEfectivamente));
		mockMvc.perform(put("/api/matches/" + matchId + "/participations/" + playerId)
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk());
	}

	protected void finishMatch(String token, Long matchId, int golesA, int golesB) throws Exception {
		String body = objectMapper.writeValueAsString(new MatchResultRequest(golesA, golesB));
		mockMvc.perform(post("/api/matches/" + matchId + "/finalizar")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk());
	}

	protected Long setupFinishedMatch10(String adminToken) throws Exception {
		return setupFinishedMatch(adminToken, OffsetDateTime.now().plusDays(1));
	}

	protected Long setupFinishedMatchInYear(String adminToken, int year) throws Exception {
		return setupFinishedMatch(adminToken, OffsetDateTime.of(year, 6, 15, 20, 0, 0, 0, ZoneOffset.UTC));
	}

	protected Long setupFinishedMatch(String adminToken, OffsetDateTime fechaHora) throws Exception {
		Long matchId = createMatch(adminToken, fechaHora);
		openConvocatoria(adminToken, matchId);
		for (int i = 0; i < 10; i++) {
			Long playerId = createPlayer("Jugador" + i);
			convocar(adminToken, matchId, playerId);
		}
		closeConvocatoria(adminToken, matchId);
		generateTeams(adminToken, matchId);
		startMatch(adminToken, matchId);
		marcarEfectivos(adminToken, matchId);
		finishMatch(adminToken, matchId, 3, 1);
		return matchId;
	}

	protected RatingSetup setupFinishedMatchForRating(String adminToken) throws Exception {
		return setupFinishedMatchForRating(adminToken, OffsetDateTime.now().plusDays(1));
	}

	protected RatingSetup setupFinishedMatchForRatingInYear(String adminToken, int year) throws Exception {
		return setupFinishedMatchForRating(adminToken, OffsetDateTime.of(year, 6, 15, 20, 0, 0, 0, ZoneOffset.UTC));
	}

	protected RatingSetup setupFinishedMatchForRating(String adminToken, OffsetDateTime fechaHora) throws Exception {
		Long matchId = createMatch(adminToken, fechaHora);
		openConvocatoria(adminToken, matchId);
		PlayerInfo calificador = registerPlayer("Calificador");
		convocar(adminToken, matchId, calificador.playerId());
		Long calificado = createPlayer("Calificado");
		convocar(adminToken, matchId, calificado);
		for (int i = 0; i < 8; i++) {
			Long playerId = createPlayer("Otro" + i);
			convocar(adminToken, matchId, playerId);
		}
		closeConvocatoria(adminToken, matchId);
		generateTeams(adminToken, matchId);
		startMatch(adminToken, matchId);
		marcarEfectivos(adminToken, matchId);
		finishMatch(adminToken, matchId, 2, 2);
		return new RatingSetup(matchId, calificador, calificado);
	}

	protected List<Long> convocadosIds(String adminToken, Long matchId) throws Exception {
		String response = mockMvc.perform(get("/api/matches/" + matchId + "/participations?page=0&size=100")
						.header("Authorization", bearer(adminToken)))
				.andReturn().getResponse().getContentAsString();
		JsonNode data = objectMapper.readTree(response).at("/data/content");
		List<Long> ids = new ArrayList<>();
		for (JsonNode node : data) {
			ids.add(node.get("playerId").asLong());
		}
		return ids;
	}

	private void marcarEfectivos(String adminToken, Long matchId) throws Exception {
		for (Long playerId : convocadosIds(adminToken, matchId)) {
			updateStats(adminToken, matchId, playerId, 0, true);
		}
	}

	protected void submitAttributeRatings(String token, Long matchId, List<AttributeRatingRequest> ratings)
			throws Exception {
		String body = objectMapper.writeValueAsString(new MatchAttributeRatingsRequest(ratings));
		mockMvc.perform(post("/api/matches/" + matchId + "/attribute-ratings")
						.header("Authorization", bearer(token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated());
	}

	protected AttributeRatingRequest buildAttributeRating(Long playerId, int tecnica, int fisico,
			int definicion, int mentalidad, int pase) {
		return new AttributeRatingRequest(playerId, tecnica, fisico, definicion, mentalidad, pase);
	}
}
