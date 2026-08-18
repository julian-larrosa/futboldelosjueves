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

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class PlayerAttributeMapper {

	public PlayerAttributesResponse toAttributesResponse(Player player, List<PlayerAttribute> attributes,
			Map<AttributeType, Double> historicalAverages) {
		List<PlayerAttributeResponse> attrResponses = attributes.stream()
				.map(a -> toAttributeResponse(a, historicalAverages))
				.sorted(Comparator.comparing(PlayerAttributeResponse::attributeType))
				.toList();
		return new PlayerAttributesResponse(player.getId(), attrResponses);
	}

	public PlayerAttributeResponse toAttributeResponse(PlayerAttribute attribute,
			Map<AttributeType, Double> historicalAverages) {
		Double average = historicalAverages.get(attribute.getAttributeType());
		return new PlayerAttributeResponse(
				attribute.getAttributeType(),
				average != null ? average : attribute.getCurrentValue());
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
