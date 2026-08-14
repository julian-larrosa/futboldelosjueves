package com.fdlj.fdlj.dto.request;

import com.fdlj.fdlj.entity.enums.PlayerPosition;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlayerRequest(

		@NotBlank(message = "El nombre es obligatorio")
		@Size(max = 100, message = "El nombre no puede superar 100 caracteres")
		String nombre,

		@NotBlank(message = "El apellido es obligatorio")
		@Size(max = 100, message = "El apellido no puede superar 100 caracteres")
		String apellido,

		@NotBlank(message = "El email es obligatorio")
		@Email(message = "El email debe tener un formato válido")
		@Size(max = 100, message = "El email no puede superar 100 caracteres")
		String email,

		@NotNull(message = "La posición es obligatoria")
		PlayerPosition posicion
) {
}
