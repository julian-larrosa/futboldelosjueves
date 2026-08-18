package com.fdlj.fdlj.repository;

import com.fdlj.fdlj.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

	@Query(value = "SELECT r FROM Rating r JOIN FETCH r.match JOIN FETCH r.calificador JOIN FETCH r.calificado "
			+ "WHERE r.match.id = :matchId ORDER BY r.id",
			countQuery = "SELECT COUNT(r) FROM Rating r WHERE r.match.id = :matchId")
	Page<Rating> findByMatchIdOrderByIdAsc(@Param("matchId") Long matchId, Pageable pageable);

	boolean existsByMatchIdAndCalificadorIdAndCalificadoId(Long matchId, Long calificadorId, Long calificadoId);

	@Query("SELECT AVG(r.puntaje) FROM Rating r WHERE r.calificado.id = :playerId")
	Double averageByCalificadoId(@Param("playerId") Long playerId);

	@Query("SELECT r.calificado.id, AVG(r.puntaje) FROM Rating r GROUP BY r.calificado.id")
	List<Object[]> averageGroupedByCalificado();

	@Query("SELECT r FROM Rating r JOIN FETCH r.calificado "
			+ "WHERE r.match.fechaHora >= :from AND r.match.fechaHora < :to ORDER BY r.match.fechaHora DESC")
	List<Rating> findByRange(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

	@Query("SELECT r FROM Rating r JOIN FETCH r.calificado ORDER BY r.match.fechaHora DESC")
	List<Rating> findAllWithCalificado();
}