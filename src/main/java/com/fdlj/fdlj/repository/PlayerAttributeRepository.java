package com.fdlj.fdlj.repository;

import com.fdlj.fdlj.entity.PlayerAttribute;
import com.fdlj.fdlj.entity.enums.AttributeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlayerAttributeRepository extends JpaRepository<PlayerAttribute, Long> {

	List<PlayerAttribute> findByPlayerId(Long playerId);

	List<PlayerAttribute> findByPlayerIdIn(Collection<Long> playerIds);

	Optional<PlayerAttribute> findByPlayerIdAndAttributeType(Long playerId, AttributeType type);
}