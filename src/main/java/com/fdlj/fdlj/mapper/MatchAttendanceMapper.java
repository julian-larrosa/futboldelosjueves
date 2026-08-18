package com.fdlj.fdlj.mapper;

import com.fdlj.fdlj.dto.response.MatchAttendanceResponse;
import com.fdlj.fdlj.entity.Hincha;
import com.fdlj.fdlj.entity.MatchAttendance;
import org.springframework.stereotype.Component;

@Component
public class MatchAttendanceMapper {

	public MatchAttendanceResponse toResponse(MatchAttendance attendance) {
		Hincha hincha = attendance.getHincha();
		return new MatchAttendanceResponse(
				attendance.getId(),
				attendance.getMatch().getId(),
				hincha.getId(),
				hincha.getNombre() + " " + hincha.getApellido(),
				attendance.getMatch().getFechaHora(),
				attendance.getMatch().getEstado()
		);
	}
}
