package com.fdlj.fdlj.repository;

import com.fdlj.fdlj.entity.MatchComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchCommentRepository extends JpaRepository<MatchComment, Long> {

	@Query("SELECT c FROM MatchComment c JOIN FETCH c.match m JOIN FETCH c.author "
			+ "WHERE c.match.id = :matchId ORDER BY c.createdAt DESC")
	List<MatchComment> findByMatchIdOrderByCreatedAtDesc(@Param("matchId") Long matchId);
}