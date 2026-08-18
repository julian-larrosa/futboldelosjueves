package com.fdlj.fdlj.entity;

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
@Table(name = "match_attendances",
		uniqueConstraints = @UniqueConstraint(name = "uk_match_attendance_match_hincha",
				columnNames = { "match_id", "hincha_id" }))
@Getter
@Setter
@NoArgsConstructor
public class MatchAttendance {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "match_id", nullable = false)
	private Match match;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "hincha_id", nullable = false)
	private Hincha hincha;
}
