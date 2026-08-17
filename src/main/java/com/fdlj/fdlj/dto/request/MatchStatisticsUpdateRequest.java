package com.fdlj.fdlj.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MatchStatisticsUpdateRequest(

		@NotNull(message = "La cantidad de goles es obligatoria")
		@Min(value = 0, message = "Los goles no pueden ser negativos")
		Integer goles,

		@NotNull(message = "El indicador jugoEfectivamente es obligatorio")
		Boolean jugoEfectivamente
) {
}
