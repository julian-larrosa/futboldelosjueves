package com.fdlj.fdlj.repository;

import com.fdlj.fdlj.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

	List<Player> findByActivoTrue();

	Optional<Player> findByIdAndActivoTrue(Long id);

	boolean existsByEmailAndActivoTrue(String email);

	boolean existsByEmailAndActivoTrueAndIdNot(String email, Long id);
}
