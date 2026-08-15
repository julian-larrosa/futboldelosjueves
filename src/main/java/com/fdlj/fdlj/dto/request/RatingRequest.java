package com.fdlj.fdlj.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RatingRequest(

		@NotNull(message = "El jugador calificado es obligatorio")
		Long calificadoId,

		@NotNull(message = "El puntaje es obligatorio")
		@Min(value = 1, message = "El puntaje debe estar entre 1 y 10")
		@Max(value = 10, message = "El puntaje debe estar entre 1 y 10")
		Integer puntaje
) {
}
