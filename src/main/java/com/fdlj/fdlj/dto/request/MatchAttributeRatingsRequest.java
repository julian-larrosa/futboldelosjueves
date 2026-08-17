package com.fdlj.fdlj.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record MatchAttributeRatingsRequest(

		@NotEmpty(message = "Debe enviar al menos una calificación")
		@Valid
		List<AttributeRatingRequest> ratings
) {
}
