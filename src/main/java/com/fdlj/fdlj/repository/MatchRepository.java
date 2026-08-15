package com.fdlj.fdlj.repository;

import com.fdlj.fdlj.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

	List<Match> findAllByOrderByFechaHoraDesc();
}
