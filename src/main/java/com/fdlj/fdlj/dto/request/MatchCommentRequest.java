package com.fdlj.fdlj.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MatchCommentRequest(

		@NotBlank(message = "El comentario es obligatorio")
		@Size(max = 2000, message = "El comentario no puede superar 2000 caracteres")
		String contenido
) {
}
