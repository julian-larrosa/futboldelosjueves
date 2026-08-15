package com.fdlj.fdlj.mapper;

import com.fdlj.fdlj.dto.request.MatchRequest;
import com.fdlj.fdlj.dto.response.MatchResponse;
import com.fdlj.fdlj.dto.response.MatchResultResponse;
import com.fdlj.fdlj.entity.Match;
import com.fdlj.fdlj.entity.enums.ResultadoPartido;
import org.springframework.stereotype.Component;

@Component
public class MatchMapper {

	public Match toEntity(MatchRequest request) {
		Match match = new Match();
		match.setFechaHora(request.fechaHora());
		match.setLugar(normalizeLugar(request.lugar()));
		return match;
	}

	public MatchResponse toResponse(Match match) {
		int convocados = match.getParticipations().size();
		return new MatchResponse(
				match.getId(),
				match.getFechaHora(),
				match.getLugar(),
				match.getEstado(),
				match.getGolesEquipoA(),
				match.getGolesEquipoB(),
				convocados
		);
	}

	public MatchResultResponse toResultResponse(Match match) {
		int golesA = match.getGolesEquipoA() != null ? match.getGolesEquipoA() : 0;
		int golesB = match.getGolesEquipoB() != null ? match.getGolesEquipoB() : 0;
		ResultadoPartido resultado;
		if (golesA > golesB) {
			resultado = ResultadoPartido.GANA_EQUIPO_A;
		} else if (golesB > golesA) {
			resultado = ResultadoPartido.GANA_EQUIPO_B;
		} else {
			resultado = ResultadoPartido.EMPATE;
		}
		return new MatchResultResponse(match.getId(), golesA, golesB, resultado);
	}

	public String normalizeLugar(String lugar) {
		if (lugar == null) {
			return null;
		}
		String trimmed = lugar.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
