package com.fdlj.fdlj.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ratings",
		uniqueConstraints = @UniqueConstraint(name = "uk_rating_match_calificador_calificado",
				columnNames = { "match_id", "calificador_id", "calificado_id" }))
@Getter
@Setter
@NoArgsConstructor
public class Rating {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "match_id", nullable = false)
	private Match match;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "calificador_id", nullable = false)
	private Player calificador;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "calificado_id", nullable = false)
	private Player calificado;

	@Column(name = "puntaje", nullable = false)
	private Integer puntaje;
}
