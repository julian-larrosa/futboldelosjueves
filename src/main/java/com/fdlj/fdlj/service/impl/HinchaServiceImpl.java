package com.fdlj.fdlj.service.impl;

import com.fdlj.fdlj.dto.response.HinchaResponse;
import com.fdlj.fdlj.dto.response.PagedResponse;
import com.fdlj.fdlj.entity.Hincha;
import com.fdlj.fdlj.exception.ResourceNotFoundException;
import com.fdlj.fdlj.mapper.HinchaMapper;
import com.fdlj.fdlj.repository.HinchaRepository;
import com.fdlj.fdlj.service.HinchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HinchaServiceImpl implements HinchaService {

	private final HinchaRepository hinchaRepository;
	private final HinchaMapper hinchaMapper;

	@Override
	@Transactional(readOnly = true)
	public PagedResponse<HinchaResponse> getAllHinchas(Pageable pageable) {
		Page<Hincha> page = hinchaRepository.findByActivoTrue(pageable);
		return PagedResponse.of(page.map(hinchaMapper::toResponse));
	}

	@Override
	@Transactional(readOnly = true)
	public HinchaResponse getHinchaById(Long id) {
		return hinchaMapper.toResponse(findActiveHincha(id));
	}

	private Hincha findActiveHincha(Long id) {
		return hinchaRepository.findByIdAndActivoTrue(id)
				.orElseThrow(() -> new ResourceNotFoundException("Hincha no encontrado con id: " + id));
	}
}
