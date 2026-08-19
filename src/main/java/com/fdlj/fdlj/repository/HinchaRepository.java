package com.fdlj.fdlj.repository;

import com.fdlj.fdlj.entity.Hincha;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HinchaRepository extends JpaRepository<Hincha, Long> {

	Optional<Hincha> findByIdAndActivoTrue(Long id);

	Optional<Hincha> findByUserId(Long userId);

	boolean existsByUserId(Long userId);

	boolean existsByUserIdAndActivoTrue(Long userId);

	Page<Hincha> findByActivoTrue(Pageable pageable);

	List<Hincha> findByActivoTrue();

	long countByActivoTrue();
}
