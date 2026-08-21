package com.fdlj.fdlj.repository;

import com.fdlj.fdlj.entity.Player;
import com.fdlj.fdlj.entity.enums.PlayerPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

	List<Player> findByActivoTrue();

	Page<Player> findByActivoTrue(Pageable pageable);

	Optional<Player> findByIdAndActivoTrue(Long id);

	boolean existsByEmailAndActivoTrue(String email);

	boolean existsByEmailAndActivoTrueAndIdNot(String email, Long id);

	boolean existsByUserId(Long userId);

	boolean existsByUserIdAndActivoTrue(Long userId);

	@Query("SELECT p FROM Player p WHERE p.activo = true " +
			"AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', CAST(:nombre AS string), '%'))) " +
			"AND (:apellido IS NULL OR LOWER(p.apellido) LIKE LOWER(CONCAT('%', CAST(:apellido AS string), '%'))) " +
			"AND (:email IS NULL OR LOWER(p.email) LIKE LOWER(CONCAT('%', CAST(:email AS string), '%'))) " +
			"AND (:posicion IS NULL OR p.posicion = :posicion)")
	Page<Player> searchPlayers(
			@Param("nombre") String nombre,
			@Param("apellido") String apellido,
			@Param("email") String email,
			@Param("posicion") PlayerPosition posicion,
			Pageable pageable);
}
