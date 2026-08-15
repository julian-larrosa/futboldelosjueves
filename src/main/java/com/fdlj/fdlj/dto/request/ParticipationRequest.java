package com.fdlj.fdlj.dto.request;

import jakarta.validation.constraints.NotNull;

public record ParticipationRequest(

		@NotNull(message = "El id del jugador es obligatorio")
		Long playerId
) {
}
