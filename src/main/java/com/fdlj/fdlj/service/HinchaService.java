package com.fdlj.fdlj.service;

import com.fdlj.fdlj.dto.response.HinchaResponse;
import com.fdlj.fdlj.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface HinchaService {

	PagedResponse<HinchaResponse> getAllHinchas(Pageable pageable);

	HinchaResponse getHinchaById(Long id);
}
