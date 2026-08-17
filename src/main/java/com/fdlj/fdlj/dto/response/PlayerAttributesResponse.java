package com.fdlj.fdlj.dto.response;

import java.util.List;

public record PlayerAttributesResponse(
		Long playerId,
		List<PlayerAttributeResponse> attributes
) {
}
