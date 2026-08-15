package com.fdlj.fdlj.mapper;

import com.fdlj.fdlj.dto.response.RatingResponse;
import com.fdlj.fdlj.entity.Rating;
import org.springframework.stereotype.Component;

@Component
public class RatingMapper {

	public RatingResponse toResponse(Rating rating) {
		return new RatingResponse(
				rating.getId(),
				rating.getMatch().getId(),
				rating.getCalificador().getId(),
				fullName(rating.getCalificador()),
				rating.getCalificado().getId(),
				fullName(rating.getCalificado()),
				rating.getPuntaje()
		);
	}

	private String fullName(com.fdlj.fdlj.entity.Player player) {
		return player.getNombre() + " " + player.getApellido();
	}
}
