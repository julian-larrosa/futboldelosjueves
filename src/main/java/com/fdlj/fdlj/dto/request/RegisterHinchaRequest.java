package com.fdlj.fdlj.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterHinchaRequest(

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

		@NotBlank(message = "La contraseña es obligatoria")
		@Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
		String password
) {
}
