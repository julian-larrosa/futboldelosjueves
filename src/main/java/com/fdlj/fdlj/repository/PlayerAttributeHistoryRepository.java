package com.fdlj.fdlj.repository;

import com.fdlj.fdlj.entity.PlayerAttributeHistory;
import com.fdlj.fdlj.entity.enums.AttributeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerAttributeHistoryRepository extends JpaRepository<PlayerAttributeHistory, Long> {

	List<PlayerAttributeHistory> findByPlayerIdOrderByMatchIdDesc(Long playerId);

	Optional<PlayerAttributeHistory> findByPlayerIdAndAttributeTypeAndMatchId(
			Long playerId, AttributeType type, Long matchId);

	long countByPlayerIdAndAttributeType(Long playerId, AttributeType type);
}
