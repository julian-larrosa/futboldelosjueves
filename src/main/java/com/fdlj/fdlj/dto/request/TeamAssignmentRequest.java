package com.fdlj.fdlj.dto.request;

import com.fdlj.fdlj.entity.enums.TeamSide;
import jakarta.validation.constraints.NotNull;

public record TeamAssignmentRequest(

		@NotNull(message = "El lado del equipo es obligatorio")
		TeamSide teamSide
) {
}
