package com.fdlj.fdlj.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(

		@NotBlank(message = "El email es obligatorio")
		@Email(message = "El email debe tener un formato válido")
		@Size(max = 100, message = "El email no puede superar 100 caracteres")
		String email,

		@NotBlank(message = "La nueva contraseña es obligatoria")
		@Size(min = 8, max = 72, message = "La nueva contraseña debe tener entre 8 y 72 caracteres")
		String newPassword
) {
}