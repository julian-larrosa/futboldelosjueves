package com.fdlj.fdlj.repository;

import com.fdlj.fdlj.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

	List<Rating> findByMatchIdOrderByIdAsc(Long matchId);

	boolean existsByMatchIdAndCalificadorIdAndCalificadoId(Long matchId, Long calificadorId, Long calificadoId);

	@Query("SELECT AVG(r.puntaje) FROM Rating r WHERE r.calificado.id = :playerId")
	Double averageByCalificadoId(@Param("playerId") Long playerId);
}
