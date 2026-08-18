package com.fdlj.fdlj.repository;

import com.fdlj.fdlj.entity.MatchAttendance;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface MatchAttendanceRepository extends JpaRepository<MatchAttendance, Long> {

	boolean existsByMatchIdAndHinchaId(Long matchId, Long hinchaId);

	Optional<MatchAttendance> findByMatchIdAndHinchaId(Long matchId, Long hinchaId);

	@Query("SELECT a FROM MatchAttendance a JOIN FETCH a.match m JOIN FETCH a.hincha "
			+ "WHERE a.match.id = :matchId ORDER BY a.id")
	List<MatchAttendance> findByMatchIdOrderByIdAsc(@Param("matchId") Long matchId);

	@Query("SELECT a FROM MatchAttendance a JOIN FETCH a.match m WHERE a.hincha.id = :hinchaId "
			+ "ORDER BY m.fechaHora DESC")
	List<MatchAttendance> findByHinchaIdOrderByMatchFechaHoraDesc(@Param("hinchaId") Long hinchaId);

	@Query("SELECT a FROM MatchAttendance a JOIN FETCH a.match m WHERE a.hincha.id = :hinchaId "
			+ "AND m.fechaHora >= :from AND m.fechaHora < :to ORDER BY m.fechaHora DESC")
	List<MatchAttendance> findByHinchaIdAndMatchRange(
			@Param("hinchaId") Long hinchaId, @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

	@Query("SELECT COUNT(a.id) FROM MatchAttendance a WHERE a.match.estado = :estado")
	long countAll(@Param("estado") MatchStatus estado);

	@Query("SELECT COUNT(a.id) FROM MatchAttendance a WHERE a.match.estado = :estado "
			+ "AND a.match.fechaHora >= :from AND a.match.fechaHora < :to")
	long countAllInRange(@Param("estado") MatchStatus estado,
			@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

	@Query("SELECT COUNT(DISTINCT a.match.id) FROM MatchAttendance a WHERE a.match.estado = :estado")
	long countDistinctMatches(@Param("estado") MatchStatus estado);

	@Query("SELECT COUNT(DISTINCT a.match.id) FROM MatchAttendance a WHERE a.match.estado = :estado "
			+ "AND a.match.fechaHora >= :from AND a.match.fechaHora < :to")
	long countDistinctMatchesInRange(@Param("estado") MatchStatus estado,
			@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

	@Query("SELECT h.id, h.nombre, h.apellido, EXTRACT(YEAR FROM a.match.fechaHora), COUNT(a.id) "
			+ "FROM MatchAttendance a JOIN a.hincha h "
			+ "WHERE a.match.estado = :estado "
			+ "GROUP BY h.id, h.nombre, h.apellido, EXTRACT(YEAR FROM a.match.fechaHora) "
			+ "ORDER BY h.apellido, h.nombre")
	List<Object[]> rankingGroupedByHincha(@Param("estado") MatchStatus estado);

	@Query("SELECT h.id, h.nombre, h.apellido, EXTRACT(YEAR FROM a.match.fechaHora), COUNT(a.id) "
			+ "FROM MatchAttendance a JOIN a.hincha h "
			+ "WHERE a.match.estado = :estado "
			+ "AND a.match.fechaHora >= :from AND a.match.fechaHora < :to "
			+ "GROUP BY h.id, h.nombre, h.apellido, EXTRACT(YEAR FROM a.match.fechaHora) "
			+ "ORDER BY h.apellido, h.nombre")
	List<Object[]> rankingGroupedByHinchaInRange(@Param("estado") MatchStatus estado,
			@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
}