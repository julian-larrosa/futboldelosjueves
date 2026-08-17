package com.fdlj.fdlj.dto.response;

import com.fdlj.fdlj.entity.enums.AttributeType;

public record PlayerAttributeResponse(
		AttributeType attributeType,
		Double currentValue
) {
}
