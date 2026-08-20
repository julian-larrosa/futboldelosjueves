package com.fdlj.fdlj.repository;

import com.fdlj.fdlj.entity.Match;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

	List<Match> findAllByOrderByFechaHoraDesc();

	@Query("SELECT m FROM Match m " +
			"WHERE (:estado IS NULL OR m.estado = :estado) " +
			"AND (:lugar IS NULL OR LOWER(m.lugar) LIKE LOWER(CONCAT('%', CAST(:lugar AS string), '%'))) " +
			"AND m.fechaHora >= :fechaDesde " +
			"AND m.fechaHora <= :fechaHasta")
	Page<Match> searchMatches(
			@Param("estado") MatchStatus estado,
			@Param("lugar") String lugar,
			@Param("fechaDesde") OffsetDateTime fechaDesde,
			@Param("fechaHasta") OffsetDateTime fechaHasta,
			Pageable pageable);
}
