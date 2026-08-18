package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.request.MatchCommentRequest;
import com.fdlj.fdlj.dto.response.MatchCommentResponse;
import com.fdlj.fdlj.entity.User;

import java.util.List;

public interface MatchCommentService {

	MatchCommentResponse createComment(Long matchId, MatchCommentRequest request, User author);

	MatchCommentResponse updateComment(Long matchId, Long commentId, MatchCommentRequest request, User author);

	List<MatchCommentResponse> getComments(Long matchId);
}
