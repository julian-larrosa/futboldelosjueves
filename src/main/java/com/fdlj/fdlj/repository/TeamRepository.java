package com.fdlj.fdlj.repository;

import com.fdlj.fdlj.entity.Team;
import com.fdlj.fdlj.entity.enums.TeamSide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

	List<Team> findByMatchIdOrderBySideAsc(Long matchId);

	Optional<Team> findByMatchIdAndSide(Long matchId, TeamSide side);

	boolean existsByMatchId(Long matchId);

	void deleteByMatchId(Long matchId);
}
