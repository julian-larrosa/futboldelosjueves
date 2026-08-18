package com.fdlj.fdlj.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AttendanceRegisterRequest(

		@NotEmpty(message = "Debe indicar al menos un hincha")
		@Size(max = 100, message = "No se pueden registrar más de 100 hinchas por partido")
		List<Long> hinchaIds
) {
}
