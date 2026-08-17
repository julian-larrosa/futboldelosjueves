package com.fdlj.fdlj.entity;

import com.fdlj.fdlj.entity.enums.AttributeType;
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
@Table(name = "player_attributes",
		uniqueConstraints = @UniqueConstraint(name = "uk_player_attribute",
				columnNames = { "player_id", "attribute_type" }))
@Getter
@Setter
@NoArgsConstructor
public class PlayerAttribute {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "player_id", nullable = false)
	private Player player;

	@Enumerated(EnumType.STRING)
	@Column(name = "attribute_type", nullable = false, length = 20)
	private AttributeType attributeType;

	@Column(name = "current_value", nullable = false)
	private Double currentValue = 5.0;
}
