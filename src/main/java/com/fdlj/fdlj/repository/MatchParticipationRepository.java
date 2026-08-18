package com.fdlj.fdlj.repository;

import com.fdlj.fdlj.entity.MatchParticipation;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchParticipationRepository extends JpaRepository<MatchParticipation, Long> {

	List<MatchParticipation> findByMatchIdOrderByIdAsc(Long matchId);

	Page<MatchParticipation> findByMatchIdOrderByIdAsc(Long matchId, Pageable pageable);

	List<MatchParticipation> findByTeamIdOrderByIdAsc(Long teamId);

	Optional<MatchParticipation> findByMatchIdAndPlayerId(Long matchId, Long playerId);

	boolean existsByMatchIdAndPlayerId(Long matchId, Long playerId);

	long countByMatchId(Long matchId);

	List<MatchParticipation> findByPlayerIdAndMatchEstadoAndJugoEfectivamenteTrueOrderByMatchFechaHoraDesc(
			Long playerId, MatchStatus estado);

	List<MatchParticipation> findByMatchIdAndJugoEfectivamenteTrue(Long matchId);

	List<MatchParticipation> findByMatchEstadoAndJugoEfectivamenteTrueOrderByMatchFechaHoraDesc(MatchStatus estado);

	@Query("SELECT p FROM MatchParticipation p " +
			"WHERE p.match.estado = :estado AND p.jugoEfectivamente = true " +
			"AND YEAR(p.match.fechaHora) = :year ORDER BY p.match.fechaHora DESC")
	List<MatchParticipation> findByMatchEstadoAndJugoEfectivamenteTrueByYearOrderByMatchFechaHoraDesc(
			@Param("estado") MatchStatus estado, @Param("year") int year);

	@Query("SELECT p FROM MatchParticipation p " +
			"WHERE p.player.id = :playerId AND p.match.estado = :estado AND p.jugoEfectivamente = true " +
			"AND YEAR(p.match.fechaHora) = :year ORDER BY p.match.fechaHora DESC")
	List<MatchParticipation> findByPlayerIdAndMatchEstadoAndJugoEfectivamenteTrueByYearOrderByMatchFechaHoraDesc(
			@Param("playerId") Long playerId, @Param("estado") MatchStatus estado, @Param("year") int year);
}
