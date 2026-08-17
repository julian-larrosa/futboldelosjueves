package com.fdlj.fdlj.repository;

import com.fdlj.fdlj.entity.MatchParticipation;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchParticipationRepository extends JpaRepository<MatchParticipation, Long> {

	List<MatchParticipation> findByMatchIdOrderByIdAsc(Long matchId);

	Page<MatchParticipation> findByMatchIdOrderByIdAsc(Long matchId, Pageable pageable);

	List<MatchParticipation> findByTeamIdOrderByIdAsc(Long teamId);

	Optional<MatchParticipation> findByMatchIdAndPlayerId(Long matchId, Long playerId);

	boolean existsByMatchIdAndPlayerId(Long matchId, Long playerId);

	List<MatchParticipation> findByPlayerIdAndMatchEstadoAndJugoEfectivamenteTrueOrderByMatchFechaHoraDesc(
			Long playerId, MatchStatus estado);

	List<MatchParticipation> findByMatchIdAndJugoEfectivamenteTrue(Long matchId);
}
