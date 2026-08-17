package com.fdlj.fdlj.dto.response;

import com.fdlj.fdlj.entity.enums.AttributeType;

public record AttributeHistoryEntry(
		Long id,
		AttributeType attributeType,
		Long matchId,
		Integer ratingValue
) {
}
