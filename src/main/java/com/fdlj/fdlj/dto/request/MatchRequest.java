package com.fdlj.fdlj.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record MatchRequest(

		@NotNull(message = "La fecha y hora es obligatoria")
		OffsetDateTime fechaHora,

		@Size(max = 150, message = "El lugar no puede superar 150 caracteres")
		String lugar
) {
}
