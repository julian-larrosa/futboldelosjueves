package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import com.fdlj.fdlj.dto.request.MatchCommentRequest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MatchCommentControllerTest extends IntegrationTestBase {

	@Test
	void createComment_adminBeforeFinish_returns201() throws Exception {
		String adminToken = adminToken();
		Long matchId = createMatch(adminToken);

		mockMvc.perform(post("/api/matches/" + matchId + "/comments")
						.header("Authorization", bearer(adminToken))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(new MatchCommentRequest("Gran partido"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.matchId").value(matchId))
				.andExpect(jsonPath("$.data.contenido").value("Gran partido"));
	}

	@Test
	void createComment_adminOnFinished_returns409() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);

		mockMvc.perform(post("/api/matches/" + matchId + "/comments")
						.header("Authorization", bearer(adminToken))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(new MatchCommentRequest("Gran partido"))))
				.andExpect(status().isConflict());
	}

	@Test
	void createComment_playerOnFinished_returns201() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		PlayerInfo player = registerPlayer("Pedro");

		mockMvc.perform(post("/api/matches/" + matchId + "/comments")
						.header("Authorization", bearer(player.token()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(new MatchCommentRequest("Que partidazo"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.authorRole").value("PLAYER"))
				.andExpect(jsonPath("$.data.contenido").value("Que partidazo"));
	}

	@Test
	void createComment_playerBeforeFinish_returns409() throws Exception {
		String adminToken = adminToken();
		Long matchId = createMatch(adminToken);
		PlayerInfo player = registerPlayer("Pedro");

		mockMvc.perform(post("/api/matches/" + matchId + "/comments")
						.header("Authorization", bearer(player.token()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(new MatchCommentRequest("Que partidazo"))))
				.andExpect(status().isConflict());
	}

	@Test
	void createComment_hincha_returns403() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		HinchaInfo hincha = registerHincha("María");

		mockMvc.perform(post("/api/matches/" + matchId + "/comments")
						.header("Authorization", bearer(hincha.token()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(new MatchCommentRequest("Que partidazo"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void createComment_blankContent_returns400() throws Exception {
		String adminToken = adminToken();
		Long matchId = createMatch(adminToken);

		mockMvc.perform(post("/api/matches/" + matchId + "/comments")
						.header("Authorization", bearer(adminToken))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(new MatchCommentRequest(""))))
				.andExpect(status().isBadRequest());
	}

	@Test
	void updateComment_adminBeforeFinish_returns200() throws Exception {
		String adminToken = adminToken();
		Long matchId = createMatch(adminToken);
		Long commentId = createComment(adminToken, matchId, "Original");

		mockMvc.perform(put("/api/matches/" + matchId + "/comments/" + commentId)
						.header("Authorization", bearer(adminToken))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(new MatchCommentRequest("Actualizado"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.contenido").value("Actualizado"));
	}

	@Test
	void updateComment_adminOnFinished_returns409() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		PlayerInfo player = registerPlayer("Pedro");
		Long commentId = createComment(player.token(), matchId, "Original");

		mockMvc.perform(put("/api/matches/" + matchId + "/comments/" + commentId)
						.header("Authorization", bearer(adminToken))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(new MatchCommentRequest("Actualizado"))))
				.andExpect(status().isConflict());
	}

	@Test
	void updateComment_playerOwnOnFinished_returns200() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		PlayerInfo player = registerPlayer("Pedro");
		Long commentId = createComment(player.token(), matchId, "Original");

		mockMvc.perform(put("/api/matches/" + matchId + "/comments/" + commentId)
						.header("Authorization", bearer(player.token()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(new MatchCommentRequest("Actualizado"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.contenido").value("Actualizado"));
	}

	@Test
	void updateComment_playerOtherOnFinished_returns403() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		PlayerInfo player1 = registerPlayer("Pedro");
		PlayerInfo player2 = registerPlayer("Lucas");
		Long commentId = createComment(player1.token(), matchId, "Original");

		mockMvc.perform(put("/api/matches/" + matchId + "/comments/" + commentId)
						.header("Authorization", bearer(player2.token()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(new MatchCommentRequest("Actualizado"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void updateComment_hincha_returns403() throws Exception {
		String adminToken = adminToken();
		Long matchId = createMatch(adminToken);
		Long commentId = createComment(adminToken, matchId, "Original");
		HinchaInfo hincha = registerHincha("María");

		mockMvc.perform(put("/api/matches/" + matchId + "/comments/" + commentId)
						.header("Authorization", bearer(hincha.token()))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(new MatchCommentRequest("Actualizado"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void getComments_anyAuthenticated_returnsList() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);
		PlayerInfo player = registerPlayer("Pedro");
		createComment(player.token(), matchId, "Comentario uno");
		createComment(player.token(), matchId, "Comentario dos");
		HinchaInfo hincha = registerHincha("María");

		mockMvc.perform(get("/api/matches/" + matchId + "/comments")
						.header("Authorization", bearer(hincha.token())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(2));
	}

	@Test
	void getComments_withoutToken_returns401() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);

		mockMvc.perform(get("/api/matches/" + matchId + "/comments"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void getComments_empty_returnsEmptyList() throws Exception {
		String adminToken = adminToken();
		Long matchId = setupFinishedMatch10(adminToken);

		mockMvc.perform(get("/api/matches/" + matchId + "/comments")
						.header("Authorization", bearer(adminToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data.length()").value(0));
	}

	@Test
	void getComments_matchNotFound_returns404() throws Exception {
		mockMvc.perform(get("/api/matches/999999/comments")
						.header("Authorization", bearer(adminToken())))
				.andExpect(status().isNotFound());
	}

	private Long createComment(String token, Long matchId, String contenido) throws Exception {
		String response = mockMvc.perform(post("/api/matches/" + matchId + "/comments")
						.header("Authorization", bearer(token))
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(new MatchCommentRequest(contenido))))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		JsonNode json = objectMapper.readTree(response);
		return json.at("/data/id").asLong();
	}
}