package com.fdlj.fdlj.controller;

import com.fdlj.fdlj.IntegrationTestBase;
import com.fdlj.fdlj.entity.Match;
import com.fdlj.fdlj.exception.ErrorResponse;
import com.fdlj.fdlj.exception.GlobalExceptionHandler;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchOptimisticLockTest extends IntegrationTestBase {

	@Autowired
	private EntityManager entityManager;

	@Test
	void concurrentModification_secondCommit_throwsOptimisticLockingFailure() {
		Match match = new Match();
		match.setLugar("Cancha Central");
		match.setFechaHora(OffsetDateTime.now().plusDays(1));
		Long matchId = matchRepository.saveAndFlush(match).getId();

		entityManager.clear();
		Match managed = matchRepository.findById(matchId).orElseThrow();

		entityManager.createNativeQuery("UPDATE matches SET version = version + 1 WHERE id = :id")
				.setParameter("id", matchId)
				.executeUpdate();

		managed.setLugar("Modificado en el segundo commit");
		assertThatThrownBy(() -> matchRepository.saveAndFlush(managed))
				.isInstanceOf(OptimisticLockingFailureException.class);
	}

	@Test
	void optimisticLockingConflict_mapsTo409() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();
		ResponseEntity<ErrorResponse> response = handler.handleOptimisticLocking(
				new OptimisticLockingFailureException("versión obsoleta"));
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody().status()).isEqualTo(409);
		assertThat(response.getBody().message()).contains("modificado por otro usuario");
	}
}
