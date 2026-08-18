package com.fdlj.fdlj.dto.response;

import com.fdlj.fdlj.entity.enums.Role;

import java.time.OffsetDateTime;

public record MatchCommentResponse(
		Long id,
		Long matchId,
		Long authorId,
		String authorNombre,
		Role authorRole,
		String contenido,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
