package com.fdlj.fdlj.mapper;

import com.fdlj.fdlj.dto.response.AttributeHistoryEntry;
import com.fdlj.fdlj.dto.response.PlayerAttributeResponse;
import com.fdlj.fdlj.dto.response.PlayerAttributesResponse;
import com.fdlj.fdlj.dto.response.PlayerAttributeHistoryResponse;
import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.entity.PlayerAttribute;
import com.fdlj.fdlj.entity.PlayerAttributeHistory;
import com.fdlj.fdlj.entity.enums.AttributeType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PlayerAttributeMapper {

	public PlayerAttributesResponse toAttributesResponse(Player player, List<PlayerAttribute> attributes,
			Map<AttributeType, Double> historicalAverages) {
		Map<AttributeType, PlayerAttribute> byType = attributes.stream()
				.collect(Collectors.toMap(PlayerAttribute::getAttributeType, Function.identity()));
		List<PlayerAttributeResponse> attrResponses = Arrays.stream(AttributeType.values())
				.map(type -> toAttributeResponse(byType.get(type), type, historicalAverages))
				.toList();
		return new PlayerAttributesResponse(player.getId(), attrResponses);
	}

	public PlayerAttributeResponse toAttributeResponse(PlayerAttribute attribute, AttributeType type,
			Map<AttributeType, Double> historicalAverages) {
		Double average = historicalAverages.get(type);
		double currentValue = attribute != null ? attribute.getCurrentValue() : 5.0;
		return new PlayerAttributeResponse(type, average != null ? average : currentValue);
	}

	public PlayerAttributeHistoryResponse toHistoryResponse(Long playerId, List<PlayerAttributeHistory> history) {
		List<AttributeHistoryEntry> entries = history.stream()
				.map(this::toHistoryEntry)
				.toList();
		return new PlayerAttributeHistoryResponse(playerId, entries);
	}

	public AttributeHistoryEntry toHistoryEntry(PlayerAttributeHistory h) {
		return new AttributeHistoryEntry(
				h.getId(),
				h.getAttributeType(),
				h.getMatch().getId(),
				h.getRatingValue());
	}
}
