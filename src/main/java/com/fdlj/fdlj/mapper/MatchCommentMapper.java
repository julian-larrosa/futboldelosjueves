package com.fdlj.fdlj.mapper;

import com.fdlj.fdlj.dto.response.MatchCommentResponse;
import com.fdlj.fdlj.entity.MatchComment;
import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.entity.User;
import org.springframework.stereotype.Component;

@Component
public class MatchCommentMapper {

	public MatchCommentResponse toResponse(MatchComment comment) {
		User author = comment.getAuthor();
		return new MatchCommentResponse(
				comment.getId(),
				comment.getMatch().getId(),
				author.getId(),
				authorName(author),
				author.getRole(),
				comment.getContenido(),
				comment.getCreatedAt(),
				comment.getUpdatedAt()
		);
	}

	private String authorName(User author) {
		Player player = author.getPlayer();
		if (player != null) {
			return player.getNombre() + " " + player.getApellido();
		}
		return author.getUsername();
	}
}
