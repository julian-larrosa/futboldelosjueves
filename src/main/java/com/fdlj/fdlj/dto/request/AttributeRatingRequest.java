package com.fdlj.fdlj.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AttributeRatingRequest(

		@NotNull(message = "El jugador calificado es obligatorio")
		Long playerId,

		@NotNull(message = "La técnica es obligatoria")
		@Min(value = 1, message = "La técnica debe estar entre 1 y 10")
		@Max(value = 10, message = "La técnica debe estar entre 1 y 10")
		Integer tecnica,

		@NotNull(message = "El físico es obligatorio")
		@Min(value = 1, message = "El físico debe estar entre 1 y 10")
		@Max(value = 10, message = "El físico debe estar entre 1 y 10")
		Integer fisico,

		@NotNull(message = "La definición es obligatoria")
		@Min(value = 1, message = "La definición debe estar entre 1 y 10")
		@Max(value = 10, message = "La definición debe estar entre 1 y 10")
		Integer definicion,

		@NotNull(message = "La mentalidad es obligatoria")
		@Min(value = 1, message = "La mentalidad debe estar entre 1 y 10")
		@Max(value = 10, message = "La mentalidad debe estar entre 1 y 10")
		Integer mentalidad,

		@NotNull(message = "El pase es obligatorio")
		@Min(value = 1, message = "El pase debe estar entre 1 y 10")
		@Max(value = 10, message = "El pase debe estar entre 1 y 10")
		Integer pase
) {
}
