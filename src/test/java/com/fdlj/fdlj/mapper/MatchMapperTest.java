package com.fdlj.fdlj.mapper;

import com.fdlj.fdlj.dto.response.MatchResponse;
import com.fdlj.fdlj.entity.Match;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MatchMapperTest {

	private final MatchMapper mapper = new MatchMapper();

	@Test
	void toResponse_usesProvidedParticipationCount() {
		Match match = new Match();
		match.setId(99L);
		match.setFechaHora(OffsetDateTime.parse("2026-08-20T18:00:00Z"));
		match.setLugar("Cancha Central");
		match.setEstado(MatchStatus.CONVOCATORIA_ABIERTA);
		match.setParticipations(null);

		MatchResponse response = mapper.toResponse(match, 7);

		assertThat(response.id()).isEqualTo(99L);
		assertThat(response.cantidadConvocados()).isEqualTo(7);
		assertThat(response.estado()).isEqualTo(MatchStatus.CONVOCATORIA_ABIERTA);
		assertThat(response.lugar()).isEqualTo("Cancha Central");
	}
}
