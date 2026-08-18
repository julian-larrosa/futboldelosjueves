package com.fdlj.fdlj.repository;

import com.fdlj.fdlj.entity.MatchParticipation;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchParticipationRepository extends JpaRepository<MatchParticipation, Long> {

	List<MatchParticipation> findByMatchIdOrderByIdAsc(Long matchId);

	@Query(value = "SELECT p FROM MatchParticipation p JOIN FETCH p.player LEFT JOIN FETCH p.team "
			+ "WHERE p.match.id = :matchId ORDER BY p.id",
			countQuery = "SELECT COUNT(p) FROM MatchParticipation p WHERE p.match.id = :matchId")
	Page<MatchParticipation> findByMatchIdOrderByIdAsc(@Param("matchId") Long matchId, Pageable pageable);

	@Query("SELECT p FROM MatchParticipation p JOIN FETCH p.player LEFT JOIN FETCH p.team "
			+ "WHERE p.match.id = :matchId ORDER BY p.id")
	List<MatchParticipation> findByMatchIdOrderByIdAscWithDetails(@Param("matchId") Long matchId);

	List<MatchParticipation> findByTeamIdOrderByIdAsc(Long teamId);

	Optional<MatchParticipation> findByMatchIdAndPlayerId(Long matchId, Long playerId);

	boolean existsByMatchIdAndPlayerId(Long matchId, Long playerId);

	long countByMatchId(Long matchId);

	@Query("SELECT p FROM MatchParticipation p JOIN FETCH p.player LEFT JOIN FETCH p.team "
			+ "WHERE p.match.estado = :estado AND p.jugoEfectivamente = true "
			+ "AND p.match.fechaHora >= :from AND p.match.fechaHora < :to ORDER BY p.match.fechaHora DESC")
	List<MatchParticipation> findByMatchEstadoAndJugoEfectivamenteTrueByRangeOrderByMatchFechaHoraDesc(
			@Param("estado") MatchStatus estado, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

	@Query("SELECT p FROM MatchParticipation p JOIN FETCH p.player LEFT JOIN FETCH p.team "
			+ "WHERE p.player.id = :playerId AND p.match.estado = :estado AND p.jugoEfectivamente = true "
			+ "AND p.match.fechaHora >= :from AND p.match.fechaHora < :to ORDER BY p.match.fechaHora DESC")
	List<MatchParticipation> findByPlayerIdAndMatchEstadoAndJugoEfectivamenteTrueByRangeOrderByMatchFechaHoraDesc(
			@Param("playerId") Long playerId, @Param("estado") MatchStatus estado,
			@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

	@Query("SELECT p FROM MatchParticipation p JOIN FETCH p.match m JOIN FETCH p.player LEFT JOIN FETCH p.team "
			+ "WHERE m.estado = :estado AND p.jugoEfectivamente = true ORDER BY m.fechaHora DESC")
	List<MatchParticipation> findByMatchEstadoAndJugoEfectivamenteTrueWithDetails(@Param("estado") MatchStatus estado);

	@Query("SELECT p FROM MatchParticipation p JOIN FETCH p.match m JOIN FETCH p.player LEFT JOIN FETCH p.team "
			+ "WHERE p.player.id = :playerId AND m.estado = :estado AND p.jugoEfectivamente = true "
			+ "ORDER BY m.fechaHora DESC")
	List<MatchParticipation> findByPlayerIdAndMatchEstadoAndJugoEfectivamenteTrueWithDetails(
			@Param("playerId") Long playerId, @Param("estado") MatchStatus estado);

	@Query("SELECT p FROM MatchParticipation p JOIN FETCH p.match m JOIN FETCH p.player LEFT JOIN FETCH p.team "
			+ "WHERE p.match.id = :matchId AND p.jugoEfectivamente = true ORDER BY p.id")
	List<MatchParticipation> findByMatchIdAndJugoEfectivamenteTrueWithDetails(@Param("matchId") Long matchId);
}