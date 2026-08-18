package com.fdlj.fdlj.repository;

import com.fdlj.fdlj.entity.MatchAttendance;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchAttendanceRepository extends JpaRepository<MatchAttendance, Long> {

	boolean existsByMatchIdAndHinchaId(Long matchId, Long hinchaId);

	Optional<MatchAttendance> findByMatchIdAndHinchaId(Long matchId, Long hinchaId);

	List<MatchAttendance> findByMatchIdOrderByIdAsc(Long matchId);

	List<MatchAttendance> findByHinchaIdOrderByMatchFechaHoraDesc(Long hinchaId);

	@Query("SELECT a FROM MatchAttendance a WHERE a.hincha.id = :hinchaId "
			+ "AND YEAR(a.match.fechaHora) = :year ORDER BY a.match.fechaHora DESC")
	List<MatchAttendance> findByHinchaIdAndMatchYear(@Param("hinchaId") Long hinchaId, @Param("year") int year);

	@Query("SELECT COUNT(a.id) FROM MatchAttendance a WHERE a.match.estado = :estado "
			+ "AND (:year IS NULL OR YEAR(a.match.fechaHora) = :year)")
	long countAll(@Param("estado") MatchStatus estado, @Param("year") Integer year);

	@Query("SELECT COUNT(DISTINCT a.match.id) FROM MatchAttendance a WHERE a.match.estado = :estado "
			+ "AND (:year IS NULL OR YEAR(a.match.fechaHora) = :year)")
	long countDistinctMatches(@Param("estado") MatchStatus estado, @Param("year") Integer year);

	@Query("SELECT h.id, h.nombre, h.apellido, YEAR(a.match.fechaHora), COUNT(a.id) "
			+ "FROM MatchAttendance a JOIN a.hincha h "
			+ "WHERE a.match.estado = :estado "
			+ "AND (:year IS NULL OR YEAR(a.match.fechaHora) = :year) "
			+ "GROUP BY h.id, h.nombre, h.apellido, YEAR(a.match.fechaHora) "
			+ "ORDER BY h.apellido, h.nombre")
	List<Object[]> rankingGroupedByHinchaAndYear(@Param("estado") MatchStatus estado, @Param("year") Integer year);
}
