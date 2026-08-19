package com.fdlj.fdlj.service.impl;

import com.fdlj.fdlj.dto.request.MatchCommentRequest;
import com.fdlj.fdlj.dto.response.MatchCommentResponse;
import com.fdlj.fdlj.entity.Match;
import com.fdlj.fdlj.entity.MatchComment;
import com.fdlj.fdlj.entity.User;
import com.fdlj.fdlj.entity.enums.MatchStatus;
import com.fdlj.fdlj.entity.enums.Role;
import com.fdlj.fdlj.exception.InvalidMatchStateException;
import com.fdlj.fdlj.exception.ResourceNotFoundException;
import com.fdlj.fdlj.mapper.MatchCommentMapper;
import com.fdlj.fdlj.repository.MatchCommentRepository;
import com.fdlj.fdlj.repository.MatchRepository;
import com.fdlj.fdlj.repository.UserRepository;
import com.fdlj.fdlj.service.MatchCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchCommentServiceImpl implements MatchCommentService {

	private final MatchRepository matchRepository;
	private final MatchCommentRepository commentRepository;
	private final MatchCommentMapper commentMapper;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public MatchCommentResponse createComment(Long matchId, MatchCommentRequest request, User author) {
		Match match = findMatch(matchId);
		User managedAuthor = reloadAuthor(author);
		validateCreate(match, managedAuthor);

		MatchComment comment = new MatchComment();
		comment.setMatch(match);
		comment.setAuthor(managedAuthor);
		comment.setContenido(request.contenido().trim());
		MatchComment saved = commentRepository.save(comment);
		log.info("Comentario creado: id={} en partido id={}, autor {}", saved.getId(), matchId, managedAuthor.getRole());
		return commentMapper.toResponse(saved);
	}

	@Override
	@Transactional
	public MatchCommentResponse updateComment(Long matchId, Long commentId, MatchCommentRequest request, User author) {
		Match match = findMatch(matchId);
		MatchComment comment = findComment(commentId);
		if (!comment.getMatch().getId().equals(matchId)) {
			throw new ResourceNotFoundException("Comentario no encontrado con id: " + commentId);
		}
		User managedAuthor = reloadAuthor(author);
		validateUpdate(match, comment, managedAuthor);

		comment.setContenido(request.contenido().trim());
		MatchComment saved = commentRepository.save(comment);
		log.info("Comentario actualizado: id={} en partido id={}", commentId, matchId);
		return commentMapper.toResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public List<MatchCommentResponse> getComments(Long matchId) {
		findMatch(matchId);
		return commentRepository.findByMatchIdOrderByCreatedAtDesc(matchId).stream()
				.map(commentMapper::toResponse)
				.toList();
	}

	private void validateCreate(Match match, User author) {
		if (author.getRole() == Role.HINCHADA) {
			throw new AccessDeniedException("La hinchada no puede escribir comentarios");
		}
		if (author.getRole() == Role.ADMIN) {
			if (match.getEstado() == MatchStatus.FINALIZADO) {
				throw new InvalidMatchStateException("El ADMIN solo puede comentar antes de que el partido finalice");
			}
			return;
		}
		if (match.getEstado() != MatchStatus.FINALIZADO) {
			throw new InvalidMatchStateException("Los jugadores solo pueden comentar un partido finalizado");
		}
	}

	private void validateUpdate(Match match, MatchComment comment, User author) {
		if (author.getRole() == Role.HINCHADA) {
			throw new AccessDeniedException("La hinchada no puede modificar comentarios");
		}
		if (author.getRole() == Role.ADMIN) {
			if (match.getEstado() == MatchStatus.FINALIZADO) {
				throw new InvalidMatchStateException("El ADMIN solo puede modificar comentarios antes de que el partido finalice");
			}
			return;
		}
		if (match.getEstado() != MatchStatus.FINALIZADO) {
			throw new InvalidMatchStateException("Los jugadores solo pueden modificar comentarios en un partido finalizado");
		}
		if (!comment.getAuthor().getId().equals(author.getId())) {
			throw new AccessDeniedException("Solo el autor puede modificar su comentario");
		}
	}

	private Match findMatch(Long matchId) {
		return matchRepository.findById(matchId)
				.orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + matchId));
	}

	private MatchComment findComment(Long commentId) {
		return commentRepository.findById(commentId)
				.orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado con id: " + commentId));
	}

	private User reloadAuthor(User author) {
		return userRepository.findById(author.getId())
				.orElseThrow(() -> new ResourceNotFoundException(
						"Usuario no encontrado con id: " + author.getId()));
	}
}
