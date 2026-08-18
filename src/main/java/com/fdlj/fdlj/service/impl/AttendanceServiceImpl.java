package com.fdlj.fdlj.service.impl;

import com.fdlj.fdlj.dto.request.AttendanceRegisterRequest;
import com.fdlj.fdlj.dto.response.AttendanceRankingResponse;
import com.fdlj.fdlj.dto.response.AttendanceStatisticsResponse;
import com.fdlj.fdlj.dto.response.MatchAttendanceResponse;
import com.fdlj.fdlj.entity.Hincha;
import com.fdlj.fdlj.entity.Match;
import com.fdlj.fdlj.entity.MatchAttendance;
import com.fdlj.fdlj.entity.User;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import com.fdlj.fdlj.entity.enums.Role;
import com.fdlj.fdlj.exception.InvalidMatchStateException;
import com.fdlj.fdlj.exception.ResourceAlreadyExistsException;
import com.fdlj.fdlj.exception.ResourceNotFoundException;
import com.fdlj.fdlj.mapper.MatchAttendanceMapper;
import com.fdlj.fdlj.repository.HinchaRepository;
import com.fdlj.fdlj.repository.MatchAttendanceRepository;
import com.fdlj.fdlj.repository.MatchRepository;
import com.fdlj.fdlj.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

	private final MatchRepository matchRepository;
	private final HinchaRepository hinchaRepository;
	private final MatchAttendanceRepository attendanceRepository;
	private final MatchAttendanceMapper attendanceMapper;

	@Override
	@Transactional
	public List<MatchAttendanceResponse> registerAttendance(Long matchId, AttendanceRegisterRequest request) {
		Match match = findMatch(matchId);
		if (match.getEstado() == MatchStatus.CANCELADO) {
			throw new InvalidMatchStateException("No se puede registrar asistencia en un partido cancelado");
		}
		List<MatchAttendanceResponse> responses = new ArrayList<>();
		for (Long hinchaId : new LinkedHashSet<>(request.hinchaIds())) {
			findActiveHincha(hinchaId);
			if (attendanceRepository.existsByMatchIdAndHinchaId(matchId, hinchaId)) {
				throw new ResourceAlreadyExistsException(
						"El hincha con id " + hinchaId + " ya fue registrado en este partido");
			}
			MatchAttendance attendance = new MatchAttendance();
			attendance.setMatch(match);
			attendance.setHincha(hinchaRepository.getReferenceById(hinchaId));
			responses.add(attendanceMapper.toResponse(attendanceRepository.save(attendance)));
			log.info("Asistencia registrada: hincha id={} en partido id={}", hinchaId, matchId);
		}
		return responses;
	}

	@Override
	@Transactional
	public void removeAttendance(Long matchId, Long hinchaId) {
		findMatch(matchId);
		MatchAttendance attendance = attendanceRepository.findByMatchIdAndHinchaId(matchId, hinchaId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"El hincha con id " + hinchaId + " no tiene asistencia registrada en este partido"));
		attendanceRepository.delete(attendance);
		log.info("Asistencia removida: hincha id={} del partido id={}", hinchaId, matchId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<MatchAttendanceResponse> getMatchAttendance(Long matchId) {
		findMatch(matchId);
		return attendanceRepository.findByMatchIdOrderByIdAsc(matchId).stream()
				.map(attendanceMapper::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<MatchAttendanceResponse> getHinchaAttendance(Long hinchaId, Integer year, User requester) {
		if (requester.getRole() == Role.ADMIN) {
			findActiveHincha(hinchaId);
		} else if (requester.getRole() == Role.HINCHADA) {
			boolean own = hinchaRepository.findByUserId(requester.getId())
					.filter(h -> h.isActivo() && h.getId().equals(hinchaId))
					.isPresent();
			if (!own) {
				throw new AccessDeniedException("Un hincha solo puede consultar su propia asistencia");
			}
		} else {
			throw new AccessDeniedException("No tiene permisos para consultar la asistencia de un hincha");
		}
		List<MatchAttendance> attendances = year == null
				? attendanceRepository.findByHinchaIdOrderByMatchFechaHoraDesc(hinchaId)
				: attendanceRepository.findByHinchaIdAndMatchRange(hinchaId, yearStartOrNull(year), yearEndOrNull(year));
		return attendances.stream()
				.map(attendanceMapper::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<AttendanceRankingResponse> getAttendanceRanking(Integer year) {
		List<Object[]> rows = year == null
				? attendanceRepository.rankingGroupedByHincha(MatchStatus.FINALIZADO)
				: attendanceRepository.rankingGroupedByHinchaInRange(
						MatchStatus.FINALIZADO, yearStartOrNull(year), yearEndOrNull(year));
		Map<Long, RankingAccumulator> accumulators = new LinkedHashMap<>();
		for (Object[] row : rows) {
			Long hinchaId = ((Number) row[0]).longValue();
			String nombre = (String) row[1];
			String apellido = (String) row[2];
			int anio = ((Number) row[3]).intValue();
			long partidos = ((Number) row[4]).longValue();
			RankingAccumulator acc = accumulators.computeIfAbsent(hinchaId,
					id -> new RankingAccumulator(hinchaId, nombre, apellido));
			acc.total += partidos;
			acc.porAnio.put(anio, partidos);
		}
		return accumulators.values().stream()
				.map(this::toRankingResponse)
				.sorted(Comparator.comparing(AttendanceRankingResponse::totalPartidos).reversed()
						.thenComparing(AttendanceRankingResponse::apellido))
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public AttendanceStatisticsResponse getAttendanceStatistics(Integer year) {
		long totalHinchas = hinchaRepository.countByActivoTrue();
		long totalAsistencias = year == null
				? attendanceRepository.countAll(MatchStatus.FINALIZADO)
				: attendanceRepository.countAllInRange(MatchStatus.FINALIZADO, yearStartOrNull(year), yearEndOrNull(year));
		long partidosConAsistencia = year == null
				? attendanceRepository.countDistinctMatches(MatchStatus.FINALIZADO)
				: attendanceRepository.countDistinctMatchesInRange(
						MatchStatus.FINALIZADO, yearStartOrNull(year), yearEndOrNull(year));
		double promedio = partidosConAsistencia > 0
				? Math.round((double) totalAsistencias / partidosConAsistencia * 100.0) / 100.0
				: 0.0;
		return new AttendanceStatisticsResponse(totalHinchas, totalAsistencias, promedio);
	}

	private AttendanceRankingResponse toRankingResponse(RankingAccumulator acc) {
		List<AttendanceRankingResponse.AnioAttendance> porAnio = acc.porAnio.entrySet().stream()
				.map(e -> new AttendanceRankingResponse.AnioAttendance(e.getKey(), e.getValue()))
				.toList();
		return new AttendanceRankingResponse(acc.hinchaId, acc.nombre, acc.apellido, acc.total, porAnio);
	}

	private Match findMatch(Long matchId) {
		return matchRepository.findById(matchId)
				.orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + matchId));
	}

	private Hincha findActiveHincha(Long hinchaId) {
		return hinchaRepository.findByIdAndActivoTrue(hinchaId)
				.orElseThrow(() -> new ResourceNotFoundException("Hincha no encontrado con id: " + hinchaId));
	}

	private OffsetDateTime yearStartOrNull(Integer year) {
		return year == null ? null : OffsetDateTime.of(year, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
	}

	private OffsetDateTime yearEndOrNull(Integer year) {
		return year == null ? null : yearStartOrNull(year).plusYears(1);
	}

	private static class RankingAccumulator {
		private final Long hinchaId;
		private final String nombre;
		private final String apellido;
		private long total;
		private final Map<Integer, Long> porAnio = new TreeMap<>();

		private RankingAccumulator(Long hinchaId, String nombre, String apellido) {
			this.hinchaId = hinchaId;
			this.nombre = nombre;
			this.apellido = apellido;
		}
	}
}
