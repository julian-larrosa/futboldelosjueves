package com.fdlj.fdlj.dto.response;

import java.util.List;

public record PlayerAttributeHistoryResponse(
		Long playerId,
		List<AttributeHistoryEntry> history
) {
}
