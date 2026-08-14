package com.fdlj.fdlj.entity;

import com.fdlj.fdlj.entity.enums.ParticipationStatus;
import com.fdlj.fdlj.entity.enums.TeamSide;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "match_participations",
		uniqueConstraints = @UniqueConstraint(name = "uk_match_participation_match_player",
				columnNames = { "match_id", "player_id" }))
@Getter
@Setter
@NoArgsConstructor
public class MatchParticipation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "match_id", nullable = false)
	private Match match;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "player_id", nullable = false)
	private Player player;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 20)
	private ParticipationStatus estado;

	@Enumerated(EnumType.STRING)
	@Column(name = "team_side", length = 10)
	private TeamSide teamSide;

	@Column(name = "goles", nullable = false)
	private Integer goles = 0;

	@Column(name = "asistencias", nullable = false)
	private Integer asistencias = 0;

	@Column(name = "jugo_efectivamente")
	private Boolean jugoEfectivamente;
}
