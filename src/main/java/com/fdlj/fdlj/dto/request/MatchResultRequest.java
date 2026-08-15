package com.fdlj.fdlj.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MatchResultRequest(

		@NotNull(message = "Los goles del equipo A son obligatorios")
		@Min(value = 0, message = "Los goles no pueden ser negativos")
		Integer golesEquipoA,

		@NotNull(message = "Los goles del equipo B son obligatorios")
		@Min(value = 0, message = "Los goles no pueden ser negativos")
		Integer golesEquipoB
) {
}
