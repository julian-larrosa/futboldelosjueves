package com.fdlj.fdlj.entity;

import com.fdlj.fdlj.entity.enums.MatchStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
public class Match {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "fecha_hora", nullable = false)
	private OffsetDateTime fechaHora;

	@Column(name = "lugar", length = 150)
	private String lugar;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 30)
	private MatchStatus estado = MatchStatus.PROGRAMADO;

	@Column(name = "goles_equipo_a")
	private Integer golesEquipoA;

	@Column(name = "goles_equipo_b")
	private Integer golesEquipoB;

	@Version
	@Column(name = "version", nullable = false)
	private Long version = 0L;

	@OneToMany(mappedBy = "match")
	private Set<MatchParticipation> participations = new HashSet<>();

	@OneToMany(mappedBy = "match")
	private Set<Rating> ratings = new HashSet<>();
}
