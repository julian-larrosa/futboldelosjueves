package com.fdlj.fdlj.repository;

import com.fdlj.fdlj.entity.PlayerAttributeHistory;
import com.fdlj.fdlj.entity.enums.AttributeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerAttributeHistoryRepository extends JpaRepository<PlayerAttributeHistory, Long> {

	List<PlayerAttributeHistory> findByPlayerIdOrderByMatchIdDesc(Long playerId);

	Optional<PlayerAttributeHistory> findByPlayerIdAndAttributeTypeAndMatchId(
			Long playerId, AttributeType type, Long matchId);

	long countByPlayerIdAndAttributeType(Long playerId, AttributeType type);

	@Query("SELECT h.attributeType, AVG(h.ratingValue) FROM PlayerAttributeHistory h "
			+ "WHERE h.player.id = :playerId GROUP BY h.attributeType")
	List<Object[]> findAverageRatingByPlayerIdGroupByAttributeType(@Param("playerId") Long playerId);
}
