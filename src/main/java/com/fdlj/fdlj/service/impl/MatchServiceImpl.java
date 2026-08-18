package com.fdlj.fdlj.service.impl;

import com.fdlj.fdlj.dto.request.MatchRequest;
import com.fdlj.fdlj.dto.request.MatchResultRequest;
import com.fdlj.fdlj.dto.response.MatchResponse;
import com.fdlj.fdlj.dto.response.PagedResponse;
import com.fdlj.fdlj.entity.Match;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import com.fdlj.fdlj.exception.InvalidMatchStateException;
import com.fdlj.fdlj.exception.ResourceNotFoundException;
import com.fdlj.fdlj.mapper.MatchMapper;
import com.fdlj.fdlj.repository.MatchParticipationRepository;
import com.fdlj.fdlj.repository.MatchRepository;
import com.fdlj.fdlj.repository.TeamRepository;
import com.fdlj.fdlj.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchServiceImpl implements MatchService {

	private final MatchRepository matchRepository;
	private final MatchParticipationRepository participationRepository;
	private final TeamRepository teamRepository;
	private final MatchMapper matchMapper;
	private final GoalsConsistencyValidator goalsConsistencyValidator;

	@Override
	@Transactional
	public MatchResponse createMatch(MatchRequest request) {
		validateFutureDate(request.fechaHora());
		MatchResponse response = matchMapper.toResponse(matchRepository.save(matchMapper.toEntity(request)));
		log.info("Partido creado: id={}, lugar={}", response.id(), response.lugar());
		return response;
	}

	@Override
	@Transactional(readOnly = true)
	public MatchResponse getMatchById(Long id) {
		return matchMapper.toResponse(findMatch(id));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<MatchResponse> getAllMatches(Pageable pageable) {
		Page<Match> page = matchRepository.findAll(pageable);
		return PagedResponse.of(page.map(matchMapper::toResponse));
	}

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<MatchResponse> searchMatches(MatchStatus estado, String lugar, OffsetDateTime fechaDesde, OffsetDateTime fechaHasta, Pageable pageable) {
		Page<Match> page = matchRepository.searchMatches(estado, lugar, fechaDesde, fechaHasta, pageable);
		return PagedResponse.of(page.map(matchMapper::toResponse));
	}

	@Override
	@Transactional
	public MatchResponse updateMatch(Long id, MatchRequest request) {
		Match match = findMatch(id);
		ensureState(match, MatchStatus.PROGRAMADO, MatchStatus.CONVOCATORIA_ABIERTA);
		if (match.getFechaHora().isAfter(OffsetDateTime.now()) && request.fechaHora().isBefore(OffsetDateTime.now())) {
			throw new InvalidMatchStateException("La nueva fecha del partido debe ser futura");
		}
		match.setFechaHora(request.fechaHora());
		match.setLugar(matchMapper.normalizeLugar(request.lugar()));
		return matchMapper.toResponse(matchRepository.save(match));
	}

	@Override
	@Transactional
	public MatchResponse openConvocatoria(Long id) {
		Match match = findMatch(id);
		transition(match, MatchStatus.PROGRAMADO, MatchStatus.CONVOCATORIA_ABIERTA);
		log.info("Convocatoria abierta para partido id={}", id);
		return matchMapper.toResponse(matchRepository.save(match));
	}

	@Override
	@Transactional
	public MatchResponse closeConvocatoria(Long id) {
		Match match = findMatch(id);
		transition(match, MatchStatus.CONVOCATORIA_ABIERTA, MatchStatus.CONVOCATORIA_CERRADA);
		log.info("Convocatoria cerrada para partido id={}", id);
		return matchMapper.toResponse(matchRepository.save(match));
	}

	@Override
	@Transactional
	public MatchResponse reopenConvocatoria(Long id) {
		Match match = findMatch(id);
		transition(match, MatchStatus.CONVOCATORIA_CERRADA, MatchStatus.CONVOCATORIA_ABIERTA);
		log.info("Convocatoria reabierta para partido id={}", id);
		return matchMapper.toResponse(matchRepository.save(match));
	}

	@Override
	@Transactional
	public MatchResponse startMatch(Long id) {
		Match match = findMatch(id);
		ensureState(match, MatchStatus.CONVOCATORIA_CERRADA);
		if (!teamRepository.existsByMatchId(id)) {
			throw new InvalidMatchStateException("No se puede iniciar el partido sin equipos generados");
		}
		match.setEstado(MatchStatus.EN_CURSO);
		log.info("Partido iniciado: id={}", id);
		return matchMapper.toResponse(matchRepository.save(match));
	}

	@Override
	@Transactional
	public MatchResponse finishMatch(Long id, MatchResultRequest request) {
		Match match = findMatch(id);
		transition(match, MatchStatus.EN_CURSO, MatchStatus.FINALIZADO);
		goalsConsistencyValidator.validateGoals(match.getId(), request.golesEquipoA(), request.golesEquipoB());
		match.setGolesEquipoA(request.golesEquipoA());
		match.setGolesEquipoB(request.golesEquipoB());
		log.info("Partido finalizado: id={}, resultado={}-{}", id, request.golesEquipoA(), request.golesEquipoB());
		return matchMapper.toResponse(matchRepository.save(match));
	}

	@Override
	@Transactional
	public MatchResponse cancelMatch(Long id) {
		Match match = findMatch(id);
		if (match.getEstado() == MatchStatus.FINALIZADO || match.getEstado() == MatchStatus.CANCELADO) {
			throw new InvalidMatchStateException("El partido ya está " + match.getEstado() + " y no puede cancelarse");
		}
		match.setEstado(MatchStatus.CANCELADO);
		log.info("Partido cancelado: id={}", id);
		return matchMapper.toResponse(matchRepository.save(match));
	}

	private Match findMatch(Long id) {
		return matchRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));
	}

	private void ensureState(Match match, MatchStatus... expected) {
		for (MatchStatus status : expected) {
			if (match.getEstado() == status) {
				return;
			}
		}
		throw new InvalidMatchStateException("Operación no permitida en estado " + match.getEstado());
	}

	private void transition(Match match, MatchStatus from, MatchStatus to) {
		if (match.getEstado() != from) {
			throw new InvalidMatchStateException("No se puede pasar de " + match.getEstado() + " a " + to);
		}
		match.setEstado(to);
	}

	private void validateFutureDate(OffsetDateTime fechaHora) {
		if (fechaHora.isBefore(OffsetDateTime.now())) {
			throw new InvalidMatchStateException("La fecha del partido debe ser futura");
		}
	}
}
