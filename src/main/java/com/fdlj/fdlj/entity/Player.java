package com.fdlj.fdlj.entity;

import com.fdlj.fdlj.entity.enums.PlayerPosition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OrderBy;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
public class Player {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nombre", nullable = false, length = 100)
	private String nombre;

	@Column(name = "apellido", nullable = false, length = 100)
	private String apellido;

	@Column(name = "email", nullable = false, unique = true, length = 100)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "posicion", length = 20)
	private PlayerPosition posicion;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;

	@OneToMany(mappedBy = "player")
	private Set<MatchParticipation> participations = new HashSet<>();

	@OneToMany(mappedBy = "calificador")
	private Set<Rating> calificacionesRealizadas = new HashSet<>();

	@OneToMany(mappedBy = "calificado")
	private Set<Rating> calificacionesRecibidas = new HashSet<>();

	@OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("attributeType ASC")
	private Set<PlayerAttribute> attributes = new HashSet<>();
}
