package com.fdlj.fdlj.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MatchStatisticsBatchRequest(

		@NotEmpty(message = "La lista de estadísticas no puede estar vacía")
		List<@Valid PlayerStats> stats
) {
	public record PlayerStats(

			@NotNull(message = "El id del jugador es obligatorio")
			Long playerId,

			@NotNull(message = "La cantidad de goles es obligatoria")
			@Min(value = 0, message = "Los goles no pueden ser negativos")
			Integer goles,

			@NotNull(message = "El indicador jugoEfectivamente es obligatorio")
			Boolean jugoEfectivamente
	) {
	}
}
