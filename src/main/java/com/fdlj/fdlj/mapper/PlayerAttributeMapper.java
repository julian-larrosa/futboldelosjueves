package com.fdlj.fdlj.mapper;

import com.fdlj.fdlj.dto.response.AttributeHistoryEntry;
import com.fdlj.fdlj.dto.response.PlayerAttributeResponse;
import com.fdlj.fdlj.dto.response.PlayerAttributesResponse;
import com.fdlj.fdlj.dto.response.PlayerAttributeHistoryResponse;
import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.entity.PlayerAttribute;
import com.fdlj.fdlj.entity.PlayerAttributeHistory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class PlayerAttributeMapper {

	public PlayerAttributesResponse toAttributesResponse(Player player, List<PlayerAttribute> attributes) {
		List<PlayerAttributeResponse> attrResponses = attributes.stream()
				.map(this::toAttributeResponse)
				.sorted(Comparator.comparing(PlayerAttributeResponse::attributeType))
				.toList();
		return new PlayerAttributesResponse(player.getId(), attrResponses);
	}

	public PlayerAttributeResponse toAttributeResponse(PlayerAttribute attribute) {
		return new PlayerAttributeResponse(
				attribute.getAttributeType(),
				attribute.getCurrentValue());
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
