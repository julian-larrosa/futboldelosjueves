package com.fdlj.fdlj.mapper;

import com.fdlj.fdlj.dto.response.PlayerResponse;
import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.entity.PlayerAttribute;
import com.fdlj.fdlj.entity.enums.AttributeType;
import com.fdlj.fdlj.entity.enums.PlayerPosition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerMapperTest {

	private final PlayerMapper mapper = new PlayerMapper();

	@Test
	void toResponse_mapsAttributesWithoutRepositoryAccess() {
		Player player = new Player();
		player.setId(42L);
		player.setNombre("Juan");
		player.setApellido("Perez");
		player.setEmail("juan@example.com");
		player.setPosicion(PlayerPosition.DELANTERO);
		player.setActivo(true);

		PlayerAttribute tecnica = new PlayerAttribute();
		tecnica.setPlayer(player);
		tecnica.setAttributeType(AttributeType.TECNICA);
		tecnica.setCurrentValue(7.5);

		PlayerAttribute pase = new PlayerAttribute();
		pase.setPlayer(player);
		pase.setAttributeType(AttributeType.PASE);
		pase.setCurrentValue(6.25);

		PlayerResponse response = mapper.toResponse(player, Map.of(player.getId(), List.of(pase, tecnica)));

		assertThat(response.id()).isEqualTo(42L);
		assertThat(response.attributes()).isNotNull();
		assertThat(response.attributes().playerId()).isEqualTo(42L);
		assertThat(response.attributes().attributes()).hasSize(2);
		assertThat(response.attributes().attributes().get(0).attributeType()).isEqualTo(AttributeType.TECNICA);
		assertThat(response.attributes().attributes().get(1).attributeType()).isEqualTo(AttributeType.PASE);
	}

	@Test
	void toResponse_withoutAttributes_returnsNullAttributesBlock() {
		Player player = new Player();
		player.setId(7L);
		player.setNombre("Sin");
		player.setApellido("Atributos");
		player.setEmail("sin@example.com");
		player.setPosicion(PlayerPosition.MEDIOCAMPISTA);
		player.setActivo(true);

		PlayerResponse response = mapper.toResponse(player, Map.of());

		assertThat(response.attributes()).isNull();
	}
}
