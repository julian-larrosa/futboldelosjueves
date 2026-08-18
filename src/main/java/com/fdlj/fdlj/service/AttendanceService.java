package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.request.AttendanceRegisterRequest;
import com.fdlj.fdlj.dto.response.AttendanceRankingResponse;
import com.fdlj.fdlj.dto.response.AttendanceStatisticsResponse;
import com.fdlj.fdlj.dto.response.MatchAttendanceResponse;
import com.fdlj.fdlj.entity.User;

import java.util.List;

public interface AttendanceService {

	List<MatchAttendanceResponse> registerAttendance(Long matchId, AttendanceRegisterRequest request);

	void removeAttendance(Long matchId, Long hinchaId);

	List<MatchAttendanceResponse> getMatchAttendance(Long matchId);

	List<MatchAttendanceResponse> getHinchaAttendance(Long hinchaId, Integer year, User requester);

	List<AttendanceRankingResponse> getAttendanceRanking(Integer year);

	AttendanceStatisticsResponse getAttendanceStatistics(Integer year);
}
